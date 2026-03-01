#!/usr/bin/env python3
"""
Find all untranslated strings and generate a report showing exactly what needs
to be translated for each language.
"""

import os
import re
from pathlib import Path
from collections import defaultdict

BASE_PATH = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")
TOOLS_PATH = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\tools")

def get_base_strings():
    """Get all translatable strings from base values/strings.xml"""
    base_file = BASE_PATH / "values" / "strings.xml"
    strings = {}

    with open(base_file, 'r', encoding='utf-8') as f:
        content = f.read()

    pattern = r'<string\s+name="([^"]+)"([^>]*)>([^<]*(?:<[^/][^<]*)*)</string>'
    for match in re.finditer(pattern, content, re.DOTALL):
        name = match.group(1)
        attrs = match.group(2)
        value = match.group(3)

        if 'translatable="false"' not in attrs:
            strings[name] = value

    return strings

def get_lang_strings(lang_code):
    """Get all strings from a language file."""
    lang_dir = f"values-{lang_code}"
    lang_file = BASE_PATH / lang_dir / "strings.xml"

    if not lang_file.exists():
        return {}

    strings = {}
    with open(lang_file, 'r', encoding='utf-8') as f:
        content = f.read()

    pattern = r'<string\s+name="([^"]+)"[^>]*>([^<]*(?:<[^/][^<]*)*)</string>'
    for match in re.finditer(pattern, content, re.DOTALL):
        name = match.group(1)
        value = match.group(2)
        strings[name] = value

    return strings

def find_untranslated(base_strings, lang_strings):
    """Find strings that are identical to English (likely untranslated)."""
    untranslated = {}

    for name, base_value in base_strings.items():
        if name in lang_strings:
            lang_value = lang_strings[name]
            # If the value is identical to English (excluding whitespace)
            if base_value.strip() == lang_value.strip():
                # Skip very short strings, numbers, placeholders
                if len(base_value.strip()) > 3:
                    untranslated[name] = base_value

    return untranslated

def get_language_folders():
    """Get all language folder codes."""
    folders = []
    for d in BASE_PATH.iterdir():
        if d.is_dir() and d.name.startswith('values-'):
            code = d.name.replace('values-', '')
            if (d / 'strings.xml').exists():
                folders.append(code)
    return sorted(folders)

def main():
    base_strings = get_base_strings()
    print(f"Base English: {len(base_strings)} translatable strings")
    print()

    languages = get_language_folders()

    # Filter out English variants since they're intentionally similar
    non_english_langs = [l for l in languages if not l.startswith('en-')]

    print("=" * 70)
    print("TRANSLATION STATUS (excluding English variants)")
    print("=" * 70)
    print(f"{'Lang':<8} {'Total':<8} {'Untranslated':<14} {'Percentage':<12}")
    print("-" * 70)

    results = []
    for lang in non_english_langs:
        lang_strings = get_lang_strings(lang)
        untranslated = find_untranslated(base_strings, lang_strings)

        translated_count = len(lang_strings) - len(untranslated)
        pct = (translated_count / len(base_strings)) * 100 if base_strings else 0

        results.append({
            'lang': lang,
            'total': len(lang_strings),
            'untranslated': untranslated,
            'percentage': pct
        })

        print(f"{lang:<8} {len(lang_strings):<8} {len(untranslated):<14} {pct:.1f}%")

    print()
    print("=" * 70)
    print("COMMONLY UNTRANSLATED STRINGS (in 15+ languages)")
    print("=" * 70)

    # Find strings untranslated in many languages
    untrans_count = defaultdict(int)
    for r in results:
        for name in r['untranslated']:
            untrans_count[name] += 1

    common_untrans = [(name, count) for name, count in untrans_count.items() if count >= 15]
    common_untrans.sort(key=lambda x: (-x[1], x[0]))

    print(f"\n{len(common_untrans)} strings need translation in 15+ languages:\n")

    # Group by prefix
    by_prefix = defaultdict(list)
    for name, count in common_untrans:
        prefix = name.split('_')[0]
        by_prefix[prefix].append((name, count))

    for prefix in sorted(by_prefix.keys()):
        items = by_prefix[prefix]
        print(f"\n{prefix.upper()} ({len(items)} strings):")
        for name, count in items[:10]:
            eng_value = base_strings.get(name, '')[:50]
            print(f"  {name} ({count} langs): \"{eng_value}...\"" if len(base_strings.get(name, '')) > 50 else f"  {name} ({count} langs): \"{eng_value}\"")
        if len(items) > 10:
            print(f"  ... and {len(items) - 10} more")

    # Write a detailed report
    report_file = TOOLS_PATH / "untranslated_strings_report.txt"
    with open(report_file, 'w', encoding='utf-8') as f:
        f.write("STRINGS THAT NEED TRANSLATION\n")
        f.write("=" * 80 + "\n\n")

        for name, count in common_untrans:
            f.write(f"{name} (untranslated in {count} languages)\n")
            f.write(f"  English: {base_strings.get(name, '')}\n\n")

    print(f"\nDetailed report written to: {report_file}")

if __name__ == "__main__":
    main()

