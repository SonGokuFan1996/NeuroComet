#!/usr/bin/env python3
"""
Batch translation applier for NeuroComet.
Imports translations from individual language files and applies them.
"""

import os
import sys
import re
import xml.etree.ElementTree as ET

# Add the translations directory to path
sys.path.insert(0, os.path.dirname(__file__))

RES_DIR = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')


def get_base_strings():
    """Get all English strings as a reference."""
    base_file = os.path.join(RES_DIR, 'values', 'strings.xml')
    tree = ET.parse(base_file)
    base_strings = {}
    for elem in tree.findall('.//string'):
        name = elem.get('name')
        text = elem.text or ''
        base_strings[name] = text
    return base_strings


def apply_translations(lang_dir, translations_dict):
    """Apply translations to a language file, only updating untranslated strings."""
    lang_file = os.path.join(RES_DIR, lang_dir, 'strings.xml')

    if not os.path.exists(lang_file):
        print(f"  ⚠️  File not found: {lang_file}")
        return 0

    base_strings = get_base_strings()

    # Read the target file line by line
    with open(lang_file, 'r', encoding='utf-8') as f:
        content = f.read()

    updated_count = 0

    for name, new_text in translations_dict.items():
        if name not in base_strings:
            continue

        eng_text = base_strings[name]

        # Escape the English text for regex matching
        # The file may have escaped apostrophes
        eng_escaped = re.escape(eng_text)

        # Build pattern to find this specific string if it matches English
        pattern = rf'(<string name="{re.escape(name)}">)({eng_escaped})(</string>)'

        if re.search(pattern, content):
            # Escape the new text for XML
            escaped_new = new_text
            # Escape ampersands that aren't already entities
            escaped_new = re.sub(r'&(?!amp;|lt;|gt;|quot;|apos;)', '&amp;', escaped_new)
            # Escape apostrophes (but not if already escaped)
            if "\\'" not in escaped_new:
                escaped_new = escaped_new.replace("'", "\\'")

            # Replace
            content = re.sub(pattern, rf'\g<1>{escaped_new}\g<3>', content)
            updated_count += 1

    # Write back
    with open(lang_file, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)

    return updated_count


def main():
    """Apply translations for all available language files."""

    print("=" * 60)
    print("NeuroComet Translation Applier")
    print("=" * 60)

    # Import translation modules
    translations_dir = os.path.join(os.path.dirname(__file__), 'translations')

    # Language mapping: translation file -> Android values folder
    LANG_MAP = {
        'pt_translations': 'values-pt',
        'it_translations': 'values-it',
        'ja_translations': 'values-ja',
        'zh_translations': 'values-zh',
        'ko_translations': 'values-ko',
        'ru_translations': 'values-ru',
        'pl_translations': 'values-pl',
        'es_translations': 'values-es',
        'fr_translations': 'values-fr',
        'de_translations': 'values-de',
        'ar_translations': 'values-ar',
        'tr_translations': 'values-tr',
        'nl_translations': 'values-nl',
        'cs_translations': 'values-cs',
        'da_translations': 'values-da',
        'fi_translations': 'values-fi',
        'hi_translations': 'values-hi',
        'sv_translations': 'values-sv',
        'nb_translations': 'values-nb',
        'vi_translations': 'values-vi',
        'th_translations': 'values-th',
        'uk_translations': 'values-uk',
        'el_translations': 'values-el',
        'hu_translations': 'values-hu',
        'in_translations': 'values-in',
        'is_translations': 'values-is',
        'iw_translations': 'values-iw',
        'ms_translations': 'values-ms',
        'ro_translations': 'values-ro',
        'ur_translations': 'values-ur',
    }

    total_updated = 0

    for module_name, lang_dir in LANG_MAP.items():
        module_file = os.path.join(translations_dir, f'{module_name}.py')

        if not os.path.exists(module_file):
            print(f"Skipping {lang_dir}: No translation file")
            continue

        # Import the module
        import importlib.util
        spec = importlib.util.spec_from_file_location(module_name, os.path.join(translations_dir, f"{module_name}.py"))
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)

        # Get the dictionary
        translations_dict = getattr(module, 'TRANSLATIONS', {})

        print(f"\nProcessing {lang_dir}...")
        count = apply_translations(lang_dir, translations_dict)
        print(f"Applied {count} translations.")

        if hasattr(module, 'TRANSLATIONS'):
            count = apply_translations(lang_dir, module.TRANSLATIONS)
            total_updated += count
        else:
            print(f"No TRANSLATIONS dict found in {module_name}")

    print("\n" + "=" * 60)
    print(f"Total: {total_updated} strings updated")
    print("=" * 60)


if __name__ == '__main__':
    main()
