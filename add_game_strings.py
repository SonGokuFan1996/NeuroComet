#!/usr/bin/env python3
"""Add missing game localization strings to all locales."""
import sys

filepath = 'flutter_app/lib/l10n/app_localizations.dart'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# New game strings per locale
strings_by_locale = {
    'es': [
        ("gamePatternTapDesc", "Pon a prueba tu memoria con patrones"),
        ("gameWorryJar", "Frasco de Preocupaciones"),
        ("gameWorryJarDesc", "Captura preocupaciones y d\u00e9jalas ir"),
        ("gameMoodMixer", "Mezclador de \u00c1nimo"),
        ("gameMoodMixerDesc", "Mezcla colores seg\u00fan tu estado de \u00e1nimo"),
        ("gameSafeSpace", "Espacio Seguro"),
        ("gameSafeSpaceDesc", "Construye tu zona de calma personal"),
        ("gameTextureTiles", "Azulejos de Textura"),
        ("gameTextureTilesDesc", "Explora texturas satisfactorias"),
        ("gameSoundGarden", "Jard\u00edn de Sonidos"),
        ("gameSoundGardenDesc", "Crea paisajes sonoros relajantes"),
        ("gameStimSequencer", "Secuenciador Stim"),
        ("gameStimSequencerDesc", "Crea patrones r\u00edtmicos de estimulaci\u00f3n"),
        ("gameConstellationConnect", "Conecta Constelaciones"),
        ("gameConstellationConnectDesc", "Conecta estrellas en patrones"),
    ],
    'de': [
        ("gamePatternTapDesc", "Teste dein Ged\u00e4chtnis mit Mustern"),
        ("gameWorryJar", "Sorgenglas"),
        ("gameWorryJarDesc", "Fange Sorgen ein und lass sie los"),
        ("gameMoodMixer", "Stimmungsmixer"),
        ("gameMoodMixerDesc", "Mische Farben passend zu deiner Stimmung"),
        ("gameSafeSpace", "Sicherer Ort"),
        ("gameSafeSpaceDesc", "Baue deine pers\u00f6nliche Ruhezone"),
        ("gameTextureTiles", "Texturkacheln"),
        ("gameTextureTilesDesc", "Erkunde befriedigende Texturen"),
        ("gameSoundGarden", "Klangarten"),
        ("gameSoundGardenDesc", "Erstelle beruhigende Klanglandschaften"),
        ("gameStimSequencer", "Stim-Sequenzer"),
        ("gameStimSequencerDesc", "Erstelle rhythmische Stim-Muster"),
        ("gameConstellationConnect", "Sternbilder Verbinden"),
        ("gameConstellationConnectDesc", "Verbinde Sterne zu Mustern"),
    ],
    'fr': [
        ("gamePatternTapDesc", "Testez votre m\u00e9moire avec des motifs"),
        ("gameWorryJar", "Bocal \u00e0 Soucis"),
        ("gameWorryJarDesc", "Capturez vos soucis et laissez-les partir"),
        ("gameMoodMixer", "M\u00e9langeur d'Humeur"),
        ("gameMoodMixerDesc", "M\u00e9langez les couleurs selon votre humeur"),
        ("gameSafeSpace", "Espace S\u00fbr"),
        ("gameSafeSpaceDesc", "Construisez votre zone de calme personnelle"),
        ("gameTextureTiles", "Tuiles de Texture"),
        ("gameTextureTilesDesc", "Explorez des textures satisfaisantes"),
        ("gameSoundGarden", "Jardin Sonore"),
        ("gameSoundGardenDesc", "Cr\u00e9ez des paysages sonores apaisants"),
        ("gameStimSequencer", "S\u00e9quenceur Stim"),
        ("gameStimSequencerDesc", "Cr\u00e9ez des motifs rythmiques de stimulation"),
        ("gameConstellationConnect", "Relier les Constellations"),
        ("gameConstellationConnectDesc", "Reliez les \u00e9toiles en motifs"),
    ],
    'pt': [
        ("gamePatternTapDesc", "Teste sua mem\u00f3ria com padr\u00f5es"),
        ("gameWorryJar", "Pote de Preocupa\u00e7\u00f5es"),
        ("gameWorryJarDesc", "Capture preocupa\u00e7\u00f5es e deixe-as ir"),
        ("gameMoodMixer", "Misturador de Humor"),
        ("gameMoodMixerDesc", "Misture cores para combinar com seu humor"),
        ("gameSafeSpace", "Espa\u00e7o Seguro"),
        ("gameSafeSpaceDesc", "Construa sua zona de calma pessoal"),
        ("gameTextureTiles", "Azulejos de Textura"),
        ("gameTextureTilesDesc", "Explore texturas satisfat\u00f3rias"),
        ("gameSoundGarden", "Jardim de Sons"),
        ("gameSoundGardenDesc", "Crie paisagens sonoras relaxantes"),
        ("gameStimSequencer", "Sequenciador Stim"),
        ("gameStimSequencerDesc", "Crie padr\u00f5es r\u00edtmicos de estimula\u00e7\u00e3o"),
        ("gameConstellationConnect", "Conectar Constela\u00e7\u00f5es"),
        ("gameConstellationConnectDesc", "Conecte estrelas em padr\u00f5es"),
    ],
    'it': [
        ("gamePatternTapDesc", "Metti alla prova la memoria con i pattern"),
        ("gameWorryJar", "Barattolo delle Preoccupazioni"),
        ("gameWorryJarDesc", "Cattura le preoccupazioni e lasciale andare"),
        ("gameMoodMixer", "Miscelatore di Umore"),
        ("gameMoodMixerDesc", "Mescola i colori in base al tuo umore"),
        ("gameSafeSpace", "Spazio Sicuro"),
        ("gameSafeSpaceDesc", "Costruisci la tua zona di calma personale"),
        ("gameTextureTiles", "Tessere di Texture"),
        ("gameTextureTilesDesc", "Esplora texture soddisfacenti"),
        ("gameSoundGarden", "Giardino Sonoro"),
        ("gameSoundGardenDesc", "Crea paesaggi sonori rilassanti"),
        ("gameStimSequencer", "Sequenziatore Stim"),
        ("gameStimSequencerDesc", "Crea pattern ritmici di stimolazione"),
        ("gameConstellationConnect", "Collega Costellazioni"),
        ("gameConstellationConnectDesc", "Collega le stelle in pattern"),
    ],
    'nl': [
        ("gamePatternTapDesc", "Test je geheugen met patronen"),
        ("gameWorryJar", "Zorgenpotje"),
        ("gameWorryJarDesc", "Vang zorgen en laat ze los"),
        ("gameMoodMixer", "Stemmingsmixer"),
        ("gameMoodMixerDesc", "Meng kleuren passend bij je stemming"),
        ("gameSafeSpace", "Veilige Plek"),
        ("gameSafeSpaceDesc", "Bouw je persoonlijke rustzone"),
        ("gameTextureTiles", "Textuurtegels"),
        ("gameTextureTilesDesc", "Verken bevredigende texturen"),
        ("gameSoundGarden", "Geluidstuin"),
        ("gameSoundGardenDesc", "Cre\u00eber rustgevende geluidslandschappen"),
        ("gameStimSequencer", "Stim-Sequencer"),
        ("gameStimSequencerDesc", "Maak ritmische stim-patronen"),
        ("gameConstellationConnect", "Sterrenbeelden Verbinden"),
        ("gameConstellationConnectDesc", "Verbind sterren tot patronen"),
    ],
    'pl': [
        ("gamePatternTapDesc", "Przetestuj pami\u0119\u0107 wzorami"),
        ("gameWorryJar", "S\u0142oik na Troski"),
        ("gameWorryJarDesc", "Z\u0142ap troski i pozw\u00f3l im odej\u015b\u0107"),
        ("gameMoodMixer", "Mikser Nastroju"),
        ("gameMoodMixerDesc", "Mieszaj kolory dopasowane do nastroju"),
        ("gameSafeSpace", "Bezpieczna Przestrze\u0144"),
        ("gameSafeSpaceDesc", "Zbuduj swoj\u0105 osobist\u0105 stref\u0119 spokoju"),
        ("gameTextureTiles", "Kafelki Tekstur"),
        ("gameTextureTilesDesc", "Odkrywaj satysfakcjonuj\u0105ce tekstury"),
        ("gameSoundGarden", "Ogr\u00f3d D\u017awi\u0119k\u00f3w"),
        ("gameSoundGardenDesc", "Tw\u00f3rz uspokajaj\u0105ce pejza\u017ce d\u017awi\u0119kowe"),
        ("gameStimSequencer", "Sekwencer Stim"),
        ("gameStimSequencerDesc", "Tw\u00f3rz rytmiczne wzorce stymulacji"),
        ("gameConstellationConnect", "\u0141\u0105czenie Gwiazdozbior\u00f3w"),
        ("gameConstellationConnectDesc", "\u0141\u0105cz gwiazdy we wzory"),
    ],
    'ru': [
        ("gamePatternTapDesc", "\u041f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u043f\u0430\u043c\u044f\u0442\u044c \u0441 \u0443\u0437\u043e\u0440\u0430\u043c\u0438"),
        ("gameWorryJar", "\u0411\u0430\u043d\u043a\u0430 \u0417\u0430\u0431\u043e\u0442"),
        ("gameWorryJarDesc", "\u041f\u043e\u0439\u043c\u0430\u0439\u0442\u0435 \u0437\u0430\u0431\u043e\u0442\u044b \u0438 \u043e\u0442\u043f\u0443\u0441\u0442\u0438\u0442\u0435 \u0438\u0445"),
        ("gameMoodMixer", "\u041c\u0438\u043a\u0448\u0435\u0440 \u041d\u0430\u0441\u0442\u0440\u043e\u0435\u043d\u0438\u044f"),
        ("gameMoodMixerDesc", "\u0421\u043c\u0435\u0448\u0438\u0432\u0430\u0439\u0442\u0435 \u0446\u0432\u0435\u0442\u0430 \u043f\u043e\u0434 \u043d\u0430\u0441\u0442\u0440\u043e\u0435\u043d\u0438\u0435"),
        ("gameSafeSpace", "\u0411\u0435\u0437\u043e\u043f\u0430\u0441\u043d\u043e\u0435 \u041c\u0435\u0441\u0442\u043e"),
        ("gameSafeSpaceDesc", "\u0421\u043e\u0437\u0434\u0430\u0439\u0442\u0435 \u043b\u0438\u0447\u043d\u0443\u044e \u0437\u043e\u043d\u0443 \u0441\u043f\u043e\u043a\u043e\u0439\u0441\u0442\u0432\u0438\u044f"),
        ("gameTextureTiles", "\u0422\u0435\u043a\u0441\u0442\u0443\u0440\u043d\u044b\u0435 \u041f\u043b\u0438\u0442\u043a\u0438"),
        ("gameTextureTilesDesc", "\u0418\u0441\u0441\u043b\u0435\u0434\u0443\u0439\u0442\u0435 \u043f\u0440\u0438\u044f\u0442\u043d\u044b\u0435 \u0442\u0435\u043a\u0441\u0442\u0443\u0440\u044b"),
        ("gameSoundGarden", "\u0417\u0432\u0443\u043a\u043e\u0432\u043e\u0439 \u0421\u0430\u0434"),
        ("gameSoundGardenDesc", "\u0421\u043e\u0437\u0434\u0430\u0432\u0430\u0439\u0442\u0435 \u0443\u0441\u043f\u043e\u043a\u0430\u0438\u0432\u0430\u044e\u0449\u0438\u0435 \u0437\u0432\u0443\u043a\u043e\u0432\u044b\u0435 \u043f\u0435\u0439\u0437\u0430\u0436\u0438"),
        ("gameStimSequencer", "\u0421\u0442\u0438\u043c-\u0421\u0435\u043a\u0432\u0435\u043d\u0441\u0435\u0440"),
        ("gameStimSequencerDesc", "\u0421\u043e\u0437\u0434\u0430\u0432\u0430\u0439\u0442\u0435 \u0440\u0438\u0442\u043c\u0438\u0447\u043d\u044b\u0435 \u0441\u0442\u0438\u043c-\u043f\u0430\u0442\u0442\u0435\u0440\u043d\u044b"),
        ("gameConstellationConnect", "\u0421\u043e\u0435\u0434\u0438\u043d\u0438\u0442\u044c \u0421\u043e\u0437\u0432\u0435\u0437\u0434\u0438\u044f"),
        ("gameConstellationConnectDesc", "\u0421\u043e\u0435\u0434\u0438\u043d\u044f\u0439\u0442\u0435 \u0437\u0432\u0451\u0437\u0434\u044b \u0432 \u0443\u0437\u043e\u0440\u044b"),
    ],
    'ja': [
        ("gamePatternTapDesc", "\u30d1\u30bf\u30fc\u30f3\u3067\u8a18\u61b6\u529b\u3092\u30c6\u30b9\u30c8"),
        ("gameWorryJar", "\u5fc3\u914d\u30d3\u30f3"),
        ("gameWorryJarDesc", "\u5fc3\u914d\u3092\u6355\u307e\u3048\u3066\u624b\u653e\u305d\u3046"),
        ("gameMoodMixer", "\u30e0\u30fc\u30c9\u30df\u30ad\u30b5\u30fc"),
        ("gameMoodMixerDesc", "\u6c17\u5206\u306b\u5408\u308f\u305b\u3066\u8272\u3092\u6df7\u305c\u3088\u3046"),
        ("gameSafeSpace", "\u30bb\u30fc\u30d5\u30b9\u30da\u30fc\u30b9"),
        ("gameSafeSpaceDesc", "\u81ea\u5206\u3060\u3051\u306e\u5b89\u3089\u304e\u306e\u5834\u6240\u3092\u4f5c\u308d\u3046"),
        ("gameTextureTiles", "\u30c6\u30af\u30b9\u30c1\u30e3\u30bf\u30a4\u30eb"),
        ("gameTextureTilesDesc", "\u5fc3\u5730\u3088\u3044\u30c6\u30af\u30b9\u30c1\u30e3\u3092\u63a2\u7d22"),
        ("gameSoundGarden", "\u30b5\u30a6\u30f3\u30c9\u30ac\u30fc\u30c7\u30f3"),
        ("gameSoundGardenDesc", "\u7652\u3057\u306e\u30b5\u30a6\u30f3\u30c9\u30b9\u30b1\u30fc\u30d7\u3092\u4f5c\u6210"),
        ("gameStimSequencer", "\u30b9\u30c6\u30a3\u30e0\u30b7\u30fc\u30b1\u30f3\u30b5\u30fc"),
        ("gameStimSequencerDesc", "\u30ea\u30ba\u30df\u30ab\u30eb\u306a\u30b9\u30c6\u30a3\u30e0\u30d1\u30bf\u30fc\u30f3\u3092\u4f5c\u6210"),
        ("gameConstellationConnect", "\u661f\u5ea7\u3064\u306a\u304e"),
        ("gameConstellationConnectDesc", "\u661f\u3092\u3064\u306a\u3044\u3067\u30d1\u30bf\u30fc\u30f3\u3092\u4f5c\u308d\u3046"),
    ],
    'ko': [
        ("gamePatternTapDesc", "\ud328\ud134\uc73c\ub85c \uae30\uc5b5\ub825 \ud14c\uc2a4\ud2b8"),
        ("gameWorryJar", "\uac71\uc815 \ud56d\uc544\ub9ac"),
        ("gameWorryJarDesc", "\uac71\uc815\uc744 \ub2f4\uace0 \ub193\uc544\uc8fc\uc138\uc694"),
        ("gameMoodMixer", "\ubb34\ub4dc \ubbf9\uc11c"),
        ("gameMoodMixerDesc", "\uae30\ubd84\uc5d0 \ub9de\uac8c \uc0c9\uc0c1\uc744 \uc11e\uc5b4\ubcf4\uc138\uc694"),
        ("gameSafeSpace", "\uc548\uc804\ud55c \uacf5\uac04"),
        ("gameSafeSpaceDesc", "\ub098\ub9cc\uc758 \ud3c9\ud654\ub85c\uc6b4 \uacf5\uac04\uc744 \ub9cc\ub4dc\uc138\uc694"),
        ("gameTextureTiles", "\ud14d\uc2a4\ucc98 \ud0c0\uc77c"),
        ("gameTextureTilesDesc", "\ub9cc\uc871\uc2a4\ub7ec\uc6b4 \ud14d\uc2a4\ucc98\ub97c \ud0d0\ud5d8\ud558\uc138\uc694"),
        ("gameSoundGarden", "\uc18c\ub9ac \uc815\uc6d0"),
        ("gameSoundGardenDesc", "\ud3b8\uc548\ud55c \uc0ac\uc6b4\ub4dc\uc2a4\ucf00\uc774\ud504\ub97c \ub9cc\ub4dc\uc138\uc694"),
        ("gameStimSequencer", "\uc2a4\ud300 \uc2dc\ud000\uc11c"),
        ("gameStimSequencerDesc", "\ub9ac\ub4dc\ubbf8\uceec\ud55c \uc2a4\ud300 \ud328\ud134\uc744 \ub9cc\ub4dc\uc138\uc694"),
        ("gameConstellationConnect", "\ubcc4\uc790\ub9ac \uc5f0\uacb0"),
        ("gameConstellationConnectDesc", "\ubcc4\uc744 \uc5f0\uacb0\ud558\uc5ec \ud328\ud134\uc744 \ub9cc\ub4dc\uc138\uc694"),
    ],
    'zh': [
        ("gamePatternTapDesc", "\u7528\u56fe\u6848\u6d4b\u8bd5\u8bb0\u5fc6\u529b"),
        ("gameWorryJar", "\u70e6\u607c\u7f50"),
        ("gameWorryJarDesc", "\u6355\u6349\u70e6\u607c\u5e76\u653e\u624b"),
        ("gameMoodMixer", "\u60c5\u7eea\u8c03\u8272\u677f"),
        ("gameMoodMixerDesc", "\u6df7\u5408\u989c\u8272\u6765\u5339\u914d\u4f60\u7684\u5fc3\u60c5"),
        ("gameSafeSpace", "\u5b89\u5168\u7a7a\u95f4"),
        ("gameSafeSpaceDesc", "\u5efa\u9020\u4f60\u7684\u4e2a\u4eba\u5e73\u9759\u533a\u57df"),
        ("gameTextureTiles", "\u7eb9\u7406\u62fc\u56fe"),
        ("gameTextureTilesDesc", "\u63a2\u7d22\u4ee4\u4eba\u6ee1\u8db3\u7684\u7eb9\u7406"),
        ("gameSoundGarden", "\u58f0\u97f3\u82b1\u56ed"),
        ("gameSoundGardenDesc", "\u521b\u5efa\u8212\u7f13\u7684\u58f0\u666f"),
        ("gameStimSequencer", "\u523a\u6fc0\u97f3\u5e8f\u5668"),
        ("gameStimSequencerDesc", "\u521b\u5efa\u6709\u8282\u594f\u7684\u523a\u6fc0\u6a21\u5f0f"),
        ("gameConstellationConnect", "\u661f\u5ea7\u8fde\u7ebf"),
        ("gameConstellationConnectDesc", "\u5c06\u661f\u661f\u8fde\u6210\u56fe\u6848"),
    ],
    'ar': [
        ("gamePatternTapDesc", "\u0627\u062e\u062a\u0628\u0631 \u0630\u0627\u0643\u0631\u062a\u0643 \u0645\u0639 \u0627\u0644\u0623\u0646\u0645\u0627\u0637"),
        ("gameWorryJar", "\u062c\u0631\u0629 \u0627\u0644\u0642\u0644\u0642"),
        ("gameWorryJarDesc", "\u0627\u0644\u062a\u0642\u0637 \u0627\u0644\u0645\u062e\u0627\u0648\u0641 \u0648\u062f\u0639\u0647\u0627 \u062a\u0630\u0647\u0628"),
        ("gameMoodMixer", "\u062e\u0644\u0627\u0637 \u0627\u0644\u0645\u0632\u0627\u062c"),
        ("gameMoodMixerDesc", "\u0627\u0645\u0632\u062c \u0627\u0644\u0623\u0644\u0648\u0627\u0646 \u0644\u062a\u062a\u0646\u0627\u0633\u0628 \u0645\u0639 \u0645\u0632\u0627\u062c\u0643"),
        ("gameSafeSpace", "\u0645\u0633\u0627\u062d\u0629 \u0622\u0645\u0646\u0629"),
        ("gameSafeSpaceDesc", "\u0627\u0628\u0646\u0650 \u0645\u0646\u0637\u0642\u0629 \u0647\u062f\u0648\u0626\u0643 \u0627\u0644\u0634\u062e\u0635\u064a\u0629"),
        ("gameTextureTiles", "\u0628\u0644\u0627\u0637 \u0627\u0644\u0642\u0648\u0627\u0645"),
        ("gameTextureTilesDesc", "\u0627\u0633\u062a\u0643\u0634\u0641 \u0642\u0648\u0627\u0645\u0627\u062a \u0645\u0631\u0636\u064a\u0629"),
        ("gameSoundGarden", "\u062d\u062f\u064a\u0642\u0629 \u0627\u0644\u0623\u0635\u0648\u0627\u062a"),
        ("gameSoundGardenDesc", "\u0623\u0646\u0634\u0626 \u0645\u0646\u0627\u0638\u0631 \u0635\u0648\u062a\u064a\u0629 \u0647\u0627\u062f\u0626\u0629"),
        ("gameStimSequencer", "\u062c\u0647\u0627\u0632 \u062a\u0633\u0644\u0633\u0644 \u0627\u0644\u062a\u062d\u0641\u064a\u0632"),
        ("gameStimSequencerDesc", "\u0623\u0646\u0634\u0626 \u0623\u0646\u0645\u0627\u0637 \u062a\u062d\u0641\u064a\u0632 \u0625\u064a\u0642\u0627\u0639\u064a\u0629"),
        ("gameConstellationConnect", "\u0631\u0628\u0637 \u0627\u0644\u0623\u0628\u0631\u0627\u062c"),
        ("gameConstellationConnectDesc", "\u0627\u0631\u0628\u0637 \u0627\u0644\u0646\u062c\u0648\u0645 \u0641\u064a \u0623\u0646\u0645\u0627\u0637"),
    ],
    'hi': [
        ("gamePatternTapDesc", "\u092a\u0948\u091f\u0930\u094d\u0928 \u0938\u0947 \u0905\u092a\u0928\u0940 \u092f\u093e\u0926\u0926\u093e\u0936\u094d\u0924 \u092a\u0930\u0916\u0947\u0902"),
        ("gameWorryJar", "\u091a\u093f\u0902\u0924\u093e \u0915\u093e \u0921\u093f\u092c\u094d\u092c\u093e"),
        ("gameWorryJarDesc", "\u091a\u093f\u0902\u0924\u093e\u0913\u0902 \u0915\u094b \u092a\u0915\u0921\u093c\u0947\u0902 \u0914\u0930 \u091b\u094b\u0921\u093c \u0926\u0947\u0902"),
        ("gameMoodMixer", "\u092e\u0942\u0921 \u092e\u093f\u0915\u094d\u0938\u0930"),
        ("gameMoodMixerDesc", "\u0905\u092a\u0928\u0947 \u092e\u0942\u0921 \u0915\u0947 \u0905\u0928\u0941\u0938\u093e\u0930 \u0930\u0902\u0917 \u092e\u093f\u0932\u093e\u090f\u0902"),
        ("gameSafeSpace", "\u0938\u0941\u0930\u0915\u094d\u0937\u093f\u0924 \u0938\u094d\u0925\u093e\u0928"),
        ("gameSafeSpaceDesc", "\u0905\u092a\u0928\u093e \u0935\u094d\u092f\u0915\u094d\u0924\u093f\u0917\u0924 \u0936\u093e\u0902\u0924 \u0915\u094d\u0937\u0947\u0924\u094d\u0930 \u092c\u0928\u093e\u090f\u0902"),
        ("gameTextureTiles", "\u091f\u0947\u0915\u094d\u0938\u091a\u0930 \u091f\u093e\u0907\u0932\u094d\u0938"),
        ("gameTextureTilesDesc", "\u0938\u0902\u0924\u094b\u0937\u091c\u0928\u0915 \u092c\u0928\u093e\u0935\u091f \u0915\u093e \u0905\u0928\u094d\u0935\u0947\u0937\u0923 \u0915\u0930\u0947\u0902"),
        ("gameSoundGarden", "\u0927\u094d\u0935\u0928\u093f \u092c\u0917\u0940\u091a\u093e"),
        ("gameSoundGardenDesc", "\u0936\u093e\u0902\u0924\u093f\u0926\u093e\u092f\u0915 \u0927\u094d\u0935\u0928\u093f \u092a\u0930\u093f\u0926\u0943\u0936\u094d\u092f \u092c\u0928\u093e\u090f\u0902"),
        ("gameStimSequencer", "\u0938\u094d\u091f\u093f\u092e \u0938\u0940\u0915\u094d\u0935\u0947\u0902\u0938\u0930"),
        ("gameStimSequencerDesc", "\u0932\u092f\u092c\u0926\u094d\u0927 \u0938\u094d\u091f\u093f\u092e \u092a\u0948\u091f\u0930\u094d\u0928 \u092c\u0928\u093e\u090f\u0902"),
        ("gameConstellationConnect", "\u0924\u093e\u0930\u093e\u092e\u0902\u0921\u0932 \u091c\u094b\u0921\u093c\u0947\u0902"),
        ("gameConstellationConnectDesc", "\u0924\u093e\u0930\u094b\u0902 \u0915\u094b \u092a\u0948\u091f\u0930\u094d\u0928 \u092e\u0947\u0902 \u091c\u094b\u0921\u093c\u0947\u0902"),
    ],
    'tr': [
        ("gamePatternTapDesc", "Desenlerle haf\u0131zan\u0131 test et"),
        ("gameWorryJar", "Endi\u015fe Kavanozu"),
        ("gameWorryJarDesc", "Endi\u015feleri yakala ve b\u0131rak"),
        ("gameMoodMixer", "Ruh Hali Kar\u0131\u015ft\u0131r\u0131c\u0131"),
        ("gameMoodMixerDesc", "Ruh haline uygun renkleri kar\u0131\u015ft\u0131r"),
        ("gameSafeSpace", "G\u00fcvenli Alan"),
        ("gameSafeSpaceDesc", "Ki\u015fisel huzur b\u00f6lgeni olu\u015ftur"),
        ("gameTextureTiles", "Doku Karolar\u0131"),
        ("gameTextureTilesDesc", "Tatmin edici dokular\u0131 ke\u015ffet"),
        ("gameSoundGarden", "Ses Bah\u00e7esi"),
        ("gameSoundGardenDesc", "Sakinle\u015ftirici ses manzaralar\u0131 olu\u015ftur"),
        ("gameStimSequencer", "Stim S\u0131ralay\u0131c\u0131"),
        ("gameStimSequencerDesc", "Ritmik stim kal\u0131plar\u0131 olu\u015ftur"),
        ("gameConstellationConnect", "Tak\u0131my\u0131ld\u0131z\u0131 Ba\u011fla"),
        ("gameConstellationConnectDesc", "Y\u0131ld\u0131zlar\u0131 kal\u0131plara ba\u011fla"),
    ],
}

# English fallback for remaining locales
en_fallback = [
    ("gamePatternTapDesc", "Test your memory with patterns"),
    ("gameWorryJar", "Worry Jar"),
    ("gameWorryJarDesc", "Capture worries and let them go"),
    ("gameMoodMixer", "Mood Mixer"),
    ("gameMoodMixerDesc", "Blend colors to match your mood"),
    ("gameSafeSpace", "Safe Space"),
    ("gameSafeSpaceDesc", "Build your personal calm zone"),
    ("gameTextureTiles", "Texture Tiles"),
    ("gameTextureTilesDesc", "Explore satisfying textures"),
    ("gameSoundGarden", "Sound Garden"),
    ("gameSoundGardenDesc", "Create calming soundscapes"),
    ("gameStimSequencer", "Stim Sequencer"),
    ("gameStimSequencerDesc", "Build rhythmic stim patterns"),
    ("gameConstellationConnect", "Constellation Connect"),
    ("gameConstellationConnectDesc", "Connect stars into patterns"),
]

remaining = ['sv', 'da', 'nb', 'fi', 'uk', 'vi', 'th', 'id', 'ms', 'he', 'el', 'cs', 'hu', 'ro', 'ur', 'is']

all_locales = list(strings_by_locale.keys()) + remaining
added = 0

for loc in all_locales:
    strings = strings_by_locale.get(loc, en_fallback)
    
    # Find the locale block
    loc_marker = f"'{loc}': {{"
    loc_idx = content.find(loc_marker)
    if loc_idx == -1:
        print(f"SKIP {loc}: locale block not found")
        continue
    
    # Find the end of this locale
    loc_end = content.find("    },", loc_idx + 1)
    loc_section = content[loc_idx:loc_end] if loc_end != -1 else ""
    
    if "gameWorryJar" in loc_section:
        print(f"SKIP {loc}: already has gameWorryJar")
        continue
    
    # Find gameEmotionGardenDesc in this locale section
    target = "'gameEmotionGardenDesc':"
    target_idx = content.find(target, loc_idx, loc_end if loc_end != -1 else len(content))
    if target_idx == -1:
        print(f"SKIP {loc}: no gameEmotionGardenDesc")
        continue
    
    # Find end of that line
    line_end = content.find("\n", target_idx)
    if line_end == -1:
        print(f"SKIP {loc}: can't find line end")
        continue
    
    # Build insertion
    insert = ""
    for key, val in strings:
        escaped = val.replace("'", "\\'")
        insert += f"\n      '{key}': '{escaped}',"
    
    content = content[:line_end] + insert + content[line_end:]
    added += 1
    print(f"OK {loc}: added {len(strings)} strings")

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"\nDone! Added strings to {added} locales.")

