"""Structural safety tests; actionlint validates YAML/expression syntax separately."""
import hashlib
from pathlib import Path
import re
import unittest

WORKFLOWS = Path(__file__).resolve().parents[2] / '.github/workflows'
PUBLISHERS = {
    'on_push_workflow.yml': ['publish-android', 'publish-js', 'publish-ios-snapshot'],
    'publish_artifacts.yml': ['android-pipeline', 'js-pipeline', 'ios-pipeline',
                              'create-tag-fallback', 'create-release', 'deploy-pages'],
    'promote_maven_central.yml': ['promote'],
}


def jobs(text):
    body = text.split('\njobs:\n', 1)[1]
    return {match.group(1): match.group(0) for match in re.finditer(
        r'^  ([A-Za-z0-9_-]+):\n.*?(?=^  [A-Za-z0-9_-]+:\n|\Z)', body, re.M | re.S)}


def dependencies(block):
    match = re.search(r'^    needs: (.+)$', block, re.M)
    if not match:
        return []
    return [value.strip(" '\"") for value in match.group(1).strip('[]').split(',')]


def top_level_conjuncts(condition):
    """Inspect the reviewed guard shape without executing GitHub expressions."""
    condition = condition.strip().removeprefix('${{').removesuffix('}}').strip()
    parts, start, depth, quote = [], 0, 0, None
    index = 0
    while index < len(condition):
        char = condition[index]
        if quote:
            if char == quote:
                quote = None
        elif char in "'\"":
            quote = char
        elif char == '(':
            depth += 1
        elif char == ')':
            depth -= 1
        elif depth == 0 and condition[index:index+2] == '||':
            return []  # A top-level OR can bypass the mandatory gate.
        elif depth == 0 and condition[index:index+2] == '&&':
            parts.append(condition[start:index].strip())
            index += 1
            start = index + 1
        index += 1
    return parts + [condition[start:].strip()]


class CiWiringTests(unittest.TestCase):
    def test_all_primary_workflows_call_the_same_independent_gate(self):
        for name in ['on_push_workflow.yml', 'nightly_workflow.yml',
                     'publish_artifacts.yml', 'promote_maven_central.yml']:
            with self.subTest(workflow=name):
                gate = jobs((WORKFLOWS / name).read_text())['detekt']
                self.assertIn('uses: ./.github/workflows/detekt.yml', gate)
                self.assertNotIn('    needs:', gate)
                self.assertNotIn('    if:', gate)
                self.assertNotIn('secrets:', gate)

    def test_every_publish_path_requires_detekt_success(self):
        for name, publishers in PUBLISHERS.items():
            parsed = jobs((WORKFLOWS / name).read_text())
            for publisher in publishers:
                with self.subTest(workflow=name, publisher=publisher):
                    block = parsed[publisher]
                    self.assertIn('detekt', dependencies(block))
                    condition = re.search(r'^    if: (.+)$', block, re.M).group(1)
                    self.assertIn("needs.detekt.result == 'success'", top_level_conjuncts(condition))

    def test_guard_inspection_rejects_or_bypass(self):
        self.assertEqual([], top_level_conjuncts("needs.detekt.result == 'success' || github.event_name == 'push'"))
        self.assertIn("needs.detekt.result == 'success'", top_level_conjuncts(
            "${{ !cancelled() && needs.detekt.result == 'success' && (github.event_name == 'push' || inputs.publish_ios) }}"))

    def test_reporting_and_nightly_dispatch_include_quality_result(self):
        for name, ids in [('on_push_workflow.yml', ['reporting']),
                          ('nightly_workflow.yml', ['trigger-e2e', 'reporting']),
                          ('publish_artifacts.yml', ['report-slack'])]:
            parsed = jobs((WORKFLOWS / name).read_text())
            for job in ids:
                self.assertIn('detekt', dependencies(parsed[job]))

    def test_promotion_gate_checks_the_requested_release_tag(self):
        gate = jobs((WORKFLOWS / 'promote_maven_central.yml').read_text())['detekt']
        self.assertIn('ref: refs/tags/${{ inputs.version }}', gate)

    def test_android_lint_jobs_are_byte_for_byte_unchanged(self):
        expected = {
            'on_push_workflow.yml': '132ff96f5d8a69a4fd0a48ffd1b82abca68ae1a61178463793955c32a79ad551',
            'nightly_workflow.yml': '2e602125cbab17e63861bc7eaabe7f618d4194615a65fd3a728503bcde4cbc58',
        }
        for name, digest in expected.items():
            block = jobs((WORKFLOWS / name).read_text())['lint']
            self.assertEqual(digest, hashlib.sha256(block.encode()).hexdigest())

    def test_shared_gate_never_generates_baselines_or_uses_partial_analysis(self):
        text = (WORKFLOWS / 'detekt.yml').read_text()
        self.assertIn('make detekt-ci', text)
        self.assertNotIn('detektBaseline', text)
        self.assertNotIn('detekt.inventory', text)
        self.assertNotIn('detekt-staged', text)
        self.assertIn('if: always()', text)
        self.assertIn('sarif_file: build/reports/detekt/merged.sarif', text)

    def test_sarif_upload_is_explicit_and_permission_safe(self):
        text = (WORKFLOWS / 'detekt.yml').read_text()
        self.assertIn('default: false', text)
        self.assertIn('inputs.upload_sarif', text)
        self.assertIn("github.actor != 'dependabot[bot]'", text)
        self.assertIn('github.event.pull_request.head.repo.full_name == github.repository', text)
        self.assertIn("hashFiles('build/reports/detekt/merged.sarif') != ''", text)


if __name__ == '__main__':
    unittest.main()
