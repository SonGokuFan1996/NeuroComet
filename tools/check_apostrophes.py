import os
import re
import glob

base_path = r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res"
output = []

for folder in glob.glob(os.path.join(base_path, 'values*')):
    strings_path = os.path.join(folder, 'strings.xml')
    if os.path.exists(strings_path):
        try:
            with open(strings_path, 'r', encoding='utf-8') as f:
                lines = f.readlines()

            for i, line in enumerate(lines, 1):
                # Find content between > and <
                match = re.search(r'>([^<]*)<', line)
                if match:
                    content = match.group(1)
                    # Check for unescaped apostrophes
                    if "'" in content:
                        output.append(f"{strings_path}:{i}: {content[:60]}")

        except Exception as e:
            output.append(f"Error in {strings_path}: {e}")

with open(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\tools\issues.txt", 'w', encoding='utf-8') as f:
    f.write("\n".join(output[:100]))  # First 100 issues
    f.write(f"\n\nTotal issues: {len(output)}")

print(f"Found {len(output)} potential issues. Check tools/issues.txt")

