#!/usr/bin/env python3
"""
Add dev strings to all language files.
These are developer-only strings that don't need translation.
"""

import os
import re
from pathlib import Path

RES_DIR = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")

# Dev strings - same in all languages (developer-only, English is fine)
DEV_STRINGS = {
    "dev_go_back": "Go Back",
    "dev_developer_options": "Developer Options",
    "dev_reset_mock_data": "Reset All Mock Data",
    "dev_restart_app": "Restart App (Show Splash)",
    "dev_reset_stay_signed_in": "Reset Stay Signed In Prompt",
    "dev_loading_stress_test": "Loading stress testing…",
    "dev_reset_audience": "Reset Forced Audience",
    "dev_apply_audience": "Apply Current Audience",
    "dev_force_under_13": "Force Under 13",
    "dev_force_teen": "Force Teen",
    "dev_force_adult": "Force Adult",
    "dev_launch_age_popup": "Launch Age Popup",
    "dev_reset_age_overrides": "Reset Age Overrides",
    "dev_refresh_counts": "Refresh Counts",
    "dev_send_test_post": "Send Test Post",
    "dev_send_test_like": "Send Test Like",
    "dev_send_test_user": "Send Test User",
    "dev_send_bulk_posts": "Send 5 Bulk Posts",
    "dev_clear_test_data": "Clear All Test Data",
    "dev_reset_achievements": "Reset Achievements",
    "dev_test_arabic_rtl": "🇸🇦 Test Arabic RTL",
    "dev_reset_system_language": "Reset to System Language",
    "dev_apply_restart": "Apply &amp; Restart",
    "dev_email": "Email",
    "dev_password": "Password",
    "dev_phone_number": "Phone Number",
    "dev_phone_placeholder": "(555) 123-4567",
}


def add_translation(content, key, value):
    """Add a new translation to the strings.xml content if it doesn't exist."""
    pattern = rf'<string\s+name="{re.escape(key)}"'
    if re.search(pattern, content):
        return content, False

    new_entry = f'    <string name="{key}">{value}</string>\n'

    if '</resources>' in content:
        content = content.replace('</resources>', new_entry + '</resources>')
        return content, True
    return content, False


def main():
    print("Adding developer strings to all language files...")

    # Get all values-* directories
    lang_dirs = [d for d in RES_DIR.iterdir() if d.is_dir() and d.name.startswith('values-')]

    total_added = 0

    for lang_dir in sorted(lang_dirs):
        xml_file = lang_dir / "strings.xml"
        if not xml_file.exists():
            continue

        content = xml_file.read_text(encoding='utf-8')
        added_in_file = 0

        for key, val in DEV_STRINGS.items():
            content, added = add_translation(content, key, val)
            if added:
                added_in_file += 1

        if added_in_file > 0:
            xml_file.write_text(content, encoding='utf-8')
            print(f"  ✓ {lang_dir.name}: Added {added_in_file} dev strings")
            total_added += added_in_file

    print(f"\n✨ TOTAL: {total_added} dev strings added!")


if __name__ == "__main__":
    main()

