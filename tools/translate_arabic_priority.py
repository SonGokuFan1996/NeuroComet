"""Fill values-ar/strings.xml with Arabic translations for the most user-visible
strings that currently fall back to English.

This script adds only strings that are missing from values-ar AND present in
the English base (values/strings.xml). It does NOT overwrite existing Arabic
translations.
"""
import re
from pathlib import Path

RES = Path(__file__).resolve().parents[1] / "app/src/main/res"
EN = RES / "values" / "strings.xml"
AR = RES / "values-ar" / "strings.xml"

# Curated Arabic translations for common UI strings.
# These are the strings most likely to appear on primary surfaces.
AR_TRANSLATIONS: dict[str, str] = {
    # Content descriptions
    "cd_back": "رجوع",
    "cd_close": "إغلاق",
    "cd_share": "مشاركة",
    "cd_options": "خيارات",
    "cd_avatar": "الصورة الرمزية",
    "cd_remove": "إزالة",
    "cd_clear_search": "مسح البحث",
    "cd_scroll_to_latest": "التمرير إلى الأحدث",
    "cd_keyboard": "لوحة المفاتيح",
    "cd_emoji_picker": "منتقي الرموز التعبيرية",
    "cd_selected_image": "الصورة المختارة",
    "cd_edit_image": "تحرير الصورة",
    "cd_user_avatar": "صورة المستخدم",
    "cd_verified": "موثّق",
    "cd_more_options": "المزيد من الخيارات",
    "cd_post_image": "صورة المنشور",
    "cd_send_comment": "إرسال تعليق",
    "cd_comment_avatar": "صورة المعلّق",
    "cd_start_call": "بدء المكالمة",
    # Backup
    "backup_title": "النسخ الاحتياطي والتخزين",
    "backup_section_back_up": "النسخ الاحتياطي",
    "backup_section_import": "الاستيراد",
    "backup_section_auto": "النسخ الاحتياطي التلقائي",
    "backup_section_what_to_back_up": "ما الذي يُنسخ احتياطيًا",
    "backup_section_security": "الأمان",
    "backup_section_restore": "الاستعادة",
    "backup_section_storage_mgmt": "إدارة التخزين",
    "backup_last_backup": "آخر نسخة احتياطية",
    "backup_no_backups_yet": "لا توجد نسخ احتياطية بعد",
    "backup_no_backups": "لم يتم العثور على نسخ احتياطية",
    "backup_no_backups_desc": "اعمل نسخة احتياطية لبياناتك لحفظها. يمكنك استعادتها على هذا الجهاز أو جهاز آخر.",
    "backup_create_to_see": "أنشئ نسخة احتياطية لترى النسخ هنا",
    "backup_size_label": "الحجم: %1$s",
    "backup_location_exported": "مُصدَّرة",
    "backup_location_imported": "مستوردة",
    "backup_location_local": "محلية",
    "backup_encrypted_label": "مشفّرة",
    "backup_export_to_file": "تصدير إلى ملف",
    "backup_export_gdrive": "التصدير إلى Google Drive أو ملف",
    "backup_export_gdrive_desc": "الحفظ على Google Drive أو مجلد التنزيلات أو أي خدمة تخزين سحابي",
    "backup_import_from_file": "استيراد من ملف",
    "backup_import_from_file_desc": "استيراد ملف نسخة احتياطية ‎.ncb من Google Drive أو التنزيلات أو موقع آخر",
    "backup_import_restore": "استيراد واستعادة",
    "backup_import_restore_desc": "استورد ملف نسخة احتياطية واستعد جميع البيانات فورًا",
    "backup_importing": "جارٍ الاستيراد",
    "backup_to_device": "نسخ احتياطي على الجهاز",
    "backup_save_locally": "الحفظ محليًا على هذا الهاتف",
    "backup_save_to_device": "احفظ نسخة احتياطية على هذا الجهاز",
    "backup_file_picker_hint": "يتيح لك منتقي الملفات اختيار Google Drive أو التنزيلات أو أي مزوّد سحابي مثبّت",
    "backup_freq_title": "تكرار النسخ الاحتياطي",
    "backup_freq_off": "معطَّل",
    "backup_freq_off_desc": "النسخ الاحتياطي اليدوي فقط",
    "backup_freq_daily": "يوميًا",
    "backup_freq_daily_desc": "كل 24 ساعة",
    "backup_freq_weekly": "أسبوعيًا",
    "backup_freq_weekly_desc": "مرة في الأسبوع",
    "backup_freq_monthly": "شهريًا",
    "backup_freq_monthly_desc": "مرة في الشهر",
    "backup_wifi_only": "Wi‑Fi فقط",
    "backup_wifi_only_desc": "النسخ الاحتياطي فقط عند الاتصال بشبكة Wi‑Fi",
    "backup_scope_profile": "الملف الشخصي",
    "backup_scope_profile_desc": "الاسم المعروض والسيرة والصورة الرمزية",
    "backup_scope_messages": "الرسائل",
    "backup_scope_messages_desc": "المحادثات والرسائل المباشرة",
    "backup_scope_posts": "المنشورات والتعليقات",
    "backup_scope_posts_desc": "منشوراتك وتعليقاتك وإعجاباتك",
    "backup_scope_bookmarks": "الإشارات المرجعية",
    "backup_scope_bookmarks_desc": "المنشورات المحفوظة",
    "backup_scope_follows": "المتابعات",
    "backup_scope_follows_desc": "قائمة المتابَعين والمتابِعين",
    "backup_scope_settings": "إعدادات التطبيق",
    "backup_scope_settings_desc": "المظهر وإمكانية الوصول والتفضيلات",
    "backup_scope_notifications": "الإشعارات",
    "backup_scope_notifications_desc": "سجل الإشعارات",
    "backup_e2e_title": "التشفير من طرف إلى طرف",
    "backup_e2e_on_desc": "النسخ الاحتياطية مشفّرة بعبارة المرور الخاصة بك",
    "backup_e2e_off_desc": "احمِ النسخ الاحتياطية بعبارة مرور",
    "backup_e2e_warning": "إذا نسيت عبارة المرور فلن تتمكن من استعادة النسخ الاحتياطية المشفّرة.",
    "backup_encrypt_title": "تشفير النسخ الاحتياطية",
    "backup_encrypt_desc": "تحمي النسخ الاحتياطية المشفّرة بياناتك بعبارة مرور. إذا نسيتها فلن تتمكن من استعادة النسخة.",
    "backup_encrypt_warning": "لا يمكن استرداد عبارة المرور في حال فقدها!",
    "backup_encrypt_enable": "تفعيل",
    "backup_restore_title": "استعادة النسخة الاحتياطية",
    "backup_restore_desc": "سيؤدي هذا إلى استعادة بياناتك من النسخة الاحتياطية المحددة. ستُكتَب البيانات الحالية المتعارضة فوق ذلك.\n\nهل تريد المتابعة؟",
    "backup_restore_button": "استعادة",
    "backup_delete_title": "حذف النسخة الاحتياطية",
    "backup_delete_desc": "سيتم حذف هذه النسخة الاحتياطية نهائيًا. لا يمكن التراجع عن هذا الإجراء.",
    "backup_delete_all_title": "حذف جميع النسخ الاحتياطية",
    "backup_delete_all_desc": "سيتم حذف جميع النسخ الاحتياطية المحلية نهائيًا. لا يمكن التراجع عن هذا الإجراء.",
    "backup_delete_all_button": "حذف الكل",
    "backup_delete_all_local": "حذف جميع النسخ المحلية",
    # Calls
    "call_accept": "قبول",
    "call_decline": "رفض",
    "call_end": "إنهاء المكالمة",
    "call_retry": "إعادة المحاولة",
    "call_mute_audio": "كتم الصوت",
    "call_unmute_audio": "إلغاء كتم الصوت",
    "call_mute_microphone": "كتم الميكروفون",
    "call_unmute_microphone": "إلغاء كتم الميكروفون",
    "call_type_prompt": "اكتب رسالة…",
    "call_status_speaking": "يتحدث",
    "call_status_thinking": "يفكر",
    "call_status_connected": "متصل",
    "call_status_calling_persona": "جارٍ الاتصال بـ %1$s…",
    "call_practice_title": "مكالمة تدريبية",
    "call_practice_intro": "تدرّب على إجراء محادثة في مساحة آمنة وداعمة.",
    "call_practice_choose_partner": "اختر شريكًا للتدريب",
    "call_practice_helper": "اضغط على شخصية أدناه لبدء مكالمتك التدريبية.",
    "call_prepare_title": "الاستعداد للمكالمة",
    # Parental
    "parental_create_pin_title": "إنشاء رمز PIN",
    "parental_enter_pin_title": "أدخل رمز PIN",
    "parental_change_pin_title": "تغيير رمز PIN",
    "parental_select_time": "اختر الوقت",
    # Settings
    "settings_pin_required_title": "رمز PIN مطلوب",
    "settings_pin_required_desc": "أدخل رمز PIN الخاص بالإشراف العائلي للمتابعة.",
    "settings_pin_incorrect_attempts": "رمز PIN غير صحيح. المحاولات المتبقية: %1$d.",
    "settings_pin_locked_minutes": "عدد كبير من المحاولات. حاول مرة أخرى بعد %1$d دقيقة.",
    "settings_pin_verify": "تحقق",
    "settings_backup_testing_only": "النسخ الاحتياطي والاستعادة (للاختبار فقط)",
    "settings_developer_mode_activated": "تم تفعيل وضع المطور",
    "settings_no_matching_results": "لا توجد إعدادات تطابق بحثك.",
    "settings_search_placeholder": "ابحث في الإعدادات",
    "settings_theme_premium_locked": "ميزة متميّزة. اضغط لفتح المظاهر.",
    "accessibility_locked": "مقفل",
    "theme_settings_premium_theme": "مظهر متميّز",
    # Font settings
    "font_settings_title": "إعدادات الخط",
    # Image editor
    "image_editor_title": "تحرير الصورة",
    "image_editor_done": "تم",
    "image_editor_brightness": "السطوع",
    "image_editor_contrast": "التباين",
    "image_editor_saturation": "التشبّع",
    "image_editor_rotate_left": "تدوير لليسار",
    "image_editor_rotate_right": "تدوير لليمين",
    "image_editor_flip_h": "قلب أفقي",
    "image_editor_flip_v": "قلب عمودي",
    "image_editor_tab_filters": "الفلاتر",
    "image_editor_tab_adjust": "ضبط",
    "image_editor_tab_draw": "رسم",
    "image_editor_tab_text": "نص",
    "image_editor_tab_crop": "اقتصاص",
    "image_editor_add_text_title": "إضافة نص",
    # Notification groups
    "notif_group_messages": "الرسائل",
    "notif_group_social": "اجتماعي",
    "notif_group_community": "المجتمع",
    "notif_group_account": "الحساب",
    "notif_group_app": "التطبيق",
    "notif_group_wellness": "العافية",
    # Notification channels
    "notif_channel_direct_messages": "الرسائل المباشرة",
    "notif_channel_direct_messages_desc": "إشعارات عند وصول رسائل مباشرة جديدة",
    "notif_channel_group_messages": "رسائل المجموعات",
    "notif_channel_group_messages_desc": "إشعارات لرسائل دردشة المجموعات",
    "notif_channel_message_requests": "طلبات الرسائل",
    "notif_channel_message_requests_desc": "إشعارات لطلبات الرسائل الجديدة",
    "notif_channel_likes": "الإعجابات",
    "notif_channel_likes_desc": "إشعارات عندما يُعجب أحد بمحتواك",
    "notif_channel_comments": "التعليقات",
    "notif_channel_comments_desc": "إشعارات عند وجود تعليقات جديدة على منشوراتك",
    "notif_channel_mentions": "الإشارات",
    "notif_channel_mentions_desc": "إشعارات عند الإشارة إليك",
    "notif_channel_follows": "المتابعون",
    "notif_channel_follows_desc": "إشعارات عند متابعة شخص لك",
    "notif_channel_friend_activity": "نشاط الأصدقاء",
    "notif_channel_friend_activity_desc": "تحديثات حول نشاط أصدقائك",
    "notif_channel_community_updates": "تحديثات المجتمع",
    "notif_channel_community_updates_desc": "إعلانات من المجتمعات التي انضممت إليها",
    "notif_channel_event_reminders": "تذكيرات الأحداث",
    "notif_channel_event_reminders_desc": "تذكيرات بالأحداث التي ستحضرها",
    "notif_channel_live_events": "البث المباشر",
    "notif_channel_live_events_desc": "إشعارات عند بدء الأحداث المباشرة",
    "notif_channel_account_security": "أمان الحساب",
    "notif_channel_account_security_desc": "تنبيهات مهمة حول أمان حسابك",
    "notif_channel_parental_alerts": "تنبيهات الإشراف العائلي",
    "notif_channel_parental_alerts_desc": "إشعارات لأحداث الإشراف العائلي",
    "notif_channel_login_alerts": "تنبيهات تسجيل الدخول",
    "notif_channel_login_alerts_desc": "تنبيهات عند تسجيل الدخول من جهاز جديد",
    "notif_channel_app_updates": "تحديثات التطبيق",
    "notif_channel_app_updates_desc": "إشعارات حول إصدارات التطبيق الجديدة",
    "notif_channel_feature_announcements": "إعلانات الميزات",
    "notif_channel_feature_announcements_desc": "إعلانات حول الميزات الجديدة",
    "notif_channel_tips_and_tricks": "نصائح وحيل",
    "notif_channel_tips_and_tricks_desc": "نصائح مفيدة للاستفادة أكثر من التطبيق",
    "notif_channel_wellness_reminders": "تذكيرات العافية",
    "notif_channel_wellness_reminders_desc": "تذكيرات لطيفة للاطمئنان على نفسك",
    "notif_channel_break_reminders": "تذكيرات الاستراحة",
    "notif_channel_break_reminders_desc": "تذكيرات لأخذ استراحة من الشاشة",
    "notif_channel_calm_mode": "وضع الهدوء",
    "notif_channel_calm_mode_desc": "إشعارات هادئة خلال ساعات الهدوء",
    # Shortcuts
    "shortcut_new_post_short": "منشور جديد",
    "shortcut_new_post_long": "إنشاء منشور جديد",
    "shortcut_dms_short": "الرسائل",
    "shortcut_dms_long": "فتح الرسائل المباشرة",
    "shortcut_regulation_short": "تنظيم",
    "shortcut_regulation_long": "ابدأ جلسة تنظيم",
    "shortcut_disabled_sign_in": "سجّل الدخول لاستخدام هذا الاختصار",
    # Common toast
    "toast_link_copied": "تم نسخ الرابط",
    # DM
    "dm_direct_message": "رسالة مباشرة",
    "dm_wallpaper": "الخلفية",
    "dm_messaging_disabled_title": "المراسلة معطّلة",
    "dm_messaging_disabled_body": "لقد حظرت هذا المستخدم. ألغِ الحظر لإرسال الرسائل أو استلامها.",
    "dm_search_messages_placeholder": "ابحث في الرسائل",
    "dm_done": "تم",
    "dm_no_messages_matching": "لا توجد رسائل تطابق \"%1$s\"",
    "dm_say_hi": "ألقِ التحية 👋",
    "dm_emoji": "الرموز التعبيرية",
    # Create story
    "create_story_photo": "صورة",
    "create_story_video": "فيديو",
    "create_story_file": "ملف",
    # Widgets
    "widget_affirmation_default_text": "أنت تبذل قصارى جهدك، وهذا يكفي.",
    "widget_daily_affirmations": "تأكيدات يومية",
    "widget_energy_percent_format": "%1$d%%",
    "widget_energy_state_good": "جيد",
    "widget_energy_state_high": "مرتفع",
    "widget_energy_state_low_battery": "منخفض",
    "widget_energy_state_moderate": "متوسط",
    "widget_energy_state_recharging": "جارٍ الشحن",
    "widget_focus_active": "في وضع التركيز…",
    "widget_focus_pause": "متوقف",
    "widget_focus_ready": "جاهز للتركيز",
    "widget_focus_start": "ابدأ التركيز",
    "widget_mood_last_checkin": "آخر تسجيل دخول: %1$s",
    "widget_stim_break_default_emoji": "🌿",
    "widget_stim_break_last_placeholder": "—",
    "widget_stim_break_next_in_minutes": "التالي خلال %1$d د",
    "widget_stim_break_since_minutes": "%1$d د منذ الأخيرة",
    # Feedback
    "feedback_title": "إرسال ملاحظات",
    "feedback_bug_submitted": "تم إرسال البلاغ. شكرًا لك!",
    "feedback_bug_title_placeholder": "لخّص المشكلة في كلمات قليلة",
    "feedback_steps_placeholder": "صِف خطوات إعادة إظهار المشكلة",
    "feedback_feature_submitted": "تم إرسال طلب الميزة. شكرًا لك!",
    "feedback_feature_title_placeholder": "لخّص فكرة ميزتك",
    "feedback_feature_desc_placeholder": "صِف الميزة ولماذا ستكون مفيدة",
    "feedback_general_placeholder": "شاركنا آراءك أو إطراءاتك أو مخاوفك",
    "feedback_thank_you": "شكرًا لملاحظاتك!",
    # Games
    "games_clear_all": "مسح الكل",
    "games_in_dev_subtitle": "المزيد من الألعاب المهدّئة في الطريق.",
    "games_release_worry": "تحرير القلق",
    "games_reset_tutorials_button": "إعادة ضبط الدروس",
    "games_reset_tutorials_desc": "إظهار دروس كل لعبة مجددًا في المرة القادمة التي تلعبها.",
    "games_reset_tutorials_title": "إعادة ضبط الدروس",
    "games_worry_placeholder": "اكتب قلقًا…",
    "game_tutorial_back": "رجوع",
    "game_tutorial_got_it": "فهمت",
    "game_tutorial_how_to_play": "كيفية اللعب",
    "game_tutorial_next": "التالي",
    "game_tutorial_skip": "تخطّي",
}


def load(path: Path) -> dict[str, str]:
    out = {}
    if not path.exists():
        return out
    content = path.read_text(encoding="utf-8", errors="replace")
    for m in re.finditer(r'<string\s+name="([^"]+)"[^>]*>(.*?)</string>', content, flags=re.DOTALL):
        out[m.group(1)] = m.group(2)
    return out


def main() -> int:
    en = load(EN)
    ar = load(AR)
    ar_content = AR.read_text(encoding="utf-8")

    # 1) Strings present in English but missing from Arabic
    to_add = []
    for k, arabic in AR_TRANSLATIONS.items():
        if k not in en:
            continue
        if k not in ar:
            to_add.append((k, arabic))

    # 2) Strings present in Arabic that equal English (stale/untranslated) — replace them
    to_replace = []
    for k, arabic in AR_TRANSLATIONS.items():
        if k in ar and k in en and ar[k] == en[k]:
            to_replace.append((k, arabic))

    # Apply replacements in-place
    for k, arabic in to_replace:
        pattern = re.compile(
            r'(<string\s+name="' + re.escape(k) + r'"[^>]*>)(.*?)(</string>)',
            flags=re.DOTALL,
        )
        ar_content = pattern.sub(lambda m: m.group(1) + arabic + m.group(3), ar_content, count=1)

    # Append new ones before </resources>
    if to_add:
        block_lines = ["", "    <!-- Arabic translations (priority UI) -->"]
        for k, arabic in to_add:
            block_lines.append(f'    <string name="{k}">{arabic}</string>')
        block = "\n".join(block_lines) + "\n"
        ar_content = ar_content.replace("</resources>", block + "</resources>", 1)

    AR.write_text(ar_content, encoding="utf-8", newline="\n")
    print(f"Added {len(to_add)} Arabic strings, replaced {len(to_replace)} stale entries.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

