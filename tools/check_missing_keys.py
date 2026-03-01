#!/usr/bin/env python3
"""
Quick check for ACTUALLY MISSING strings (not just untranslated).
"""
import re
from pathlib import Path

BASE_PATH = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")

def get_string_names(file_path):
    """Get all string names from a file."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    pattern = r'<string\s+name="([^"]+)"'
    return set(re.findall(pattern, content))

def main():
    base_file = BASE_PATH / "values" / "strings.xml"
    base_names = get_string_names(base_file)

    # Remove non-translatable
    with open(base_file, 'r', encoding='utf-8') as f:
        content = f.read()

    non_trans = set()
    pattern = r'<string\s+name="([^"]+)"[^>]*translatable="false"'
    non_trans = set(re.findall(pattern, content))

    translatable = base_names - non_trans

    print(f"Base file has {len(translatable)} translatable strings")
    print(f"Non-translatable: {len(non_trans)}")
    print()

    # Check each language
    problems = []
    for lang_dir in sorted(BASE_PATH.iterdir()):
        if not lang_dir.is_dir() or not lang_dir.name.startswith('values-'):
            continue

        strings_file = lang_dir / 'strings.xml'
        if not strings_file.exists():
            continue

        lang_names = get_string_names(strings_file)

        # Find missing (in base but not in lang)
        missing = translatable - lang_names

        # Find extra (in lang but not in base) - these are also problematic
        extra = lang_names - base_names

        if missing or extra:
            problems.append({
                'lang': lang_dir.name,
                'missing': missing,
                'extra': extra
            })

    if problems:
        print("PROBLEMS FOUND:")
        print("=" * 60)
        for p in problems:
            if p['missing']:
                print(f"\n{p['lang']} - MISSING {len(p['missing'])} strings:")
                for name in sorted(p['missing'])[:10]:
                    print(f"  - {name}")
                if len(p['missing']) > 10:
                    print(f"  ... and {len(p['missing'])-10} more")
            if p['extra']:
                print(f"\n{p['lang']} - EXTRA {len(p['extra'])} strings (in lang but not base):")
                for name in sorted(p['extra'])[:5]:
                    print(f"  + {name}")
    else:
        print("ALL LANGUAGES HAVE ALL REQUIRED STRING KEYS!")
        print("(Some values may still be in English, but all keys exist)")

if __name__ == "__main__":
    main()

