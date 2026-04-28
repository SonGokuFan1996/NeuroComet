"""For each values-*/strings.xml locale, report how many strings are still
identical to the English base (i.e., untranslated)."""
import re
from pathlib import Path

RES = Path(__file__).resolve().parents[1] / "app/src/main/res"

def load(path):
    out = {}
    if not path.exists():
        return out
    content = path.read_text(encoding="utf-8", errors="replace")
    for m in re.finditer(r'<string\s+name="([^"]+)"[^>]*>(.*?)</string>', content, flags=re.DOTALL):
        out[m.group(1)] = m.group(2)
    return out

en = load(RES / "values" / "strings.xml")

# Keys whose English *is* the correct value in every locale (brand names, acronyms, format strings).
# We won't flag these as "untranslated".
ALLOW_ENGLISH = {
    "app_name", "brand_name",
}

results = []
for loc_dir in sorted(RES.glob("values-*")):
    if loc_dir.name in ("values-night", "values-v31", "values-land") or "-" in loc_dir.name.removeprefix("values-") and loc_dir.name.count("-") > 1:
        # Skip qualifier-only dirs (night, version). Keep locale-only like values-hi, values-en-rUS.
        pass
    loc = load(loc_dir / "strings.xml")
    # How many of loc's keys equal English and the English itself is "translatable-looking"?
    same_as_en = [k for k, v in loc.items() if k in en and v == en[k] and k not in ALLOW_ENGLISH]
    # Also count keys entirely missing from loc vs en
    missing = [k for k in en if k not in loc]
    results.append((loc_dir.name, len(loc), len(same_as_en), len(missing)))

print(f"{'locale':20s} {'total':>6s} {'untranslated':>14s} {'missing':>10s}")
for name, total, unt, miss in results:
    print(f"{name:20s} {total:>6d} {unt:>14d} {miss:>10d}")

