#!/usr/bin/env python3
"""Create a complete disposable policy; never modify the enforced YAML."""
import argparse
from pathlib import Path

RULES = (
    'CognitiveComplexMethod', 'CoroutineLaunchedInTestWithoutRunTest',
    'GlobalCoroutineUsage', 'SuspendFunSwallowedCancellation', 'Deprecation',
    'ElseCaseInsteadOfExhaustiveWhen', 'MissingUseCall', 'NullableToStringCall',
    'PropertyUsedBeforeDeclaration',
)
parser = argparse.ArgumentParser()
parser.add_argument('--input', type=Path, required=True)
parser.add_argument('--output', type=Path, required=True)
parser.add_argument('--evaluate-todos', action='store_true', help='Inventory ordinary TODO comments diagnostically.')
args = parser.parse_args()
if args.input.resolve() == args.output.resolve():
    raise SystemExit('Experimental output must not replace the enforced policy.')
policy = args.input.read_text()
for rule in RULES:
    original = f'  {rule}:\n    active: false'
    if policy.count(original) != 1:
        raise SystemExit(f'Expected exactly one explicitly disabled {rule}.')
    policy = policy.replace(original, f'  {rule}:\n    active: true', 1)
if args.evaluate_todos:
    marker = "        value: 'STOPSHIP:'\n    allowedPatterns: ''"
    if policy.count(marker) != 1:
        raise SystemExit('Expected the configured STOPSHIP comment marker.')
    replacement = ("        value: 'STOPSHIP:'\n"
                   "      - reason: 'Diagnostic TODO inventory; not enforced policy.'\n"
                   "        value: 'TODO:'\n    allowedPatterns: ''")
    policy = policy.replace(marker, replacement, 1)
args.output.parent.mkdir(parents=True, exist_ok=True)
args.output.write_text(policy)
print('Disposable experimental policy enables: ' + ', '.join(RULES))
