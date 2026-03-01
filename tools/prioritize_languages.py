#!/usr/bin/env python3
"""
Prioritize translation work by counting missing and untranslated strings.
"""
import os
print("Script starting...")
import re
from pathlib import Path

BASE_PATH = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")

def parse_strings_xml(file_path):
    """Parse strings.xml and return a dict of {name: value}"""
    strings = {}
    try:
        if not os.path.exists(file_path):
            print(f"File not found: {file_path}")
            return {}

        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Use regex instead of ET to avoid encoding issues and be more robust
        pattern = r'<string\s+name="([^"]+)"[^>]*>([^<]*(?:<[^/][^<]*)*)</string>'
        for match in re.finditer(pattern, content, re.DOTALL):
            name = match.group(1)
            value = match.group(2)
            # Unescape common entities
            value = value.replace('&amp;', '&').replace('&lt;', '<').replace('&gt;', '>').replace('&quot;', '"').replace('&apos;', "'").replace("\\'", "'")
            strings[name] = value
    except Exception as e:
        print(f"  ERROR parsing {file_path}: {e}")
    return strings

def get_base_strings():
    """Get all translatable strings from base values/strings.xml"""
    base_file = BASE_PATH / "values" / "strings.xml"
    strings = {}

    # Using simple regex to avoid XML parsing issues with some characters if any,
    # but for base file, let's stick to parsing or regex.
    # Check_translation_completeness used regex for base, let's copy that for robustness.

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

def main():
    base_strings = get_base_strings()
    total_base = len(base_strings)

    print(f"Base Translatable Strings: {total_base}")
    print("-" * 60)
    print(f"{'Language':<10} | {'Missing':<10} | {'Untranslated':<15} | {'Work Needed':<15} | {'Progress'}")
    print("-" * 60)

    # Get all language folders
    language_folders = sorted([
        d for d in BASE_PATH.iterdir()
        if d.is_dir() and d.name.startswith('values-') and (d / 'strings.xml').exists()
    ])

    priorities = []

    for lang_dir in language_folders:
        lang_code = lang_dir.name.replace('values-', '')
        strings_file = lang_dir / 'strings.xml'

        lang_strings = parse_strings_xml(strings_file)

        # 1. Count Missing keys
        missing_keys = set(base_strings.keys()) - set(lang_strings.keys())
        match_count = len(set(base_strings.keys()) & set(lang_strings.keys()))

        # 2. Count Potentially Untranslated (Same as English)
        potential_untranslated = 0

        # Whitelist of keys meant to be the same or ignored
        # 'app_name', 'emoji_*', 'url_*', 'email_*' are common
        ignore_partial = ['app_name', 'emoji_', 'url', 'email', 'default_user_name']

        for name, base_val in base_strings.items():
            if name in lang_strings:
                lang_val = lang_strings[name]

                # Check for "Same as English"
                # Strip and normalize spaces
                b_norm = ' '.join(base_val.strip().split())
                l_norm = ' '.join(lang_val.strip().split())

                if b_norm == l_norm:
                    # Filter out short strings or likely universals
                    if len(b_norm) < 3: # "OK", "Hi"
                        pass
                    elif base_val.startswith('%') or base_val.startswith('@'): # Formats/Refs
                        pass
                    elif any(ign in name for ign in ignore_partial):
                        pass
                    # Filter out purely numeric or symbol-heavy strings?
                    # Let's assume if it matches English exactly and is long enough, it's suspect.
                    else:
                        potential_untranslated += 1

        work_needed = len(missing_keys) + potential_untranslated
        progress = 1.0 - (work_needed / total_base)

        priorities.append({
            'lang': lang_code,
            'missing': len(missing_keys),
            'untranslated': potential_untranslated,
            'work': work_needed,
            'progress': progress
        })

    # Sort descending by work needed
    priorities.sort(key=lambda x: x['work'], reverse=True)

    with open("priorities.txt", "w", encoding="utf-8") as f:
        f.write(f"Base Translatable Strings: {total_base}\n")
        f.write("-" * 60 + "\n")
        f.write(f"{'Language':<10} | {'Missing':<10} | {'Untranslated':<15} | {'Work Needed':<15} | {'Progress'}\n")
        f.write("-" * 60 + "\n")
        for p in priorities:
            f.write(f"{p['lang']:<10} | {p['missing']:<10} | {p['untranslated']:<15} | {p['work']:<15} | {p['progress']:.1%}\n")

    print("Done writing to priorities.txt")

if __name__ == "__main__":
    main()
