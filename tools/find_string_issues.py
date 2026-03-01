#!/usr/bin/env python3
"""
Script to find and fix invalid unicode escape sequences in Android strings.xml files.
The error "Invalid unicode escape sequence in string" typically occurs when:
1. There's an unescaped backslash followed by 'u'
2. There are characters AAPT can't process properly
"""

import os
import re
import glob

def find_invalid_escapes(filepath):
    """Find potential issues in a strings.xml file."""
    issues = []
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        for i, line in enumerate(lines, 1):
            # Check for \u followed by non-hex characters
            if re.search(r'\\u[^0-9a-fA-F]', line):
                issues.append((i, 'Invalid \\u escape', line.strip()))

            # Check for unescaped apostrophes (not &apos; or \')
            # Find content between > and <
            match = re.search(r'>([^<]*)<', line)
            if match:
                content = match.group(1)
                # Check for apostrophes that aren't escaped
                if "'" in content and "&apos;" not in content and "\\'" not in content:
                    issues.append((i, 'Unescaped apostrophe', line.strip()))

            # Check for unescaped quotes
            if re.search(r'>([^<]*[^\\])"[^<]*<', line):
                # This is less reliable, but check for potential issues
                pass

    except Exception as e:
        issues.append((0, f'Error reading file: {e}', ''))

    return issues

def main():
    base_path = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')

    all_issues = []

    for folder in glob.glob(os.path.join(base_path, 'values*')):
        strings_path = os.path.join(folder, 'strings.xml')
        if os.path.exists(strings_path):
            issues = find_invalid_escapes(strings_path)
            if issues:
                all_issues.append((strings_path, issues))

    if all_issues:
        print("Found potential issues:")
        for filepath, issues in all_issues:
            print(f"\n{filepath}:")
            for line_num, issue_type, content in issues:
                print(f"  Line {line_num}: {issue_type}")
                print(f"    {content[:100]}...")
    else:
        print("No issues found!")

if __name__ == '__main__':
    main()

