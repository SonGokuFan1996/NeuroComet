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
all_untranslated = []

for lang_dir in BASE_PATH.glob("values-*"):
    lang = lang_dir.name.replace("values-", "")
    if len(lang) > 3: continue

    lang_strings = get_strings(lang_dir / "strings.xml")
    for name, val in base_strings.items():
        if name in lang_strings and lang_strings[name] == val:
            if len(val) > 3 and not val.startswith('%') and not val.startswith('@'):
                all_untranslated.append(name)

common_untranslated = Counter(all_untranslated).most_common(100)
for name, count in common_untranslated:
    print(f"{name}: {count} languages")

