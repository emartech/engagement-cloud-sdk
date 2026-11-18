#!/usr/bin/env python3
"""Build module-specific staged baselines; never edit committed baseline files."""
import argparse
from pathlib import Path
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--input', type=Path, required=True)
parser.add_argument('--output', type=Path, required=True)
parser.add_argument('--modules', nargs='+', required=True)
args = parser.parse_args()
if args.input.resolve() == args.output.resolve() or args.input.resolve() in args.output.resolve().parents:
    raise SystemExit('Staged baseline output must be outside committed baselines.')
args.output.mkdir(parents=True, exist_ok=True)
for module in args.modules:
    if Path(module).name != module or module in {'.', '..'}:
        raise SystemExit('Invalid module name: ' + module)
    current, manual = set(), set()
    for baseline in sorted((args.input / module).glob('*.xml')):
        document = ET.parse(baseline).getroot()
        current.update(entry.text for entry in document.findall('./CurrentIssues/ID'))
        manual.update(entry.text for entry in document.findall('./ManuallySuppressedIssues/ID'))
    result = ET.Element('SmellBaseline')
    for name, values in [('ManuallySuppressedIssues', manual), ('CurrentIssues', current)]:
        section = ET.SubElement(result, name)
        for value in sorted(values):
            ET.SubElement(section, 'ID').text = value
    ET.indent(result)
    ET.ElementTree(result).write(args.output / (module + '.xml'), encoding='utf-8', xml_declaration=True)
print('Prepared staged baseline unions for ' + ', '.join(args.modules))
