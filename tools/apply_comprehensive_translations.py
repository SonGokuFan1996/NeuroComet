#!/usr/bin/env python3
"""Apply comprehensive translations to localized strings.xml files."""

import argparse
import logging
import re
import sys
from pathlib import Path
from typing import Dict, List, Mapping, Optional

sys.path.insert(0, str(Path(__file__).parent))

# Import translations dynamically
import importlib.util
spec = importlib.util.spec_from_file_location(
    "comprehensive_translations",
    Path(__file__).parent / "comprehensive_translations.py",
)
trans_module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(trans_module)
TRANSLATIONS = getattr(trans_module, "TRANSLATIONS", None)

DEFAULT_BASE_PATH = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")
RELATIVE_BASE_PATH = Path(__file__).parents[1] / "app" / "src" / "main" / "res"
STRING_PATTERN = re.compile(r'<string\s+name="([^"]+)"[^>]*>([^<]*(?:<[^/][^<]*)*)</string>', re.DOTALL)
LOGGER = logging.getLogger("apply_comprehensive_translations")


def parse_args(argv: Optional[List[str]] = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-path", type=Path, help="Override the default res directory.")
    parser.add_argument(
        "--lang",
        action="append",
        dest="languages",
        metavar="LANG",
        help="Only process the specified language code (repeatable).",
    )
    parser.add_argument("--dry-run", action="store_true", help="Report changes without writing files.")
    parser.add_argument("--verbose", action="store_true", help="Enable verbose logging.")
    return parser.parse_args(argv)


def configure_logging(verbose: bool) -> None:
    level = logging.DEBUG if verbose else logging.INFO
    logging.basicConfig(level=level, format="[%(levelname)s] %(message)s")


def resolve_base_path(user_path: Optional[Path]) -> Path:
    if user_path:
        return user_path.expanduser().resolve()
    for candidate in (DEFAULT_BASE_PATH, RELATIVE_BASE_PATH):
        if candidate.exists():
            return candidate.resolve()
    return RELATIVE_BASE_PATH.resolve()


def get_base_strings(base_path: Path) -> Dict[str, str]:
    base_file = base_path / "values" / "strings.xml"
    if not base_file.exists():
        raise FileNotFoundError(f"Base strings file not found: {base_file}")
    content = base_file.read_text(encoding="utf-8")
    strings: Dict[str, str] = {}
    for match in STRING_PATTERN.finditer(content):
        strings[match.group(1)] = match.group(2)
    return strings


def escape_translated_value(value: str) -> str:
    result = value
    if "&" in result and not re.search(r"&(amp|lt|gt|quot|apos);", result):
        result = result.replace("&", "&amp;")
    if "'" in result and "\\'" not in result:
        result = result.replace("'", "\\'")
    return result


def apply_translations_to_file(
    lang_code: str,
    translations: Mapping[str, str],
    base_strings: Mapping[str, str],
    base_path: Path,
    dry_run: bool,
) -> int:
    lang_dir = f"values-{lang_code}"
    lang_file = base_path / lang_dir / "strings.xml"
    if not lang_file.exists():
        LOGGER.warning("Missing file for %s: %s", lang_code, lang_file)
        return 0

    content = lang_file.read_text(encoding="utf-8")
    updated = 0

    for name, new_value in translations.items():
        eng_value = base_strings.get(name)
        if eng_value is None:
            continue
        eng_escaped = re.escape(eng_value)
        pattern = rf'(<string name="{re.escape(name)}"[^>]*>)({eng_escaped})(</string>)'
        if re.search(pattern, content):
            escaped_new = escape_translated_value(new_value)
            content = re.sub(pattern, rf"\g<1>{escaped_new}\g<3>", content)
            updated += 1

    if updated and not dry_run:
        lang_file.write_text(content, encoding="utf-8", newline="\n")

    return updated


def validate_translations(translations: Optional[Mapping[str, Mapping[str, str]]]) -> None:
    if not isinstance(translations, Mapping) or not translations:
        raise ValueError("TRANSLATIONS is missing or empty.")


def main(argv: Optional[List[str]] = None) -> int:
    args = parse_args(argv)
    configure_logging(args.verbose)

    validate_translations(TRANSLATIONS)
    base_path = resolve_base_path(args.base_path)
    LOGGER.info("Using base path: %s", base_path)

    try:
        base_strings = get_base_strings(base_path)
    except FileNotFoundError as exc:
        LOGGER.error(str(exc))
        return 1

    languages = args.languages or list(TRANSLATIONS.keys())
    missing_langs = [lang for lang in languages if lang not in TRANSLATIONS]
    if missing_langs:
        LOGGER.error("Unknown language codes requested: %s", ", ".join(sorted(missing_langs)))
        return 1

    total_updated = 0
    for lang_code in languages:
        LOGGER.info("Processing %s", lang_code)
        updated = apply_translations_to_file(
            lang_code=lang_code,
            translations=TRANSLATIONS[lang_code],
            base_strings=base_strings,
            base_path=base_path,
            dry_run=args.dry_run,
        )
        LOGGER.info("Updated %s strings", updated)
        total_updated += updated

    LOGGER.info(
        "TOTAL: Updated %s strings across %s languages", total_updated, len(languages)
    )
    if args.dry_run:
        LOGGER.info("Dry run: no files were modified.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
