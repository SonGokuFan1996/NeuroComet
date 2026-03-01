import os
import glob

base_path = r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res"
fixed_count = 0

for strings_path in glob.glob(os.path.join(base_path, 'values*', 'strings.xml')):
    try:
        with open(strings_path, 'r', encoding='utf-8') as f:
            content = f.read()

        if "<?xml version='1.0' encoding='utf-8'?>" in content:
            new_content = content.replace(
                "<?xml version='1.0' encoding='utf-8'?>",
                '<?xml version="1.0" encoding="utf-8"?>'
            )
            with open(strings_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Fixed: {os.path.basename(os.path.dirname(strings_path))}")
            fixed_count += 1
    except Exception as e:
        print(f"Error in {strings_path}: {e}")

print(f"\nTotal files fixed: {fixed_count}")

