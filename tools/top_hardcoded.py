import re
from collections import Counter
c = Counter()
with open('hardcoded_strings_report.txt', encoding='utf-8') as f:
    for line in f:
        m = re.search(r"L\s*\d+\s+'(.+)'$", line.rstrip())
        if m:
            c[m.group(1)] += 1
for lit, n in sorted(c.items(), key=lambda x: -x[1])[:40]:
    print(f"  {n:3d}  {lit!r}")

