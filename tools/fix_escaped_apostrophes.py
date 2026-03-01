#!/usr/bin/env python3
"""
Fix double-escaped apostrophes (\\') in Android strings.xml files.
These cause "Invalid unicode escape sequence" errors in AAPT.
"""

import os
import re
import glob

RES_DIR = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')


def fix_file(filepath):
    """Fix double-escaped apostrophes in a single file."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        original = content

        # Fix double-escaped apostrophes: \\' -> \'
        content = content.replace("\\\\'", "\\'")

        # Also check for cases where \' became \\ ' or similar
        content = re.sub(r"\\\\+'", r"\\'", content)

        if content != original:
            with open(filepath, 'w', encoding='utf-8', newline='\n') as f:
                f.write(content)
            count = original.count("\\\\'")
            print(f"  ✅ Fixed {count} double-escaped apostrophes in {os.path.basename(os.path.dirname(filepath))}")
            return count
        else:
            return 0
    except Exception as e:
        print(f"  ❌ Error processing {filepath}: {e}")
        return 0


def main():
    print("=" * 60)
    print("Fixing double-escaped apostrophes in strings.xml files")
    print("=" * 60)

    total_fixed = 0

    # Find all strings.xml files in values-* directories
    pattern = os.path.join(RES_DIR, 'values-*', 'strings.xml')
    files = glob.glob(pattern)

    for filepath in files:
        fixed = fix_file(filepath)
        total_fixed += fixed

    print("\n" + "=" * 60)
    print(f"Total fixes: {total_fixed}")
    print("=" * 60)


if __name__ == '__main__':
    main()

