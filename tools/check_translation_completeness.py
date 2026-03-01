#!/usr/bin/env python3
"""
Script to check translation completeness across all language files.
"""
import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path

BASE_PATH = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")

def parse_strings_xml(file_path):
    """Parse strings.xml and return a dict of {name: value}"""
    strings = {}
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        for elem in root:
            if elem.tag == 'string':
                name = elem.get('name')
                if name:
                    # Get full text content including CDATA
                    text = ''.join(elem.itertext())
                    strings[name] = text
    except ET.ParseError as e:
        print(f"  ERROR parsing {file_path}: {e}")
    return strings

def get_base_strings():
    """Get all translatable strings from base values/strings.xml"""
    base_file = BASE_PATH / "values" / "strings.xml"
    strings = {}
    non_translatable = set()

    with open(base_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find all string elements
    pattern = r'<string\s+name="([^"]+)"([^>]*)>([^<]*(?:<[^/][^<]*)*)</string>'
    for match in re.finditer(pattern, content, re.DOTALL):
        name = match.group(1)
        attrs = match.group(2)
        value = match.group(3)

        # Check if marked as non-translatable
        if 'translatable="false"' in attrs:
            non_translatable.add(name)
        else:
            strings[name] = value

    return strings, non_translatable

def check_all_languages():
    """Check all language folders for translation completeness."""
    base_strings, non_translatable = get_base_strings()
    total_translatable = len(base_strings)

    print(f"=" * 70)
    print(f"TRANSLATION COMPLETENESS REPORT")
    print(f"=" * 70)
    print(f"Base strings (values/strings.xml): {total_translatable} translatable strings")
    print(f"Non-translatable strings (skipped): {len(non_translatable)}")
    print(f"=" * 70)
    print()

    # Get all language folders
    language_folders = sorted([
        d for d in BASE_PATH.iterdir()
        if d.is_dir() and d.name.startswith('values-') and (d / 'strings.xml').exists()
    ])

    results = []

    for lang_dir in language_folders:
        lang_code = lang_dir.name.replace('values-', '')
        strings_file = lang_dir / 'strings.xml'

        lang_strings = parse_strings_xml(strings_file)

        # Find missing strings
        missing = set(base_strings.keys()) - set(lang_strings.keys())

        # Find potentially untranslated strings (same as English)
        potentially_untranslated = []
        for name, value in lang_strings.items():
            if name in base_strings:
                # Compare values, ignoring whitespace differences
                base_val = base_strings[name].strip()
                lang_val = value.strip()
                if base_val == lang_val and len(base_val) > 3 and not base_val.startswith('%'):
                    # Check if it's not a proper noun or technical term
                    if not any(skip in name for skip in ['app_name', 'emoji_', 'url', 'email']):
                        potentially_untranslated.append(name)

        count = len(lang_strings)
        missing_count = len(missing)
        untranslated_count = len(potentially_untranslated)

        results.append({
            'lang': lang_code,
            'count': count,
            'missing': missing,
            'missing_count': missing_count,
            'potentially_untranslated': potentially_untranslated,
            'untranslated_count': untranslated_count
        })

    # Print summary
    print(f"{'Language':<15} {'Strings':<10} {'Missing':<10} {'Untranslated?':<15} {'Status'}")
    print(f"-" * 70)

    issues_found = False
    for r in results:
        status = "✅ OK" if r['missing_count'] == 0 and r['untranslated_count'] < 5 else "⚠️ NEEDS REVIEW"
        if r['missing_count'] > 0:
            status = "❌ MISSING STRINGS"
            issues_found = True
        elif r['untranslated_count'] >= 10:
            status = "⚠️ MANY UNTRANSLATED"
            issues_found = True

        print(f"{r['lang']:<15} {r['count']:<10} {r['missing_count']:<10} {r['untranslated_count']:<15} {status}")

    print()

    # Detailed report for languages with issues
    for r in results:
        if r['missing_count'] > 0:
            print(f"\n{'='*70}")
            print(f"MISSING STRINGS for {r['lang']}:")
            print(f"{'='*70}")
            for name in sorted(r['missing'])[:20]:  # Show first 20
                print(f"  - {name}")
            if len(r['missing']) > 20:
                print(f"  ... and {len(r['missing']) - 20} more")

        if r['untranslated_count'] >= 10:
            print(f"\n{'='*70}")
            print(f"POTENTIALLY UNTRANSLATED in {r['lang']} (same as English):")
            print(f"{'='*70}")
            for name in sorted(r['potentially_untranslated'])[:20]:  # Show first 20
                print(f"  - {name}: \"{base_strings[name][:50]}...\"" if len(base_strings[name]) > 50 else f"  - {name}: \"{base_strings[name]}\"")
            if len(r['potentially_untranslated']) > 20:
                print(f"  ... and {len(r['potentially_untranslated']) - 20} more")

    print()
    print("=" * 70)
    if issues_found:
        print("RESULT: ⚠️ Some languages need attention")
    else:
        print("RESULT: ✅ All languages appear complete!")
    print("=" * 70)

    return issues_found

if __name__ == "__main__":
    check_all_languages()

