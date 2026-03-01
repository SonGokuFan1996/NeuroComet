#!/usr/bin/env python3
"""
Add all user-facing strings to English variants.
"""

import os
import re
from pathlib import Path

RES_DIR = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")

# User-facing strings for English variants
ENGLISH_STRINGS = {
    "messaging_search_conversations": "Search conversations…",
    "messaging_no_results": "No results found",
    "messaging_no_conversations_yet": "No conversations yet",
    "messaging_start_conversation": "Start a conversation with someone!",
    "messaging_start_your_conversation": "Start your conversation",
    "messaging_user_blocked": "User blocked",
    "messaging_unblock_to_send": "Unblock to send messages",
    "messaging_tap_to_retry": "Tap to retry",
    "messaging_quick_emoji": "Quick emoji",
    "messaging_close_search": "Close search",
    "age_verify_your_age": "Your age",
    "age_confirm": "Confirm",
    "age_skip_for_now": "Skip for now",
    "badges_badge_unlocked": "🎉 Badge Unlocked! 🎉",
    "badges_xp_earned": "+%d XP earned",
    "badges_unlocked_count": "%d unlocked",
    "badges_close": "Close",
    "badges_awesome": "Awesome!",
    "badges_all": "All",
    "category_no_posts": "No posts found in \\'%s\\'",
    "category_be_first": "Be the first to share an insight!",
    "create_moment": "Create Moment",
    "debug_reset_counters": "Reset Counters",
    "debug_tools": "🔧 Debug Tools",
    "debug_performance_overlay_title": "Performance Overlay",
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
    print("Adding user-facing strings to English variants...")

    for variant in ['values-en-rAU', 'values-en-rCA', 'values-en-rGB']:
        xml_file = RES_DIR / variant / "strings.xml"
        if not xml_file.exists():
            continue

        content = xml_file.read_text(encoding='utf-8')
        added_in_file = 0

        for key, val in ENGLISH_STRINGS.items():
            content, added = add_translation(content, key, val)
            if added:
                added_in_file += 1

        if added_in_file > 0:
            xml_file.write_text(content, encoding='utf-8')
            print(f"  ✓ {variant}: Added {added_in_file} strings")

    print("\n✨ Done!")


if __name__ == "__main__":
    main()

