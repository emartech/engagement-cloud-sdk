#!/usr/bin/env python3
"""Summarize per-task SARIF and reconcile baseline identities without altering either."""
import argparse
from collections import Counter
import hashlib
import html
import json
from pathlib import Path
from urllib.parse import unquote
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--reports', type=Path, required=True)
parser.add_argument('--output', type=Path, required=True)
parser.add_argument('--baseline-dir', type=Path)
parser.add_argument('--baseline-layout', choices=['flat', 'module'], default='module')
parser.add_argument('--html-index', type=Path)
args = parser.parse_args()
rows = []
identities = {}
counts = {}
for report in sorted(args.reports.glob('*.sarif')):
    if report.name == 'merged.sarif' or 'staged' in report.name.lower():
        continue
    task = report.stem
    keys = set()
    task_count = 0
    for run in json.loads(report.read_text())['runs']:
        levels = {r['id']: r.get('defaultConfiguration', {}).get('level', 'warning')
                  for r in run['tool']['driver'].get('rules', [])}
        for result in run.get('results', []):
            location = result['locations'][0]['physicalLocation']
            path = unquote(location['artifactLocation']['uri'])
            rule = result['ruleId']
            severity = result.get('level', levels.get(rule, 'warning'))
            source_set = path.split('/src/', 1)[-1].split('/')[0] if '/src/' in path else 'generated'
            category = 'test' if source_set.lower().endswith('test') or source_set == 'test' else 'production'
            if path.endswith('.kts'):
                source_set, category = 'build-script', 'build-script'
            rows.append({'task': task, 'module': task.split('-detekt', 1)[0], 'rule': rule,
                         'severity': severity, 'category': category, 'source_set': source_set, 'path': path})
            keys.add((rule.split('.')[-1], Path(path).name, result['partialFingerprints']['signature/v1']))
            task_count += 1
    identities[task] = keys
    counts[task] = task_count

baseline_counts = {}
if args.baseline_dir:
    for baseline in sorted(args.baseline_dir.rglob('*.xml')):
        if args.baseline_layout == 'module' and baseline.parent == args.baseline_dir:
            continue  # Historical flat provisional files, not active module/task baselines.
        task = baseline.stem if args.baseline_layout == 'flat' else baseline.parent.name + '-' + baseline.stem
        actual = set()
        for entry in ET.parse(baseline).getroot().iter('ID'):
            rule, name, signature = entry.text.split(':', 2)
            actual.add((rule, name, hashlib.sha1(signature.encode()).hexdigest()))
        expected = identities.get(task, set())
        if actual != expected:
            raise SystemExit(f'{baseline}: orphan IDs={len(actual-expected)}, missing IDs={len(expected-actual)}')
        baseline_counts[str(baseline)] = len(actual)
    covered = {Path(p).stem if args.baseline_layout == 'flat' else Path(p).parent.name+'-'+Path(p).stem
               for p in baseline_counts}
    missing = [task for task, keys in identities.items() if keys and task not in covered]
    if missing:
        raise SystemExit('Missing baselines for reports: ' + ', '.join(missing))

summary = {'report_occurrences': len(rows), 'unique_baseline_identities': sum(map(len, identities.values())),
           'duplicate_identity_occurrences': len(rows)-sum(map(len, identities.values())),
           'by_task': counts, 'baseline_entries': baseline_counts}
for dimension in ['module', 'rule', 'severity', 'category', 'source_set']:
    summary['by_' + dimension] = dict(sorted(Counter(row[dimension] for row in rows).items()))
args.output.parent.mkdir(parents=True, exist_ok=True)
args.output.write_text(json.dumps(summary, indent=2, sort_keys=True) + '\n')
args.output.with_suffix('.findings.json').write_text(json.dumps(rows, indent=2) + '\n')
if args.html_index:
    links = ''.join(f'<tr><td>{html.escape(task)}</td><td>{count}</td><td><a href="{html.escape(task)}.html">HTML</a></td></tr>'
                    for task, count in sorted(counts.items()))
    args.html_index.write_text('<!doctype html><html lang="en"><meta charset="utf-8"><title>Detekt reports</title>'
                              '<h1>Detekt per-task reports</h1><table><tr><th>Task</th><th>Findings</th><th>Report</th></tr>'
                              + links + '</table></html>\n')
print(f'Report occurrences: {len(rows)}; unique baseline identities: {summary["unique_baseline_identities"]}')
if args.baseline_dir:
    print(f'Reconciled {len(baseline_counts)} baselines; {sum(baseline_counts.values())} IDs; zero orphan/missing IDs.')
