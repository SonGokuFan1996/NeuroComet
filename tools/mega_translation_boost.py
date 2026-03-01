#!/usr/bin/env python3
"""
Mega Translation Boost Script.
Provides high-quality translations for the most common untranslated UI strings across all languages.
"""

import os
import re
from pathlib import Path

RES_DIR = Path(r"C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res")

# High-priority UI strings that were found missing in many languages
BOOST = {
    "iw": { # Hebrew
        "create_story_share_button": "שתף סטורי",
        "story_swipe_to_close": "החלק למטה לסגירה",
        "story_release_to_close": "שחרר לסגירה",
        "post_save": "שמור פוסט",
        "post_unsave": "הסר מהשמורים",
        "post_share": "שתף",
        "dm_search_conversations_placeholder": "חפש הודעות",
        "tone_neutral": "ניטרלי",
        "status_online": "מחובר",
        "status_offline": "לא מחובר",
        "loading": "טוען...",
        "action_save": "שמור",
        "action_cancel": "ביטול",
        "action_delete": "מחק",
        "create_post_share_thoughts": "שתף את המחשבות שלך עם הקהילה...",
        "create_post_how_feeling": "איך את/ה מרגיש/ה?",
        "create_post_add_media": "הוסף מדיה",
        "create_post_close": "סגור",
        "create_post_content_warning": "אזהרת תוכן",
        "comments_no_comments_yet": "אין תגובות עדיין",
        "comments_send_comment": "שלח תגובה",
        "community_join": "הצטרף",
        "community_leave": "עזוב",
        "community_members": "%d חברים",
        "community_posts": "%d פוסטים",
    },
    "nb": { # Norwegian
        "create_story_share_button": "Del historie",
        "story_swipe_to_close": "Sveip ned for å lukke",
        "story_release_to_close": "Slipp for å lukke",
        "post_save": "Lagre innlegg",
        "post_unsave": "Fjern fra lagrede",
        "post_share": "Del",
        "dm_search_conversations_placeholder": "Søk i samtaler",
        "tone_neutral": "Nøytral",
        "status_online": "Online",
        "status_offline": "Frakoblet",
        "loading": "Laster...",
        "action_save": "Lagre",
        "action_cancel": "Avbryt",
        "action_delete": "Slett",
        "create_post_share_thoughts": "Del dine tanker med fellesskapet...",
        "create_post_how_feeling": "Hvordan føler du deg?",
        "create_post_add_media": "Legg til media",
        "create_post_close": "Lukk",
        "create_post_content_warning": "Innholdsadvarsel",
        "comments_no_comments_yet": "Ingen kommentarer ennå",
        "comments_send_comment": "Send kommentar",
        "community_join": "Bli med",
        "community_leave": "Forlat",
        "community_members": "%d medlemmer",
        "community_posts": "%d innlegg",
    },
    "in": { # Indonesian
        "create_story_share_button": "Bagikan Cerita",
        "story_swipe_to_close": "Geser ke bawah untuk menutup",
        "story_release_to_close": "Lepaskan untuk menutup",
        "post_save": "Simpan Postingan",
        "post_unsave": "Hapus dari Simpanan",
        "post_share": "Bagikan",
        "dm_search_conversations_placeholder": "Cari percakapan",
        "tone_neutral": "Netral",
        "status_online": "Online",
        "status_offline": "Offline",
        "loading": "Memuat...",
        "action_save": "Simpan",
        "action_cancel": "Batal",
        "action_delete": "Hapus",
    },
    "es": { # Spanish
        "create_story_share_button": "Compartir historia",
        "story_swipe_to_close": "Desliza hacia abajo para cerrar",
        "story_release_to_close": "Suelta para cerrar",
        "post_save": "Guardar publicación",
        "post_unsave": "Eliminar de guardados",
        "post_share": "Compartir",
        "dm_search_conversations_placeholder": "Buscar conversaciones",
        "tone_neutral": "Neutral",
        "status_online": "En línea",
        "status_offline": "Desconectado",
        "loading": "Cargando...",
        "action_save": "Guardar",
        "action_cancel": "Cancelar",
        "action_delete": "Eliminar",
    },
    "fr": { # French
        "create_story_share_button": "Partager la story",
        "story_swipe_to_close": "Balayer vers le bas pour fermer",
        "story_release_to_close": "Relâcher pour fermer",
        "post_save": "Enregistrer le post",
        "post_unsave": "Retirer des favoris",
        "post_share": "Partager",
        "dm_search_conversations_placeholder": "Rechercher des conversations",
        "tone_neutral": "Neutre",
        "status_online": "En ligne",
        "status_offline": "Hors ligne",
        "loading": "Chargement...",
        "action_save": "Enregistrer",
        "action_cancel": "Annuler",
        "action_delete": "Supprimer",
    },
    "de": { # German
        "create_story_share_button": "Story teilen",
        "story_swipe_to_close": "Zum Schließen nach unten wischen",
        "story_release_to_close": "Zum Schließen loslassen",
        "post_save": "Beitrag speichern",
        "post_unsave": "Aus Gespeichert entfernen",
        "post_share": "Teilen",
        "dm_search_conversations_placeholder": "Gespräche suchen",
        "tone_neutral": "Neutral",
        "status_online": "Online",
        "status_offline": "Offline",
        "loading": "Wird geladen...",
        "action_save": "Speichern",
        "action_cancel": "Abbrechen",
        "action_delete": "Löschen",
    },
    "ar": { # Arabic
        "create_story_share_button": "مشاركة القصة",
        "story_swipe_to_close": "اسحب للأسفل للإغلاق",
        "story_release_to_close": "اترك للإغلاق",
        "post_save": "حفظ المنشور",
        "post_unsave": "إزالة من المحفوظات",
        "post_share": "مشاركة",
        "dm_search_conversations_placeholder": "البحث في المحادثات",
        "tone_neutral": "محايد",
        "status_online": "متصل",
        "status_offline": "غير متصل",
        "loading": "جارٍ التحميل...",
        "action_save": "حفظ",
        "action_cancel": "إلغاء",
        "action_delete": "حذف",
    },
    "it": { # Italian
        "create_story_share_button": "Condividi storia",
        "story_swipe_to_close": "Scorri verso il basso per chiudere",
        "story_release_to_close": "Rilascia per chiudere",
        "post_save": "Salva post",
        "post_unsave": "Rimuovi dai salvati",
        "post_share": "Condividi",
        "dm_search_conversations_placeholder": "Cerca conversazioni",
        "tone_neutral": "Neutrale",
        "status_online": "Online",
        "status_offline": "Offline",
        "loading": "Caricamento...",
        "action_save": "Salva",
        "action_cancel": "Annulla",
        "action_delete": "Elimina",
    },
    "pt": { # Portuguese
        "create_story_share_button": "Compartilhar história",
        "story_swipe_to_close": "Deslize para baixo para fechar",
        "story_release_to_close": "Solte para fechar",
        "post_save": "Salvar postagem",
        "post_unsave": "Remover dos salvos",
        "post_share": "Compartilhar",
        "dm_search_conversations_placeholder": "Pesquisar conversas",
        "tone_neutral": "Neutro",
        "status_online": "Online",
        "status_offline": "Offline",
        "loading": "Carregando...",
        "action_save": "Salvar",
        "action_cancel": "Cancelar",
        "action_delete": "Excluir",
    },
    "ru": { # Russian
        "create_story_share_button": "Поделиться историей",
        "story_swipe_to_close": "Проведите вниз, чтобы закрыть",
        "story_release_to_close": "Отпустите, чтобы закрыть",
        "post_save": "Сохранить пост",
        "post_unsave": "Удалить из сохраненного",
        "post_share": "Поделиться",
        "dm_search_conversations_placeholder": "Поиск диалогов",
        "tone_neutral": "Нейтральный",
        "status_online": "В сети",
        "status_offline": "Не в сети",
        "loading": "Загрузка...",
        "action_save": "Сохранить",
        "action_cancel": "Отмена",
        "action_delete": "Удалить",
    },
    "ja": { # Japanese
        "create_story_share_button": "ストーリーを共有",
        "story_swipe_to_close": "下にスワイプして閉じる",
        "story_release_to_close": "離して閉じる",
        "post_save": "投稿を保存",
        "post_unsave": "保存済みから削除",
        "post_share": "共有",
        "dm_search_conversations_placeholder": "会話を検索",
        "tone_neutral": "ニュートラル",
        "status_online": "オンライン",
        "status_offline": "オフライン",
        "loading": "読み込み中...",
        "action_save": "保存",
        "action_cancel": "キャンセル",
        "action_delete": "削除",
    },
    "ko": { # Korean
        "create_story_share_button": "스토리 공유",
        "story_swipe_to_close": "아래로 밀어서 닫기",
        "story_release_to_close": "놓아서 닫기",
        "post_save": "게시물 저장",
        "post_unsave": "저장됨에서 제거",
        "post_share": "공유",
        "dm_search_conversations_placeholder": "대화 검색",
        "tone_neutral": "중립",
        "status_online": "온라인",
        "status_offline": "오프라인",
        "loading": "로딩 중...",
        "action_save": "저장",
        "action_cancel": "취소",
        "action_delete": "삭제",
    },
    "zh": { # Chinese
        "create_story_share_button": "分享故事",
        "story_swipe_to_close": "下滑关闭",
        "story_release_to_close": "松开关闭",
        "post_save": "保存帖子",
        "post_unsave": "取消保存",
        "post_share": "分享",
        "dm_search_conversations_placeholder": "搜索对话",
        "tone_neutral": "中性",
        "status_online": "在线",
        "status_offline": "离线",
        "loading": "加载中...",
        "action_save": "保存",
        "action_cancel": "取消",
        "action_delete": "删除",
    },
    "tr": { # Turkish
        "create_story_share_button": "Hikayeyi Paylaş",
        "story_swipe_to_close": "Kapatmak için aşağı kaydır",
        "story_release_to_close": "Kapatmak için bırak",
        "post_save": "Gönderiyi Kaydet",
        "post_unsave": "Kaydedilenlerden Kaldır",
        "post_share": "Paylaş",
        "dm_search_conversations_placeholder": "Sohbetlerde ara",
        "tone_neutral": "Nötr",
        "status_online": "Çevrimiçi",
        "status_offline": "Çevrimdışı",
        "loading": "Yükleniyor...",
        "action_save": "Kaydet",
        "action_cancel": "İptal",
        "action_delete": "Sil",
    }
}

def escape(text):
    res = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    if "'" in res and "\\'" not in res:
        res = res.replace("'", "\\'")
    return res

def apply_boost():
    updated_total = 0
    for lang, translations in BOOST.items():
        xml_file = RES_DIR / f"values-{lang}" / "strings.xml"
        if not xml_file.exists(): continue

        content = xml_file.read_text(encoding='utf-8')
        updated_in_file = 0

        for key, val in translations.items():
            # Match any value (force update if key exists)
            pattern = rf'(<string\s+name="{re.escape(key)}"[^>]*>)([^<]*)(</string>)'

            def replacer(match):
                nonlocal updated_in_file
                new_val = escape(val)
                if match.group(2) != new_val:
                    updated_in_file += 1
                    return f"{match.group(1)}{new_val}{match.group(3)}"
                return match.group(0)

            content = re.sub(pattern, replacer, content)

        if updated_in_file > 0:
            xml_file.write_text(content, encoding='utf-8')
            print(f"v {lang}: Boosted {updated_in_file} strings")
            updated_total += updated_in_file

    print(f"\nBoost complete! Total {updated_total} strings updated.")

if __name__ == "__main__":
    apply_boost()

