"""Scan Arabic strings.xml for non-Arabic contamination (Turkish, English, etc.).

Flags any <string> value that contains Latin alphabet letters or Turkish-specific
glyphs while it SHOULD be Arabic script.
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
AR = ROOT / "app" / "src" / "main" / "res" / "values-ar" / "strings.xml"

content = AR.read_text(encoding="utf-8")

TURKISH_LETTERS = set("şğıİçöüŞĞÇÖÜ")
LATIN_RE = re.compile(r"[A-Za-z]{3,}")
ARABIC_RE = re.compile(r"[\u0600-\u06FF]")

suspects = []
for m in re.finditer(
    r'<string\s+name="([^"]+)"[^>]*>(.*?)</string>',
    content,
    flags=re.DOTALL,
):
    name, val = m.group(1), m.group(2)
    # Strip format specifiers and XML entities / emoji / digits / punctuation
    clean = re.sub(r"%\d*\$?[sd]|&[a-z]+;|[0-9\s\.,!?;:'\"…·•/@#()\[\]\-_+=<>&\\]", "", val)
    has_turk = any(c in TURKISH_LETTERS for c in clean)
    latins = LATIN_RE.findall(clean)
    has_arabic = bool(ARABIC_RE.search(clean))
    # Allow tokens that are pure brand names / acronyms (always Latin).
    brand_tokens = {"NeuroComet", "App", "M3E", "OK", "AI", "DM", "RC", "Wi", "Fi", "URL", "PIN", "ID", "IP", "API", "SMS", "FAQ"}
    non_brand_latins = [t for t in latins if t not in brand_tokens]
    if has_turk or (non_brand_latins and not has_arabic):
        suspects.append((name, val[:160], non_brand_latins, has_turk))

for name, val, latins, turk in suspects[:50]:
    print(f"{'[TURK]' if turk else '[LAT ]'} {name}: {val!r}  latin={latins[:5]}")
print(f"\nTotal suspects: {len(suspects)}")

