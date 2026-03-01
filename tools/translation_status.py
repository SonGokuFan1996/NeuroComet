import os
import re
from pathlib import Path

base_path = Path('app/src/main/res')
base_strings = base_path / 'values' / 'strings.xml'

# Read base English strings
with open(base_strings, 'r', encoding='utf-8') as f:
    base_content = f.read()

# Extract all string values from base
base_pattern = r'<string name="([^"]+)"[^>]*>([^<]+)</string>'
base_strings_dict = {m.group(1): m.group(2) for m in re.finditer(base_pattern, base_content)}

# Check each language
langs = []
for folder in sorted(base_path.iterdir()):
    if folder.is_dir() and folder.name.startswith('values-') and not folder.name.startswith('values-en'):
        strings_file = folder / 'strings.xml'
        if strings_file.exists():
            with open(strings_file, 'r', encoding='utf-8') as f:
                content = f.read()

            lang_strings = {m.group(1): m.group(2) for m in re.finditer(base_pattern, content)}

            # Count how many are still in English (same as base)
            same_as_base = 0
            for name, value in lang_strings.items():
                if name in base_strings_dict and value == base_strings_dict[name]:
                    # Skip technical strings that shouldn't be translated
                    if not any(x in name for x in ['emoji_', '_translatable']):
                        same_as_base += 1

            total = len(lang_strings)
            translated = total - same_as_base
            pct = (translated / total * 100) if total > 0 else 0
            langs.append((folder.name.replace('values-', ''), translated, total, pct, same_as_base))

# Sort by percentage (lowest first)
langs.sort(key=lambda x: x[3])

print('Language Translation Status (sorted by completion):')
print('=' * 60)
for lang, trans, total, pct, untrans in langs:
    status = 'OK' if pct >= 95 else 'PARTIAL' if pct >= 80 else 'NEEDS WORK'
    print(f'{status:10} {lang:8} {trans:4}/{total:4} ({pct:5.1f}%) - {untrans:3} untranslated')
print('=' * 60)
print(f'Languages needing work (< 80%): {sum(1 for l in langs if l[3] < 80)}')
print(f'Partially done (80-95%): {sum(1 for l in langs if 80 <= l[3] < 95)}')
print(f'Complete (>= 95%): {sum(1 for l in langs if l[3] >= 95)}')

