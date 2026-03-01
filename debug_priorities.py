import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path

BASE_PATH = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")

def parse_strings_xml(file_path):
    strings = {}
    tree = ET.parse(file_path)
    root = tree.getroot()
    for elem in root:
        if elem.tag == 'string':
            name = elem.get('name')
            if name:
                text = ''.join(elem.itertext())
                strings[name] = text
    return strings

iw_file = BASE_PATH / "values-iw" / "strings.xml"
base_file = BASE_PATH / "values" / "strings.xml"

iw_strings = parse_strings_xml(iw_file)
base_strings = parse_strings_xml(base_file)

name = "about_copyright"
if name in iw_strings and name in base_strings:
    iw_val = iw_strings[name]
    base_val = base_strings[name]
    print(f"Base: {base_val}")
    print(f"IW: {iw_val}")
    print(f"Match: {iw_val == base_val}")
else:
    print(f"{name} not found in one of the files")

