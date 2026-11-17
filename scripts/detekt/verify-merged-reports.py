#!/usr/bin/env python3
"""Prove that the official XML/SARIF merges retain every distinct per-task finding."""
import argparse
import json
from pathlib import Path
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--reports', type=Path, required=True)
args = parser.parse_args()

def xml_findings(path):
    return {(file.attrib['name'], tuple(sorted(error.attrib.items())))
            for file in ET.parse(path).getroot().findall('file') for error in file.findall('error')}

def sarif_findings(path):
    return {(result['ruleId'], result['locations'][0]['physicalLocation']['artifactLocation']['uri'],
             result['partialFingerprints']['signature/v1'])
            for run in json.loads(path.read_text())['runs'] for result in run.get('results', [])}

for extension, findings in [('xml', xml_findings), ('sarif', sarif_findings)]:
    expected = set()
    inputs = [p for p in args.reports.glob('*.' + extension)
              if p.name != 'merged.' + extension and 'staged' not in p.name.lower()]
    if not inputs:
        raise SystemExit(f'No per-task {extension} reports found.')
    for report in inputs:
        expected.update(findings(report))
    actual = findings(args.reports / ('merged.' + extension))
    if actual != expected:
        raise SystemExit(f'{extension}: missing={len(expected-actual)}, unexpected={len(actual-expected)}')
    print(f'{extension}: merge retained all {len(expected)} distinct finding identities.')
