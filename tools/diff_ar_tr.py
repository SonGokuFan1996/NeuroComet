"""Diff values-ar against values-tr and values/.
Find any string in values-ar/strings.xml that equals the Turkish translation
(i.e. accidentally copy-pasted Turkish into the Arabic file)."""
import re
from pathlib import Path

RES = Path(__file__).resolve().parents[1] / "app/src/main/res"

def load(path):
    out = {}
    if not path.exists():
        return out
    content = path.read_text(encoding="utf-8")
    for m in re.finditer(r'<string\s+name="([^"]+)"[^>]*>(.*?)</string>', content, flags=re.DOTALL):
        out[m.group(1)] = m.group(2)
    return out

en = load(RES / "values" / "strings.xml")
tr = load(RES / "values-tr" / "strings.xml")
ar = load(RES / "values-ar" / "strings.xml")

# Cases where ar == tr and both differ from en → suspicious
same = []
for k, v in ar.items():
    if k in tr and tr[k] == v and en.get(k) != v:
        same.append((k, v[:120]))
print(f"Strings in Arabic that equal Turkish: {len(same)}")
for k, v in same[:40]:
    print(f"  {k}: {v!r}")

