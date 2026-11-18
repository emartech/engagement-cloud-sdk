"""Real Git/index and installer boundary tests; Detekt execution is verified separately."""
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

SCRIPTS = Path(__file__).resolve().parent
PROJECT = SCRIPTS.parents[1]


class GitFixture(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.env = dict(os.environ, GIT_CONFIG_GLOBAL=str(self.root / 'global-gitconfig'),
                        GIT_CONFIG_NOSYSTEM='1')
        self.env.pop('GIT_INDEX_FILE', None)
        subprocess.run(['git', 'init', '-q', str(self.root)], env=self.env, check=True)

    def git(self, *arguments):
        return subprocess.check_output(['git', '-C', str(self.root), *arguments], env=self.env)


class SnapshotTests(GitFixture):
    def setUp(self):
        super().setUp()
        wrapper = self.root / 'gradlew'
        wrapper.write_text('#!/bin/sh\n'
                           'printf "invoked" > gradle-invoked\n'
                           'exit 0\n')
        wrapper.chmod(0o755)

    def stage(self, name, content):
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content)
        self.git('add', '--', name)
        return path

    def analyze(self):
        return subprocess.run([sys.executable, str(SCRIPTS / 'staged-index.py')], cwd=self.root,
                              env=self.env, capture_output=True, text=True)

    def test_no_kotlin_does_not_start_gradle(self):
        self.stage('readme.md', 'documentation\n')
        result = self.analyze()
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertFalse((self.root / 'gradle-invoked').exists())
        self.assertIn('No staged Kotlin', result.stdout)

    def test_snapshot_uses_index_not_unstaged_content_and_handles_spaces(self):
        name = 'module/src/commonMain/Path with spaces.kt'
        path = self.stage(name, '// staged content\n')
        path.write_text('// different unstaged content\n')
        result = self.analyze()
        self.assertEqual(0, result.returncode, result.stderr)
        snapshot = self.root / 'build/detekt/staged/source' / name
        self.assertEqual('// staged content\n', snapshot.read_text())
        self.assertEqual('// different unstaged content\n', path.read_text())
        self.assertTrue((self.root / 'gradle-invoked').exists())

    def test_newline_in_path_is_preserved_without_line_splitting(self):
        name = 'module/src/Path\nwith newline.kt'
        self.stage(name, '// staged content\n')
        self.assertEqual(0, self.analyze().returncode)
        self.assertEqual('// staged content\n', (self.root / 'build/detekt/staged/source' / name).read_text())

    def test_staged_kotlin_symlink_is_rejected(self):
        (self.root / 'Target.kt').symlink_to('outside-file')
        self.git('add', 'Target.kt')
        result = self.analyze()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('regular, merged files', result.stderr)
        self.assertFalse((self.root / 'gradle-invoked').exists())


class InstallerTests(GitFixture):
    def setUp(self):
        super().setUp()
        hooks = self.root / '.githooks'
        hooks.mkdir()
        shutil.copy2(PROJECT / '.githooks/pre-commit', hooks / 'pre-commit')
        (hooks / 'pre-commit').chmod(0o755)

    def install(self):
        return subprocess.run(['bash', str(SCRIPTS / 'install-hooks.sh')], cwd=self.root,
                              env=self.env, capture_output=True, text=True)

    def test_installation_is_repository_local_and_idempotent(self):
        self.assertEqual(0, self.install().returncode)
        self.assertEqual(0, self.install().returncode)
        self.assertEqual(b'.githooks\n', self.git('config', '--local', '--get', 'core.hooksPath'))
        self.assertFalse((self.root / '.githooks/pre-push').exists())
        self.assertFalse((self.root / 'global-gitconfig').exists())

    def test_existing_local_hooks_path_is_not_overwritten(self):
        self.git('config', '--local', 'core.hooksPath', 'other-hooks')
        self.assertNotEqual(0, self.install().returncode)
        self.assertEqual(b'other-hooks\n', self.git('config', '--get', 'core.hooksPath'))

    def test_explicit_confirmation_preserves_old_hook_files(self):
        self.git('config', '--local', 'core.hooksPath', 'other-hooks')
        old = self.root / 'other-hooks/pre-push'
        old.parent.mkdir()
        old.write_text('existing user hook\n')
        self.env['CONFIRM_REPLACE_HOOKS'] = '1'
        self.assertEqual(0, self.install().returncode)
        self.assertEqual(b'.githooks\n', self.git('config', '--get', 'core.hooksPath'))
        self.assertEqual('existing user hook\n', old.read_text())

    def test_existing_global_hooks_path_is_not_shadowed(self):
        (self.root / 'global-gitconfig').write_text('[core]\n hooksPath = external-hooks\n')
        self.assertNotEqual(0, self.install().returncode)
        self.assertEqual(b'external-hooks\n', self.git('config', '--get', 'core.hooksPath'))

    def test_existing_default_hook_is_not_shadowed(self):
        hook = self.root / '.git/hooks/pre-push'
        hook.write_text('#!/bin/sh\nexit 0\n')
        hook.chmod(0o755)
        self.assertNotEqual(0, self.install().returncode)
        self.assertEqual('#!/bin/sh\nexit 0\n', hook.read_text())


class StagedBaselineTests(unittest.TestCase):
    def test_module_unions_are_isolated_and_inputs_unchanged(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for module, identifier in [('sdk', 'Rule:File.kt:sdk'), ('sdk-fcm', 'Rule:File.kt:fcm')]:
                path = root / 'input' / module / 'task.xml'
                path.parent.mkdir(parents=True)
                path.write_text('<SmellBaseline><ManuallySuppressedIssues/><CurrentIssues><ID>'
                                + identifier + '</ID></CurrentIssues></SmellBaseline>')
            before = {p: p.read_bytes() for p in (root / 'input').rglob('*.xml')}
            subprocess.run([sys.executable, str(SCRIPTS / 'merge-staged-baselines.py'),
                            '--input', str(root / 'input'), '--output', str(root / 'output'),
                            '--modules', 'sdk', 'sdk-fcm', 'empty'], check=True, capture_output=True)
            self.assertIn('Rule:File.kt:sdk', (root / 'output/sdk.xml').read_text())
            self.assertNotIn('fcm', (root / 'output/sdk.xml').read_text())
            self.assertIn('Rule:File.kt:fcm', (root / 'output/sdk-fcm.xml').read_text())
            self.assertEqual(before, {p: p.read_bytes() for p in before})


class MakeEnvironmentTests(GitFixture):
    def setUp(self):
        super().setUp()
        shutil.copy2(PROJECT / 'Makefile', self.root / 'Makefile')
        shutil.copy2(PROJECT / '.env.example', self.root / '.env.example')
        scripts = self.root / 'scripts/detekt'
        scripts.mkdir(parents=True)
        shutil.copy2(SCRIPTS / 'staged-index.py', scripts / 'staged-index.py')

    def test_static_command_ignores_env_file_but_lint_retains_existing_env_semantics(self):
        (self.root / '.env').write_text('invalid make syntax\n')
        staged = subprocess.run(['make', 'detekt-staged'], cwd=self.root, env=self.env,
                                capture_output=True, text=True)
        self.assertEqual(0, staged.returncode, staged.stderr)
        lint = subprocess.run(['make', '-n', 'lint'], cwd=self.root, env=self.env,
                              capture_output=True, text=True)
        self.assertNotEqual(0, lint.returncode)
        mixed = subprocess.run(['make', '-n', 'detekt-staged', 'lint'], cwd=self.root, env=self.env,
                               capture_output=True, text=True)
        self.assertNotEqual(0, mixed.returncode)


if __name__ == '__main__':
    unittest.main()
