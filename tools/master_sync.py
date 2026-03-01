#!/usr/bin/env python3
"""
Master Translation Synchronizer.
Merges ALL known translation dictionaries and applies them aggressively.
"""

import os
import re
import importlib.util
from pathlib import Path

# Paths
TOOLS_DIR = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\tools")
PROJECT_DIR = TOOLS_DIR.parent
RES_DIR = PROJECT_DIR / "app" / "src" / "main" / "res"
TRANSLATIONS_DIR = TOOLS_DIR / "translations"

def get_base_strings():
    """Get all base English strings, arrays, and plurals."""
    base_file = RES_DIR / "values" / "strings.xml"
    content = base_file.read_text(encoding='utf-8')

    strings = {}
    # Strings
    pattern = r'<string name="([^"]+)"[^>]*>([^<]*)</string>'
    for name, value in re.findall(pattern, content):
        strings[name] = value

    # Arrays
    array_pattern = r'<string-array name="([^"]+)"[^>]*>(.*?)</string-array>'
    for name, items_block in re.findall(array_pattern, content, re.DOTALL):
        items = re.findall(r'<item[^>]*>([^<]*)</item>', items_block)
        strings[f"ARRAY:{name}"] = items

    return strings

def load_all_dicts():
    """Consolidated dictionary from all python files in tools/ and tools/translations/"""
    master = {}

    def merge_dict(lang, d):
        if not d: return
        if lang not in master: master[lang] = {}
        master[lang].update(d)

    # 1. Scan tools/*.py
    for f in TOOLS_DIR.glob("*.py"):
        if f.name in ["prioritize_languages.py", "check_apostrophes.py", "quick_audit.py"]: continue
        try:
            # Look for lines like "TRANSLATIONS = {" or "TRANSLATIONS_ES = {"
            content = f.read_text(encoding='utf-8', errors='ignore')
            # Extract language-specific dicts like TRANSLATIONS_ES
            for match in re.finditer(r'TRANSLATIONS_([A-Z]{2})\s*=\s*\{(.*?)\}', content, re.DOTALL):
                lang = match.group(1).lower()
                items = dict(re.findall(r'"([^"]+)"\s*:\s*"([^"]*)"', match.group(2)))
                merge_dict(lang, items)

            # Extract standard TRANSLATIONS if it's a lang-specific file or has a top-level dict
            # (Handled by import if it has a consistent structure)
        except: pass

    # 2. Import comprehensive_translations
    try:
        spec = importlib.util.spec_from_file_location("ct", TOOLS_DIR / "comprehensive_translations.py")
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        if hasattr(module, "TRANSLATIONS"):
            for lang, d in module.TRANSLATIONS.items():
                merge_dict(lang, d)
    except: pass

    # 3. Import everything from tools/translations/
    for f in TRANSLATIONS_DIR.glob("*.py"):
        try:
            lang_code = f.name.split("_")[0]
            if len(lang_code) != 2: continue
            spec = importlib.util.spec_from_file_location("t", f)
            module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(module)
            if hasattr(module, "TRANSLATIONS"):
                merge_dict(lang_code, module.TRANSLATIONS)
        except: pass

    return master

def escape(text):
    if not text: return ""
    res = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    if "'" in res and "\\'" not in res:
        res = res.replace("'", "\\'")
    return res

def run():
    base = get_base_strings()
    master = load_all_dicts()

    print(f"Master dictionary has {len(master)} languages.")
    for l in sorted(master.keys()):
        print(f"  {l}: {len(master[l])} translations")

    total_files = 0
    total_strings = 0

    for lang_dir in RES_DIR.glob("values-*"):
        lang = lang_dir.name.replace("values-", "")
        # Fallback for regional
        lookup = lang
        if "-" in lookup: lookup = lookup.split("-")[0]

        if lookup not in master: continue

        xml_file = lang_dir / "strings.xml"
        if not xml_file.exists(): continue

        content = xml_file.read_text(encoding='utf-8')
        updated = 0

        # Aggressive String Replacement
        def replacer(match):
            nonlocal updated
            tag_start, name, current_val, tag_end = match.groups()

            if name in master[lookup]:
                translated = master[lookup][name]
                base_val = base.get(name)

                # Replace if:
                # 1. Matches base English
                # 2. Is empty
                # 3. Matches key name (placeholder)
                # 4. Contains only English word "Placeholder"
                norm_curr = current_val.strip().replace("\\'", "'")
                norm_base = base_val.strip().replace("\\'", "'") if base_val else None

                should = False
                if base_val and norm_curr == norm_base: should = True
                elif not norm_curr: should = True
                elif norm_curr == name: should = True
                elif "placeholder" in norm_curr.lower(): should = True

                if should:
                    new_val = escape(translated)
                    if new_val != current_val:
                        updated += 1
                        return f"{tag_start}{new_val}{tag_end}"

            return match.group(0)

        new_content = re.sub(r'(<string\s+name="([^"]+)"[^>]*>)([^<]*)(</string>)', replacer, content)

        if updated > 0:
            xml_file.write_text(new_content, encoding='utf-8')
            print(f"v {lang}: updated {updated} strings")
            total_strings += updated
            total_files += 1

    print(f"\nDONE! Updated {total_strings} strings across {total_files} files.")

if __name__ == "__main__":
    run()
