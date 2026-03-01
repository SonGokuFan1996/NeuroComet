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
        return f"Error: {e}"

# Count baseline
baseline_path = os.path.join(base_path, "values", "strings.xml")
baseline_count = count_strings(baseline_path)
print(f"Baseline (values/strings.xml): {baseline_count} strings")
print()

# Count all locales
results = []
for folder in sorted(glob.glob(os.path.join(base_path, "values-*"))):
    strings_path = os.path.join(folder, "strings.xml")
    if os.path.exists(strings_path):
        folder_name = os.path.basename(folder)
        count = count_strings(strings_path)
        if isinstance(count, int):
            missing = baseline_count - count if isinstance(baseline_count, int) else "?"
            results.append((folder_name, count, missing))
        else:
            results.append((folder_name, count, "?"))

print("Locale counts:")
for folder, count, missing in results:
    if isinstance(missing, int) and missing > 0:
        print(f"  {folder}: {count} strings, {missing} missing")
    elif isinstance(count, str):  # Error
        print(f"  {folder}: {count}")

print()
print("Files with missing strings:")
for folder, count, missing in results:
    if isinstance(missing, int) and missing > 0:
        print(f"  {folder}: {missing} missing")

