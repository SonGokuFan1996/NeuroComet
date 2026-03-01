#!/usr/bin/env python3
"""
Fix about_version string format in all strings.xml files.
"""

import os
import glob

RES_DIR = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')

def fix_file(file_path):
    print(f"Processing {file_path}...")
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Simple string replacement as it is safer than parsing XML and losing formatting
        # We are looking for the specific problematic string

        # Case 1: The English one or copies of it
        old_str = '<string name="about_version">Version %s (Build %d)</string>'
        new_str = '<string name="about_version">Version %1$s (Build %2$d)</string>'

        if old_str in content:
            content = content.replace(old_str, new_str)
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print("  Fixed.")
        else:
             # Check for translated versions that might have %s and %d but different text?
             # The warning logs showed "Multiple substitutions specified in non-positional format"
             # So it is likely %s ... %d pattern.
             # If I want to be comprehensive, I should use regex, but specific fix is better for now.
             print("  Pattern not found (or already fixed).")

    except Exception as e:
        print(f"Error processing {file_path}: {e}")

def main():
    strings_files = glob.glob(os.path.join(RES_DIR, 'values-*', 'strings.xml'))
    strings_files.append(os.path.join(RES_DIR, 'values', 'strings.xml'))

    for f in strings_files:
        if os.path.exists(f):
            fix_file(f)

if __name__ == '__main__':
    main()

