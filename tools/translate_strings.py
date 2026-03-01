#!/usr/bin/env python3
"""
Translation script for NeuroComet Android strings.
Translates missing strings from English to target languages.
"""

import os
import re
import xml.etree.ElementTree as ET
from xml.dom import minidom

# Language mappings with their native names for context
LANGUAGES = {
    'values-ar': ('Arabic', 'ar'),
    'values-cs': ('Czech', 'cs'),
    'values-da': ('Danish', 'da'),
    'values-de': ('German', 'de'),
    'values-el': ('Greek', 'el'),
    'values-es': ('Spanish', 'es'),
    'values-fi': ('Finnish', 'fi'),
    'values-fr': ('French', 'fr'),
    'values-hi': ('Hindi', 'hi'),
    'values-hu': ('Hungarian', 'hu'),
    'values-in': ('Indonesian', 'id'),
    'values-is': ('Icelandic', 'is'),
    'values-it': ('Italian', 'it'),
    'values-iw': ('Hebrew', 'he'),
    'values-ja': ('Japanese', 'ja'),
    'values-ko': ('Korean', 'ko'),
    'values-ms': ('Malay', 'ms'),
    'values-nb': ('Norwegian Bokmål', 'nb'),
    'values-nl': ('Dutch', 'nl'),
    'values-pl': ('Polish', 'pl'),
    'values-pt': ('Portuguese', 'pt'),
    'values-ro': ('Romanian', 'ro'),
    'values-ru': ('Russian', 'ru'),
    'values-sv': ('Swedish', 'sv'),
    'values-th': ('Thai', 'th'),
    'values-tr': ('Turkish', 'tr'),
    'values-uk': ('Ukrainian', 'uk'),
    'values-ur': ('Urdu', 'ur'),
    'values-vi': ('Vietnamese', 'vi'),
    'values-zh': ('Chinese Simplified', 'zh'),
}

RES_DIR = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')


def parse_strings_xml(filepath):
    """Parse strings.xml and return dict of name -> text."""
    tree = ET.parse(filepath)
    strings = {}
    for elem in tree.findall('.//string'):
        name = elem.get('name')
        # Get raw text including any formatting
        text = elem.text or ''
        strings[name] = text
    return strings, tree


def get_untranslated_strings(base_strings, lang_strings):
    """Find strings that are identical to English (untranslated)."""
    untranslated = {}
    for name, eng_text in base_strings.items():
        if name in lang_strings and lang_strings[name] == eng_text:
            # Skip app_name and strings that shouldn't be translated
            if name in ['app_name'] or eng_text.strip() == '':
                continue
            # Skip format-only strings
            if re.match(r'^%\d+\$[ds]$', eng_text.strip()):
                continue
            untranslated[name] = eng_text
    return untranslated


def escape_xml(text):
    """Escape special characters for Android XML."""
    if text is None:
        return ''
    # Handle apostrophes
    text = text.replace("'", "\\'")
    return text


def write_strings_xml(filepath, tree, updated_strings):
    """Update strings in the XML tree and write back."""
    root = tree.getroot()

    for elem in root.findall('.//string'):
        name = elem.get('name')
        if name in updated_strings:
            elem.text = updated_strings[name]

    # Write with proper formatting
    tree.write(filepath, encoding='utf-8', xml_declaration=True)


def analyze_translation_status():
    """Analyze and print translation status for all languages."""
    base_file = os.path.join(RES_DIR, 'values', 'strings.xml')
    base_strings, _ = parse_strings_xml(base_file)

    print(f"Base English has {len(base_strings)} strings\n")
    print("=" * 60)

    results = []

    for lang_dir, (lang_name, lang_code) in LANGUAGES.items():
        lang_file = os.path.join(RES_DIR, lang_dir, 'strings.xml')
        if not os.path.exists(lang_file):
            print(f"{lang_dir}: No strings.xml!")
            continue

        lang_strings, _ = parse_strings_xml(lang_file)
        untranslated = get_untranslated_strings(base_strings, lang_strings)

        total = len(base_strings)
        untranslated_count = len(untranslated)
        translated_count = total - untranslated_count
        pct = (translated_count / total) * 100

        results.append((lang_dir, lang_name, translated_count, total, pct, untranslated_count))

    # Sort by percentage
    results.sort(key=lambda x: x[4], reverse=True)

    for lang_dir, lang_name, translated, total, pct, untranslated in results:
        status = "[OK]" if pct >= 99 else "[PARTIAL]" if pct >= 50 else "[NEEDS]"
        print(f"{status} {lang_name:20} ({lang_dir:15}): {translated:4}/{total} ({pct:5.1f}%) - {untranslated} need translation")

    return results


def export_untranslated_for_language(lang_dir):
    """Export untranslated strings for a specific language."""
    base_file = os.path.join(RES_DIR, 'values', 'strings.xml')
    base_strings, _ = parse_strings_xml(base_file)

    lang_file = os.path.join(RES_DIR, lang_dir, 'strings.xml')
    if not os.path.exists(lang_file):
        print(f"Error: {lang_file} not found")
        return

    lang_strings, _ = parse_strings_xml(lang_file)
    untranslated = get_untranslated_strings(base_strings, lang_strings)

    lang_name = LANGUAGES.get(lang_dir, ('Unknown', 'xx'))[0]

    print(f"\n{lang_name} ({lang_dir}) - {len(untranslated)} strings need translation:\n")
    print("-" * 60)

    for name, text in list(untranslated.items())[:50]:  # First 50
        print(f'<string name="{name}">{text}</string>')

    if len(untranslated) > 50:
        print(f"\n... and {len(untranslated) - 50} more strings")


if __name__ == '__main__':
    import sys

    # Set stdout to UTF-8 for Unicode support
    sys.stdout.reconfigure(encoding='utf-8')

    if len(sys.argv) > 1:
        if sys.argv[1] == 'export':
            lang = sys.argv[2] if len(sys.argv) > 2 else 'values-fi'
            export_untranslated_for_language(lang)
        else:
            print("Usage: python translate_strings.py [export <lang_dir>]")
    else:
        analyze_translation_status()

