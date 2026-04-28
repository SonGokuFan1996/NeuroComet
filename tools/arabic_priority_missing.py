"""List critical missing keys in values-ar that are most user-visible
(navigation, titles, commonly-shown buttons & toasts)."""
import re
from pathlib import Path

RES = Path(__file__).resolve().parents[1] / "app/src/main/res"

def load(path):
    out = {}
    if not path.exists():
        return out
    content = path.read_text(encoding="utf-8", errors="replace")
    for m in re.finditer(r'<string\s+name="([^"]+)"[^>]*>(.*?)</string>', content, flags=re.DOTALL):
        out[m.group(1)] = m.group(2)
    return out

en = load(RES / "values" / "strings.xml")
ar = load(RES / "values-ar" / "strings.xml")

# Prefixes that typically represent user-visible UI strings we want translated
PRIORITY_PREFIXES = (
    "nav_", "menu_", "action_", "cd_", "toast_", "settings_", "notifications_",
    "notif_group_", "notif_channel_", "dm_", "messages_", "feed_", "explore_",
    "profile_", "create_", "post_", "shortcut_", "backup_", "parental_", "auth_",
    "tutorial_", "game_", "games_", "widget_", "image_editor_", "call_",
    "sub_", "about_", "font_settings_", "feedback_", "status_",
)

missing = sorted(k for k in en if k not in ar and k.startswith(PRIORITY_PREFIXES))
stale = sorted(k for k, v in ar.items() if k in en and v == en[k])

print(f"Priority missing in values-ar: {len(missing)}")
for k in missing[:80]:
    print(f"  + {k} = {en[k][:80]!r}")

print(f"\nEnglish values left stale in values-ar: {len(stale)}")
for k in stale[:80]:
    print(f"  * {k} = {ar[k][:80]!r}")

