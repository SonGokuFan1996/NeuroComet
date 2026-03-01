#!/usr/bin/env python3
"""
Generate a detailed translation status report and create missing translation files.
"""
import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path
from collections import defaultdict

BASE_PATH = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")
OUTPUT_PATH = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\tools\translation_report.txt")

def parse_strings_xml(file_path):
    """Parse strings.xml and return a dict of {name: value}"""
    strings = {}
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Use regex to extract strings to avoid XML parsing issues
        pattern = r'<string\s+name="([^"]+)"[^>]*>([^<]*(?:<[^/][^<]*)*)</string>'
        for match in re.finditer(pattern, content, re.DOTALL):
            name = match.group(1)
            value = match.group(2)
            strings[name] = value
    except Exception as e:
        print(f"Error parsing {file_path}: {e}")
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
    """Generate detailed translation report."""
    base_strings, non_translatable = get_base_strings()
    total_translatable = len(base_strings)

    output_lines = []
    output_lines.append("=" * 80)
    output_lines.append("DETAILED TRANSLATION STATUS REPORT")
    output_lines.append("=" * 80)
    output_lines.append(f"Base strings (values/strings.xml): {total_translatable} translatable strings")
    output_lines.append(f"Non-translatable strings (skipped): {len(non_translatable)}")
    output_lines.append("=" * 80)
    output_lines.append("")

    # Get all language folders
    language_folders = sorted([
        d for d in BASE_PATH.iterdir()
        if d.is_dir() and d.name.startswith('values-') and (d / 'strings.xml').exists()
    ])

    # Categorize untranslated strings
    technical_terms = {
        'ADHD', 'LGBTQ+', 'PTSD', 'OCD', 'AAC', 'TalkBack', 'Material You',
        'OpenDyslexic', 'Lexend', 'Atkinson', 'Hyperlegible', 'PIN', 'OK',
        'NeuroComet', 'Premium', 'RSVP', 'AMA'
    }

    # Strings that are OK to be in English (technical terms, short codes, etc.)
    acceptable_english = set()
    for name, value in base_strings.items():
        # Skip numbers, placeholders
        if value.strip().isdigit():
            acceptable_english.add(name)
        # Skip very short values that might be universal
        if len(value.strip()) <= 2:
            acceptable_english.add(name)
        # Skip strings that contain technical terms
        for term in technical_terms:
            if term in value:
                acceptable_english.add(name)
        # Skip format strings that are just placeholders
        if re.match(r'^%\d+\$[ds]( of %\d+\$[ds])?$', value.strip()):
            acceptable_english.add(name)

    summary_data = []

    for lang_dir in language_folders:
        lang_code = lang_dir.name.replace('values-', '')
        strings_file = lang_dir / 'strings.xml'

        lang_strings = parse_strings_xml(strings_file)

        # Find missing strings
        missing = set(base_strings.keys()) - set(lang_strings.keys())

        # Find untranslated strings (same as English, excluding acceptable ones)
        untranslated = []
        for name, value in lang_strings.items():
            if name in base_strings and name not in acceptable_english:
                base_val = base_strings[name].strip()
                lang_val = value.strip()
                if base_val == lang_val and len(base_val) > 3:
                    untranslated.append(name)

        # Calculate percentage
        translated_count = len(lang_strings) - len(untranslated) - len(missing)
        percentage = (translated_count / total_translatable) * 100 if total_translatable > 0 else 0

        summary_data.append({
            'lang': lang_code,
            'total': len(lang_strings),
            'missing': len(missing),
            'untranslated': len(untranslated),
            'translated': translated_count,
            'percentage': percentage,
            'missing_list': sorted(missing),
            'untranslated_list': sorted(untranslated)
        })

    # Print summary table
    output_lines.append(f"{'Language':<12} {'Total':<8} {'Missing':<10} {'Untranslated':<14} {'Translated':<12} {'%':<8}")
    output_lines.append("-" * 80)

    for data in summary_data:
        output_lines.append(
            f"{data['lang']:<12} {data['total']:<8} {data['missing']:<10} {data['untranslated']:<14} "
            f"{data['translated']:<12} {data['percentage']:.1f}%"
        )

    output_lines.append("")
    output_lines.append("=" * 80)
    output_lines.append("LANGUAGES REQUIRING ATTENTION (more than 100 untranslated strings)")
    output_lines.append("=" * 80)
    output_lines.append("")

    for data in summary_data:
        if data['untranslated'] > 100:
            output_lines.append(f"\n{data['lang'].upper()} - {data['untranslated']} strings need translation:")
            output_lines.append("-" * 40)
            # Group by prefix
            by_prefix = defaultdict(list)
            for name in data['untranslated_list'][:50]:  # First 50
                prefix = name.split('_')[0] if '_' in name else name
                by_prefix[prefix].append(name)

            for prefix in sorted(by_prefix.keys()):
                output_lines.append(f"  [{prefix}]: {', '.join(by_prefix[prefix][:5])}" +
                                  (f" ... +{len(by_prefix[prefix])-5} more" if len(by_prefix[prefix]) > 5 else ""))

    # Write report
    with open(OUTPUT_PATH, 'w', encoding='utf-8') as f:
        f.write('\n'.join(output_lines))

    print(f"Report written to: {OUTPUT_PATH}")

    # Print summary to console
    print("\nSUMMARY:")
    print("-" * 60)

    # Languages with less than 90% translation
    needs_work = [d for d in summary_data if d['percentage'] < 90]
    mostly_done = [d for d in summary_data if 90 <= d['percentage'] < 100]
    complete = [d for d in summary_data if d['percentage'] >= 99.5]

    print(f"Languages >= 99.5% translated: {len(complete)}")
    print(f"Languages 90-99% translated: {len(mostly_done)}")
    print(f"Languages < 90% translated: {len(needs_work)}")

    if needs_work:
        print("\nLanguages needing significant work:")
        for d in sorted(needs_work, key=lambda x: x['percentage']):
            print(f"  {d['lang']}: {d['percentage']:.1f}% ({d['untranslated']} untranslated)")

    return summary_data

if __name__ == "__main__":
    check_all_languages()

