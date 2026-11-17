"""Standard-library tests for inventory safety; full enforcement is tested in CI."""
import hashlib
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

SCRIPTS = Path(__file__).resolve().parent


class InventoryReconciliationTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.reports = self.root / 'reports'
        self.baselines = self.root / 'baselines'
        self.reports.mkdir()
        (self.baselines / 'sdk').mkdir(parents=True)
        signature = 'internal fun example(value: String?)'
        result = {
            'ruleId': 'detekt.potential-bugs.UnsafeCallOnNullableType',
            'locations': [{'physicalLocation': {'artifactLocation': {'uri': 'sdk/src/commonMain/Example.kt'}}}],
            'partialFingerprints': {'signature/v1': hashlib.sha1(signature.encode()).hexdigest()},
        }
        data = {'runs': [{'tool': {'driver': {'rules': []}}, 'results': [result, result]}]}
        (self.reports / 'sdk-detektMainAndroid.sarif').write_text(json.dumps(data))
        self.baseline = self.baselines / 'sdk/detektMainAndroid.xml'
        self.baseline.write_text('<SmellBaseline><CurrentIssues><ID>UnsafeCallOnNullableType:Example.kt:'
                                 + signature + '</ID></CurrentIssues></SmellBaseline>')

    def reconcile(self):
        return subprocess.run([sys.executable, str(SCRIPTS / 'report-inventory.py'),
                               '--reports', str(self.reports), '--baseline-dir', str(self.baselines),
                               '--output', str(self.root / 'summary.json')], capture_output=True, text=True)

    def test_duplicate_occurrences_reconcile_to_one_reported_identity(self):
        result = self.reconcile()
        self.assertEqual(0, result.returncode, result.stderr)
        summary = json.loads((self.root / 'summary.json').read_text())
        self.assertEqual(2, summary['report_occurrences'])
        self.assertEqual(1, summary['unique_baseline_identities'])
        self.assertEqual(1, sum(summary['baseline_entries'].values()))

    def test_orphan_baseline_identity_is_rejected(self):
        self.baseline.write_text(self.baseline.read_text().replace('internal fun example', 'internal fun other'))
        result = self.reconcile()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('orphan IDs=1', result.stderr)

    def test_missing_baseline_is_rejected(self):
        self.baseline.unlink()
        result = self.reconcile()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('Missing baselines', result.stderr)

    def test_missing_baseline_entry_is_rejected(self):
        self.baseline.write_text('<SmellBaseline><CurrentIssues/></SmellBaseline>')
        result = self.reconcile()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('missing IDs=1', result.stderr)


class ExperimentalPolicyTests(unittest.TestCase):
    def test_enforced_policy_cannot_be_overwritten(self):
        with tempfile.TemporaryDirectory() as directory:
            policy = Path(directory) / 'policy.yml'
            policy.write_text('sentinel\n')
            result = subprocess.run([sys.executable, str(SCRIPTS / 'experimental-policy.py'),
                                     '--input', str(policy), '--output', str(policy)], capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertEqual('sentinel\n', policy.read_text())


if __name__ == '__main__':
    unittest.main()
