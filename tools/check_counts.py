import os
import glob
import xml.etree.ElementTree as ET

base_path = r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res"

def count_strings(path):
    try:
        tree = ET.parse(path)
        root = tree.getroot()
        count = 0
        for child in root:
            if child.tag == "string" and child.attrib.get("name"):
                count += 1
        return count
    except Exception as e:
        return str(e)

# Count baseline
baseline_path = os.path.join(base_path, "values", "strings.xml")
baseline_count = count_strings(baseline_path)
print(f"Baseline: {baseline_count}")

# Count all locales
for folder in sorted(glob.glob(os.path.join(base_path, "values-*"))):
    strings_path = os.path.join(folder, "strings.xml")
    if os.path.exists(strings_path):
        folder_name = os.path.basename(folder)
        count = count_strings(strings_path)
        status = "OK" if count == baseline_count else f"MISSING {baseline_count - count}" if isinstance(count, int) else "ERROR"
        print(f"{folder_name}: {count} - {status}")

