#!/usr/bin/env python3
"""
Deduplicate string resources in all strings.xml files.
"""

import os
import glob
import xml.etree.ElementTree as ET

RES_DIR = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')

def deduplicate_file(file_path):
    print(f"Processing {file_path}...")
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        seen_names = set()
        to_remove = []

        for child in root:
            # Check for name attribute in string, string-array, plurals
            if 'name' in child.attrib:
                name = child.attrib['name']
                if name in seen_names:
                    print(f"  Duplicate found: {name}")
                    to_remove.append(child)
                else:
                    seen_names.add(name)

        if to_remove:
            print(f"  Removing {len(to_remove)} duplicates...")
            for child in to_remove:
                root.remove(child)

            # Write back
            # ElementTree doesn't preserve comments/whitespace well, but for now getting it to build is priority.
            # We can use a simple write, or try to be smarter if formatting matters a lot.
            # Given the previous tool usage, formatting seems to be standard XML.
            tree.write(file_path, encoding='utf-8', xml_declaration=True)
            print("  Saved.")
        else:
            print("  No duplicates found.")

    except Exception as e:
        print(f"Error processing {file_path}: {e}")

def main():
    strings_files = glob.glob(os.path.join(RES_DIR, 'values-*', 'strings.xml'))
    # Also check base values/strings.xml just in case, though I manually fixed it.
    strings_files.append(os.path.join(RES_DIR, 'values', 'strings.xml'))

    for f in strings_files:
        if os.path.exists(f):
            deduplicate_file(f)

if __name__ == '__main__':
    main()

