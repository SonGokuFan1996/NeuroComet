"""Inject translations for the Explore / Messages / Notifications header
subtitles that were previously hardcoded to English.

Run once from the repo root:
    python tools/inject_subtitle_translations.py

Safe to re-run: existing entries for any given key are replaced in-place.
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES  = ROOT / "app" / "src" / "main" / "res"

# locale_code -> { key: value }
# Keys handled:
#   explore_header_subtitle
#   messages_header_unread_one
#   messages_header_unread_many      (uses %1$d)
#   messages_header_subtitle_empty
#   notifications_subtitle_new_one
#   notifications_subtitle_new_many  (uses %d)
#   notifications_subtitle_caught_up
TRANSLATIONS: dict[str, dict[str, str]] = {
    "ar": {
        "explore_header_subtitle": "اكتشف المواضيع التي تحبها بدون ضجيج. ✨",
        "messages_header_unread_one": "لديك رسالة غير مقروءة واحدة",
        "messages_header_unread_many": "لديك %1$d رسائل غير مقروءة",
        "messages_header_subtitle_empty": "روابط ذات معنى ✨",
        "notifications_subtitle_new_one": "لديك إشعار جديد واحد",
        "notifications_subtitle_new_many": "لديك %d إشعارات جديدة",
        "notifications_subtitle_caught_up": "لقد اطلعت على كل شيء! ✨",
    },
    "cs": {
        "explore_header_subtitle": "Najdi témata, která máš rád/a, bez šumu. ✨",
        "messages_header_unread_one": "Máš 1 nepřečtenou zprávu",
        "messages_header_unread_many": "Máš %1$d nepřečtených zpráv",
        "messages_header_subtitle_empty": "Smysluplná propojení ✨",
        "notifications_subtitle_new_one": "Máš 1 nové oznámení",
        "notifications_subtitle_new_many": "Máš %d nových oznámení",
        "notifications_subtitle_caught_up": "Máš vše přečtené! ✨",
    },
    "da": {
        "explore_header_subtitle": "Find emner, du elsker, uden støj. ✨",
        "messages_header_unread_one": "Du har 1 ulæst besked",
        "messages_header_unread_many": "Du har %1$d ulæste beskeder",
        "messages_header_subtitle_empty": "Meningsfulde forbindelser ✨",
        "notifications_subtitle_new_one": "Du har 1 ny notifikation",
        "notifications_subtitle_new_many": "Du har %d nye notifikationer",
        "notifications_subtitle_caught_up": "Du er opdateret! ✨",
    },
    "de": {
        "explore_header_subtitle": "Finde Themen, die du liebst, ohne Lärm. ✨",
        "messages_header_unread_one": "Du hast 1 ungelesene Nachricht",
        "messages_header_unread_many": "Du hast %1$d ungelesene Nachrichten",
        "messages_header_subtitle_empty": "Bedeutsame Verbindungen ✨",
        "notifications_subtitle_new_one": "Du hast 1 neue Benachrichtigung",
        "notifications_subtitle_new_many": "Du hast %d neue Benachrichtigungen",
        "notifications_subtitle_caught_up": "Du bist auf dem neuesten Stand! ✨",
    },
    "el": {
        "explore_header_subtitle": "Βρες θέματα που αγαπάς, χωρίς θόρυβο. ✨",
        "messages_header_unread_one": "Έχεις 1 μη αναγνωσμένο μήνυμα",
        "messages_header_unread_many": "Έχεις %1$d μη αναγνωσμένα μηνύματα",
        "messages_header_subtitle_empty": "Ουσιαστικές συνδέσεις ✨",
        "notifications_subtitle_new_one": "Έχεις 1 νέα ειδοποίηση",
        "notifications_subtitle_new_many": "Έχεις %d νέες ειδοποιήσεις",
        "notifications_subtitle_caught_up": "Είσαι ενημερωμένος/η! ✨",
    },
    "es": {
        "explore_header_subtitle": "Descubre temas que te encantan, sin ruido. ✨",
        "messages_header_unread_one": "Tienes 1 mensaje sin leer",
        "messages_header_unread_many": "Tienes %1$d mensajes sin leer",
        "messages_header_subtitle_empty": "Conexiones con sentido ✨",
        "notifications_subtitle_new_one": "Tienes 1 notificación nueva",
        "notifications_subtitle_new_many": "Tienes %d notificaciones nuevas",
        "notifications_subtitle_caught_up": "¡Estás al día! ✨",
    },
    "fi": {
        "explore_header_subtitle": "Löydä aiheet, joista pidät, ilman hälyä. ✨",
        "messages_header_unread_one": "Sinulla on 1 lukematon viesti",
        "messages_header_unread_many": "Sinulla on %1$d lukematonta viestiä",
        "messages_header_subtitle_empty": "Merkityksellisiä yhteyksiä ✨",
        "notifications_subtitle_new_one": "Sinulla on 1 uusi ilmoitus",
        "notifications_subtitle_new_many": "Sinulla on %d uutta ilmoitusta",
        "notifications_subtitle_caught_up": "Olet ajan tasalla! ✨",
    },
    "fr": {
        "explore_header_subtitle": "Trouve les sujets que tu aimes, sans le bruit. ✨",
        "messages_header_unread_one": "Tu as 1 message non lu",
        "messages_header_unread_many": "Tu as %1$d messages non lus",
        "messages_header_subtitle_empty": "Des liens qui comptent ✨",
        "notifications_subtitle_new_one": "Tu as 1 nouvelle notification",
        "notifications_subtitle_new_many": "Tu as %d nouvelles notifications",
        "notifications_subtitle_caught_up": "Tu es à jour ! ✨",
    },
    "hi": {
        "explore_header_subtitle": "बिना शोर-शराबे के अपने पसंदीदा विषय खोजें। ✨",
        "messages_header_unread_one": "आपके पास 1 अपठित संदेश है",
        "messages_header_unread_many": "आपके पास %1$d अपठित संदेश हैं",
        "messages_header_subtitle_empty": "सार्थक संबंध ✨",
        "notifications_subtitle_new_one": "आपके पास 1 नई सूचना है",
        "notifications_subtitle_new_many": "आपके पास %d नई सूचनाएँ हैं",
        "notifications_subtitle_caught_up": "आप सब कुछ देख चुके हैं! ✨",
    },
    "hu": {
        "explore_header_subtitle": "Találd meg a kedvenc témáidat zaj nélkül. ✨",
        "messages_header_unread_one": "1 olvasatlan üzeneted van",
        "messages_header_unread_many": "%1$d olvasatlan üzeneted van",
        "messages_header_subtitle_empty": "Értelmes kapcsolatok ✨",
        "notifications_subtitle_new_one": "1 új értesítésed van",
        "notifications_subtitle_new_many": "%d új értesítésed van",
        "notifications_subtitle_caught_up": "Mindent elolvastál! ✨",
    },
    "in": {  # Indonesian
        "explore_header_subtitle": "Temukan topik yang kamu suka tanpa kebisingan. ✨",
        "messages_header_unread_one": "Kamu punya 1 pesan belum dibaca",
        "messages_header_unread_many": "Kamu punya %1$d pesan belum dibaca",
        "messages_header_subtitle_empty": "Koneksi yang bermakna ✨",
        "notifications_subtitle_new_one": "Kamu punya 1 notifikasi baru",
        "notifications_subtitle_new_many": "Kamu punya %d notifikasi baru",
        "notifications_subtitle_caught_up": "Kamu sudah terkini! ✨",
    },
    "is": {
        "explore_header_subtitle": "Finndu efni sem þér líkar án hávaða. ✨",
        "messages_header_unread_one": "Þú átt 1 ólesin skilaboð",
        "messages_header_unread_many": "Þú átt %1$d ólesin skilaboð",
        "messages_header_subtitle_empty": "Tengsl sem skipta máli ✨",
        "notifications_subtitle_new_one": "Þú átt 1 nýja tilkynningu",
        "notifications_subtitle_new_many": "Þú átt %d nýjar tilkynningar",
        "notifications_subtitle_caught_up": "Þú ert í takt! ✨",
    },
    "it": {
        "explore_header_subtitle": "Trova gli argomenti che ami senza rumore. ✨",
        "messages_header_unread_one": "Hai 1 messaggio non letto",
        "messages_header_unread_many": "Hai %1$d messaggi non letti",
        "messages_header_subtitle_empty": "Connessioni che contano ✨",
        "notifications_subtitle_new_one": "Hai 1 nuova notifica",
        "notifications_subtitle_new_many": "Hai %d nuove notifiche",
        "notifications_subtitle_caught_up": "Sei aggiornato! ✨",
    },
    "iw": {  # Hebrew
        "explore_header_subtitle": "מצא נושאים שאתה אוהב בלי רעש. ✨",
        "messages_header_unread_one": "יש לך הודעה חדשה אחת",
        "messages_header_unread_many": "יש לך %1$d הודעות שלא נקראו",
        "messages_header_subtitle_empty": "חיבורים משמעותיים ✨",
        "notifications_subtitle_new_one": "יש לך התראה חדשה אחת",
        "notifications_subtitle_new_many": "יש לך %d התראות חדשות",
        "notifications_subtitle_caught_up": "התעדכנת בהכול! ✨",
    },
    "ja": {
        "explore_header_subtitle": "雑音なしで、好きなトピックを見つけよう。✨",
        "messages_header_unread_one": "未読メッセージが 1 件あります",
        "messages_header_unread_many": "未読メッセージが %1$d 件あります",
        "messages_header_subtitle_empty": "意味のあるつながり ✨",
        "notifications_subtitle_new_one": "新しい通知が 1 件あります",
        "notifications_subtitle_new_many": "新しい通知が %d 件あります",
        "notifications_subtitle_caught_up": "すべて確認済みです！✨",
    },
    "ko": {
        "explore_header_subtitle": "소음 없이 좋아하는 주제를 찾아보세요. ✨",
        "messages_header_unread_one": "읽지 않은 메시지가 1개 있어요",
        "messages_header_unread_many": "읽지 않은 메시지가 %1$d개 있어요",
        "messages_header_subtitle_empty": "의미 있는 연결 ✨",
        "notifications_subtitle_new_one": "새 알림이 1개 있어요",
        "notifications_subtitle_new_many": "새 알림이 %d개 있어요",
        "notifications_subtitle_caught_up": "모두 확인했어요! ✨",
    },
    "ms": {
        "explore_header_subtitle": "Cari topik yang anda suka tanpa bunyi bising. ✨",
        "messages_header_unread_one": "Anda ada 1 mesej belum dibaca",
        "messages_header_unread_many": "Anda ada %1$d mesej belum dibaca",
        "messages_header_subtitle_empty": "Hubungan yang bermakna ✨",
        "notifications_subtitle_new_one": "Anda ada 1 pemberitahuan baharu",
        "notifications_subtitle_new_many": "Anda ada %d pemberitahuan baharu",
        "notifications_subtitle_caught_up": "Anda telah mengemas kini semuanya! ✨",
    },
    "nb": {
        "explore_header_subtitle": "Finn temaer du elsker, uten støy. ✨",
        "messages_header_unread_one": "Du har 1 ulest melding",
        "messages_header_unread_many": "Du har %1$d uleste meldinger",
        "messages_header_subtitle_empty": "Meningsfulle forbindelser ✨",
        "notifications_subtitle_new_one": "Du har 1 ny varsling",
        "notifications_subtitle_new_many": "Du har %d nye varslinger",
        "notifications_subtitle_caught_up": "Du er oppdatert! ✨",
    },
    "nl": {
        "explore_header_subtitle": "Ontdek onderwerpen die je leuk vindt, zonder ruis. ✨",
        "messages_header_unread_one": "Je hebt 1 ongelezen bericht",
        "messages_header_unread_many": "Je hebt %1$d ongelezen berichten",
        "messages_header_subtitle_empty": "Zinvolle connecties ✨",
        "notifications_subtitle_new_one": "Je hebt 1 nieuwe melding",
        "notifications_subtitle_new_many": "Je hebt %d nieuwe meldingen",
        "notifications_subtitle_caught_up": "Je bent helemaal bij! ✨",
    },
    "pl": {
        "explore_header_subtitle": "Znajdź tematy, które lubisz, bez szumu. ✨",
        "messages_header_unread_one": "Masz 1 nieprzeczytaną wiadomość",
        "messages_header_unread_many": "Masz %1$d nieprzeczytanych wiadomości",
        "messages_header_subtitle_empty": "Znaczące więzi ✨",
        "notifications_subtitle_new_one": "Masz 1 nowe powiadomienie",
        "notifications_subtitle_new_many": "Masz %d nowych powiadomień",
        "notifications_subtitle_caught_up": "Wszystko przeczytane! ✨",
    },
    "pt": {
        "explore_header_subtitle": "Descubra tópicos que você ama, sem o ruído. ✨",
        "messages_header_unread_one": "Você tem 1 mensagem não lida",
        "messages_header_unread_many": "Você tem %1$d mensagens não lidas",
        "messages_header_subtitle_empty": "Conexões com significado ✨",
        "notifications_subtitle_new_one": "Você tem 1 nova notificação",
        "notifications_subtitle_new_many": "Você tem %d novas notificações",
        "notifications_subtitle_caught_up": "Você está em dia! ✨",
    },
    "ro": {
        "explore_header_subtitle": "Găsește subiectele care îți plac, fără zgomot. ✨",
        "messages_header_unread_one": "Ai 1 mesaj necitit",
        "messages_header_unread_many": "Ai %1$d mesaje necitite",
        "messages_header_subtitle_empty": "Conexiuni pline de sens ✨",
        "notifications_subtitle_new_one": "Ai 1 notificare nouă",
        "notifications_subtitle_new_many": "Ai %d notificări noi",
        "notifications_subtitle_caught_up": "Ești la zi! ✨",
    },
    "ru": {
        "explore_header_subtitle": "Находи темы, которые тебе нравятся, без шума. ✨",
        "messages_header_unread_one": "У тебя 1 непрочитанное сообщение",
        "messages_header_unread_many": "У тебя %1$d непрочитанных сообщений",
        "messages_header_subtitle_empty": "Осмысленные связи ✨",
        "notifications_subtitle_new_one": "У тебя 1 новое уведомление",
        "notifications_subtitle_new_many": "У тебя %d новых уведомлений",
        "notifications_subtitle_caught_up": "Ты всё прочитал(а)! ✨",
    },
    "sv": {
        "explore_header_subtitle": "Hitta ämnen du älskar utan brus. ✨",
        "messages_header_unread_one": "Du har 1 oläst meddelande",
        "messages_header_unread_many": "Du har %1$d olästa meddelanden",
        "messages_header_subtitle_empty": "Meningsfulla kontakter ✨",
        "notifications_subtitle_new_one": "Du har 1 ny avisering",
        "notifications_subtitle_new_many": "Du har %d nya aviseringar",
        "notifications_subtitle_caught_up": "Du är uppdaterad! ✨",
    },
    "th": {
        "explore_header_subtitle": "ค้นหาหัวข้อที่คุณชื่นชอบโดยไม่มีเสียงรบกวน ✨",
        "messages_header_unread_one": "คุณมีข้อความที่ยังไม่ได้อ่าน 1 ข้อความ",
        "messages_header_unread_many": "คุณมีข้อความที่ยังไม่ได้อ่าน %1$d ข้อความ",
        "messages_header_subtitle_empty": "การเชื่อมต่อที่มีความหมาย ✨",
        "notifications_subtitle_new_one": "คุณมีการแจ้งเตือนใหม่ 1 รายการ",
        "notifications_subtitle_new_many": "คุณมีการแจ้งเตือนใหม่ %d รายการ",
        "notifications_subtitle_caught_up": "คุณอ่านครบแล้ว! ✨",
    },
    "tr": {
        "explore_header_subtitle": "Gürültüye kapılmadan sevdiğin konuları keşfet. ✨",
        "messages_header_unread_one": "1 okunmamış mesajın var",
        "messages_header_unread_many": "%1$d okunmamış mesajın var",
        "messages_header_subtitle_empty": "Anlamlı bağlantılar ✨",
        "notifications_subtitle_new_one": "1 yeni bildirimin var",
        "notifications_subtitle_new_many": "%d yeni bildirimin var",
        "notifications_subtitle_caught_up": "Her şeyi gördün! ✨",
    },
    "uk": {
        "explore_header_subtitle": "Знаходь теми, які тобі до душі, без шуму. ✨",
        "messages_header_unread_one": "У тебе 1 непрочитане повідомлення",
        "messages_header_unread_many": "У тебе %1$d непрочитаних повідомлень",
        "messages_header_subtitle_empty": "Змістовні звʼязки ✨",
        "notifications_subtitle_new_one": "У тебе 1 нове сповіщення",
        "notifications_subtitle_new_many": "У тебе %d нових сповіщень",
        "notifications_subtitle_caught_up": "Ти все переглянув(ла)! ✨",
    },
    "ur": {
        "explore_header_subtitle": "شور کے بغیر اپنی پسند کے موضوعات ڈھونڈیں۔ ✨",
        "messages_header_unread_one": "آپ کے پاس 1 غیر پڑھا پیغام ہے",
        "messages_header_unread_many": "آپ کے پاس %1$d غیر پڑھے پیغامات ہیں",
        "messages_header_subtitle_empty": "بامعنی روابط ✨",
        "notifications_subtitle_new_one": "آپ کے پاس 1 نئی اطلاع ہے",
        "notifications_subtitle_new_many": "آپ کے پاس %d نئی اطلاعات ہیں",
        "notifications_subtitle_caught_up": "آپ نے سب کچھ دیکھ لیا! ✨",
    },
    "vi": {
        "explore_header_subtitle": "Khám phá những chủ đề bạn yêu thích mà không bị nhiễu. ✨",
        "messages_header_unread_one": "Bạn có 1 tin nhắn chưa đọc",
        "messages_header_unread_many": "Bạn có %1$d tin nhắn chưa đọc",
        "messages_header_subtitle_empty": "Những kết nối ý nghĩa ✨",
        "notifications_subtitle_new_one": "Bạn có 1 thông báo mới",
        "notifications_subtitle_new_many": "Bạn có %d thông báo mới",
        "notifications_subtitle_caught_up": "Bạn đã xem hết! ✨",
    },
    "zh": {
        "explore_header_subtitle": "找到你喜爱的话题，没有杂音。✨",
        "messages_header_unread_one": "你有 1 条未读消息",
        "messages_header_unread_many": "你有 %1$d 条未读消息",
        "messages_header_subtitle_empty": "有意义的连接 ✨",
        "notifications_subtitle_new_one": "你有 1 条新通知",
        "notifications_subtitle_new_many": "你有 %d 条新通知",
        "notifications_subtitle_caught_up": "全部看完啦！✨",
    },
}

# English variants — keep the English copy verbatim from values/strings.xml.
EN_COPY = {
    "explore_header_subtitle": "Find topics you love without the noise. ✨",
    "messages_header_unread_one": "You have 1 unread message",
    "messages_header_unread_many": "You have %1$d unread messages",
    "messages_header_subtitle_empty": "Meaningful connections ✨",
    "notifications_subtitle_new_one": "You have 1 new notification",
    "notifications_subtitle_new_many": "You have %d new notifications",
    "notifications_subtitle_caught_up": "You're all caught up! ✨",
}
for loc in ("en-rAU", "en-rCA", "en-rGB"):
    TRANSLATIONS[loc] = EN_COPY


def xml_escape(value: str) -> str:
    # Android string-resource escaping: apostrophe must be backslash-escaped.
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

        # Replace existing entry in-place if present; otherwise insert before </resources>.
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
    print("Injecting header subtitle translations…")
    for locale, entries in TRANSLATIONS.items():
        inject(locale, entries)
    print("Done.")


if __name__ == "__main__":
    main()

