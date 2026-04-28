"""Replace high-frequency hardcoded user-visible literals with stringResource
references. Safe, conservative rules only.

Handles:
  * contentDescription = "Literal"  → contentDescription = stringResource(R.string.key)
  * Text("Literal")                 → Text(stringResource(R.string.key))
  * Text(text = "Literal")          → Text(text = stringResource(R.string.key))
  * Text("Literal", ...)            → Text(stringResource(R.string.key), ...)

The replacement only fires for literals in LITERAL_MAP. It preserves any
trailing arguments and existing parameter order.

After running, caller files must have `import androidx.compose.ui.res.stringResource`.
This script adds that import automatically when at least one replacement was
applied in a file.
"""
from __future__ import annotations
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app" / "src" / "main" / "java"
STRINGS_XML = ROOT / "app" / "src" / "main" / "res" / "values" / "strings.xml"

# literal → resource key (key must exist in strings.xml or be added below)
LITERAL_MAP: dict[str, str] = {
    # Already present keys
    "Cancel": "action_cancel",
    "Save": "action_save",
    "Delete": "action_delete",
    "Retry": "action_retry",
    "Back": "cd_back",
    "Close": "cd_close",
    "Share": "cd_share",
    "More options": "cd_more_options",
    "Avatar": "cd_avatar",
    "Verified": "cd_verified",
    "Post image": "cd_post_image",
    "User avatar": "cd_user_avatar",
    # Keys we will add below
    "More": "action_more",
    "Add": "action_add",
    "Skip": "action_skip",
    "Post": "action_post",
    "Like": "action_like",
    "Comment": "action_comment",
    "Bookmark": "action_bookmark",
    "Open": "action_open",
    "Allow": "action_allow",
    "Search": "cd_search",
    "Close search": "cd_close_search",
    "Clear": "cd_clear",
    "Selected": "cd_selected_state",
    "Profile picture": "cd_profile_picture",
    "Caller avatar": "cd_caller_avatar",
    "Sent": "status_sent",
    "Sending": "status_sending",
    "Failed": "status_failed",
    "Location": "cd_location",
    "Messages": "nav_messages",
    "Call History": "call_history_title",
    "Practice Call": "call_practice_title",
    "Filters": "image_editor_tab_filters",
    "Comments": "comments_title",
    "Add story": "stories_add_story",
    "How are you feeling?": "feed_mood_prompt",
    # Round 2
    "Done": "action_done",
    "Submit": "action_submit",
    "Edit": "action_edit",
    "Remove": "action_remove",
    "Vote": "action_vote",
    "Send": "action_send",
    "Enter": "action_enter",
    "Apply": "action_apply",
    "Continue": "action_continue",
    "Verify": "settings_pin_verify",
    "Unblock": "action_unblock",
    "Search messages…": "dm_search_messages_placeholder",
    "Type a message": "call_type_prompt",
    "Voice call": "cd_voice_call",
    "Video call": "cd_video_call",
    "Contacts": "nav_contacts",
    "Recent": "label_recent",
    "Video Calls": "label_video_calls",
    "Group": "label_group",
    "Group avatar": "cd_group_avatar",
    "Wallpaper": "dm_wallpaper",
    "Scroll to bottom": "cd_scroll_to_latest",
    "Scroll to latest": "cd_scroll_to_latest",
    "Take photo": "cd_take_photo",
    "Remove attachment": "cd_remove_attachment",
    "Cancel recording": "cd_cancel_recording",
    "Recording...": "label_recording",
    "Recording cancelled": "label_recording_cancelled",
    "View profile": "menu_view_profile",
    "Report User": "menu_report_user",
    "Messaging paused": "dm_messaging_paused",
    "Unblock to resume conversation": "dm_unblock_to_resume",
    "Smileys": "emoji_category_smileys",
    "Gestures": "emoji_category_gestures",
    "Hearts": "emoji_category_hearts",
    "Objects": "emoji_category_objects",
    "Animals": "emoji_category_animals",
    "New message": "aosp_new_message",
    "Send message": "cd_send_message",
    "Tap to retry": "action_tap_to_retry",
    "Quick emoji": "label_quick_emoji",
    "User blocked": "label_user_blocked",
    "Unblock to send messages": "dm_unblock_to_send",
    "Start your conversation": "dm_start_conversation",
    "Access your contacts": "contacts_access_title",
    "Allow contacts access": "contacts_allow_access_title",
    "People on NeuroComet": "contacts_people_on_app",
    "Search posts, people, topics...": "explore_search_placeholder",
    "Cancel search": "explore_cancel_search",
    "Clear All": "games_clear_all",
    "Clear filter": "cd_clear_filter",
    "Trending Now": "explore_trending_now",
    "Story": "label_story",
    "Hot": "label_hot",
    "Viral": "label_viral",
    "See all": "action_see_all",
    "Create PIN": "parental_create_pin_button",
    "Change PIN": "parental_change_pin_title",
    "Enter PIN": "parental_enter_pin_button",
    "Parental Controls": "parental_controls_title",
    "Top Voted": "feedback_top_voted",
    "Newest": "feedback_newest",
    "Closed Beta": "beta_closed_label",
    "Gallery": "label_gallery",
    "Edit image": "cd_edit_image",
    "Image being edited": "cd_image_editing",
    "No image selected": "label_no_image_selected",
    "Change App Icon": "icon_change_title",
    "Apply Icon": "icon_apply_button",
    "Add Shortcut": "icon_add_shortcut_button",
    "Create Shortcut": "icon_create_shortcut_button",
}

# Keys we need to add to values/strings.xml (only if missing)
NEW_STRINGS: dict[str, str] = {
    "action_more": "More",
    "action_add": "Add",
    "action_skip": "Skip",
    "action_post": "Post",
    "action_like": "Like",
    "action_comment": "Comment",
    "action_bookmark": "Bookmark",
    "action_open": "Open",
    "action_allow": "Allow",
    "cd_search": "Search",
    "cd_close_search": "Close search",
    "cd_clear": "Clear",
    "cd_selected_state": "Selected",
    "cd_profile_picture": "Profile picture",
    "cd_caller_avatar": "Caller avatar",
    "status_sent": "Sent",
    "status_sending": "Sending",
    "status_failed": "Failed",
    "cd_location": "Location",
    "call_history_title": "Call History",
    "comments_title": "Comments",
    "stories_add_story": "Add story",
    "feed_mood_prompt": "How are you feeling?",
    # Round 2
    "action_done": "Done",
    "action_submit": "Submit",
    "action_edit": "Edit",
    "action_remove": "Remove",
    "action_vote": "Vote",
    "action_send": "Send",
    "action_enter": "Enter",
    "action_apply": "Apply",
    "action_continue": "Continue",
    "action_unblock": "Unblock",
    "action_see_all": "See all",
    "action_tap_to_retry": "Tap to retry",
    "cd_voice_call": "Voice call",
    "cd_video_call": "Video call",
    "nav_contacts": "Contacts",
    "label_recent": "Recent",
    "label_video_calls": "Video Calls",
    "label_group": "Group",
    "cd_group_avatar": "Group avatar",
    "cd_take_photo": "Take photo",
    "cd_remove_attachment": "Remove attachment",
    "cd_cancel_recording": "Cancel recording",
    "label_recording": "Recording…",
    "label_recording_cancelled": "Recording cancelled",
    "menu_view_profile": "View profile",
    "menu_report_user": "Report user",
    "dm_messaging_paused": "Messaging paused",
    "dm_unblock_to_resume": "Unblock to resume conversation",
    "emoji_category_smileys": "Smileys",
    "emoji_category_gestures": "Gestures",
    "emoji_category_hearts": "Hearts",
    "emoji_category_objects": "Objects",
    "emoji_category_animals": "Animals",
    "aosp_new_message": "New message",
    "cd_send_message": "Send message",
    "label_quick_emoji": "Quick emoji",
    "label_user_blocked": "User blocked",
    "dm_unblock_to_send": "Unblock to send messages",
    "dm_start_conversation": "Start your conversation",
    "contacts_access_title": "Access your contacts",
    "contacts_allow_access_title": "Allow contacts access",
    "contacts_people_on_app": "People on NeuroComet",
    "explore_search_placeholder": "Search posts, people, topics…",
    "explore_cancel_search": "Cancel search",
    "cd_clear_filter": "Clear filter",
    "explore_trending_now": "Trending Now",
    "label_story": "Story",
    "label_hot": "Hot",
    "label_viral": "Viral",
    "parental_create_pin_button": "Create PIN",
    "parental_enter_pin_button": "Enter PIN",
    "parental_controls_title": "Parental Controls",
    "feedback_top_voted": "Top Voted",
    "feedback_newest": "Newest",
    "beta_closed_label": "Closed Beta",
    "label_gallery": "Gallery",
    "cd_image_editing": "Image being edited",
    "label_no_image_selected": "No image selected",
    "icon_change_title": "Change App Icon",
    "icon_apply_button": "Apply Icon",
    "icon_add_shortcut_button": "Add Shortcut",
    "icon_create_shortcut_button": "Create Shortcut",
}

IMPORT_LINE = "import androidx.compose.ui.res.stringResource"
R_IMPORT_NEEDED = True  # all files under kyilmaz.neurocomet have R auto-imported via same package


def ensure_strings_xml(new: dict[str, str]) -> None:
    content = STRINGS_XML.read_text(encoding="utf-8")
    existing = set(re.findall(r'<string\s+name="([^"]+)"', content))
    to_add = {k: v for k, v in new.items() if k not in existing}
    if not to_add:
        return
    lines = ["", "    <!-- Added by replace_common_hardcoded.py -->"]
    for k, v in to_add.items():
        lines.append(f'    <string name="{k}">{v}</string>')
    block = "\n".join(lines) + "\n"
    content = content.replace("</resources>", block + "</resources>", 1)
    STRINGS_XML.write_text(content, encoding="utf-8")
    print(f"Added {len(to_add)} keys to values/strings.xml")


def replace_in_file(path: Path) -> int:
    try:
        original = path.read_text(encoding="utf-8")
    except Exception:
        return 0
    text = original
    count = 0

    for literal, key in LITERAL_MAP.items():
        esc = re.escape(literal)

        # contentDescription = "literal"   (and optional trailing)
        def _cd_sub(m: re.Match) -> str:
            nonlocal count
            count += 1
            return f'contentDescription = stringResource(R.string.{key})'
        new = re.sub(
            rf'contentDescription\s*=\s*"{esc}"(?!\s*\+)',
            _cd_sub, text
        )
        if new != text:
            text = new

        # Text("literal")   — single argument only
        def _text_sub(m: re.Match) -> str:
            nonlocal count
            count += 1
            return f'Text(stringResource(R.string.{key}))'
        new = re.sub(
            rf'\bText\s*\(\s*"{esc}"\s*\)',
            _text_sub, text
        )
        if new != text:
            text = new

        # Text(text = "literal")   — only this named arg
        def _text_named_sub(m: re.Match) -> str:
            nonlocal count
            count += 1
            return f'Text(text = stringResource(R.string.{key}))'
        new = re.sub(
            rf'\bText\s*\(\s*text\s*=\s*"{esc}"\s*\)',
            _text_named_sub, text
        )
        if new != text:
            text = new

        # Text("literal", ...) — with trailing params
        def _text_multi_sub(m: re.Match) -> str:
            nonlocal count
            count += 1
            return f'Text(stringResource(R.string.{key}),'
        new = re.sub(
            rf'\bText\s*\(\s*"{esc}"\s*,',
            _text_multi_sub, text
        )
        if new != text:
            text = new

        # Text(text = "literal", ...) — with trailing params
        def _text_named_multi_sub(m: re.Match) -> str:
            nonlocal count
            count += 1
            return f'Text(text = stringResource(R.string.{key}),'
        new = re.sub(
            rf'\bText\s*\(\s*text\s*=\s*"{esc}"\s*,',
            _text_named_multi_sub, text
        )
        if new != text:
            text = new

    if count == 0:
        return 0

    # Ensure stringResource import
    if IMPORT_LINE not in text:
        # Insert after package line or after existing imports
        import_anchor = re.search(r"(\nimport [^\n]+\n)+", text)
        if import_anchor:
            insert_pos = import_anchor.end()
            text = text[:insert_pos] + IMPORT_LINE + "\n" + text[insert_pos:]
        else:
            # after package line
            pkg = re.search(r"^package [^\n]+\n", text)
            if pkg:
                text = text[:pkg.end()] + "\n" + IMPORT_LINE + "\n" + text[pkg.end():]

    # For files NOT in the base com.kyilmaz.neurocomet package, the R class
    # isn't auto-imported — add it if missing.
    pkg_match = re.search(r"^package\s+([^\s\n]+)", text, flags=re.MULTILINE)
    if pkg_match and pkg_match.group(1) != "com.kyilmaz.neurocomet":
        if "import com.kyilmaz.neurocomet.R" not in text:
            import_anchor = re.search(r"(\nimport [^\n]+\n)+", text)
            if import_anchor:
                insert_pos = import_anchor.end()
                text = text[:insert_pos] + "import com.kyilmaz.neurocomet.R\n" + text[insert_pos:]

    path.write_text(text, encoding="utf-8", newline="\n")
    return count


def main() -> int:
    ensure_strings_xml(NEW_STRINGS)
    # Skip dev/debug/test files (non-user-facing)
    SKIP = {"DevOptionsScreen.kt", "LanguageStringChecker.kt", "ErrorBoundary.kt"}
    total = 0
    affected = 0
    for kt in SRC.rglob("*.kt"):
        if kt.name in SKIP:
            continue
        n = replace_in_file(kt)
        if n:
            total += n
            affected += 1
            print(f"  {kt.relative_to(ROOT).as_posix():60s}  {n} replacements")
    print(f"\nTotal: {total} replacements across {affected} files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

