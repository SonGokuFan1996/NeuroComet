"""Add missing string resources for all unresolved references listed in
unresolved_keys.txt. Uses curated English text where possible, falls back
to humanized key names.
"""
from __future__ import annotations
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
KEYS_FILE = ROOT / "unresolved_keys.txt"
STRINGS_XML = ROOT / "app" / "src" / "main" / "res" / "values" / "strings.xml"

# Curated defaults keyed by exact string name.
CURATED: dict[str, str] = {
    # Generic content descriptions
    "cd_user_avatar": "User avatar",
    "cd_verified": "Verified",
    "cd_more_options": "More options",
    "cd_post_image": "Post image",
    "cd_send_comment": "Send comment",
    "cd_comment_avatar": "Commenter avatar",
    "cd_start_call": "Start call",
    "toast_link_copied": "Link copied to clipboard",
    # Feedback
    "feedback_title": "Send Feedback",
    "feedback_bug_submitted": "Bug report submitted. Thank you!",
    "feedback_bug_title_placeholder": "Summarize the bug in a few words",
    "feedback_steps_placeholder": "Describe the steps to reproduce the issue",
    "feedback_feature_submitted": "Feature request submitted. Thank you!",
    "feedback_feature_title_placeholder": "Summarize your feature idea",
    "feedback_feature_desc_placeholder": "Describe the feature and why it would help",
    "feedback_general_placeholder": "Share your thoughts, compliments, or concerns",
    "feedback_thank_you": "Thank you for your feedback!",
    # Font settings
    "font_settings_title": "Font Settings",
    # Image editor
    "image_editor_title": "Edit Image",
    "image_editor_done": "Done",
    "image_editor_brightness": "Brightness",
    "image_editor_contrast": "Contrast",
    "image_editor_saturation": "Saturation",
    "image_editor_rotate_left": "Rotate Left",
    "image_editor_rotate_right": "Rotate Right",
    "image_editor_flip_h": "Flip Horizontal",
    "image_editor_flip_v": "Flip Vertical",
    "image_editor_tab_filters": "Filters",
    "image_editor_tab_adjust": "Adjust",
    "image_editor_tab_draw": "Draw",
    "image_editor_tab_text": "Text",
    "image_editor_tab_crop": "Crop",
    "image_editor_add_text_title": "Add Text",
    # Notification channel groups
    "notif_group_messages": "Messages",
    "notif_group_social": "Social",
    "notif_group_community": "Community",
    "notif_group_account": "Account",
    "notif_group_app": "App",
    "notif_group_wellness": "Wellness",
    # Notification channels
    "notif_channel_direct_messages": "Direct Messages",
    "notif_channel_direct_messages_desc": "Notifications for new direct messages",
    "notif_channel_group_messages": "Group Messages",
    "notif_channel_group_messages_desc": "Notifications for group chat messages",
    "notif_channel_message_requests": "Message Requests",
    "notif_channel_message_requests_desc": "Notifications for new message requests",
    "notif_channel_likes": "Likes",
    "notif_channel_likes_desc": "Notifications when someone likes your content",
    "notif_channel_comments": "Comments",
    "notif_channel_comments_desc": "Notifications for new comments on your posts",
    "notif_channel_mentions": "Mentions",
    "notif_channel_mentions_desc": "Notifications when you\u2019re mentioned",
    "notif_channel_follows": "Followers",
    "notif_channel_follows_desc": "Notifications when someone follows you",
    "notif_channel_friend_activity": "Friend Activity",
    "notif_channel_friend_activity_desc": "Updates about your friends\u2019 activity",
    "notif_channel_community_updates": "Community Updates",
    "notif_channel_community_updates_desc": "Announcements from communities you\u2019ve joined",
    "notif_channel_event_reminders": "Event Reminders",
    "notif_channel_event_reminders_desc": "Reminders for events you\u2019re attending",
    "notif_channel_live_events": "Live Events",
    "notif_channel_live_events_desc": "Notifications when live events start",
    "notif_channel_account_security": "Account Security",
    "notif_channel_account_security_desc": "Critical alerts about your account security",
    "notif_channel_parental_alerts": "Parental Alerts",
    "notif_channel_parental_alerts_desc": "Notifications for parental control events",
    "notif_channel_login_alerts": "Login Alerts",
    "notif_channel_login_alerts_desc": "Alerts when a new device signs in to your account",
    "notif_channel_app_updates": "App Updates",
    "notif_channel_app_updates_desc": "Notifications about new app versions",
    "notif_channel_feature_announcements": "Feature Announcements",
    "notif_channel_feature_announcements_desc": "Announcements about new features",
    "notif_channel_tips_and_tricks": "Tips & Tricks",
    "notif_channel_tips_and_tricks_desc": "Helpful tips to get more out of the app",
    "notif_channel_wellness_reminders": "Wellness Reminders",
    "notif_channel_wellness_reminders_desc": "Gentle reminders to check in with yourself",
    "notif_channel_break_reminders": "Break Reminders",
    "notif_channel_break_reminders_desc": "Reminders to take a break from your screen",
    "notif_channel_calm_mode": "Calm Mode",
    "notif_channel_calm_mode_desc": "Quiet notifications during calm hours",
    # Parental controls
    "parental_select_time": "Select Time",
    "parental_create_pin_title": "Create PIN",
    "parental_enter_pin_title": "Enter PIN",
    "parental_change_pin_title": "Change PIN",
    # Settings
    "settings_pin_required_title": "PIN Required",
    "settings_pin_required_desc": "Enter your parental PIN to continue.",
    "settings_pin_incorrect_attempts": "Incorrect PIN. %1$d attempts remaining.",
    "settings_pin_locked_minutes": "Too many attempts. Try again in %1$d minute(s).",
    "settings_pin_verify": "Verify",
    "settings_backup_testing_only": "Backup & Restore (testing only)",
    "settings_developer_mode_activated": "Developer mode activated",
    "settings_no_matching_results": "No settings match your search.",
    "settings_search_placeholder": "Search settings",
    # Calling
    "call_prepare_title": "Prepare for Call",
    "call_practice_title": "Practice Call",
    "call_practice_intro": "Practice having a conversation in a safe, supportive space.",
    "call_practice_choose_partner": "Choose a practice partner",
    "call_practice_helper": "Tap a persona below to start your practice call.",
    "call_status_speaking": "Speaking",
    "call_status_thinking": "Thinking",
    "call_status_connected": "Connected",
    "call_status_calling_persona": "Calling %1$s\u2026",
    "call_end": "End call",
    "call_retry": "Retry",
    "call_mute_audio": "Mute audio",
    "call_unmute_audio": "Unmute audio",
    "call_mute_microphone": "Mute microphone",
    "call_unmute_microphone": "Unmute microphone",
    "call_type_prompt": "Type a message\u2026",
    # Games
    "game_breathing_count": "Breaths: %1$d",
    "game_breathing_exhale": "Breathe out\u2026",
    "game_breathing_hold": "Hold\u2026",
    "game_breathing_inhale": "Breathe in\u2026",
    "game_breathing_pause": "Pause",
    "game_breathing_start": "Start",
    "game_cd_clear": "Clear",
    "game_cd_new_stars": "New stars",
    "game_cd_pause": "Pause",
    "game_cd_play": "Play",
    "game_cd_reset_tutorials": "Reset tutorials",
    "game_constellation_lines_count": "Lines: %1$d",
    "game_emotion_calm": "Calm",
    "game_emotion_curious": "Curious",
    "game_emotion_excited": "Excited",
    "game_emotion_flower_count": "Flowers: %1$d",
    "game_emotion_grateful": "Grateful",
    "game_emotion_happy": "Happy",
    "game_emotion_hopeful": "Hopeful",
    "game_emotion_loved": "Loved",
    "game_emotion_peaceful": "Peaceful",
    "game_emotion_proud": "Proud",
    "game_mood_blend": "Blend",
    "game_mood_calm": "Calm",
    "game_mood_dreamy": "Dreamy",
    "game_mood_first": "First color",
    "game_mood_happy": "Happy",
    "game_mood_passionate": "Passionate",
    "game_mood_peaceful": "Peaceful",
    "game_mood_refreshed": "Refreshed",
    "game_mood_second": "Second color",
    "game_mood_thoughtful": "Thoughtful",
    "game_mood_your_mood": "Your mood",
    "game_pattern_level_count": "Level %1$d",
    "game_pattern_start_button": "Start",
    "game_pattern_state_fail": "Try again!",
    "game_pattern_state_input": "Your turn",
    "game_pattern_state_showing": "Watch the pattern",
    "game_pattern_state_success": "Nice!",
    "game_pattern_state_waiting": "Ready?",
    "game_pattern_try_again_button": "Try Again",
    "game_safe_art": "Art",
    "game_safe_books": "Books",
    "game_safe_candle": "Candle",
    "game_safe_cozy_couch": "Cozy couch",
    "game_safe_crystal": "Crystal",
    "game_safe_flowers": "Flowers",
    "game_safe_hot_drink": "Hot drink",
    "game_safe_meditation": "Meditation",
    "game_safe_moon_light": "Moonlight",
    "game_safe_music": "Music",
    "game_safe_plant": "Plant",
    "game_safe_plushie": "Plushie",
    "game_sound_bell": "Bell",
    "game_sound_chime": "Chime",
    "game_sound_drum": "Drum",
    "game_sound_guitar": "Guitar",
    "game_sound_horn": "Horn",
    "game_sound_melody": "Melody",
    "game_sound_note": "Note",
    "game_sound_notes_count": "Notes: %1$d",
    "game_sound_piano": "Piano",
    "game_stim_bpm_label": "%1$d BPM",
    "game_texture_breeze": "Breeze",
    "game_texture_cold": "Cold",
    "game_texture_electric": "Electric",
    "game_texture_fluffy": "Fluffy",
    "game_texture_ice": "Ice",
    "game_texture_leaf": "Leaf",
    "game_texture_stone": "Stone",
    "game_texture_taps_count": "Taps: %1$d",
    "game_texture_warm": "Warm",
    "game_texture_wave": "Wave",
    "game_tutorial_back": "Back",
    "game_tutorial_got_it": "Got it",
    "game_tutorial_how_to_play": "How to play",
    "game_tutorial_next": "Next",
    "game_tutorial_skip": "Skip",
    "game_worry_empty_jar": "Your worry jar is empty. Add a worry to release it.",
    "game_worry_released_count": "Worries released: %1$d",
    "games_clear_all": "Clear All",
    "games_in_dev_subtitle": "More calming games are on the way.",
    "games_release_worry": "Release worry",
    "games_reset_tutorials_button": "Reset tutorials",
    "games_reset_tutorials_desc": "Show every game\u2019s tutorial again the next time you play.",
    "games_reset_tutorials_title": "Reset tutorials",
    "games_worry_placeholder": "Write a worry\u2026",
    # Widgets
    "widget_affirmation_default_text": "You are doing your best, and that is enough.",
    "widget_daily_affirmations": "Daily Affirmations",
    "widget_energy_percent_format": "%1$d%%",
    "widget_energy_state_good": "Good",
    "widget_energy_state_high": "High",
    "widget_energy_state_low_battery": "Low",
    "widget_energy_state_moderate": "Moderate",
    "widget_energy_state_recharging": "Recharging",
    "widget_focus_active": "Focusing\u2026",
    "widget_focus_pause": "Paused",
    "widget_focus_ready": "Ready to focus",
    "widget_focus_start": "Start focus",
    "widget_mood_last_checkin": "Last check-in: %1$s",
    "widget_stim_break_default_emoji": "🌿",
    "widget_stim_break_last_placeholder": "\u2014",
    "widget_stim_break_next_in_minutes": "Next in %1$d min",
    "widget_stim_break_since_minutes": "%1$d min since last",
}

# Tutorial title/desc auto-generation: human-friendly defaults per game & step.
TUTORIAL_STEPS = {
    "bubble_pop": [
        ("Pop the Bubbles", "Tap any bubble to pop it and feel a satisfying burst."),
        ("Watch Them Sparkle", "Each pop releases a little sparkle of color."),
        ("Again and Again", "Bubbles keep reappearing \u2014 pop at your own pace."),
    ],
    "fidget": [
        ("Spin the Fidget", "Swipe across the spinner to send it spinning."),
        ("Feel the Momentum", "Longer swipes make the spinner go faster."),
        ("Relax and Repeat", "Let it spin down, then give it another swipe."),
    ],
    "color_flow": [
        ("Choose a Color", "Pick any color that matches your mood."),
        ("Paint With Your Finger", "Drag across the canvas to flow color around."),
        ("Breathe and Enjoy", "Watch the colors ripple and unwind."),
    ],
    "pattern": [
        ("Watch the Pattern", "A sequence of tiles will light up. Remember the order."),
        ("Repeat It Back", "Tap the tiles in the same order to complete the level."),
        ("Level Up", "Each level adds one more step to the pattern."),
        ("Take Your Time", "There\u2019s no timer \u2014 go at a pace that feels calm."),
    ],
    "infinity": [
        ("Trace the Line", "Drag your finger along the infinity path."),
        ("Paint as You Go", "Your finger leaves a gentle trail of color."),
        ("Keep Flowing", "The path loops forever \u2014 draw for as long as you like."),
        ("Clear and Restart", "Tap clear any time to start with a fresh canvas."),
    ],
    "rain": [
        ("Sensory Rain", "Relax as gentle raindrops fall across the screen."),
        ("Tune the Rain", "Use the slider to make it a drizzle or a downpour."),
        ("Tap a Drop", "Tap a raindrop to hear a soft plink."),
        ("Put on Headphones", "Sounds are calmer with headphones."),
    ],
    "breathing": [
        ("Breathing Bubbles", "Follow the bubble to guide your breath."),
        ("Breathe In", "Inhale slowly as the bubble grows."),
        ("Hold", "Hold your breath while the bubble pauses."),
        ("Breathe Out", "Exhale gently as the bubble shrinks."),
        ("Repeat", "Keep going for as long as feels good."),
    ],
    "texture": [
        ("Tap a Tile", "Tap any tile to feel its texture."),
        ("Feel the Difference", "Each tile has its own vibration and sound."),
        ("Find Your Favorite", "Notice which textures feel most soothing."),
        ("Come Back Anytime", "Textures are always here when you need them."),
    ],
    "sound": [
        ("Pick a Sound", "Choose an instrument to plant in your garden."),
        ("Plant the Note", "Tap to place a sound in the soil."),
        ("Listen to It Grow", "Sounds play back together in a loop."),
        ("Build a Melody", "Add more sounds to layer your calming melody."),
    ],
    "stim": [
        ("Create a Beat", "Tap the pads to build your own calming rhythm."),
        ("Play Your Loop", "Press play to hear your sequence repeat."),
        ("Adjust the Tempo", "Slide to slow it down or speed it up."),
        ("Save Your Vibe", "Your sequence keeps going until you stop it."),
    ],
    "emotion": [
        ("Pick an Emotion", "Choose the feeling that\u2019s closest to right now."),
        ("Plant a Flower", "Every emotion grows a flower in your garden."),
        ("Watch It Bloom", "Your garden grows richer with each check-in."),
        ("Return Often", "Come back whenever you want to add a new bloom."),
    ],
    "safe": [
        ("Build Your Safe Space", "Add the objects that help you feel at ease."),
        ("Arrange It Your Way", "Drag items to place them however you like."),
        ("Stay As Long As You Want", "Your safe space is always here for you."),
        ("Customize It Later", "Swap in new objects whenever you need a change."),
    ],
    "constellation": [
        ("Connect the Stars", "Drag between stars to draw your own lines."),
        ("Make a Shape", "Each line becomes part of your constellation."),
        ("Clear the Sky", "Tap clear to start with a fresh night sky."),
        ("Come Back Anytime", "Your constellation saves between sessions."),
    ],
    "mood": [
        ("Blend Your Mood", "Choose two colors that match how you feel."),
        ("Mix Them Together", "Watch the colors swirl into a new shade."),
        ("That\u2019s Your Mood", "The blend represents your mood right now."),
        ("Check In Anytime", "Come back to blend a new mood whenever you like."),
    ],
    "worry": [
        ("Write a Worry", "Type whatever\u2019s on your mind, big or small."),
        ("Put It in the Jar", "Tap to place the worry inside the jar."),
        ("Release It", "Shake or tap to gently release the worry."),
        ("Let It Go", "You can always add another when you\u2019re ready."),
    ],
    "zen": [
        ("Draw in the Sand", "Drag your finger to trace patterns in the sand."),
        ("Feel the Flow", "Lines ripple outward as you draw."),
        ("Smooth It Over", "Shake or tap clear to smooth the sand."),
        ("Take a Breath", "Draw and erase for as long as feels calming."),
    ],
}


def humanize(key: str) -> str:
    """Last-resort pretty-print of a snake_case key."""
    parts = key.split("_")
    return " ".join(p.capitalize() for p in parts if p)


def resolve(key: str) -> str:
    if key in CURATED:
        return CURATED[key]
    # tutorial_<game>_stepN_(title|desc)
    m = re.match(r"tutorial_([a-z_]+?)_step(\d+)_(title|desc)$", key)
    if m:
        game_key, step_num, kind = m.group(1), int(m.group(2)), m.group(3)
        # Some keys collapse multi-word games (bubble_pop, color_flow, safe_space -> safe,
        # sensory_rain -> rain, emotion_garden -> emotion, stim_sequencer -> stim,
        # sound_garden -> sound, pattern_tap -> pattern, infinity_draw -> infinity,
        # fidget_spinner -> fidget, breathing_bubbles -> breathing,
        # texture_tiles -> texture, zen_garden -> zen, worry_jar -> worry,
        # mood_mixer -> mood, constellation_builder -> constellation)
        lookup_keys = [
            game_key,
            game_key.split("_")[0],
        ]
        steps = None
        for lk in lookup_keys:
            if lk in TUTORIAL_STEPS:
                steps = TUTORIAL_STEPS[lk]
                break
        if steps and 1 <= step_num <= len(steps):
            title, desc = steps[step_num - 1]
            return title if kind == "title" else desc
        # Fallback
        return f"Step {step_num}" if kind == "title" else f"Tutorial step {step_num}."
    # Generic fallback
    return humanize(key)


def escape_xml(text: str) -> str:
    # Preserve %1$s, %1$d, %%. Escape apostrophes & ampersands/quotes minimally.
    text = text.replace("&", "&amp;")
    text = text.replace("<", "&lt;").replace(">", "&gt;")
    # Android strings need escaped apostrophes and double quotes.
    text = text.replace("'", "\\'")
    text = text.replace('"', '\\"')
    return text


def main() -> int:
    keys = [ln.strip() for ln in KEYS_FILE.read_text(encoding="utf-8").splitlines() if ln.strip()]
    xml = STRINGS_XML.read_text(encoding="utf-8")
    # Skip keys that already exist (belt-and-suspenders)
    existing = set(re.findall(r'<string\s+name="([^"]+)"', xml))
    existing_plurals = set(re.findall(r'<plurals\s+name="([^"]+)"', xml))
    to_add = [k for k in keys if k not in existing]
    if not to_add and "backup_count_stored" in existing_plurals and "wellbeing_detox_days_chip" in existing_plurals:
        print("Nothing to add.")
        return 0

    lines = ["", "    <!-- Auto-generated: missing string fallbacks -->"]
    for k in to_add:
        val = escape_xml(resolve(k))
        lines.append(f'    <string name="{k}">{val}</string>')
    # Plurals
    if "backup_count_stored" not in existing_plurals:
        lines.append('    <plurals name="backup_count_stored">')
        lines.append('        <item quantity="one">%d backup stored</item>')
        lines.append('        <item quantity="other">%d backups stored</item>')
        lines.append('    </plurals>')
    if "wellbeing_detox_days_chip" not in existing_plurals:
        lines.append('    <plurals name="wellbeing_detox_days_chip">')
        lines.append('        <item quantity="one">%d day</item>')
        lines.append('        <item quantity="other">%d days</item>')
        lines.append('    </plurals>')
    block = "\n".join(lines) + "\n"

    new_xml = xml.replace("</resources>", block + "</resources>", 1)
    STRINGS_XML.write_text(new_xml, encoding="utf-8")
    print(f"Added {len(to_add)} strings.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

