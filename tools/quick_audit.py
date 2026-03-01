import os
import re
from pathlib import Path
from collections import Counter

BASE_PATH = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")

def get_strings(xml_path):
    if not xml_path.exists(): return {}
    content = xml_path.read_text(encoding='utf-8')
    return dict(re.findall(r'<string name="([^"]+)"[^>]*>([^<]+)</string>', content))

base_strings = get_strings(BASE_PATH / "values" / "strings.xml")
untranslated_report = {}

for lang_dir in BASE_PATH.glob("values-*"):
    lang = lang_dir.name.replace("values-", "")
    if len(lang) > 3: continue # Skip regional for now

    lang_strings = get_strings(lang_dir / "strings.xml")
    untranslated = []
    for name, val in base_strings.items():
        if name in lang_strings and lang_strings[name] == val:
            # Check if it's not a technical term
            if len(val) > 3 and not val.startswith('%') and not val.startswith('@'):
                untranslated.append((name, val))

    if untranslated:
        untranslated_report[lang] = untranslated[:20] # Take top 20 for briefness

for lang, items in untranslated_report.items():
    print(f"--- {lang} ---")
    for name, val in items:
        print(f"  {name}: {val}")

