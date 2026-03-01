#!/usr/bin/env python3
"""
Aggressively applies all known translations from dictionaries to localized strings.xml files.
Only updates strings that currently match their English base value or are empty.
"""

import os
import re
import importlib.util
from pathlib import Path

# Paths
TOOLS_DIR = Path(__file__).parent
PROJECT_DIR = TOOLS_DIR.parent
RES_DIR = PROJECT_DIR / "app" / "src" / "main" / "res"
TRANSLATIONS_DIR = TOOLS_DIR / "translations"

# Regex patterns
STRING_PATTERN = re.compile(r'(<string\s+name="([^"]+)"[^>]*>)([^<]*)(</string>)', re.DOTALL)
ARRAY_PATTERN = re.compile(r'(<string-array\s+name="([^"]+)"[^>]*>)(.*?)(</string-array>)', re.DOTALL)
PLURAL_PATTERN = re.compile(r'(<plurals\s+name="([^"]+)"[^>]*>)(.*?)(</plurals>)', re.DOTALL)
ITEM_PATTERN = re.compile(r'(<item[^>]*>)([^<]*)(</item>)', re.DOTALL)

def load_base_strings():
    """Load base English strings as reference."""
    base_file = RES_DIR / "values" / "strings.xml"
    if not base_file.exists():
        print(f"Error: Base strings file not found at {base_file}")
        return {}

    content = base_file.read_text(encoding="utf-8")
    base_strings = {}
    for match in STRING_PATTERN.finditer(content):
        base_strings[match.group(2)] = match.group(3)

    # Store arrays and plurals too
    for match in ARRAY_PATTERN.finditer(content):
        tag, name, items_content, end_tag = match.groups()
        items = []
        for item_match in ITEM_PATTERN.finditer(items_content):
            items.append(item_match.group(2))
        base_strings[f"ARRAY:{name}"] = items

    for match in PLURAL_PATTERN.finditer(content):
        tag, name, items_content, end_tag = match.groups()
        items = {} # quantity -> value
        for item_match in re.finditer(r'<item\s+quantity="([^"]+)"[^>]*>([^<]*)</item>', items_content, re.DOTALL):
            items[item_match.group(1)] = item_match.group(2)
        base_strings[f"PLURAL:{name}"] = items

    return base_strings

def load_master_translations():
    """Aggregate all translation dictionaries into a single master dict."""
    master = {}

    # 1. Load from comprehensive_translations.py
    comp_file = TOOLS_DIR / "comprehensive_translations.py"
    if comp_file.exists():
        spec = importlib.util.spec_from_file_location("ct", comp_file)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        if hasattr(module, "TRANSLATIONS"):
            for lang, trans in module.TRANSLATIONS.items():
                if lang not in master: master[lang] = {}
                master[lang].update(trans)

    # 2. Load from tools/translations/*.py
    for trans_file in TRANSLATIONS_DIR.glob("*_translations.py"):
        lang_code = trans_file.name.replace("_translations.py", "")
        # Some might have _new or _clean suffixes, handle them or skip
        if "_" in lang_code:
            # Check if it starts with a standard 2-char code
            base_code = lang_code.split("_")[0]
            if len(base_code) == 2:
                lang_code = base_code
            else:
                continue # Skip weird files

        try:
            spec = importlib.util.spec_from_file_location(f"trans_{lang_code}", trans_file)
            module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(module)
            if hasattr(module, "TRANSLATIONS"):
                if lang_code not in master: master[lang_code] = {}
                master[lang_code].update(module.TRANSLATIONS)
        except Exception as e:
            print(f"Warning: Could not load {trans_file}: {e}")

    return master

def escape_for_xml(text):
    """Clean and escape text for Android XML."""
    if not text: return ""
    # Android strings.xml requires escaping & and '
    # But plurals/arrays/newlines need care.
    res = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    if "'" in res and "\\'" not in res:
        res = res.replace("'", "\\'")
    return res

def apply_translations():
    base_strings = load_base_strings()
    master_translations = load_master_translations()

    print(f"Loaded {len(base_strings)} base strings/arrays/plurals")
    print(f"Loaded translation dictionaries for {len(master_translations)} languages")

    total_updated = 0

    # Process each localized directory
    for lang_dir in RES_DIR.glob("values-*"):
        lang_code = lang_dir.name.replace("values-", "")
        strings_file = lang_dir / "strings.xml"
        if not strings_file.exists(): continue

        # Handle regional fallback (e.g. pt-rBR -> pt)
        lookup_code = lang_code
        if "-" in lookup_code:
            parts = lookup_code.split("-")
            if parts[0] in master_translations:
                lookup_code = parts[0]

        if lookup_code not in master_translations:
            continue

        translations = master_translations[lookup_code]
        content = strings_file.read_text(encoding="utf-8")
        updated_in_file = 0

        def string_replacer(match):
            nonlocal updated_in_file
            tag, name, value, end_tag = match.groups()

            # If we have a translation for this key
            if name in translations:
                target_val = translations[name]
                # Compare current value with English base
                base_val = base_strings.get(name)

                # Aggressive Check: Ignore space/newline differences
                norm_value = value.strip().replace("\\'", "'")
                norm_base = base_val.strip().replace("\\'", "'") if base_val else None

                should_replace = False
                # If matches base English
                if base_val and norm_value == norm_base: should_replace = True
                # Or matches base name (common placeholder)
                elif norm_value == name: should_replace = True
                # Or is empty
                elif not norm_value: should_replace = True

                if should_replace:
                    new_val = escape_for_xml(target_val)
                    # Verify it's actually different from what's there
                    if new_val != value:
                        updated_in_file += 1
                        return f"{tag}{new_val}{end_tag}"

            return match.group(0)

        new_content = STRING_PATTERN.sub(string_replacer, content)

        # Also try to handle arrays if they are in the dictionary
        # Dictionary often doesn't have ARRAYS, but let's check
        # This script focuses on <string> for now as it's the bulk.

        if updated_in_file > 0:
            strings_file.write_text(new_content, encoding="utf-8")
            print(f"v {lang_code}: Updated {updated_in_file} strings")
            total_updated += updated_in_file

    print("=" * 60)
    print(f"Total translations applied: {total_updated}")
    print("=" * 60)

if __name__ == "__main__":
    apply_translations()
