"""Inject translations for the AuthScreen welcome/feature copy that was
previously flagged translatable="false" and therefore always stayed English.

Run once from the repo root:
    python tools/inject_auth_welcome_translations.py

Safe to re-run: existing entries for any given key are replaced in-place.
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES  = ROOT / "app" / "src" / "main" / "res"

# Keys handled:
#   auth_welcome_title
#   auth_welcome_body
#   auth_sign_in_subtitle
#   auth_sign_up_subtitle
#   auth_feature_accessible_title
#   auth_feature_accessible_body
#   auth_feature_private_title
#   auth_feature_private_body
#   auth_feature_flexible_title
#   auth_feature_flexible_body
TRANSLATIONS: dict[str, dict[str, str]] = {
    "ar": {
        "auth_welcome_title": "هادئ، واضح، وجاهز عندما تكون مستعدًا",
        "auth_welcome_body": "سجّل الدخول أو أنشئ حسابك بتصميم أكثر هدوءًا، ومسافات مريحة، وتأثيرات أقل تشتيتًا لتبدأ بسهولة.",
        "auth_sign_in_subtitle": "تابع من حيث توقفت: التغذية والرسائل والإعدادات.",
        "auth_sign_up_subtitle": "أنشئ حسابًا آمنًا، ثم اختر التجربة التي تناسبك.",
        "auth_feature_accessible_title": "سهل الوصول من أول نقرة",
        "auth_feature_accessible_body": "مسافات قابلة للقراءة، وتسلسل هادئ، ومفاجآت بصرية أقل لتبقى الشاشة مريحة.",
        "auth_feature_private_title": "الخصوصية أولًا بشكل افتراضي",
        "auth_feature_private_body": "يبقى حسابك محميًا بتسجيل دخول آمن، وفحوصات كلمة المرور، وتحقق اختياري.",
        "auth_feature_flexible_title": "مصمم للعقول المختلفة",
        "auth_feature_flexible_body": "تبقى السمات وإعدادات الحركة والتخصيص معك بعد تسجيل الدخول.",
    },
    "cs": {
        "auth_welcome_title": "Klidně, přehledně a připraveno, až budeš chtít",
        "auth_welcome_body": "Přihlas se nebo si založ účet v klidnějším rozložení, s čitelnými rozestupy a méně rušivými efekty, aby byl start snadný.",
        "auth_sign_in_subtitle": "Navaž tam, kde jsi skončil/a – v kanálu, zprávách i nastavení.",
        "auth_sign_up_subtitle": "Vytvoř si bezpečný účet a vyber si prostředí, které ti sedí.",
        "auth_feature_accessible_title": "Přístupné od prvního klepnutí",
        "auth_feature_accessible_body": "Čitelné rozestupy, klidnější hierarchie a méně vizuálních překvapení drží obrazovku přívětivou.",
        "auth_feature_private_title": "Soukromí na prvním místě",
        "auth_feature_private_body": "Tvůj účet chrání bezpečné přihlášení, kontroly hesel a volitelné ověření.",
        "auth_feature_flexible_title": "Pro různé mozky",
        "auth_feature_flexible_body": "Motivy, nastavení pohybu a personalizace tě provázejí i po přihlášení.",
    },
    "da": {
        "auth_welcome_title": "Rolig, klar og klar, når du er",
        "auth_welcome_body": "Log ind eller opret en konto med et roligere layout, læsbar afstand og færre distraherende effekter, så det er nemt at komme i gang.",
        "auth_sign_in_subtitle": "Fortsæt, hvor du slap, med dit feed, beskeder og indstillinger.",
        "auth_sign_up_subtitle": "Opret en sikker konto og vælg den oplevelse, der passer dig bedst.",
        "auth_feature_accessible_title": "Tilgængelig fra første tryk",
        "auth_feature_accessible_body": "Læsbar afstand, roligere hierarki og færre visuelle overraskelser gør skærmen imødekommende.",
        "auth_feature_private_title": "Privatliv først som standard",
        "auth_feature_private_body": "Din konto forbliver beskyttet med sikker login, adgangskodetjek og valgfri verificering.",
        "auth_feature_flexible_title": "Bygget til forskellige hjerner",
        "auth_feature_flexible_body": "Temaer, bevægelsesindstillinger og personalisering følger med, når du er logget ind.",
    },
    "de": {
        "auth_welcome_title": "Ruhig, klar und bereit, wenn du es bist",
        "auth_welcome_body": "Melde dich an oder erstelle ein Konto – mit ruhigerem Layout, gut lesbaren Abständen und weniger ablenkenden Effekten.",
        "auth_sign_in_subtitle": "Mach dort weiter, wo du aufgehört hast: Feed, Nachrichten und Einstellungen.",
        "auth_sign_up_subtitle": "Erstelle ein sicheres Konto und wähle dann das Erlebnis, das zu dir passt.",
        "auth_feature_accessible_title": "Zugänglich vom ersten Tippen an",
        "auth_feature_accessible_body": "Lesbare Abstände, ruhigere Hierarchie und weniger visuelle Überraschungen halten den Bildschirm angenehm.",
        "auth_feature_private_title": "Privatsphäre zuerst – standardmäßig",
        "auth_feature_private_body": "Dein Konto bleibt geschützt mit sicherer Anmeldung, Passwortprüfungen und optionaler Verifizierung.",
        "auth_feature_flexible_title": "Für unterschiedliche Köpfe gemacht",
        "auth_feature_flexible_body": "Themes, Bewegungseinstellungen und Personalisierung begleiten dich auch nach der Anmeldung.",
    },
    "el": {
        "auth_welcome_title": "Ήρεμα, καθαρά και έτοιμα όταν είσαι",
        "auth_welcome_body": "Συνδέσου ή δημιούργησε λογαριασμό με πιο ήρεμη διάταξη, ευανάγνωστα διαστήματα και λιγότερα ενοχλητικά εφέ.",
        "auth_sign_in_subtitle": "Συνέχισε από εκεί που σταμάτησες — ροή, μηνύματα και ρυθμίσεις.",
        "auth_sign_up_subtitle": "Δημιούργησε έναν ασφαλή λογαριασμό και διάλεξε την εμπειρία που σου ταιριάζει.",
        "auth_feature_accessible_title": "Προσβάσιμο από το πρώτο άγγιγμα",
        "auth_feature_accessible_body": "Ευανάγνωστα διαστήματα, πιο ήρεμη ιεραρχία και λιγότερες οπτικές εκπλήξεις κρατούν την οθόνη φιλική.",
        "auth_feature_private_title": "Η ιδιωτικότητα πρώτα, από προεπιλογή",
        "auth_feature_private_body": "Ο λογαριασμός σου παραμένει προστατευμένος με ασφαλή σύνδεση, ελέγχους κωδικού και προαιρετική επαλήθευση.",
        "auth_feature_flexible_title": "Φτιαγμένο για διαφορετικά μυαλά",
        "auth_feature_flexible_body": "Θέματα, ρυθμίσεις κίνησης και εξατομίκευση σε ακολουθούν μετά τη σύνδεση.",
    },
    "es": {
        "auth_welcome_title": "Tranquilo, claro y listo cuando lo estés",
        "auth_welcome_body": "Inicia sesión o crea tu cuenta con un diseño más tranquilo, espacios legibles y menos efectos que distraen para que empezar sea fácil.",
        "auth_sign_in_subtitle": "Retoma donde lo dejaste con tu feed, mensajes y ajustes.",
        "auth_sign_up_subtitle": "Crea una cuenta segura y elige la experiencia que mejor se adapte a ti.",
        "auth_feature_accessible_title": "Accesible desde el primer toque",
        "auth_feature_accessible_body": "Espacios legibles, jerarquía más tranquila y menos sorpresas visuales mantienen la pantalla amigable.",
        "auth_feature_private_title": "Privacidad por defecto",
        "auth_feature_private_body": "Tu cuenta sigue protegida con inicio de sesión seguro, controles de contraseña y verificación opcional.",
        "auth_feature_flexible_title": "Hecho para cerebros distintos",
        "auth_feature_flexible_body": "Temas, ajustes de movimiento y personalización continúan contigo tras iniciar sesión.",
    },
    "fi": {
        "auth_welcome_title": "Rauhallinen, selkeä ja valmis, kun sinä olet",
        "auth_welcome_body": "Kirjaudu sisään tai luo tili rauhallisemmalla ulkoasulla, luettavilla väleillä ja vähemmän häiritsevillä tehosteilla.",
        "auth_sign_in_subtitle": "Jatka siitä, mihin jäit: feed, viestit ja asetukset.",
        "auth_sign_up_subtitle": "Luo turvallinen tili ja valitse sinulle sopiva kokemus.",
        "auth_feature_accessible_title": "Saavutettava ensimmäisestä kosketuksesta",
        "auth_feature_accessible_body": "Luettavat välit, rauhallisempi hierarkia ja vähemmän visuaalisia yllätyksiä pitävät näytön lähestyttävänä.",
        "auth_feature_private_title": "Yksityisyys ensin oletusarvoisesti",
        "auth_feature_private_body": "Tilisi pysyy suojattuna turvallisella kirjautumisella, salasanatarkistuksilla ja valinnaisella vahvistuksella.",
        "auth_feature_flexible_title": "Tehty erilaisille aivoille",
        "auth_feature_flexible_body": "Teemat, liikeasetukset ja yksilöinti kulkevat mukana kirjautumisen jälkeen.",
    },
    "fr": {
        "auth_welcome_title": "Calme, clair et prêt quand tu l’es",
        "auth_welcome_body": "Connecte-toi ou crée ton compte avec une mise en page plus calme, un espacement lisible et moins d’effets distrayants.",
        "auth_sign_in_subtitle": "Reprends où tu t’es arrêté·e avec ton fil, tes messages et tes réglages.",
        "auth_sign_up_subtitle": "Crée un compte sécurisé, puis choisis l’expérience qui te correspond.",
        "auth_feature_accessible_title": "Accessible dès le premier tap",
        "auth_feature_accessible_body": "Espacement lisible, hiérarchie plus apaisée et moins de surprises visuelles gardent l’écran accueillant.",
        "auth_feature_private_title": "Confidentialité par défaut",
        "auth_feature_private_body": "Ton compte reste protégé avec une connexion sécurisée, des vérifications de mot de passe et une validation en option.",
        "auth_feature_flexible_title": "Conçu pour des cerveaux différents",
        "auth_feature_flexible_body": "Thèmes, réglages de mouvement et personnalisation te suivent après la connexion.",
    },
    "hi": {
        "auth_welcome_title": "शांत, स्पष्ट और जब आप तैयार हों, तब तैयार",
        "auth_welcome_body": "साइन इन करें या खाता बनाएँ — शांत लेआउट, पढ़ने योग्य स्पेसिंग और कम विचलित करने वाले इफ़ेक्ट के साथ शुरुआत आसान है।",
        "auth_sign_in_subtitle": "जहाँ छोड़ा था वहीं से जारी रखें — फीड, संदेश और सेटिंग्स।",
        "auth_sign_up_subtitle": "एक सुरक्षित खाता बनाएँ, फिर अपने लिए सबसे उपयुक्त अनुभव चुनें।",
        "auth_feature_accessible_title": "पहले टैप से ही सुलभ",
        "auth_feature_accessible_body": "पढ़ने योग्य स्पेसिंग, शांत हायरार्की और कम दृश्य आश्चर्य स्क्रीन को सहज बनाए रखते हैं।",
        "auth_feature_private_title": "डिफ़ॉल्ट रूप से गोपनीयता-प्रथम",
        "auth_feature_private_body": "सुरक्षित साइन-इन, पासवर्ड जाँच और वैकल्पिक सत्यापन के साथ आपका खाता सुरक्षित रहता है।",
        "auth_feature_flexible_title": "अलग-अलग दिमागों के लिए बनाया गया",
        "auth_feature_flexible_body": "साइन इन के बाद भी थीम, मोशन सेटिंग्स और पर्सनलाइज़ेशन आपके साथ बने रहते हैं।",
    },
    "hu": {
        "auth_welcome_title": "Nyugodt, tiszta és készen áll, amikor te is",
        "auth_welcome_body": "Jelentkezz be vagy hozz létre fiókot nyugodtabb elrendezéssel, olvasható térközökkel és kevesebb zavaró effekttel.",
        "auth_sign_in_subtitle": "Folytasd, ahol abbahagytad: hírfolyam, üzenetek és beállítások.",
        "auth_sign_up_subtitle": "Hozz létre biztonságos fiókot, majd válaszd ki a hozzád illő élményt.",
        "auth_feature_accessible_title": "Elérhető az első érintéstől",
        "auth_feature_accessible_body": "Olvasható térközök, nyugodtabb hierarchia és kevesebb vizuális meglepetés tartja barátságosan a képernyőt.",
        "auth_feature_private_title": "Adatvédelem előre, alapértelmezetten",
        "auth_feature_private_body": "Fiókodat biztonságos bejelentkezés, jelszóellenőrzések és opcionális megerősítés védi.",
        "auth_feature_flexible_title": "Különböző elmékre szabva",
        "auth_feature_flexible_body": "Témák, mozgásbeállítások és személyre szabás bejelentkezés után is veled maradnak.",
    },
    "in": {  # Indonesian
        "auth_welcome_title": "Tenang, jelas, dan siap saat kamu siap",
        "auth_welcome_body": "Masuk atau buat akun dengan tata letak lebih tenang, spasi mudah dibaca, dan efek yang lebih sedikit.",
        "auth_sign_in_subtitle": "Lanjutkan dari tempatmu berhenti — feed, pesan, dan pengaturan.",
        "auth_sign_up_subtitle": "Buat akun yang aman, lalu pilih pengalaman yang paling cocok untukmu.",
        "auth_feature_accessible_title": "Mudah diakses sejak ketukan pertama",
        "auth_feature_accessible_body": "Spasi yang mudah dibaca, hierarki lebih tenang, dan lebih sedikit kejutan visual membuat layar tetap bersahabat.",
        "auth_feature_private_title": "Privasi didahulukan secara default",
        "auth_feature_private_body": "Akunmu tetap terlindungi dengan masuk aman, pengecekan kata sandi, dan verifikasi opsional.",
        "auth_feature_flexible_title": "Dibuat untuk berbagai otak",
        "auth_feature_flexible_body": "Tema, pengaturan gerakan, dan personalisasi tetap ikut setelah kamu masuk.",
    },
    "is": {
        "auth_welcome_title": "Rólegt, skýrt og tilbúið þegar þú ert",
        "auth_welcome_body": "Skráðu þig inn eða búðu til aðgang með rólegra útliti, læsilegu bili og minna truflandi áhrifum.",
        "auth_sign_in_subtitle": "Haltu áfram þar sem frá var horfið: straumur, skilaboð og stillingar.",
        "auth_sign_up_subtitle": "Stofnaðu öruggan aðgang og veldu upplifunina sem hentar þér best.",
        "auth_feature_accessible_title": "Aðgengilegt frá fyrsta snertingu",
        "auth_feature_accessible_body": "Læsilegt bil, rólegri stigveldi og færri sjónrænar óvæntingar halda skjánum vingjarnlegum.",
        "auth_feature_private_title": "Persónuvernd sjálfgefið fyrst",
        "auth_feature_private_body": "Aðgangurinn þinn er varinn með öruggri innskráningu, lykilorðsskoðun og valfrjálsri staðfestingu.",
        "auth_feature_flexible_title": "Gert fyrir ólíka huga",
        "auth_feature_flexible_body": "Þemu, hreyfingarstillingar og sérsnið fylgja þér eftir innskráningu.",
    },
    "it": {
        "auth_welcome_title": "Calmo, chiaro e pronto quando lo sei tu",
        "auth_welcome_body": "Accedi o crea un account con un layout più tranquillo, spaziature leggibili e meno effetti distrattivi.",
        "auth_sign_in_subtitle": "Riprendi da dove avevi lasciato: feed, messaggi e impostazioni.",
        "auth_sign_up_subtitle": "Crea un account sicuro e scegli l’esperienza più adatta a te.",
        "auth_feature_accessible_title": "Accessibile dal primo tocco",
        "auth_feature_accessible_body": "Spaziature leggibili, gerarchia più calma e meno sorprese visive rendono lo schermo accogliente.",
        "auth_feature_private_title": "Privacy prima di tutto, in automatico",
        "auth_feature_private_body": "Il tuo account resta protetto con accesso sicuro, controlli della password e verifica opzionale.",
        "auth_feature_flexible_title": "Pensato per cervelli diversi",
        "auth_feature_flexible_body": "Temi, impostazioni di movimento e personalizzazione ti seguono dopo l’accesso.",
    },
    "iw": {  # Hebrew
        "auth_welcome_title": "רגוע, ברור ומוכן כשאתה מוכן",
        "auth_welcome_body": "התחבר/י או צור/י חשבון בפריסה רגועה יותר, מרווחים קריאים ופחות אפקטים מסיחי דעת.",
        "auth_sign_in_subtitle": "המשך/י מהמקום שבו עצרת: פיד, הודעות והגדרות.",
        "auth_sign_up_subtitle": "צור/י חשבון מאובטח ובחר/י את החוויה שמתאימה לך.",
        "auth_feature_accessible_title": "נגיש כבר מההקשה הראשונה",
        "auth_feature_accessible_body": "מרווחים קריאים, היררכיה רגועה יותר ופחות הפתעות ויזואליות משאירים את המסך נעים.",
        "auth_feature_private_title": "פרטיות קודם, כברירת מחדל",
        "auth_feature_private_body": "החשבון שלך מוגן עם התחברות מאובטחת, בדיקות סיסמה ואימות אופציונלי.",
        "auth_feature_flexible_title": "בנוי עבור מוחות שונים",
        "auth_feature_flexible_body": "ערכות נושא, הגדרות תנועה והתאמה אישית ממשיכות איתך לאחר ההתחברות.",
    },
    "ja": {
        "auth_welcome_title": "穏やかに、わかりやすく、あなたの準備ができたら",
        "auth_welcome_body": "落ち着いたレイアウト、読みやすい間隔、少なめの演出で、サインインやアカウント作成を気軽に始められます。",
        "auth_sign_in_subtitle": "フィード、メッセージ、設定を、続きから再開できます。",
        "auth_sign_up_subtitle": "安全なアカウントを作成し、あなたに合った体験を選びましょう。",
        "auth_feature_accessible_title": "最初のタップからアクセシブル",
        "auth_feature_accessible_body": "読みやすい間隔、落ち着いた階層、視覚的な驚きの少なさで、画面を親しみやすく保ちます。",
        "auth_feature_private_title": "初期設定からプライバシー優先",
        "auth_feature_private_body": "安全なサインイン、パスワードチェック、任意の認証で、アカウントをしっかり保護します。",
        "auth_feature_flexible_title": "さまざまな脳のために設計",
        "auth_feature_flexible_body": "テーマ、モーション設定、パーソナライズはサインイン後もそのまま引き継がれます。",
    },
    "ko": {
        "auth_welcome_title": "차분하고 명확하게, 준비되면 시작해요",
        "auth_welcome_body": "차분한 레이아웃, 읽기 편한 간격, 산만함을 줄인 효과로 로그인이나 계정 만들기가 쉬워요.",
        "auth_sign_in_subtitle": "피드, 메시지, 설정을 이어서 계속 사용하세요.",
        "auth_sign_up_subtitle": "안전한 계정을 만든 뒤, 자신에게 맞는 경험을 선택하세요.",
        "auth_feature_accessible_title": "첫 터치부터 접근성",
        "auth_feature_accessible_body": "읽기 좋은 간격, 차분한 계층, 시각적 놀람이 적어 화면이 편안해요.",
        "auth_feature_private_title": "기본값으로 개인정보 우선",
        "auth_feature_private_body": "안전한 로그인, 비밀번호 점검, 선택적 인증으로 계정을 지켜드려요.",
        "auth_feature_flexible_title": "서로 다른 두뇌를 위한 디자인",
        "auth_feature_flexible_body": "테마, 모션 설정, 개인화는 로그인 후에도 그대로 이어집니다.",
    },
    "ms": {
        "auth_welcome_title": "Tenang, jelas, dan sedia apabila anda sedia",
        "auth_welcome_body": "Log masuk atau buat akaun dengan susun atur lebih tenang, jarak mudah dibaca dan kesan yang kurang mengganggu.",
        "auth_sign_in_subtitle": "Sambung dari tempat anda berhenti — suapan, mesej dan tetapan.",
        "auth_sign_up_subtitle": "Buat akaun yang selamat, kemudian pilih pengalaman yang sesuai untuk anda.",
        "auth_feature_accessible_title": "Mudah diakses dari ketukan pertama",
        "auth_feature_accessible_body": "Jarak yang mudah dibaca, hierarki lebih tenang dan kurang kejutan visual mengekalkan skrin mesra.",
        "auth_feature_private_title": "Privasi didahulukan secara lalai",
        "auth_feature_private_body": "Akaun anda kekal dilindungi dengan log masuk selamat, pemeriksaan kata laluan dan pengesahan pilihan.",
        "auth_feature_flexible_title": "Dibina untuk pelbagai minda",
        "auth_feature_flexible_body": "Tema, tetapan gerakan dan pemperibadian terus bersama anda selepas log masuk.",
    },
    "nb": {
        "auth_welcome_title": "Rolig, tydelig og klart når du er klar",
        "auth_welcome_body": "Logg inn eller opprett konto med roligere layout, lesbar avstand og færre distraherende effekter.",
        "auth_sign_in_subtitle": "Fortsett der du slapp — feed, meldinger og innstillinger.",
        "auth_sign_up_subtitle": "Opprett en sikker konto og velg opplevelsen som passer best for deg.",
        "auth_feature_accessible_title": "Tilgjengelig fra første trykk",
        "auth_feature_accessible_body": "Lesbare avstander, roligere hierarki og færre visuelle overraskelser holder skjermen imøtekommende.",
        "auth_feature_private_title": "Personvern først, som standard",
        "auth_feature_private_body": "Kontoen din er beskyttet med sikker pålogging, passordsjekker og valgfri verifisering.",
        "auth_feature_flexible_title": "Bygget for ulike hjerner",
        "auth_feature_flexible_body": "Temaer, bevegelsesinnstillinger og tilpasning følger med etter innlogging.",
    },
    "nl": {
        "auth_welcome_title": "Rustig, helder en klaar wanneer jij dat bent",
        "auth_welcome_body": "Meld je aan of maak een account met een rustigere lay-out, leesbare spaties en minder afleidende effecten.",
        "auth_sign_in_subtitle": "Ga verder waar je gebleven was: feed, berichten en instellingen.",
        "auth_sign_up_subtitle": "Maak een veilig account en kies de ervaring die het beste bij je past.",
        "auth_feature_accessible_title": "Toegankelijk vanaf de eerste tap",
        "auth_feature_accessible_body": "Leesbare spaties, rustigere hiërarchie en minder visuele verrassingen houden het scherm uitnodigend.",
        "auth_feature_private_title": "Privacy eerst, standaard",
        "auth_feature_private_body": "Je account blijft beschermd met veilig inloggen, wachtwoordcontroles en optionele verificatie.",
        "auth_feature_flexible_title": "Gemaakt voor verschillende breinen",
        "auth_feature_flexible_body": "Thema's, bewegingsinstellingen en personalisatie blijven meedoen nadat je bent ingelogd.",
    },
    "pl": {
        "auth_welcome_title": "Spokojnie, jasno i gotowe, gdy Ty jesteś",
        "auth_welcome_body": "Zaloguj się lub załóż konto – spokojniejszy układ, czytelne odstępy i mniej rozpraszających efektów ułatwiają start.",
        "auth_sign_in_subtitle": "Kontynuuj tam, gdzie skończyłeś/-aś – kanał, wiadomości i ustawienia.",
        "auth_sign_up_subtitle": "Utwórz bezpieczne konto, a potem wybierz doświadczenie dopasowane do Ciebie.",
        "auth_feature_accessible_title": "Dostępne od pierwszego dotknięcia",
        "auth_feature_accessible_body": "Czytelne odstępy, spokojniejsza hierarchia i mniej wizualnych niespodzianek utrzymują ekran przyjaznym.",
        "auth_feature_private_title": "Prywatność domyślnie na pierwszym miejscu",
        "auth_feature_private_body": "Konto chronią bezpieczne logowanie, kontrole haseł i opcjonalna weryfikacja.",
        "auth_feature_flexible_title": "Dla różnych umysłów",
        "auth_feature_flexible_body": "Motywy, ustawienia ruchu i personalizacja zostają z Tobą po zalogowaniu.",
    },
    "pt": {
        "auth_welcome_title": "Calmo, claro e pronto quando você estiver",
        "auth_welcome_body": "Entre ou crie sua conta com um layout mais tranquilo, espaçamento legível e menos efeitos que distraem.",
        "auth_sign_in_subtitle": "Continue de onde parou — feed, mensagens e configurações.",
        "auth_sign_up_subtitle": "Crie uma conta segura e escolha a experiência que combina com você.",
        "auth_feature_accessible_title": "Acessível desde o primeiro toque",
        "auth_feature_accessible_body": "Espaçamentos legíveis, hierarquia mais calma e menos surpresas visuais mantêm a tela acolhedora.",
        "auth_feature_private_title": "Privacidade em primeiro lugar por padrão",
        "auth_feature_private_body": "Sua conta continua protegida com login seguro, verificações de senha e verificação opcional.",
        "auth_feature_flexible_title": "Feito para cérebros diferentes",
        "auth_feature_flexible_body": "Temas, ajustes de movimento e personalização continuam com você depois do login.",
    },
    "ro": {
        "auth_welcome_title": "Calm, clar și gata când ești și tu",
        "auth_welcome_body": "Conectează-te sau creează cont cu un aspect mai liniștit, spațiere lizibilă și mai puține efecte care distrag atenția.",
        "auth_sign_in_subtitle": "Continuă de unde ai rămas — feed, mesaje și setări.",
        "auth_sign_up_subtitle": "Creează un cont sigur, apoi alege experiența care ți se potrivește cel mai bine.",
        "auth_feature_accessible_title": "Accesibil de la prima atingere",
        "auth_feature_accessible_body": "Spațieri lizibile, ierarhie mai calmă și mai puține surprize vizuale păstrează ecranul prietenos.",
        "auth_feature_private_title": "Confidențialitate pe primul loc, implicit",
        "auth_feature_private_body": "Contul tău rămâne protejat cu autentificare sigură, verificări de parolă și verificare opțională.",
        "auth_feature_flexible_title": "Conceput pentru minți diferite",
        "auth_feature_flexible_body": "Teme, setări de mișcare și personalizare te însoțesc și după autentificare.",
    },
    "ru": {
        "auth_welcome_title": "Спокойно, понятно и готово, когда готов ты",
        "auth_welcome_body": "Войди или создай аккаунт с более спокойной раскладкой, удобными отступами и меньшим количеством отвлекающих эффектов.",
        "auth_sign_in_subtitle": "Продолжи с того же места — лента, сообщения и настройки.",
        "auth_sign_up_subtitle": "Создай защищённый аккаунт и выбери подходящий тебе опыт.",
        "auth_feature_accessible_title": "Доступно с первого касания",
        "auth_feature_accessible_body": "Читаемые отступы, спокойная иерархия и меньше визуальных сюрпризов делают экран дружелюбным.",
        "auth_feature_private_title": "Приватность по умолчанию",
        "auth_feature_private_body": "Аккаунт защищён безопасным входом, проверками пароля и необязательной верификацией.",
        "auth_feature_flexible_title": "Сделано для разных мозгов",
        "auth_feature_flexible_body": "Темы, настройки движения и персонализация останутся с тобой после входа.",
    },
    "sv": {
        "auth_welcome_title": "Lugnt, tydligt och redo när du är",
        "auth_welcome_body": "Logga in eller skapa ett konto med lugnare layout, läsbar avstånd och färre distraherande effekter.",
        "auth_sign_in_subtitle": "Fortsätt där du slutade — flöde, meddelanden och inställningar.",
        "auth_sign_up_subtitle": "Skapa ett säkert konto och välj den upplevelse som passar dig bäst.",
        "auth_feature_accessible_title": "Tillgängligt från första trycket",
        "auth_feature_accessible_body": "Läsbart avstånd, lugnare hierarki och färre visuella överraskningar håller skärmen välkomnande.",
        "auth_feature_private_title": "Integritet först som standard",
        "auth_feature_private_body": "Ditt konto förblir skyddat med säker inloggning, lösenordskontroller och valfri verifiering.",
        "auth_feature_flexible_title": "Byggt för olika hjärnor",
        "auth_feature_flexible_body": "Teman, rörelseinställningar och personalisering följer med efter inloggning.",
    },
    "th": {
        "auth_welcome_title": "สงบ ชัดเจน และพร้อมเมื่อคุณพร้อม",
        "auth_welcome_body": "ลงชื่อเข้าใช้หรือสร้างบัญชีด้วยเลย์เอาต์ที่สงบขึ้น ระยะห่างที่อ่านง่าย และเอฟเฟกต์ที่รบกวนน้อยลง",
        "auth_sign_in_subtitle": "ทำต่อจากที่ค้างไว้ — ฟีด ข้อความ และการตั้งค่า",
        "auth_sign_up_subtitle": "สร้างบัญชีที่ปลอดภัย แล้วเลือกประสบการณ์ที่เหมาะกับคุณ",
        "auth_feature_accessible_title": "เข้าถึงได้ตั้งแต่แตะครั้งแรก",
        "auth_feature_accessible_body": "ระยะห่างที่อ่านง่าย ลำดับที่สงบขึ้น และความเซอร์ไพรส์ทางสายตาที่น้อยลงทำให้หน้าจอเป็นมิตร",
        "auth_feature_private_title": "ความเป็นส่วนตัวมาก่อนเป็นค่าเริ่มต้น",
        "auth_feature_private_body": "บัญชีของคุณได้รับการปกป้องด้วยการลงชื่อเข้าใช้ที่ปลอดภัย การตรวจสอบรหัสผ่าน และการยืนยันแบบไม่บังคับ",
        "auth_feature_flexible_title": "สร้างขึ้นสำหรับสมองที่หลากหลาย",
        "auth_feature_flexible_body": "ธีม การตั้งค่าการเคลื่อนไหว และการปรับแต่งยังอยู่กับคุณหลังจากลงชื่อเข้าใช้",
    },
    "tr": {
        "auth_welcome_title": "Sakin, net ve hazır olduğunda seninle",
        "auth_welcome_body": "Daha sakin bir yerleşim, okunabilir boşluklar ve daha az dikkat dağıtan efektlerle oturum aç veya hesap oluştur.",
        "auth_sign_in_subtitle": "Kaldığın yerden devam et — akış, mesajlar ve ayarlar.",
        "auth_sign_up_subtitle": "Güvenli bir hesap oluştur, sonra sana en uygun deneyimi seç.",
        "auth_feature_accessible_title": "İlk dokunuştan itibaren erişilebilir",
        "auth_feature_accessible_body": "Okunabilir boşluklar, daha sakin bir hiyerarşi ve daha az görsel sürpriz ekranı dostça tutar.",
        "auth_feature_private_title": "Varsayılan olarak gizlilik önde",
        "auth_feature_private_body": "Hesabın güvenli girişler, parola kontrolleri ve isteğe bağlı doğrulama ile korunur.",
        "auth_feature_flexible_title": "Farklı zihinler için tasarlandı",
        "auth_feature_flexible_body": "Temalar, hareket ayarları ve kişiselleştirme giriş yaptıktan sonra da seninle kalır.",
    },
    "uk": {
        "auth_welcome_title": "Спокійно, зрозуміло та готове, коли готовий ти",
        "auth_welcome_body": "Увійди або створи акаунт зі спокійнішою розкладкою, зручними відступами та меншою кількістю відволікаючих ефектів.",
        "auth_sign_in_subtitle": "Продовж з того ж місця: стрічка, повідомлення та налаштування.",
        "auth_sign_up_subtitle": "Створи безпечний акаунт і обери досвід, який тобі підходить.",
        "auth_feature_accessible_title": "Доступно з першого дотику",
        "auth_feature_accessible_body": "Читані відступи, спокійніша ієрархія і менше візуальних несподіванок роблять екран приязним.",
        "auth_feature_private_title": "Приватність за замовчуванням",
        "auth_feature_private_body": "Твій акаунт захищений безпечним входом, перевірками пароля та необовʼязковою верифікацією.",
        "auth_feature_flexible_title": "Зроблено для різних мізків",
        "auth_feature_flexible_body": "Теми, налаштування руху та персоналізація залишаться з тобою після входу.",
    },
    "ur": {
        "auth_welcome_title": "پرسکون، واضح اور تیار جب آپ تیار ہوں",
        "auth_welcome_body": "سائن اِن کریں یا اکاؤنٹ بنائیں — پرسکون لے آؤٹ، پڑھنے میں آسان وقفہ اور کم مداخلت کرنے والے اثرات کے ساتھ۔",
        "auth_sign_in_subtitle": "جہاں چھوڑا تھا وہاں سے جاری رکھیں — فیڈ، پیغامات اور ترتیبات۔",
        "auth_sign_up_subtitle": "ایک محفوظ اکاؤنٹ بنائیں اور پھر اپنے لیے موزوں تجربہ چنیں۔",
        "auth_feature_accessible_title": "پہلے ٹیپ سے ہی قابلِ رسائی",
        "auth_feature_accessible_body": "پڑھنے کے قابل وقفہ، پرسکون ترتیب اور کم بصری حیرتیں اسکرین کو دوستانہ رکھتی ہیں۔",
        "auth_feature_private_title": "طے شدہ طور پر پرائیویسی سب سے پہلے",
        "auth_feature_private_body": "آپ کا اکاؤنٹ محفوظ سائن اِن، پاس ورڈ چیکس اور اختیاری تصدیق کے ساتھ محفوظ رہتا ہے۔",
        "auth_feature_flexible_title": "مختلف دماغوں کے لیے بنایا گیا",
        "auth_feature_flexible_body": "سائن اِن کے بعد بھی تھیمز، موشن سیٹنگز اور پرسنلائزیشن آپ کے ساتھ رہتی ہیں۔",
    },
    "vi": {
        "auth_welcome_title": "Bình tĩnh, rõ ràng và sẵn sàng khi bạn sẵn sàng",
        "auth_welcome_body": "Đăng nhập hoặc tạo tài khoản với bố cục nhẹ nhàng hơn, khoảng cách dễ đọc và ít hiệu ứng gây xao nhãng.",
        "auth_sign_in_subtitle": "Tiếp tục từ chỗ bạn dừng — bảng tin, tin nhắn và cài đặt.",
        "auth_sign_up_subtitle": "Tạo một tài khoản an toàn, rồi chọn trải nghiệm phù hợp nhất với bạn.",
        "auth_feature_accessible_title": "Dễ tiếp cận ngay từ lần chạm đầu tiên",
        "auth_feature_accessible_body": "Khoảng cách dễ đọc, phân cấp nhẹ nhàng hơn và ít bất ngờ thị giác giúp màn hình thân thiện.",
        "auth_feature_private_title": "Ưu tiên quyền riêng tư theo mặc định",
        "auth_feature_private_body": "Tài khoản của bạn được bảo vệ bằng đăng nhập an toàn, kiểm tra mật khẩu và xác minh tuỳ chọn.",
        "auth_feature_flexible_title": "Được thiết kế cho nhiều loại não khác nhau",
        "auth_feature_flexible_body": "Chủ đề, cài đặt chuyển động và cá nhân hoá vẫn đi cùng bạn sau khi đăng nhập.",
    },
    "zh": {
        "auth_welcome_title": "平静、清晰，随时为你准备好",
        "auth_welcome_body": "以更平静的布局、易读的间距和更少的干扰效果登录或注册，让开始变得轻松。",
        "auth_sign_in_subtitle": "从上次离开的地方继续——动态、消息和设置。",
        "auth_sign_up_subtitle": "创建一个安全的账户，然后选择最适合你的体验。",
        "auth_feature_accessible_title": "从第一次点击就可访问",
        "auth_feature_accessible_body": "可读的间距、更平静的层级和更少的视觉意外让屏幕保持友好。",
        "auth_feature_private_title": "默认以隐私为先",
        "auth_feature_private_body": "安全登录、密码检查和可选验证共同守护你的账户。",
        "auth_feature_flexible_title": "为不同的大脑而建",
        "auth_feature_flexible_body": "登录之后，主题、动效设置和个性化都会继续伴随你。",
    },
}

# English regional variants — keep base English copy.
EN_COPY = {
    "auth_welcome_title": "Calm, clear, and ready when you are",
    "auth_welcome_body": "Sign in or create your account with a quieter layout, readable spacing, and fewer distracting effects so getting started feels easy.",
    "auth_sign_in_subtitle": "Pick up where you left off with your feed, messages, and settings.",
    "auth_sign_up_subtitle": "Create a secure account, then choose the experience that fits you best.",
    "auth_feature_accessible_title": "Accessible from the first tap",
    "auth_feature_accessible_body": "Readable spacing, calmer hierarchy, and fewer visual surprises help the screen stay approachable.",
    "auth_feature_private_title": "Privacy-forward by default",
    "auth_feature_private_body": "Your account stays protected with secure sign-in, password checks, and optional verification.",
    "auth_feature_flexible_title": "Built for different brains",
    "auth_feature_flexible_body": "Themes, motion settings, and personalization still carry through once you're signed in.",
}
for loc in ("en-rAU", "en-rCA", "en-rGB"):
    TRANSLATIONS[loc] = EN_COPY


def xml_escape(value: str) -> str:
    return value.replace("'", "\\'")


def inject(locale: str, entries: dict[str, str]) -> None:
    path = RES / f"values-{locale}" / "strings.xml"
    if not path.exists():
        print(f"  skip (missing): {path.name}")
        return

    text = path.read_text(encoding="utf-8")

    for key, value in entries.items():
        escaped = xml_escape(value)
        new_line = f'    <string name="{key}">{escaped}</string>'
        pattern = re.compile(
            rf'^[ \t]*<string\s+name="{re.escape(key)}"[^>]*>.*?</string>\s*$',
            re.MULTILINE | re.DOTALL,
        )
        if pattern.search(text):
            text = pattern.sub(new_line, text, count=1)
        else:
            text = text.replace("</resources>", f"{new_line}\n</resources>", 1)

    path.write_text(text, encoding="utf-8")
    print(f"  ok: values-{locale}")


def main() -> None:
    print("Injecting auth welcome/feature translations…")
    for locale, entries in TRANSLATIONS.items():
        inject(locale, entries)
    print("Done.")


if __name__ == "__main__":
    main()

