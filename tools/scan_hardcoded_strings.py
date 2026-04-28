"""Scan Kotlin sources under app/src/main for likely user-facing hardcoded
strings that should be lifted into strings.xml.

Heuristics:
- Text("...") / Text(text = "...") in @Composable context
- contentDescription = "..."  (anywhere)
- placeholder = { Text("...") }, label = { Text("...") }, title = { Text("...") }, text = { Text("...") }
- Toast.makeText(ctx, "...")
- stringResource is already excluded (we're only looking for literal strings).

We filter out:
- empty strings, single chars, all-digits, all-emoji
- URLs, file paths, resource IDs, debug log tags
- obvious technical keys / JSON keys (lowercase with _/:/.)
- Strings that are already the resource value ("…" fallback)
"""
from __future__ import annotations
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app" / "src" / "main" / "java"

# Filters
URL_RE = re.compile(r"^(?:https?://|mailto:|file:|content://|android\.|com\.|tel:|market://|geo:|#|/|\.)")
KEY_RE = re.compile(r"^[a-z][a-z0-9_]*(?:[._][a-z0-9_]+)*$")  # e.g. "user_id", "api.key"
ONLY_SYMBOL_RE = re.compile(r"^[\W\d\s_·•…]+$")
DATE_FMT_RE = re.compile(r"^[%#dMyHhmsSEazZG\-/:. ,']+$")

# Patterns to scan
PATTERNS = [
    # Text("..."), Text(text = "...", ...)
    re.compile(r'\bText\s*\(\s*(?:text\s*=\s*)?"([^"\n\\]{3,}(?:\\.[^"\n\\]*)*)"'),
    # contentDescription = "..."
    re.compile(r'contentDescription\s*=\s*"([^"\n\\]{2,}(?:\\.[^"\n\\]*)*)"'),
    # Toast.makeText(ctx, "...")
    re.compile(r'Toast\.makeText\s*\([^,]+,\s*"([^"\n\\]{3,}(?:\\.[^"\n\\]*)*)"'),
]

# Exclude strings that look like animation labels, test tags, or dev identifiers
ANIM_LABEL_RE = re.compile(r"^[a-z][a-z0-9_\-]*(?:[A-Z][a-zA-Z0-9]*)*$")
DEV_FILES = {
    "DevOptionsScreen.kt", "AccountLifecycleDevSection.kt", "CameraCallDevSection.kt",
    "ModernApisDevSection.kt", "DeepLinkDiagnosticsDevSection.kt",
    "LanguageStringChecker.kt", "ErrorBoundary.kt", "PerformanceMonitor.kt",
    "DebugOverlay.kt",
}


def is_technical(s: str) -> bool:
    s = s.strip()
    if not s or len(s) < 2:
        return True
    if "$" in s:  # Kotlin template strings — skip (author intent is dynamic)
        return True
    if URL_RE.match(s):
        return True
    if ONLY_SYMBOL_RE.match(s):
        return True
    if DATE_FMT_RE.match(s):
        return True
    # Animation labels / test tags like 'popup-scale', 'typing_dot_1', 'pulseScale'
    if " " not in s and ("-" in s or "_" in s) and len(s) <= 40:
        return True
    # camelCase single token without spaces → likely identifier
    if " " not in s and re.match(r"^[a-z]+[A-Z]", s):
        return True
    # Heuristic: string must contain at least one space OR at least one uppercase letter OR at least 4 word characters
    has_space = " " in s
    has_upper = any(c.isupper() for c in s)
    word_chars = sum(c.isalpha() for c in s)
    if not has_space and not has_upper and word_chars < 4:
        return True
    # Skip strings that are all-lowercase single tokens like "bold", "regular"
    if not has_space and not has_upper and s.islower() and word_chars <= 8:
        return True
    return False


def main() -> None:
    findings: list[tuple[str, int, str, str]] = []
    for kt in SRC.rglob("*.kt"):
        try:
            content = kt.read_text(encoding="utf-8")
        except Exception:
            continue
        # Skip test / preview files
        name = kt.name.lower()
        if name.endswith("preview.kt") or "test" in name:
            continue
        if kt.name in DEV_FILES:
            continue
        # Skip generated / internal files
        rel = kt.relative_to(ROOT).as_posix()
        for pattern in PATTERNS:
            for m in pattern.finditer(content):
                literal = m.group(1)
                if is_technical(literal):
                    continue
                line_no = content.count("\n", 0, m.start()) + 1
                # Try to extract a 40-char context
                findings.append((rel, line_no, pattern.pattern[:20], literal))

    # Deduplicate by (file, literal)
    seen = set()
    unique = []
    for entry in findings:
        key = (entry[0], entry[3])
        if key in seen:
            continue
        seen.add(key)
        unique.append(entry)

    # Group by file
    by_file: dict[str, list[tuple[int, str, str]]] = {}
    for rel, line_no, pat, lit in unique:
        by_file.setdefault(rel, []).append((line_no, pat, lit))

    # Print report summary
    print(f"Total unique hardcoded literals: {len(unique)}")
    print(f"Files affected: {len(by_file)}\n")
    # Write full list
    out = ROOT / "hardcoded_strings_report.txt"
    with out.open("w", encoding="utf-8") as f:
        for rel, items in sorted(by_file.items(), key=lambda x: -len(x[1])):
            f.write(f"\n== {rel} ({len(items)}) ==\n")
            for line_no, pat, lit in sorted(items):
                f.write(f"  L{line_no:5d}  {lit!r}\n")
    print(f"Wrote full report to {out}")

    # Print top files
    top = sorted(by_file.items(), key=lambda x: -len(x[1]))[:30]
    for rel, items in top:
        print(f"  {len(items):4d}  {rel}")


if __name__ == "__main__":
    main()

