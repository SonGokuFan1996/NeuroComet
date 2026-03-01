import os

file_path = r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res\values\strings.xml"

with open(file_path, 'rb') as f:
    content = f.read()

# Check for BOM
if content[:3] == b'\xef\xbb\xbf':
    print("File has UTF-8 BOM")
    content = content[3:]
else:
    print("No BOM detected")

# Check first 100 bytes as hex
print(f"\nFirst 100 bytes (hex):")
for i, b in enumerate(content[:100]):
    if i % 16 == 0:
        print(f"\n{i:04x}: ", end="")
    print(f"{b:02x} ", end="")
print()

# Look for problematic characters in lines 15-22
text = content.decode('utf-8')
lines = text.split('\n')

print(f"\nLines 15-22 (1-indexed 16-23):")
for i in range(15, min(22, len(lines))):
    line = lines[i]
    print(f"Line {i+1}: {repr(line[:80])}")

# Check for any non-standard characters
print(f"\nChecking for unusual characters in line 17 (1-indexed):")
if len(lines) >= 17:
    line = lines[16]  # 0-indexed line 16 = 1-indexed line 17
    for j, c in enumerate(line):
        code = ord(c)
        if code > 127 or code < 32 and c not in '\t\n\r':
            print(f"  Position {j}: char '{c}' (U+{code:04X})")

