#!/usr/bin/env python3
"""Merge new translations and apply to XML files."""
import os
import re
import sys
import importlib.util

TOOLS_DIR = os.path.dirname(__file__)
RES_DIR = os.path.join(TOOLS_DIR, '..', 'app', 'src', 'main', 'res')
TRANSLATIONS_DIR = os.path.join(TOOLS_DIR, 'translations')

# Language mapping
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

def get_base_strings():
    """Get all English strings."""
    base_file = os.path.join(RES_DIR, 'values', 'strings.xml')
    with open(base_file, 'r', encoding='utf-8') as f:
        content = f.read()
    base_strings = {}
    for match in re.finditer(r'<string name="([^"]+)"[^>]*>([^<]+)</string>', content):
        base_strings[match.group(1)] = match.group(2)
    return base_strings

def load_module(module_path):
    """Load a Python module from path."""
    spec = importlib.util.spec_from_file_location("module", module_path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

def apply_translations_to_xml(lang_dir, translations_dict, base_strings):
    """Apply translations to XML file."""
    lang_file = os.path.join(RES_DIR, lang_dir, 'strings.xml')

    if not os.path.exists(lang_file):
        print(f"  File not found: {lang_file}")
        return 0

    with open(lang_file, 'r', encoding='utf-8') as f:
        content = f.read()

    updated_count = 0

    for name, new_text in translations_dict.items():
        if name not in base_strings:
            continue

        eng_text = base_strings[name]

        # Pattern to find this string if it matches English (untranslated)
        eng_escaped = re.escape(eng_text)
        pattern = rf'(<string name="{re.escape(name)}">)({eng_escaped})(</string>)'

        if re.search(pattern, content):
            # Escape new text for XML
            escaped_new = new_text
            escaped_new = re.sub(r'&(?!amp;|lt;|gt;|quot;|apos;)', '&amp;', escaped_new)
            if "\\'" not in escaped_new:
                escaped_new = escaped_new.replace("'", "\\'")

            content = re.sub(pattern, rf'\g<1>{escaped_new}\g<3>', content)
            updated_count += 1

    with open(lang_file, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)

    return updated_count

def main():
    print("=" * 60)
    print("NeuroComet Translation Merger & Applier")
    print("=" * 60)

    base_strings = get_base_strings()
    print(f"Loaded {len(base_strings)} English strings\n")

    total_updated = 0

    for module_name, lang_dir in LANG_MAP.items():
        # Check for existing translation file
        module_file = os.path.join(TRANSLATIONS_DIR, f'{module_name}.py')
        new_module_file = os.path.join(TRANSLATIONS_DIR, f'{module_name}_new.py')

        translations = {}

        # Load existing translations
        if os.path.exists(module_file):
            try:
                module = load_module(module_file)
                if hasattr(module, 'TRANSLATIONS'):
                    translations.update(module.TRANSLATIONS)
            except Exception as e:
                print(f"  Warning: Error loading {module_name}: {e}")

        # Merge new translations (if they exist)
        if os.path.exists(new_module_file):
            try:
                new_module = load_module(new_module_file)
                if hasattr(new_module, 'TRANSLATIONS'):
                    translations.update(new_module.TRANSLATIONS)
                    print(f"Merged {len(new_module.TRANSLATIONS)} new translations for {lang_dir}")
            except Exception as e:
                print(f"  Warning: Error loading {module_name}_new: {e}")

        if translations:
            count = apply_translations_to_xml(lang_dir, translations, base_strings)
            if count > 0:
                print(f"  Applied {count} translations to {lang_dir}")
                total_updated += count

    print("\n" + "=" * 60)
    print(f"Total: {total_updated} translations applied")
    print("=" * 60)

if __name__ == '__main__':
    main()

