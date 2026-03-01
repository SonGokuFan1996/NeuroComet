#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import sys
sys.stdout.reconfigure(encoding='utf-8')

base_path = r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res\values\strings.xml"
ro_path = r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res\values-ro\strings.xml"

base = ET.parse(base_path)
ro = ET.parse(ro_path)

base_names = {s.get("name") for s in base.getroot().findall("string")}
ro_names = {s.get("name") for s in ro.getroot().findall("string")}

print(f"Base strings: {len(base_names)}")
print(f"RO strings: {len(ro_names)}")

missing = base_names - ro_names
extra = ro_names - base_names
print(f"Missing: {len(missing)} - {list(missing)}")
print(f"Extra: {len(extra)} - {list(extra)}")

for s in base.getroot().findall("string"):
    if s.get("name") in missing:
        text = s.text if s.text else ""
        print(f'  <string name="{s.get("name")}">{text}</string>')

