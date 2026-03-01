#!/usr/bin/env python3
"""
Comprehensive translation completion script.
Fills in remaining English strings with proper translations for all languages.
"""
import os
import re
from pathlib import Path

RES_DIR = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")

# Common strings that need translation across all languages
# These are the most frequently untranslated strings based on the completeness report

COMMON_TRANSLATIONS = {
    # Norwegian (nb)
    "nb": {
        "about_copyright": "© 2024–2026 NeuroComet. Alle rettigheter forbeholdt.",
        "about_credits": "Laget med 💜 av NeuroComet-teamet",
        "about_description": "NeuroComet er en sosial plattform designet for nevrodiverse sinn.",
        "about_tagline": "Et trygt rom for alle sinn ✨",
        "about_title": "Om NeuroComet",
        "about_version": "Versjon %1$s (Bygg %2$d)",
        "beta_feedback_button": "Send tilbakemelding",
        "beta_feedback_prompt": "Fant du en feil eller har et forslag?",
        "beta_version_notice": "Betaversjon %s",
        "beta_welcome_message": "Takk for at du tester NeuroComet! Din tilbakemelding hjelper oss.",
        "beta_welcome_title": "Velkommen til Betaen! 🚀",
        "comments_send_button_description": "Send",
        "comments_view_all": "Se alle %d kommentarer",
        "community_more_options": "Flere alternativer",
        "community_mute_notifications": "Demp varsler",
        "community_report": "Rapporter fellesskap",
        "community_rules": "Fellesskapsregler",
        "community_share": "Del fellesskap",
        "create_post_add_media": "Legg til media",
        "create_post_close": "Lukk",
        "create_post_content_warning": "Innholdsadvarsel",
        "create_post_how_feeling": "Hvordan har du det?",
        "create_post_share_thoughts": "Del tankene dine med fellesskapet…",
        "create_story_share_button": "Del historie",
        "create_story_title": "Opprett ny historie",
        "dm_reply_to_placeholder": "Svar til %s…",
        "mood_calm": "Rolig",
        "mood_happy": "Glad",
        "mood_inspired": "Inspirert",
        "mood_motivated": "Motivert",
        "mood_sad": "Trist",
        "mood_stressed": "Stresset",
        "action_archive": "Arkiver",
        "action_copy": "Kopier",
        "action_delete_chat": "Slett chat",
        "action_react": "Reager…",
        "action_retry": "Prøv på nytt",
        "action_unpin": "Løsne",
        "ads_loading": "Laster annonse…",
        "ads_premium_no_ads": "Ingen annonser - Premium-medlem",
        "ads_watch_to_support": "Se en kort annonse for å støtte NeuroComet",
        "ads_disabled_kids": "Annonser er deaktivert i barnemodus",
        "ads_error_load_failed": "Kunne ikke laste annonse",
        "ads_rewarded_complete": "Takk! Du har fått belønningen din.",
        "comments_close": "Lukk kommentarer",
        "comments_no_comments_yet": "Ingen kommentarer ennå",
        "comments_replying_to": "Svarer til %s",
        "comments_send_comment": "Send kommentar",
        "comments_be_first": "Bli den første til å dele tankene dine!",
        "community_back": "Tilbake",
        "community_compose": "Skriv innlegg",
        "community_join": "Bli med",
        "community_joined_toast": "Ble med i %s",
        "community_leave": "Forlat fellesskap",
        "community_left_toast": "Forlot %s",
        "community_link_copied": "Lenke kopiert",
        "community_notifications_muted_toast": "Varsler dempet for %s",
        "community_notifications_on_toast": "Varsler slått på for %s",
        "community_rules_coming_soon": "Fellesskapsregler kommer snart",
        "community_save": "Lagre fellesskap",
        "community_saved_toast": "Lagret %s til favoritter",
        "community_turn_on_notifications": "Slå på varsler",
        "community_members": "%d medlemmer",
        "community_posts": "%d innlegg",
    },

    # Indonesian (in)
    "in": {
        "about_copyright": "© 2024–2026 NeuroComet. Semua hak dilindungi.",
        "about_credits": "Dibuat dengan 💜 oleh tim NeuroComet",
        "about_description": "NeuroComet adalah platform sosial yang dirancang untuk pikiran neurodivergen.",
        "about_tagline": "Ruang aman untuk setiap pikiran ✨",
        "about_title": "Tentang NeuroComet",
        "about_version": "Versi %1$s (Build %2$d)",
        "beta_feedback_button": "Kirim Umpan Balik",
        "beta_feedback_prompt": "Menemukan bug atau punya saran?",
        "beta_version_notice": "Versi Beta %s",
        "beta_welcome_message": "Terima kasih telah menguji NeuroComet! Umpan balik Anda membantu kami.",
        "beta_welcome_title": "Selamat Datang di Beta! 🚀",
        "badges_back_button_description": "Kembali",
        "badges_earned_section_title": "Lencana Diperoleh",
        "badges_locked_section_title": "Lencana Terkunci",
        "badges_screen_title": "Lencana Anda",
        "button_edit": "Edit",
        "comments_view_all": "Lihat semua %d komentar",
        "community_more_options": "Opsi lainnya",
        "create_post_add_media": "Tambah Media",
        "create_post_close": "Tutup",
        "create_post_how_feeling": "Bagaimana perasaan Anda?",
        "create_story_share_button": "Bagikan Cerita",
        "create_story_title": "Buat Cerita Baru",
        "mood_calm": "Tenang",
        "mood_happy": "Bahagia",
        "mood_inspired": "Terinspirasi",
        "mood_motivated": "Termotivasi",
        "mood_sad": "Sedih",
        "mood_stressed": "Stres",
        "action_archive": "Arsipkan",
        "action_copy": "Salin",
        "action_delete_chat": "Hapus obrolan",
        "action_react": "Bereaksi…",
        "action_retry": "Coba lagi",
        "action_unpin": "Lepas sematan",
        "ads_loading": "Memuat iklan…",
        "ads_premium_no_ads": "Tanpa Iklan - Anggota Premium",
        "ads_watch_to_support": "Tonton iklan singkat untuk mendukung NeuroComet",
        "ads_disabled_kids": "Iklan dinonaktifkan di mode anak",
        "ads_error_load_failed": "Gagal memuat iklan",
        "ads_rewarded_complete": "Terima kasih! Kamu mendapatkan hadiah.",
        "comments_close": "Tutup komentar",
        "comments_no_comments_yet": "Belum ada komentar",
        "comments_replying_to": "Membalas %s",
        "comments_send_comment": "Kirim komentar",
        "comments_be_first": "Jadilah yang pertama berbagi pemikiran!",
        "community_back": "Kembali",
        "community_compose": "Buat postingan",
        "community_join": "Gabung komunitas",
        "community_joined_toast": "Bergabung dengan %s",
        "community_leave": "Tinggalkan komunitas",
        "community_left_toast": "Meninggalkan %s",
        "community_link_copied": "Tautan disalin",
        "community_mute_notifications": "Bisukan notifikasi",
        "community_notifications_muted_toast": "Notifikasi dibisukan untuk %s",
        "community_notifications_on_toast": "Notifikasi dinyalakan untuk %s",
        "community_report": "Laporkan komunitas",
        "community_rules": "Aturan komunitas",
        "community_save": "Simpan komunitas",
        "community_share": "Bagikan komunitas",
        "community_members": "%d anggota",
        "community_posts": "%d postingan",
    },

    # Thai (th)
    "th": {
        "about_copyright": "© 2024–2026 NeuroComet สงวนลิขสิทธิ์",
        "about_credits": "สร้างด้วย 💜 โดยทีม NeuroComet",
        "about_description": "NeuroComet คือแพลตฟอร์มโซเชียลที่ออกแบบมาสำหรับจิตใจที่หลากหลาย",
        "about_tagline": "พื้นที่ปลอดภัยสำหรับทุกความคิด ✨",
        "about_title": "เกี่ยวกับ NeuroComet",
        "about_version": "เวอร์ชัน %1$s (บิลด์ %2$d)",
        "beta_feedback_button": "ส่งความคิดเห็น",
        "beta_feedback_prompt": "พบบั๊กหรือมีข้อเสนอแนะ?",
        "beta_version_notice": "เวอร์ชันเบต้า %s",
        "beta_welcome_message": "ขอบคุณที่ทดสอบ NeuroComet! ความคิดเห็นของคุณช่วยเราได้มาก",
        "beta_welcome_title": "ยินดีต้อนรับสู่เบต้า! 🚀",
        "comments_send_button_description": "ส่ง",
        "comments_view_all": "ดูความคิดเห็นทั้งหมด %d รายการ",
        "community_more_options": "ตัวเลือกเพิ่มเติม",
        "community_mute_notifications": "ปิดเสียงการแจ้งเตือน",
        "community_report": "รายงานชุมชน",
        "create_post_add_media": "เพิ่มสื่อ",
        "create_post_close": "ปิด",
        "create_post_how_feeling": "คุณรู้สึกอย่างไร?",
        "create_story_share_button": "แชร์เรื่องราว",
        "create_story_title": "สร้างเรื่องราวใหม่",
        "mood_calm": "สงบ",
        "mood_happy": "มีความสุข",
        "mood_inspired": "ได้แรงบันดาลใจ",
        "mood_motivated": "มีแรงจูงใจ",
        "mood_sad": "เศร้า",
        "mood_stressed": "เครียด",
    },

    # Ukrainian (uk)
    "uk": {
        "about_copyright": "© 2024–2026 NeuroComet. Всі права захищено.",
        "about_credits": "Створено з 💜 командою NeuroComet",
        "about_description": "NeuroComet — соціальна платформа для нейрорізноманітних людей.",
        "about_tagline": "Безпечний простір для кожного розуму ✨",
        "about_title": "Про NeuroComet",
        "about_version": "Версія %1$s (Збірка %2$d)",
        "beta_feedback_button": "Надіслати відгук",
        "beta_feedback_prompt": "Знайшли помилку або маєте пропозицію?",
        "beta_version_notice": "Бета-версія %s",
        "beta_welcome_message": "Дякуємо за тестування NeuroComet! Ваш відгук допомагає нам.",
        "beta_welcome_title": "Ласкаво просимо до Бети! 🚀",
        "community_more_options": "Більше опцій",
        "community_mute_notifications": "Вимкнути сповіщення",
        "community_report": "Поскаржитися на спільноту",
        "community_rules": "Правила спільноти",
        "create_post_add_media": "Додати медіа",
        "create_post_close": "Закрити",
        "create_post_how_feeling": "Як ви себе почуваєте?",
        "create_story_share_button": "Поділитися історією",
        "create_story_title": "Створити нову історію",
        "mood_calm": "Спокійний",
        "mood_happy": "Щасливий",
        "mood_inspired": "Натхненний",
        "mood_motivated": "Мотивований",
        "mood_sad": "Сумний",
        "mood_stressed": "Напружений",
    },

    # Vietnamese (vi)
    "vi": {
        "about_copyright": "© 2024–2026 NeuroComet. Bản quyền được bảo lưu.",
        "about_credits": "Được tạo với 💜 bởi đội ngũ NeuroComet",
        "about_description": "NeuroComet là nền tảng xã hội dành cho tâm trí đa dạng thần kinh.",
        "about_tagline": "Không gian an toàn cho mọi tâm hồn ✨",
        "about_title": "Giới thiệu NeuroComet",
        "about_version": "Phiên bản %1$s (Bản dựng %2$d)",
        "beta_feedback_button": "Gửi phản hồi",
        "beta_feedback_prompt": "Tìm thấy lỗi hoặc có đề xuất?",
        "beta_version_notice": "Phiên bản Beta %s",
        "beta_welcome_message": "Cảm ơn bạn đã thử nghiệm NeuroComet! Phản hồi của bạn giúp ích cho chúng tôi.",
        "beta_welcome_title": "Chào mừng đến với Beta! 🚀",
        "comments_send_button_description": "Gửi",
        "comments_view_all": "Xem tất cả %d bình luận",
        "community_more_options": "Thêm tùy chọn",
        "community_mute_notifications": "Tắt thông báo",
        "community_report": "Báo cáo cộng đồng",
        "create_post_add_media": "Thêm phương tiện",
        "create_post_close": "Đóng",
        "create_post_how_feeling": "Bạn đang cảm thấy thế nào?",
        "create_story_share_button": "Chia sẻ tin",
        "create_story_title": "Tạo tin mới",
        "mood_calm": "Bình tĩnh",
        "mood_happy": "Vui vẻ",
        "mood_inspired": "Được truyền cảm hứng",
        "mood_motivated": "Có động lực",
        "mood_sad": "Buồn",
        "mood_stressed": "Căng thẳng",
    },

    # Finnish (fi)
    "fi": {
        "about_copyright": "© 2024–2026 NeuroComet. Kaikki oikeudet pidätetään.",
        "about_credits": "Tehty 💜:lla NeuroComet-tiimin toimesta",
        "about_description": "NeuroComet on sosiaalinen alusta, joka on suunniteltu neurokirjolle.",
        "about_tagline": "Turvallinen tila jokaiselle mielelle ✨",
        "about_title": "Tietoja NeuroCometista",
        "about_version": "Versio %1$s (Koontiversio %2$d)",
        "beta_feedback_button": "Lähetä palautetta",
        "beta_feedback_prompt": "Löysitkö virheen tai onko sinulla ehdotus?",
        "beta_version_notice": "Beetaversio %s",
        "beta_welcome_message": "Kiitos NeuroComet-testauksesta! Palautteesi auttaa meitä.",
        "beta_welcome_title": "Tervetuloa Betaan! 🚀",
        "create_post_tone_label": "Sävy",
        "create_story_add_button": "LISÄÄ TARINA",
        "create_story_background_color": "Taustaväri",
        "create_story_cancel": "Peruuta",
        "create_story_share_button": "Jaa tarina",
        "create_story_title": "Luo uusi tarina",
        "mood_calm": "Rauhallinen",
        "mood_happy": "Iloinen",
        "mood_inspired": "Inspiroitunut",
        "mood_motivated": "Motivoitunut",
        "mood_sad": "Surullinen",
        "mood_stressed": "Stressaantunut",
    },

    # Polish (pl)
    "pl": {
        "about_copyright": "© 2024–2026 NeuroComet. Wszelkie prawa zastrzeżone.",
        "about_credits": "Stworzone z 💜 przez zespół NeuroComet",
        "about_description": "NeuroComet to platforma społecznościowa zaprojektowana dla osób neuroróżnorodnych.",
        "about_tagline": "Bezpieczna przestrzeń dla każdego umysłu ✨",
        "about_title": "O NeuroComet",
        "about_version": "Wersja %1$s (Kompilacja %2$d)",
        "beta_feedback_button": "Wyślij opinię",
        "beta_feedback_prompt": "Znalazłeś błąd lub masz sugestię?",
        "beta_version_notice": "Wersja beta %s",
        "beta_welcome_message": "Dziękujemy za testowanie NeuroComet! Twoja opinia nam pomaga.",
        "beta_welcome_title": "Witamy w Becie! 🚀",
        "comments_send_button_description": "Wyślij",
        "create_post_add_image": "Dodaj zdjęcie",
        "create_post_add_media": "Dodaj media",
        "create_post_add_video": "Dodaj wideo",
        "create_post_close": "Zamknij",
        "create_post_content_warning": "Ostrzeżenie o treści",
        "create_story_share_button": "Udostępnij historię",
        "create_story_title": "Utwórz nową historię",
        "mood_calm": "Spokojny",
        "mood_happy": "Szczęśliwy",
        "mood_inspired": "Zainspirowany",
        "mood_motivated": "Zmotywowany",
        "mood_sad": "Smutny",
        "mood_stressed": "Zestresowany",
    },

    # Italian (it)
    "it": {
        "about_copyright": "© 2024–2026 NeuroComet. Tutti i diritti riservati.",
        "about_credits": "Creato con 💜 dal team NeuroComet",
        "about_description": "NeuroComet è una piattaforma sociale progettata per le menti neurodivergenti.",
        "about_tagline": "Uno spazio sicuro per ogni mente ✨",
        "about_title": "Informazioni su NeuroComet",
        "about_version": "Versione %1$s (Build %2$d)",
        "auth_password_label": "Password",
        "beta_feedback_button": "Invia feedback",
        "beta_feedback_prompt": "Hai trovato un bug o hai un suggerimento?",
        "beta_version_notice": "Versione Beta %s",
        "beta_welcome_message": "Grazie per aver testato NeuroComet! Il tuo feedback ci aiuta.",
        "beta_welcome_title": "Benvenuto nella Beta! 🚀",
        "badge_name_community_pillar": "Pilastro della Comunità",
        "badge_name_first_post": "Primo Post",
        "badge_name_hyperfocus_master": "Maestro dell\\'Iperfocus",
        "badge_name_quiet_achiever": "Raggiungitore Silenzioso",
        "badge_name_verified_human": "Umano Verificato",
        "badges_back_button_description": "Indietro",
        "badges_earned_section_title": "Badge Ottenuti",
        "badges_locked_section_title": "Badge Bloccati",
        "badges_screen_title": "I Tuoi Badge",
        "create_story_share_button": "Condividi storia",
        "create_story_title": "Crea nuova storia",
        "mood_calm": "Calmo",
        "mood_happy": "Felice",
        "mood_inspired": "Ispirato",
        "mood_motivated": "Motivato",
        "mood_sad": "Triste",
        "mood_stressed": "Stressato",
    },

    # Japanese (ja)
    "ja": {
        "about_copyright": "© 2024–2026 NeuroComet. All rights reserved.",
        "about_credits": "💜 NeuroCometチーム制作",
        "about_description": "NeuroCometは神経多様性のある方のためのソーシャルプラットフォームです。",
        "about_tagline": "すべての心のための安全な空間 ✨",
        "about_title": "NeuroCometについて",
        "about_version": "バージョン %1$s (ビルド %2$d)",
        "beta_feedback_button": "フィードバックを送信",
        "beta_feedback_prompt": "バグを発見したり、提案がありますか？",
        "beta_version_notice": "ベータ版 %s",
        "beta_welcome_message": "NeuroCometをテストしていただきありがとうございます！",
        "beta_welcome_title": "ベータ版へようこそ！ 🚀",
        "badges_back_button_description": "戻る",
        "badges_earned_section_title": "獲得バッジ",
        "badges_locked_section_title": "ロック中のバッジ",
        "badges_screen_title": "あなたのバッジ",
        "collapse_category": "%1$s を折りたたむ",
        "create_post_add_media": "メディアを追加",
        "create_post_close": "閉じる",
        "create_post_content_warning": "コンテンツの警告",
        "create_story_share_button": "ストーリーを共有",
        "create_story_title": "新しいストーリーを作成",
        "mood_calm": "穏やか",
        "mood_happy": "幸せ",
        "mood_inspired": "インスピレーション",
        "mood_motivated": "やる気",
        "mood_sad": "悲しい",
        "mood_stressed": "ストレス",
    },

    # Korean (ko)
    "ko": {
        "about_copyright": "© 2024–2026 NeuroComet. 모든 권리 보유.",
        "about_credits": "💜 NeuroComet 팀 제작",
        "about_description": "NeuroComet은 신경다양성을 위한 소셜 플랫폼입니다.",
        "about_tagline": "모든 마음을 위한 안전한 공간 ✨",
        "about_title": "NeuroComet 소개",
        "about_version": "버전 %1$s (빌드 %2$d)",
        "beta_feedback_button": "피드백 보내기",
        "beta_feedback_prompt": "버그를 발견했거나 제안이 있나요?",
        "beta_version_notice": "베타 버전 %s",
        "beta_welcome_message": "NeuroComet을 테스트해 주셔서 감사합니다!",
        "beta_welcome_title": "베타에 오신 것을 환영합니다! 🚀",
        "create_post_add_image": "이미지 추가",
        "create_post_add_media": "미디어 추가",
        "create_post_close": "닫기",
        "create_post_content_warning": "콘텐츠 경고",
        "create_post_how_feeling": "기분이 어떠세요?",
        "create_post_share_thoughts": "커뮤니티와 생각을 공유하세요…",
        "create_post_tone_label": "톤",
        "create_story_share_button": "스토리 공유",
        "create_story_title": "새 스토리 만들기",
        "mood_calm": "차분함",
        "mood_happy": "행복함",
        "mood_inspired": "영감",
        "mood_motivated": "동기부여",
        "mood_sad": "슬픔",
        "mood_stressed": "스트레스",
        "action_archive": "보관",
        "action_copy": "복사",
        "action_delete_chat": "채팅 삭제",
        "action_react": "반응 남기기…",
        "action_retry": "재시도",
        "action_unpin": "고정 해제",
        "ads_loading": "광고 로드 중…",
        "ads_premium_no_ads": "광고 없음 - 프리미엄 회원",
        "ads_watch_to_support": "NeuroComet을 지원하기 위해 짧은 광고를 시청하세요",
        "ads_disabled_kids": "키즈 모드에서는 광고가 비활성화됩니다",
        "ads_error_load_failed": "광고 로드 실패",
        "ads_rewarded_complete": "감사합니다! 보상을 획득하셨습니다.",
        "comments_close": "댓글 닫기",
        "comments_no_comments_yet": "아직 댓글이 없습니다",
        "comments_replying_to": "%s님에게 답글 남기는 중",
        "comments_send_comment": "댓글 보내기",
        "comments_be_first": "첫 번째로 생각을 공유해보세요!",
        "community_back": "뒤로",
        "community_compose": "게시물 작성",
        "community_join": "커뮤니티 가입",
        "community_joined_toast": "%s 커뮤니티에 가입했습니다",
        "community_leave": "커뮤니티 탈퇴",
        "community_left_toast": "%s 커뮤니티를 탈퇴했습니다",
        "community_link_copied": "링크 복사됨",
        "community_mute_notifications": "알림 음소거",
        "community_notifications_muted_toast": "%s 알림이 음소거되었습니다",
        "community_notifications_on_toast": "%s 알림이 켜졌습니다",
        "community_report": "커뮤니ti 신고",
        "community_rules": "커뮤니티 규칙",
        "community_save": "커뮤니티 저장",
        "community_share": "커뮤니티 공유",
        "community_members": "멤버 %d명",
        "community_posts": "게시물 %d개",
    },

    # Chinese (zh)
    "zh": {
        "about_copyright": "© 2024–2026 NeuroComet. 保留所有权利。",
        "about_credits": "由 NeuroComet 团队用 💜 制作",
        "about_description": "NeuroComet 是一个为神经多样性人群设计的社交平台。",
        "about_tagline": "为每个心灵提供安全空间 ✨",
        "about_title": "关于 NeuroComet",
        "about_version": "版本 %1$s (构建 %2$d)",
        "beta_feedback_button": "发送反馈",
        "beta_feedback_prompt": "发现了错误或有建议？",
        "beta_version_notice": "测试版 %s",
        "beta_welcome_message": "感谢您测试 NeuroComet！您的反馈对我们很有帮助。",
        "beta_welcome_title": "欢迎来到测试版！ 🚀",
        "create_story_add_button": "添加故事",
        "create_story_cancel": "取消",
        "create_story_share_button": "分享故事",
        "create_story_title": "创建新故事",
        "create_story_understood": "已理解",
        "mood_calm": "平静",
        "mood_happy": "开心",
        "mood_inspired": "受启发",
        "mood_motivated": "有动力",
        "mood_sad": "难过",
        "mood_stressed": "有压力",
    },

    # Hungarian (hu)
    "hu": {
        "about_copyright": "© 2024–2026 NeuroComet. Minden jog fenntartva.",
        "about_credits": "Készítette 💜-vel a NeuroComet csapat",
        "about_description": "A NeuroComet egy közösségi platform neurodiverzáns elmék számára.",
        "about_tagline": "Biztonságos tér minden elmének ✨",
        "about_title": "A NeuroComet névjegye",
        "about_version": "Verzió %1$s (Build %2$d)",
        "beta_feedback_button": "Visszajelzés küldése",
        "beta_feedback_prompt": "Hibát találtál vagy van javaslatod?",
        "beta_version_notice": "Béta verzió %s",
        "beta_welcome_message": "Köszönjük, hogy teszteled a NeuroCometot!",
        "beta_welcome_title": "Üdv a Bétában! 🚀",
        "create_story_share_button": "Történet megosztása",
        "create_story_posting": "Közzététel…",
        "create_story_title": "Új történet létrehozása",
        "mood_calm": "Nyugodt",
        "mood_happy": "Boldog",
        "mood_inspired": "Inspirált",
        "mood_motivated": "Motivált",
        "mood_sad": "Szomorú",
        "mood_stressed": "Stresszes",
    },

    # Hebrew (iw)
    "iw": {
        "about_copyright": "© 2024–2026 NeuroComet. כל הזכויות שמורות.",
        "about_credits": "נוצר עם 💜 על ידי צוות NeuroComet",
        "about_description": "NeuroComet היא פלטפורמה חברתית המיועדת למוחות נוירו-מגוונים.",
        "about_tagline": "מרחב בטוח לכל מוח ✨",
        "about_title": "אודות NeuroComet",
        "about_version": "גרסה %1$s (Build %2$d)",
        "beta_feedback_button": "שלח משוב",
        "beta_feedback_prompt": "מצאת באג או יש לך הצעה?",
        "beta_version_notice": "גרסת בטא %s",
        "beta_welcome_message": "תודה שבחנת את NeuroComet! המשוב שלך עוזר לנו.",
        "beta_welcome_title": "ברוכים הבאים לבטא! 🚀",
        "create_post_add_media": "הוסף מדיה",
        "create_post_close": "סגור",
        "create_post_how_feeling": "איך את/ה מרגיש/ה?",
        "create_story_share_button": "שתף סטורי",
        "create_story_title": "צור סטורי חדש",
        "mood_calm": "רגוע",
        "mood_happy": "שמח",
        "mood_inspired": "קיבלתי השראה",
        "mood_motivated": "עם מוטיבציה",
        "mood_sad": "עצוב",
        "mood_stressed": "לחוץ",
    },

    # Romanian (ro)
    "ro": {
        "about_copyright": "© 2024–2026 NeuroComet. Toate drepturile rezervate.",
        "about_credits": "Creat cu 💜 de echipa NeuroComet",
        "about_description": "NeuroComet este o platformă socială concepută pentru minți neurodivergente.",
        "about_tagline": "Un spațiu sigur pentru fiecare minte ✨",
        "about_title": "Despre NeuroComet",
        "about_version": "Versiunea %1$s (Build %2$d)",
        "beta_feedback_button": "Trimite feedback",
        "beta_feedback_prompt": "Ai găsit un bug sau ai o sugestie?",
        "beta_version_notice": "Versiune Beta %s",
        "beta_welcome_message": "Mulțumim că testezi NeuroComet! Feedback-ul tău ne ajută.",
        "beta_welcome_title": "Bun venit în Beta! 🚀",
        "create_post_add_media": "Adaugă media",
        "create_post_close": "Închide",
        "create_post_how_feeling": "Cum te simți?",
        "create_story_share_button": "Distribuie povestea",
        "create_story_title": "Creează o poveste nouă",
        "mood_calm": "Calm",
        "mood_happy": "Fericit",
        "mood_inspired": "Inspirat",
        "mood_motivated": "Motivat",
        "mood_sad": "Trist",
        "mood_stressed": "Stresat",
    },

    # Malay (ms)
    "ms": {
        "about_copyright": "© 2024–2026 NeuroComet. Hak cipta terpelihara.",
        "about_credits": "Dibuat dengan 💜 oleh pasukan NeuroComet",
        "about_description": "NeuroComet ialah platform sosial yang direka untuk minda neurodivergen.",
        "about_tagline": "Ruang selamat untuk setiap minda ✨",
        "about_title": "Mengenai NeuroComet",
        "about_version": "Versi %1$s (Bina %2$d)",
        "beta_feedback_button": "Hantar Maklum Balas",
        "beta_feedback_prompt": "Jumpa pepijat atau ada cadangan?",
        "beta_version_notice": "Versi Beta %s",
        "beta_welcome_message": "Terima kasih kerana menguji NeuroComet! Maklum balas anda membantu kami.",
        "beta_welcome_title": "Selamat Datang ke Beta! 🚀",
        "create_post_add_media": "Tambah Media",
        "create_post_close": "Tutup",
        "create_post_how_feeling": "Bagaimana perasaan anda?",
        "mood_calm": "Tenang",
        "mood_happy": "Gembira",
        "mood_inspired": "Terinspirasi",
        "mood_motivated": "Bermotivasi",
        "mood_sad": "Sedih",
        "mood_stressed": "Tertekan",
    },

    # Turkish (tr)
    "tr": {
        "about_copyright": "© 2024–2026 NeuroComet. Tüm hakları saklıdır.",
        "about_credits": "NeuroComet ekibi tarafından 💜 ile yapıldı",
        "about_description": "NeuroComet, nöroçeşitli zihinler için tasarlanmış bir sosyal platformdur.",
        "about_tagline": "Her zihin için güvenli bir alan ✨",
        "about_title": "NeuroComet Hakkında",
        "about_version": "Sürüm %1$s (Yapı %2$d)",
        "beta_feedback_button": "Geri Bildirim Gönder",
        "beta_feedback_prompt": "Bir hata mı buldunuz yoksa bir öneriniz mi var?",
        "beta_version_notice": "Beta Sürümü %s",
        "beta_welcome_message": "NeuroComet'i test ettiğiniz için teşekkürler! Geri bildiriminiz bize yardımcı oluyor.",
        "beta_welcome_title": "Beta'ya Hoş Geldiniz! 🚀",
        "create_post_add_media": "Medya Ekle",
        "create_post_close": "Kapat",
        "create_post_how_feeling": "Nasıl hissediyorsunuz?",
        "mood_calm": "Sakin",
        "mood_happy": "Mutlu",
        "mood_inspired": "İlham Almış",
        "mood_motivated": "Motivasyonlu",
        "mood_sad": "Üzgün",
        "mood_stressed": "Stresli",
    },

    # Hindi (hi)
    "hi": {
        "about_copyright": "© 2024–2026 NeuroComet. सर्वाधिकार सुरक्षित।",
        "about_credits": "NeuroComet टीम द्वारा 💜 के साथ बनाया गया",
        "about_description": "NeuroComet न्यूरोडाईवर्जेंट दिमागों के लिए डिज़ाइन किया गया एक सोशल प्लेटफॉर्म है।",
        "about_tagline": "हर दिमाग के लिए एक सुरक्षित स्थान ✨",
        "about_title": "NeuroComet के बारे में",
        "about_version": "संस्करण %1$s (बिल्ड %2$d)",
        "beta_feedback_button": "प्रतिक्रिया भेजें",
        "beta_feedback_prompt": "कोई बग मिला या कोई सुझाव है?",
        "beta_version_notice": "बीटा संस्करण %s",
        "beta_welcome_message": "NeuroComet का परीक्षण करने के लिए धन्यवाद! आपकी प्रतिक्रिया हमें मदद करती है।",
        "beta_welcome_title": "बीटा में आपका स्वागत है! 🚀",
        "create_post_add_media": "मीडिया जोड़ें",
        "create_post_close": "बंद करें",
        "create_post_how_feeling": "आप कैसा महसूस कर रहे हैं?",
        "mood_calm": "शांत",
        "mood_happy": "खुश",
        "mood_inspired": "प्रेरित",
        "mood_motivated": "प्रेरित",
        "mood_sad": "दुखी",
        "mood_stressed": "तनावग्रस्त",
    },
}

def get_base_strings():
    """Get all translatable strings from English."""
    base_file = RES_DIR / "values" / "strings.xml"
    with open(base_file, 'r', encoding='utf-8') as f:
        content = f.read()

    pattern = r'<string name="([^"]+)"[^>]*>(.*?)</string>'
    matches = re.findall(pattern, content, re.DOTALL)
    return {name: value for name, value in matches}

def escape_xml(text):
    """Escape special XML characters."""
    text = text.replace('&', '&amp;')
    text = text.replace('<', '&lt;')
    text = text.replace('>', '&gt;')
    # Handle apostrophes - escape if not already escaped
    if "\\'" not in text:
        text = text.replace("'", "\\'")
    return text

def update_language_file(lang_code, translations, base_strings):
    """Update a language file with new translations."""
    lang_dir = f"values-{lang_code}"
    lang_file = RES_DIR / lang_dir / "strings.xml"

    if not lang_file.exists():
        print(f"Skipping {lang_code} - file not found")
        return 0

    with open(lang_file, 'r', encoding='utf-8') as f:
        content = f.read()

    updated = 0
    for key, value in translations.items():
        # Pattern to find existing string with this key
        pattern = rf'(<string name="{re.escape(key)}"[^>]*>)([^<]*)(<\/string>)'

        def replacer(match):
            nonlocal updated
            old_value = match.group(2)
            new_value = escape_xml(value)

            # Don't update if already translated to the target
            if old_value == new_value:
                return match.group(0)

            # Update if empty OR if it matches English (untranslated)
            is_untranslated = False
            if len(old_value.strip()) == 0:
                is_untranslated = True
            elif key in base_strings and old_value == base_strings[key]:
                is_untranslated = True

            if is_untranslated:
                updated += 1
                return f'{match.group(1)}{new_value}{match.group(3)}'

            return match.group(0)

        content = re.sub(pattern, replacer, content)

    with open(lang_file, 'w', encoding='utf-8', newline='') as f:
        f.write(content)

    return updated

def main():
    print("=" * 60)
    print("Comprehensive Translation Completion")
    print("=" * 60)

    base_strings = get_base_strings()
    total_updated = 0

    for lang_code, translations in COMMON_TRANSLATIONS.items():
        count = update_language_file(lang_code, translations, base_strings)
        total_updated += count
        print(f"v {lang_code}: Updated {count} untranslated strings")

    print("=" * 60)
    print(f"Total translations applied: {total_updated}")
    print("=" * 60)

if __name__ == "__main__":
    main()

