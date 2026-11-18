#!/usr/bin/env python3
"""Analyze staged Kotlin blobs, never the unstaged working-tree version."""
import base64
import fcntl
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import time


def git(*args):
    return subprocess.check_output(['git', '--literal-pathspecs', *args])


def main():
    root = Path(os.fsdecode(git('rev-parse', '--show-toplevel').rstrip(b'\n')))
    os.chdir(root)
    raw = git('diff', '--cached', '--name-only', '--diff-filter=ACMR', '-z')
    paths = [os.fsdecode(path) for path in raw.split(b'\0') if path.endswith((b'.kt', b'.kts'))]
    if not paths:
        print('No staged Kotlin files; Detekt skipped.')
        return 0

    started = time.monotonic()
    parent = root / 'build/detekt'
    parent.mkdir(parents=True, exist_ok=True)
    lock = (parent / '.staged.lock').open('w')
    fcntl.flock(lock, fcntl.LOCK_EX)
    temporary = Path(tempfile.mkdtemp(prefix='staged-index-', dir=parent))
    snapshot = parent / 'staged'
    try:
        encoded = []
        for path in paths:
            relative = Path(path)
            if relative.is_absolute() or '..' in relative.parts:
                raise ValueError('Unsafe staged path: ' + path)
            entries = git('ls-files', '--stage', '-z', '--', path).split(b'\0')
            entries = [entry for entry in entries if entry]
            if len(entries) != 1:
                raise ValueError('Unmerged or missing index entry: ' + path)
            metadata, _ = entries[0].split(b'\t', 1)
            mode, oid, stage = metadata.split()
            if stage != b'0' or mode not in {b'100644', b'100755'}:
                raise ValueError('Staged Kotlin files must be regular, merged files: ' + path)
            destination = temporary / 'source' / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(git('cat-file', 'blob', oid.decode('ascii')))
            encoded.append(base64.b64encode(path.encode('utf-8')).decode('ascii'))
        (temporary / 'paths.txt').write_text('\n'.join(encoded) + '\n')
        if snapshot.exists():
            shutil.rmtree(snapshot)  # Only this integration's generated snapshot directory.
        temporary.rename(snapshot)
        print(f'Analyzing {len(paths)} staged Kotlin file(s) from the Git index.', flush=True)
        result = subprocess.run([
            str(root / 'gradlew'), 'detektStagedAll', '--console=plain', '--continue', '--max-workers=2',
            '--no-watch-fs', # Index snapshots are replaced outside Gradle; do not reuse stale VFS entries.
            '-Dorg.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8',
        ], cwd=root)
        elapsed = time.monotonic() - started
        print(f'Staged Detekt finished in {elapsed:.2f}s; complete analysis remains pending CI.', flush=True)
        if result.returncode:
            print('Staged Detekt failed. Reproduce with: make detekt-staged', file=sys.stderr)
        return result.returncode
    finally:
        if temporary.exists():
            shutil.rmtree(temporary)
        lock.close()


if __name__ == '__main__':
    try:
        sys.exit(main())
    except (ValueError, OSError, subprocess.CalledProcessError) as error:
        print('Staged Detekt could not inspect the index: ' + str(error), file=sys.stderr)
        print('Reproduce with: make detekt-staged', file=sys.stderr)
        sys.exit(1)
