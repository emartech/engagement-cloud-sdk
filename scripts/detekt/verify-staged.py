#!/usr/bin/env python3
"""Exercise the real hook with a private index; never commit or change the real index."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile
import time

ROOT = Path(__file__).resolve().parents[2]
EVIDENCE = ROOT / 'build/detekt-verification/staged'
PATH = 'engagement-cloud-sdk/src/commonMain/kotlin/com/sap/ec/hooktest/HookIndexProbe.kt'
CLEAN = 'package com.sap.ec.hooktest\n\ninternal const val HOOK_INDEX_PROBE: String = "ready"\n'
VIOLATION = CLEAN + '// FIXME: staged-only controlled violation\n'


def file_hashes(directory):
    return {p.relative_to(ROOT).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in directory.rglob('*.xml')}


def main():
    EVIDENCE.mkdir(parents=True, exist_ok=True)
    baseline_before = file_hashes(ROOT / 'config/detekt/baseline')
    path = ROOT / PATH
    if path.exists():
        raise SystemExit('Refusing to overwrite an existing hook probe.')
    directory_existed = path.parent.exists()
    outcomes = {}
    with tempfile.TemporaryDirectory() as directory:
        index = Path(directory) / 'index'
        env = dict(os.environ, GIT_INDEX_FILE=str(index))

        def git(*args, input=None):
            return subprocess.check_output(['git', '-C', str(ROOT), *args], env=env, input=input)

        def reset():
            git('read-tree', 'HEAD')

        def stage(relative, content):
            blob = git('hash-object', '-w', '--stdin', input=content.encode()).decode().strip()
            git('update-index', '--add', '--cacheinfo', '100644,' + blob + ',' + relative)

        def hook(name, failure=False, bypass=False):
            before = hashlib.sha256(index.read_bytes()).hexdigest()
            environment = dict(env)
            if bypass:
                environment['SKIP_DETEKT'] = '1'
            started = time.monotonic()
            result = subprocess.run([str(ROOT / '.githooks/pre-commit')], cwd=ROOT, env=environment,
                                    stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
            elapsed = time.monotonic() - started
            (EVIDENCE / (name + '.log')).write_text(result.stdout)
            outcomes[name] = {'exit_code': result.returncode, 'seconds': elapsed}
            if bool(result.returncode) != failure:
                raise SystemExit(f'{name}: unexpected exit {result.returncode}; see staged log.')
            if failure and ('ForbiddenComment' not in result.stdout or 'make detekt-staged' not in result.stdout):
                raise SystemExit(f'{name}: missing expected finding/reproduction command.')
            if hashlib.sha256(index.read_bytes()).hexdigest() != before:
                raise SystemExit('Hook changed the index.')
            if file_hashes(ROOT / 'config/detekt/baseline') != baseline_before:
                raise SystemExit('Hook changed committed baselines.')
            if any('> Task ' in line and not any(part in line for part in ['detektStaged', 'prepareDetektStagedBaselines'])
                   for line in result.stdout.splitlines()):
                raise SystemExit('Hook scheduled non-staged Gradle tasks.')
            print(name, 'exit', result.returncode, 'seconds', round(elapsed, 2), flush=True)
            return result.stdout

        try:
            reset()
            stage('hook-docs-only.md', 'documentation\n')
            output = hook('no-kotlin')
            if 'No staged Kotlin' not in output or '> Task ' in output:
                raise SystemExit('No-Kotlin hook started Gradle or did not skip.')
            reset()
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(CLEAN)
            stage(PATH, CLEAN)
            hook('clean-cold')
            hook('clean-warm')
            if outcomes['clean-warm']['seconds'] >= 15:
                raise SystemExit('Warm staged check exceeded the 15-second target.')
            stage(PATH, VIOLATION)
            hook('staged-violation-working-tree-clean', failure=True)
            if path.read_text() != CLEAN:
                raise SystemExit('Hook changed working-tree content.')
            stage(PATH, CLEAN)
            path.write_text(VIOLATION)
            hook('staged-clean-working-tree-violation')
            if path.read_text() != VIOLATION:
                raise SystemExit('Hook changed unstaged content.')
            reset()
            old = 'composeApp/src/commonMain/kotlin/com/sap/ec/sample/enableTracking.kt'
            content = git('show', 'HEAD:' + old).decode().rstrip('\n') + '\n'
            git('update-index', '--force-remove', old)
            stage('composeApp/src/commonMain/kotlin/com/sap/ec/sample/enable tracking.kt', content)
            hook('rename-with-spaces')
            reset()
            old = 'engagement-cloud-sdk-android-fcm/src/main/java/com/sap/ec/service/fcm/model/FirebaseMessagingServiceRegistrationOptions.kt'
            stage(old, git('show', 'HEAD:' + old).decode() + '\n// Staged baseline verification.\n')
            hook('existing-baselined-library-file')
            reset()
            git('update-index', '--force-remove', old)
            output = hook('deleted-kotlin')
            if '> Task ' in output:
                raise SystemExit('Deleted-only Kotlin change started Gradle.')
            reset()
            stage(PATH, VIOLATION)
            hook('explicit-emergency-bypass', bypass=True)
        finally:
            path.unlink(missing_ok=True)
            if not directory_existed and path.parent.exists():
                path.parent.rmdir()
    (EVIDENCE / 'outcomes.json').write_text(json.dumps(
        {'scenarios': outcomes, 'index_unchanged_by_hook': True, 'working_tree_probes_removed': not path.exists(),
         'committed_baselines_unchanged': file_hashes(ROOT / 'config/detekt/baseline') == baseline_before},
        indent=2, sort_keys=True) + '\n')
    print('Real staged/index divergence, scope, baseline and hook checks passed.')


if __name__ == '__main__':
    main()
