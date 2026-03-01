#!/usr/bin/env python3
"""
Fix strings that need formatted="false" attribute in Android strings.xml files.
These strings have multiple substitutions that should not use positional format.

This script:
1. Scans all strings.xml files in values-* directories
2. Detects strings with multiple format specifiers (%s, %d, etc.) without positional markers
3. Adds formatted="false" attribute to those strings
4. Reports all fixes made
"""

import os
import re
import glob
from typing import Optional, Set, Tuple

RES_DIR = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')

# Known strings that need formatted="false" attribute (manual list)
KNOWN_STRINGS_NEEDING_FORMATTED_FALSE = [
    'mock_notif_repost_msg',
    'topic_members_posts',
    'post_comments_shares_count',
    'post_media_count',
]

# Regex to match format specifiers
# Matches: %s, %d, %f, %x, %o, %e, %g, %a, %c, %b, %h, %n, %%
# But NOT positional ones like %1$s, %2$d
NON_POSITIONAL_FORMAT = re.compile(r'(?<!%)%(?!\d+\$)[sdifxXoOeEgGaAcCbBhHn%]')
# Note: POSITIONAL_FORMAT removed as it was unused


def has_multiple_non_positional_formats(text: str) -> bool:
    """Check if text has multiple non-positional format specifiers."""
    # Count non-positional format specifiers
    non_positional = NON_POSITIONAL_FORMAT.findall(text)
    # Exclude %% which is an escape for literal %
    non_positional = [f for f in non_positional if f != '%%']
    return len(non_positional) >= 2


def detect_strings_needing_fix(filepath: str) -> Set[str]:
    """Detect string names that need formatted='false' but don't have it."""
    strings_to_fix = set()

    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # Pattern to find strings without formatted="false"
        # Captures: name and content
        pattern = r'<string name="([^"]+)"(?!\s+formatted)>([^<]+)</string>'

        for match in re.finditer(pattern, content):
            name = match.group(1)
            text = match.group(2)

            if has_multiple_non_positional_formats(text):
                strings_to_fix.add(name)

    except Exception as e:
        print(f"  ⚠️  Error reading {filepath}: {e}")

    return strings_to_fix


def fix_file(filepath: str, strings_to_fix: Optional[Set[str]] = None) -> Tuple[int, Set[str]]:
    """
    Fix strings missing formatted='false' attribute in a single file.

    Args:
        filepath: Path to the strings.xml file
        strings_to_fix: Optional set of string names to fix. If None, auto-detects.

    Returns:
        Tuple of (number of fixes, set of string names fixed)
    """
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        original = content
        fixed_names = set()

        # Combine known strings with detected ones
        all_strings_to_fix = set(KNOWN_STRINGS_NEEDING_FORMATTED_FALSE)
        if strings_to_fix:
            all_strings_to_fix.update(strings_to_fix)

        # Also auto-detect in this file
        detected = detect_strings_needing_fix(filepath)
        all_strings_to_fix.update(detected)

        for string_name in all_strings_to_fix:
            # Pattern to match the string without formatted="false"
            pattern = rf'<string name="{re.escape(string_name)}">(.+?)</string>'
            replacement = rf'<string name="{string_name}" formatted="false">\1</string>'

            new_content = re.sub(pattern, replacement, content)
            if new_content != content:
                content = new_content
                fixed_names.add(string_name)

        if content != original:
            with open(filepath, 'w', encoding='utf-8', newline='\n') as f:
                f.write(content)
            lang = os.path.basename(os.path.dirname(filepath))
            print(f"  ✅ {lang}: Fixed {len(fixed_names)} strings: {', '.join(sorted(fixed_names))}")
            return len(fixed_names), fixed_names
        else:
            return 0, set()
    except Exception as e:
        print(f"  ❌ Error processing {filepath}: {e}")
        return 0, set()


def scan_base_strings() -> Set[str]:
    """Scan the base strings.xml to find all strings with formatted='false'."""
    base_path = os.path.join(RES_DIR, 'values', 'strings.xml')
    strings_with_formatted = set()

    if os.path.exists(base_path):
        try:
            with open(base_path, 'r', encoding='utf-8') as f:
                content = f.read()

            # Find all strings that have formatted="false"
            pattern = r'<string name="([^"]+)" formatted="false">'
            for match in re.finditer(pattern, content):
                strings_with_formatted.add(match.group(1))

            print(f"📋 Found {len(strings_with_formatted)} strings with formatted='false' in base strings.xml")

        except Exception as e:
            print(f"⚠️  Error reading base strings.xml: {e}")

    return strings_with_formatted


def main():
    print("=" * 70)
    print("🔧 Android String Format Fixer")
    print("   Adds formatted='false' to strings with multiple format specifiers")
    print("=" * 70)
    print()

    # First, scan base strings.xml to get the list of strings that need formatted="false"
    base_strings = scan_base_strings()

    print()
    print("🔍 Scanning translation files...")
    print()

    total_fixed = 0
    all_fixed_names = set()

    # Find all strings.xml files in values-* directories
    pattern = os.path.join(RES_DIR, 'values-*', 'strings.xml')
    files = glob.glob(pattern)

    for filepath in sorted(files):
        fixed, names = fix_file(filepath, base_strings)
        total_fixed += fixed
        all_fixed_names.update(names)

    print()
    print("=" * 70)
    if total_fixed > 0:
        print(f"✅ Total fixes: {total_fixed} strings across {len(files)} files")
        print(f"   Fixed string names: {', '.join(sorted(all_fixed_names))}")
    else:
        print("✨ All files are already correctly formatted!")
    print("=" * 70)


if __name__ == '__main__':
    main()
