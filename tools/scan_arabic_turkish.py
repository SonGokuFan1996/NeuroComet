"""Look for Turkish words in Arabic strings.xml."""
import re
from pathlib import Path

AR = Path(__file__).resolve().parents[1] / "app/src/main/res/values-ar/strings.xml"
content = AR.read_text(encoding="utf-8")

# Common Turkish words (also covering case where diacritics may be stripped)
turkish_words = [
    "Ayarlar", "Bildirim", "Bildirimler", "Mesaj", "Mesajlar", "Profil",
    "Giris", "Giriş", "Cikis", "Çıkış", "Kaydet", "Gonder", "Gönder",
    "Yukle", "Yükle", "Tamam", "Iptal", "İptal", "Merhaba", "Hos", "Hoş",
    "Basla", "Başla", "Devam", "Paylas", "Paylaş", "Yorum", "Begen", "Beğen",
    "Takip", "Sifre", "Şifre", "Kullanici", "Kullanıcı", "Hesap", "Anasayfa",
    "Ana sayfa", "Geri", "Ileri", "İleri", "Yeni", "Duzenle", "Düzenle",
    "Sil", "Ekle", "Kapat", "Arama", "Ara", "Secim", "Seçim", "Sec", "Seç",
    "Görüntüle", "Goruntule", "Yardim", "Yardım",
]
pattern = re.compile(r"\b(" + "|".join(re.escape(w) for w in turkish_words) + r")\b")

found = []
for m in re.finditer(r'<string\s+name="([^"]+)"[^>]*>(.*?)</string>', content, flags=re.DOTALL):
    name, val = m.group(1), m.group(2)
    hits = pattern.findall(val)
    if hits:
        found.append((name, val[:150], hits))

for name, val, hits in found:
    print(f"{name}: {val!r}  turk={hits}")
print(f"Total: {len(found)}")

