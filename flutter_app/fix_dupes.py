import re

file_path = "C:/Users/bkyil/AndroidStudioProjects/NeuroComet/flutter_app/lib/l10n/app_localizations.dart"
with open(file_path, "r", encoding="utf-8") as f:
    lines = f.readlines()

new_lines = []
in_map = False
current_map_keys = set()

for line in lines:
    if "=> {" in line and "Map<String, String>" in line:
        in_map = True
        current_map_keys = set()
        new_lines.append(line)
        continue

    if in_map and "};" in line:
        in_map = False
        new_lines.append(line)
        continue

    if in_map:
        # Match something like: 'feed': 'Feed',
        m = re.search(r"^\s*'([^']+)'\s*:", line)
        if m:
            key = m.group(1)
            if key in current_map_keys:
                # Comment out duplicate
                new_lines.append("// Duplicate: " + line)
                continue
            else:
                current_map_keys.add(key)
                
    new_lines.append(line)

with open(file_path, "w", encoding="utf-8") as f:
    f.writelines(new_lines)
