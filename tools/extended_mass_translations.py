#!/usr/bin/env python3
"""
Extended mass translation generator for NeuroComet.
Covers languages not included in the original mass_translations.py.
"""

import os
import re

RES_DIR = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')

def get_strings(filepath):
    """Parse strings.xml and return dict of name -> text."""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    return dict(re.findall(r'<string name="([^"]+)">([^<]*)</string>', content))

def update_strings_file(lang_dir, translations):
    """Update strings in a language file with new translations."""
    lang_file = os.path.join(RES_DIR, lang_dir, 'strings.xml')

    if not os.path.exists(lang_file):
        print(f"Skipping {lang_dir} (file not found)")
        return 0

    base_file = os.path.join(RES_DIR, 'values', 'strings.xml')
    base_strings = get_strings(base_file)

    with open(lang_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    updated = 0
    new_lines = []

    for line in lines:
        replaced = False
        for name, new_text in translations.items():
            pattern = rf'^(\s*<string name="{re.escape(name)}">)(.*)(<\/string>\s*)$'
            match = re.match(pattern, line)
            if match:
                current = match.group(2)
                # Update if likely English (matches base or is known filler)
                if name in base_strings:
                    # If current translation looks like English (basic heuristic: matches base string exact)
                    # OR if we want to force update (let's assume we do for Mass Translation to be safe properly)
                    # But checking if it equals base_strings[name] is safer to avoid overwriting manual work.
                    if current == base_strings[name] or current == "":
                        escaped = new_text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
                        if "\\'" not in escaped:
                            escaped = escaped.replace("'", "\\'")
                        new_lines.append(f'{match.group(1)}{escaped}{match.group(3)}')
                        updated += 1
                        replaced = True
                        break
        if not replaced:
            new_lines.append(line)

    with open(lang_file, 'w', encoding='utf-8', newline='') as f:
        f.writelines(new_lines)

    return updated

# ============================================================================
# TRANSLATIONS
# ============================================================================

# Spanish (es)
TRANSLATIONS_ES = {
    "auth_app_tagline": "Un espacio seguro para cada mente ✨",
    "auth_sign_in": "Iniciar sesión",
    "auth_sign_up": "Registrarse",
    "auth_email_label": "Correo electrónico",
    "auth_password_label": "Contraseña",
    "auth_create_account": "Crear cuenta",
    "nav_feed": "Inicio",
    "nav_explore": "Explorar",
    "nav_messages": "Mensajes",
    "nav_notifications": "Notificaciones",
    "nav_settings": "Ajustes",
    "create_post_button": "PUBLICAR",
    "comments_title": "Comentarios",
    "action_save": "Guardar",
    "action_cancel": "Cancelar",
    "action_delete": "Eliminar",
    "action_edit": "Editar",
    "status_online": "En línea",
    "status_offline": "Desconectado",
    "settings_title": "Ajustes",
    "settings_logout": "Cerrar sesión",
    "dm_send": "Enviar",
    "create_post_hint": "¿Qué estás pensando?",
    "about_title": "Acerca de NeuroComet",
    "about_description": "NeuroComet es una plataforma social diseñada para mentes neurodivergentes.",
    "about_credits": "Hecho con 💜 por el equipo NeuroComet",
    "beta_welcome_title": "¡Bienvenido a la Beta! 🚀",
    "beta_feedback_button": "Enviar comentarios",
    "create_story_title": "Crear historia",
    "create_story_share_button": "Compartir historia",
    "community_join": "Unirse a la comunidad",
    "community_leave": "Salir de la comunidad",
    "avatar_style_pixel": "Pixel Art",
    "auth_2fa_title": "Autenticación en dos pasos",
    "auth_2fa_description": "Ingresa el código de 6 dígitos enviado a tu dispositivo",
    "auth_2fa_code_label": "Código de 6 dígitos",
    "auth_2fa_verify": "Verificar",
    "auth_2fa_code_placeholder": "123456",
    "explore_adhd": "TDAH",
    "explore_autism": "Autismo",
    "explore_anxiety": "Ansiedad",
    "explore_lgbtq": "LGBTQ+",
    "explore_bipolar": "Bipolar",
    "explore_depression": "Depresión",
    "settings_dark_mode_title": "Modo oscuro",
    "settings_language_title": "Idioma",
}

# French (fr)
TRANSLATIONS_FR = {
    "auth_app_tagline": "Un espace sûr pour chaque esprit ✨",
    "auth_sign_in": "Se connecter",
    "auth_sign_up": "S'inscrire",
    "auth_email_label": "E-mail",
    "auth_password_label": "Mot de passe",
    "auth_create_account": "Créer un compte",
    "nav_feed": "Accueil",
    "nav_explore": "Explorer",
    "nav_messages": "Messages",
    "nav_notifications": "Notifications",
    "nav_settings": "Paramètres",
    "create_post_button": "PUBLIER",
    "comments_title": "Commentaires",
    "action_save": "Enregistrer",
    "action_cancel": "Annuler",
    "action_delete": "Supprimer",
    "action_edit": "Modifier",
    "status_online": "En ligne",
    "status_offline": "Hors ligne",
    "settings_title": "Paramètres",
    "settings_logout": "Déconnexion",
    "dm_send": "Envoyer",
    "create_post_hint": "À quoi pensez-vous ?",
    "about_title": "À propos de NeuroComet",
    "about_description": "NeuroComet est une plateforme sociale conçue pour les esprits neurodivergents.",
    "about_credits": "Fait avec 💜 par l'équipe NeuroComet",
    "beta_welcome_title": "Bienvenue dans la Bêta ! 🚀",
    "beta_feedback_button": "Envoyer des commentaires",
    "create_story_title": "Créer une histoire",
    "create_story_share_button": "Partager l'histoire",
    "community_join": "Rejoindre la communauté",
    "community_leave": "Quitter la communauté",
    "avatar_style_pixel": "Pixel Art",
}

# German (de)
TRANSLATIONS_DE = {
    "auth_app_tagline": "Ein sicherer Raum für jeden Geist ✨",
    "auth_sign_in": "Anmelden",
    "auth_sign_up": "Registrieren",
    "auth_email_label": "E-Mail",
    "auth_password_label": "Passwort",
    "auth_create_account": "Konto erstellen",
    "nav_feed": "Feed",
    "nav_explore": "Entdecken",
    "nav_messages": "Nachrichten",
    "nav_notifications": "Benachrichtigungen",
    "nav_settings": "Einstellungen",
    "create_post_button": "POSTEN",
    "comments_title": "Kommentare",
    "action_save": "Speichern",
    "action_cancel": "Abbrechen",
    "action_delete": "Löschen",
    "action_edit": "Bearbeiten",
    "status_online": "Online",
    "status_offline": "Offline",
    "settings_title": "Einstellungen",
    "settings_logout": "Abmelden",
    "dm_send": "Senden",
    "create_post_hint": "Was beschäftigt dich?",
    "about_title": "Über NeuroComet",
    "about_description": "NeuroComet ist eine soziale Plattform für neurodivergente Köpfe.",
    "about_credits": "Mit 💜 vom NeuroComet-Team gemacht",
    "beta_welcome_title": "Willkommen in der Beta! 🚀",
    "beta_feedback_button": "Feedback senden",
    "create_story_title": "Story erstellen",
    "create_story_share_button": "Story teilen",
    "community_join": "Community beitreten",
    "community_leave": "Community verlassen",
    "avatar_style_pixel": "Pixel Art",
}

# Indonesian (in)
TRANSLATIONS_IN = {
    "auth_app_tagline": "Ruang aman untuk setiap pikiran ✨",
    "auth_sign_in": "Masuk",
    "auth_sign_up": "Daftar",
    "auth_email_label": "Email",
    "auth_password_label": "Kata Sandi",
    "auth_create_account": "Buat Akun",
    "nav_feed": "Beranda",
    "nav_explore": "Jelajahi",
    "nav_messages": "Pesan",
    "nav_notifications": "Notifikasi",
    "nav_settings": "Pengaturan",
    "create_post_button": "POSTING",
    "comments_title": "Komentar",
    "action_save": "Simpan",
    "action_cancel": "Batal",
    "action_delete": "Hapus",
    "action_edit": "Edit",
    "status_online": "Online",
    "status_offline": "Offline",
    "settings_title": "Pengaturan",
    "settings_logout": "Keluar",
    "dm_send": "Kirim",
    "create_post_hint": "Apa yang Anda pikirkan?",
}

# Vietnamese (vi)
TRANSLATIONS_VI = {
    "auth_app_tagline": "Không gian an toàn cho mọi tâm hồn ✨",
    "auth_sign_in": "Đăng nhập",
    "auth_sign_up": "Đăng ký",
    "auth_email_label": "Email",
    "auth_password_label": "Mật khẩu",
    "auth_create_account": "Tạo tài khoản",
    "nav_feed": "Bảng tin",
    "nav_explore": "Khám phá",
    "nav_messages": "Tin nhắn",
    "nav_notifications": "Thông báo",
    "nav_settings": "Cài đặt",
    "create_post_button": "ĐĂNG",
    "comments_title": "Bình luận",
    "action_save": "Lưu",
    "action_cancel": "Hủy",
    "action_delete": "Xóa",
    "action_edit": "Chỉnh sửa",
    "status_online": "Trực tuyến",
    "status_offline": "Ngoại tuyến",
    "settings_title": "Cài đặt",
    "settings_logout": "Đăng xuất",
    "dm_send": "Gửi",
    "create_post_hint": "Bạn đang nghĩ gì?",
    "about_title": "Giới thiệu về NeuroComet",
    "about_description": "NeuroComet là một nền tảng xã hội được thiết kế cho người có đa dạng thần kinh.",
    "about_credits": "Được thực hiện với 💜 bởi đội ngũ NeuroComet",
    "beta_welcome_title": "Chào mừng đến với Beta! 🚀",
    "beta_feedback_button": "Gửi phản hồi",
    "create_story_title": "Tạo tin",
    "create_story_share_button": "Chia sẻ tin",
    "community_join": "Tham gia cộng đồng",
    "community_leave": "Rời cộng đồng",
    "avatar_style_pixel": "Pixel Art",
}

# Thai (th)
TRANSLATIONS_TH = {
    "auth_app_tagline": "พื้นที่ปลอดภัยสำหรับทุกความคิด ✨",
    "auth_sign_in": "เข้าสู่ระบบ",
    "auth_sign_up": "ลงทะเบียน",
    "auth_email_label": "อีเมล",
    "auth_password_label": "รหัสผ่าน",
    "auth_create_account": "สร้างบัญชี",
    "nav_feed": "ฟีด",
    "nav_explore": "สำรวจ",
    "nav_messages": "ข้อความ",
    "nav_notifications": "การแจ้งเตือน",
    "nav_settings": "การตั้งค่า",
    "create_post_button": "โพสต์",
    "comments_title": "ความคิดเห็น",
    "action_save": "บันทึก",
    "action_cancel": "ยกเลิก",
    "action_delete": "ลบ",
    "action_edit": "แก้ไข",
    "status_online": "ออนไลน์",
    "status_offline": "ออฟไลน์",
    "settings_title": "การตั้งค่า",
    "settings_logout": "ออกจากระบบ",
    "dm_send": "ส่ง",
    "create_post_hint": "คุณกำลังคิดอะไรอยู่?",
}

# Swedish (sv)
TRANSLATIONS_SV = {
    "auth_app_tagline": "Ett säkert utrymme för varje sinne ✨",
    "auth_sign_in": "Logga in",
    "auth_sign_up": "Registrera dig",
    "auth_email_label": "E-post",
    "auth_password_label": "Lösenord",
    "auth_create_account": "Skapa konto",
    "nav_feed": "Flöde",
    "nav_explore": "Utforska",
    "nav_messages": "Meddelanden",
    "nav_notifications": "Aviseringar",
    "nav_settings": "Inställningar",
    "create_post_button": "PUBLICERA",
    "comments_title": "Kommentarer",
    "action_save": "Spara",
    "action_cancel": "Avbryt",
    "action_delete": "Ta bort",
    "action_edit": "Redigera",
    "status_online": "Online",
    "status_offline": "Offline",
    "settings_title": "Inställningar",
    "settings_logout": "Logga ut",
    "dm_send": "Skicka",
    "create_post_hint": "Vad tänker du på?",
    "about_title": "Om NeuroComet",
    "about_description": "NeuroComet är en social plattform designad för neurodivergenta sinnen.",
    "about_credits": "Gjord med 💜 av NeuroComet-teamet",
    "beta_welcome_title": "Välkommen till Betan! 🚀",
    "beta_feedback_button": "Skicka feedback",
    "create_story_title": "Skapa händelse",
    "create_story_share_button": "Dela händelse",
    "community_join": "Gå med i community",
    "community_leave": "Lämna community",
    "avatar_style_pixel": "Pixelkonst",
}

# Danish (da)
TRANSLATIONS_DA = {
    "auth_app_tagline": "Et sikkert rum for ethvert sind ✨",
    "auth_sign_in": "Log ind",
    "auth_sign_up": "Tilmeld dig",
    "auth_email_label": "E-mail",
    "auth_password_label": "Adgangskode",
    "auth_create_account": "Opret konto",
    "nav_feed": "Feed",
    "nav_explore": "Udforsk",
    "nav_messages": "Beskeder",
    "nav_notifications": "Notifikationer",
    "nav_settings": "Indstillinger",
    "create_post_button": "SLÅ OP",
    "comments_title": "Kommentarer",
    "action_save": "Gem",
    "action_cancel": "Annuller",
    "action_delete": "Slet",
    "action_edit": "Rediger",
    "status_online": "Online",
    "status_offline": "Offline",
    "settings_title": "Indstillinger",
    "settings_logout": "Log ud",
    "dm_send": "Send",
    "create_post_hint": "Hvad tænker du på?",
}

# Norwegian Bokmål (nb)
TRANSLATIONS_NB = {
    "auth_app_tagline": "Et trygt rom for alle sinn ✨",
    "auth_sign_in": "Logg inn",
    "auth_sign_up": "Registrer deg",
    "auth_email_label": "E-post",
    "auth_password_label": "Passord",
    "auth_create_account": "Opprett konto",
    "nav_feed": "Feed",
    "nav_explore": "Utforsk",
    "nav_messages": "Meldinger",
    "nav_notifications": "Varsler",
    "nav_settings": "Innstillinger",
    "create_post_button": "PUBLISER",
    "comments_title": "Kommentarer",
    "action_save": "Lagre",
    "action_cancel": "Avbryt",
    "action_delete": "Slett",
    "action_edit": "Rediger",
    "status_online": "Pålogget",
    "status_offline": "Avlogget",
    "settings_title": "Innstillinger",
    "settings_logout": "Logg ut",
    "dm_send": "Send",
    "create_post_hint": "Hva tenker du på?",
}

# Finnish (fi)
TRANSLATIONS_FI = {
    "auth_app_tagline": "Turvallinen tila jokaiselle mielelle ✨",
    "auth_sign_in": "Kirjaudu sisään",
    "auth_sign_up": "Rekisteröidy",
    "auth_email_label": "Sähköposti",
    "auth_password_label": "Salasana",
    "auth_create_account": "Luo tili",
    "nav_feed": "Etusivu",
    "nav_explore": "Tutki",
    "nav_messages": "Viestit",
    "nav_notifications": "Ilmoitukset",
    "nav_settings": "Asetukset",
    "create_post_button": "JULKAISE",
    "comments_title": "Kommentit",
    "action_save": "Tallenna",
    "action_cancel": "Peruuta",
    "action_delete": "Poista",
    "action_edit": "Muokkaa",
    "status_online": "Paikalla",
    "status_offline": "Poissa",
    "settings_title": "Asetukset",
    "settings_logout": "Kirjaudu ulos",
    "dm_send": "Lähetä",
    "create_post_hint": "Mitä mietit?",
    "about_title": "Tietoja NeuroCometista",
    "about_description": "NeuroComet on sosiaalinen alusta, joka on suunniteltu neurokirjolle.",
    "about_credits": "Tehty 💜:lla NeuroComet-tiimin toimesta",
    "beta_welcome_title": "Tervetuloa Betaan! 🚀",
    "beta_feedback_button": "Lähetä palautetta",
    "create_story_title": "Luo tarina",
    "create_story_share_button": "Jaa tarina",
    "community_join": "Liity yhteisöön",
    "community_leave": "Poistu yhteisöstä",
    "avatar_style_pixel": "Pikselitaide",
}

# Czech (cs)
TRANSLATIONS_CS = {
    "auth_app_tagline": "Bezpečný prostor pro každou mysl ✨",
    "auth_sign_in": "Přihlásit se",
    "auth_sign_up": "Registrovat se",
    "auth_email_label": "E-mail",
    "auth_password_label": "Heslo",
    "auth_create_account": "Vytvořit účet",
    "nav_feed": "Hlavní",
    "nav_explore": "Prozkoumat",
    "nav_messages": "Zprávy",
    "nav_notifications": "Upozornění",
    "nav_settings": "Nastavení",
    "create_post_button": "PŘIDAT",
    "comments_title": "Komentáře",
    "action_save": "Uložit",
    "action_cancel": "Zrušit",
    "action_delete": "Smazat",
    "action_edit": "Upravit",
    "status_online": "Online",
    "status_offline": "Offline",
    "settings_title": "Nastavení",
    "settings_logout": "Odhlásit",
    "dm_send": "Odeslat",
    "create_post_hint": "Na co myslíte?",
    "about_title": "O NeuroComet",
    "about_description": "NeuroComet je sociální platforma navržená pro neurodivergentní mysli.",
    "about_credits": "Vyrobeno s 💜 týmem NeuroComet",
    "beta_welcome_title": "Vítejte v Betě! 🚀",
    "beta_feedback_button": "Odeslat zpětnou vazbu",
    "create_story_title": "Vytvořit příběh",
    "create_story_share_button": "Sdílet příběh",
    "community_join": "Připojit se ke komunitě",
    "community_leave": "Opustit komunitu",
    "avatar_style_pixel": "Pixel Art",
}

# Hungarian (hu)
TRANSLATIONS_HU = {
    "auth_app_tagline": "Biztonságos tér minden elmének ✨",
    "auth_sign_in": "Bejelentkezés",
    "auth_sign_up": "Regisztráció",
    "auth_email_label": "E-mail",
    "auth_password_label": "Jelszó",
    "auth_create_account": "Fiók létrehozása",
    "nav_feed": "Kezdőlap",
    "nav_explore": "Felfedezés",
    "nav_messages": "Üzenetek",
    "nav_notifications": "Értesítések",
    "nav_settings": "Beállítások",
    "create_post_button": "KÖZZÉTÉTEL",
    "comments_title": "Hozzászólások",
    "action_save": "Mentés",
    "action_cancel": "Mégsem",
    "action_delete": "Törlés",
    "action_edit": "Szerkesztés",
    "status_online": "Elérhető",
    "status_offline": "Nem elérhető",
    "settings_title": "Beállítások",
    "settings_logout": "Kijelentkezés",
    "dm_send": "Küldés",
    "create_post_hint": "Mi jár a fejedben?",
}

# Romanian (ro)
TRANSLATIONS_RO = {
    "auth_app_tagline": "Un spațiu sigur pentru fiecare minte ✨",
    "auth_sign_in": "Autentificare",
    "auth_sign_up": "Înregistrare",
    "auth_email_label": "E-mail",
    "auth_password_label": "Parolă",
    "auth_create_account": "Creează cont",
    "nav_feed": "Acasă",
    "nav_explore": "Explorare",
    "nav_messages": "Mesaje",
    "nav_notifications": "Notificări",
    "nav_settings": "Setări",
    "create_post_button": "POSTEAZĂ",
    "comments_title": "Comentarii",
    "action_save": "Salvează",
    "action_cancel": "Anulează",
    "action_delete": "Șterge",
    "action_edit": "Editează",
    "status_online": "Online",
    "status_offline": "Offline",
    "settings_title": "Setări",
    "settings_logout": "Deconectare",
    "dm_send": "Trimite",
    "create_post_hint": "La ce te gândești?",
}

# Ukrainian (uk)
TRANSLATIONS_UK = {
    "auth_app_tagline": "Безпечний простір для кожного розуму ✨",
    "auth_sign_in": "Увійти",
    "auth_sign_up": "Реєстрація",
    "auth_email_label": "Електронна пошта",
    "auth_password_label": "Пароль",
    "auth_create_account": "Створити акаунт",
    "nav_feed": "Стрічка",
    "nav_explore": "Огляд",
    "nav_messages": "Повідомлення",
    "nav_notifications": "Сповіщення",
    "nav_settings": "Налаштування",
    "create_post_button": "ОПУБЛІКУВАТИ",
    "comments_title": "Коментарі",
    "action_save": "Зберегти",
    "action_cancel": "Скасувати",
    "action_delete": "Видалити",
    "action_edit": "Редагувати",
    "status_online": "В мережі",
    "status_offline": "Не в мережі",
    "settings_title": "Налаштування",
    "settings_logout": "Вийти",
    "dm_send": "Надіслати",
    "create_post_hint": "Про що ви думаєте?",
}

# Malay (ms)
TRANSLATIONS_MS = {
    "auth_app_tagline": "Ruang selamat untuk setiap minda ✨",
    "auth_sign_in": "Log Masuk",
    "auth_sign_up": "Daftar",
    "auth_email_label": "E-mel",
    "auth_password_label": "Kata Laluan",
    "auth_create_account": "Cipta Akaun",
    "nav_feed": "Utama",
    "nav_explore": "Jelajah",
    "nav_messages": "Mesej",
    "nav_notifications": "Pemberitahuan",
    "nav_settings": "Tetapan",
    "create_post_button": "SIAR",
    "comments_title": "Komen",
    "action_save": "Simpan",
    "action_cancel": "Batal",
    "action_delete": "Padam",
    "action_edit": "Edit",
    "status_online": "Dalam Talian",
    "status_offline": "Luar Talian",
    "settings_title": "Tetapan",
    "settings_logout": "Log Keluar",
    "dm_send": "Hantar",
    "create_post_hint": "Apa yang anda fikirkan?",
}

# Greek (el)
TRANSLATIONS_EL = {
    "auth_app_tagline": "Ένας ασφαλής χώρος για κάθε μυαλό ✨",
    "auth_sign_in": "Σύνδεση",
    "auth_sign_up": "Εγγραφή",
    "auth_email_label": "Email",
    "auth_password_label": "Κωδικός",
    "auth_create_account": "Δημιουργία λογαριασμού",
    "nav_feed": "Αρχική",
    "nav_explore": "Εξερεύνηση",
    "nav_messages": "Μηνύματα",
    "nav_notifications": "Ειδοποιήσεις",
    "nav_settings": "Ρυθμίσεις",
    "create_post_button": "ΔΗΜΟΣΙΕΥΣΗ",
    "comments_title": "Σχόλια",
    "action_save": "Αποθήκευση",
    "action_cancel": "Ακύρωση",
    "action_delete": "Διαγραφή",
    "action_edit": "Επεξεργασία",
    "status_online": "Συνδεδεμένος",
    "status_offline": "Αποσυνδεδεμένος",
    "settings_title": "Ρυθμίσεις",
    "settings_logout": "Αποσύνδεση",
    "dm_send": "Αποστολή",
    "create_post_hint": "Τι σκέφτεστε;",
}

ALL_TRANSLATIONS = {
    'values-es': TRANSLATIONS_ES,
    'values-fr': TRANSLATIONS_FR,
    'values-de': TRANSLATIONS_DE,
    'values-in': TRANSLATIONS_IN,
    'values-vi': TRANSLATIONS_VI,
    'values-th': TRANSLATIONS_TH,
    'values-sv': TRANSLATIONS_SV,
    'values-da': TRANSLATIONS_DA,
    'values-nb': TRANSLATIONS_NB,
    'values-fi': TRANSLATIONS_FI,
    'values-cs': TRANSLATIONS_CS,
    'values-hu': TRANSLATIONS_HU,
    'values-ro': TRANSLATIONS_RO,
    'values-uk': TRANSLATIONS_UK,
    'values-ms': TRANSLATIONS_MS,
    'values-el': TRANSLATIONS_EL,
}

def main():
    """Apply all translations."""
    print("Applying extended mass translations...\n")

    total = 0
    for lang_dir, translations in ALL_TRANSLATIONS.items():
        count = update_strings_file(lang_dir, translations)
        total += count
        print(f"v {lang_dir}: Updated {count} strings")

    print(f"\nTotal: {total} strings updated")

if __name__ == '__main__':
    main()

