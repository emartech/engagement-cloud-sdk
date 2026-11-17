#!/usr/bin/env python3
"""CI-only before/during/after checks; never regenerate baselines or leave source edits."""
import hashlib
import json
import os
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[2]
BASELINES = ROOT / 'config/detekt/baseline'
EVIDENCE = ROOT / 'build/detekt-verification/enforcement'
PROBES = ROOT / 'engagement-cloud-sdk/src/commonMain/kotlin/com/sap/ec/civerification'


def baseline_hashes():
    return {p.relative_to(ROOT).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in sorted(BASELINES.rglob('*.xml'))}


def run_gate(name):
    path = EVIDENCE / (name + '.log')
    print('Running complete CI gate:', name, flush=True)
    with path.open('w') as log:
        result = subprocess.run(['make', 'detekt-ci'], cwd=ROOT, stdout=log, stderr=subprocess.STDOUT)
    print('Exit:', result.returncode, '; log:', path.relative_to(ROOT), flush=True)
    return result.returncode, path.read_text()


def main():
    if os.environ.get('CI') != 'true':
        raise SystemExit('This complete-matrix test is CI-only.')
    EVIDENCE.mkdir(parents=True, exist_ok=True)
    before = baseline_hashes()
    if not before:
        raise SystemExit('Baselines must exist before enforcement verification.')
    cases = [
        ('syntax', 'DetektCiSyntaxProbe.kt', 'ForbiddenComment',
         'package com.sap.ec.civerification\n\n// FIXME: controlled CI-only new finding\n'),
        ('type', 'DetektCiTypeProbe.kt', 'UnsafeCallOnNullableType',
         'package com.sap.ec.civerification\n\n'
         'internal fun detektCiTypeProbe(value: String?): Int = value!!.length\n'),
    ]
    paths = [PROBES / case[1] for case in cases]
    if any(path.exists() for path in paths):
        raise SystemExit('Refusing to overwrite an existing probe path.')
    directory_existed = PROBES.exists()
    outcomes = {}
    try:
        code, _ = run_gate('before')
        if code != 0:
            raise SystemExit('Unchanged sources did not pass before controlled violations.')
        outcomes['before'] = code
        PROBES.mkdir(parents=True, exist_ok=True)
        for name, filename, rule, content in cases:
            probe = PROBES / filename
            probe.write_text(content)
            try:
                code, output = run_gate(name + '-violation')
                if code == 0 or '[' + rule + ']' not in output:
                    raise SystemExit(f'{name} probe did not fail with the expected {rule} finding.')
                outcomes[name + '-violation'] = code
                reports = ROOT / 'build/reports/detekt'
                for report in reports.glob('*.sarif'):
                    (EVIDENCE / (name + '-' + report.name)).write_bytes(report.read_bytes())
            finally:
                probe.unlink(missing_ok=True)
            code, _ = run_gate(name + '-restored')
            if code != 0:
                raise SystemExit(f'Removing the {name} probe did not restore the pass.')
            outcomes[name + '-restored'] = code
            if baseline_hashes() != before:
                raise SystemExit('Baseline contents changed during enforcement verification.')
    finally:
        for path in paths:
            path.unlink(missing_ok=True)
        if not directory_existed and PROBES.exists():
            PROBES.rmdir()
    if baseline_hashes() != before:
        raise SystemExit('Baseline contents changed.')
    (EVIDENCE / 'outcomes.json').write_text(json.dumps(
        {'exit_codes': outcomes, 'baseline_sha256_before_and_after': before,
         'baselines_unchanged': True, 'probes_removed': all(not p.exists() for p in paths)},
        indent=2, sort_keys=True) + '\n')
    print('Verified syntax/type failure and restoration; baselines unchanged; probes removed.')


if __name__ == '__main__':
    main()
