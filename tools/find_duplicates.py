#!/usr/bin/env python3
"""Find and remove duplicate strings in strings.xml"""

import re
from pathlib import Path
from collections import Counter

xml_file = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res\values\strings.xml")
content = xml_file.read_text(encoding='utf-8')

# Find all string names
names = re.findall(r'<string\s+name="([^"]+)"', content)
dupes = [n for n, c in Counter(names).items() if c > 1]

print(f"Found {len(dupes)} duplicate strings:")
for d in dupes:
    print(f"  - {d}")

