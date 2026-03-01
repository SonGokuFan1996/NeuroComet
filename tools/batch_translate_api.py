#!/usr/bin/env python3
"""
Batch Translation Script for NeuroComet
Translates strings using external translation APIs.

Supported APIs:
- Google Cloud Translation API
- DeepL API
- OpenAI GPT API
- Azure Translator API
- Anthropic Claude API

Usage:
    python batch_translate_api.py --api google --target es
    python batch_translate_api.py --api deepl --target all
    python batch_translate_api.py --api openai --target fr --batch-size 50
    python batch_translate_api.py --status
"""

import os
import re
import json
import time
import argparse
import sys
from pathlib import Path
from typing import Dict, List, Optional, Tuple
from abc import ABC, abstractmethod

# Project paths
TOOLS_DIR = Path(__file__).parent
PROJECT_DIR = TOOLS_DIR.parent
RES_DIR = PROJECT_DIR / 'app' / 'src' / 'main' / 'res'
TRANSLATIONS_DIR = TOOLS_DIR / 'translations'
CACHE_DIR = TOOLS_DIR / '.translation_cache'

# Import secret loader
sys.path.insert(0, str(TOOLS_DIR))
try:
    from secrets_loader import get_secret
except ImportError:
    def get_secret(key, default=None):
        return os.environ.get(key, default)

# Language configurations
LANGUAGES = {
    'ar': ('Arabic', 'values-ar', 'ar'),
    'cs': ('Czech', 'values-cs', 'cs'),
    'da': ('Danish', 'values-da', 'da'),
    'de': ('German', 'values-de', 'de'),
    'el': ('Greek', 'values-el', 'el'),
    'es': ('Spanish', 'values-es', 'es'),
    'fi': ('Finnish', 'values-fi', 'fi'),
    'fr': ('French', 'values-fr', 'fr'),
    'hi': ('Hindi', 'values-hi', 'hi'),
    'hu': ('Hungarian', 'values-hu', 'hu'),
    'id': ('Indonesian', 'values-in', 'id'),
    'is': ('Icelandic', 'values-is', 'is'),
    'it': ('Italian', 'values-it', 'it'),
    'he': ('Hebrew', 'values-iw', 'he'),
    'ja': ('Japanese', 'values-ja', 'ja'),
    'ko': ('Korean', 'values-ko', 'ko'),
    'ms': ('Malay', 'values-ms', 'ms'),
    'nb': ('Norwegian', 'values-nb', 'nb'),
    'nl': ('Dutch', 'values-nl', 'nl'),
    'pl': ('Polish', 'values-pl', 'pl'),
    'pt': ('Portuguese', 'values-pt', 'pt'),
    'ro': ('Romanian', 'values-ro', 'ro'),
    'ru': ('Russian', 'values-ru', 'ru'),
    'sv': ('Swedish', 'values-sv', 'sv'),
    'th': ('Thai', 'values-th', 'th'),
    'tr': ('Turkish', 'values-tr', 'tr'),
    'uk': ('Ukrainian', 'values-uk', 'uk'),
    'ur': ('Urdu', 'values-ur', 'ur'),
    'vi': ('Vietnamese', 'values-vi', 'vi'),
    'zh': ('Chinese', 'values-zh', 'zh-CN'),
}

# Keys that should not be translated
NON_TRANSLATABLE = {
    'app_name',
    'emoji_thumbs_up', 'emoji_heart', 'emoji_smile', 'emoji_hands', 'emoji_more',
}


class TranslationAPI(ABC):
    """Abstract base class for translation APIs."""

    @abstractmethod
    def translate(self, texts: List[str], target_lang: str, source_lang: str = 'en') -> List[str]:
        """Translate a list of texts to target language."""
        pass

    @abstractmethod
    def get_name(self) -> str:
        """Return the API name."""
        pass


class GoogleTranslateAPI(TranslationAPI):
    """Google Cloud Translation API."""

    def __init__(self, api_key: str):
        self.api_key = api_key
        self.base_url = "https://translation.googleapis.com/language/translate/v2"

    def get_name(self) -> str:
        return "Google Cloud Translation"

    def translate(self, texts: List[str], target_lang: str, source_lang: str = 'en') -> List[str]:
        try:
            import requests
        except ImportError:
            print("Error: 'requests' library required. Install with: pip install requests")
            sys.exit(1)

        results = []
        batch_size = 100

        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]

            params = {
                'key': self.api_key,
                'target': target_lang,
                'source': source_lang,
                'format': 'text'
            }

            data = {'q': batch}

            response = requests.post(self.base_url, params=params, data=data)

            if response.status_code != 200:
                raise Exception(f"Google API error: {response.status_code} - {response.text}")

            result = response.json()
            translations = result['data']['translations']
            results.extend([t['translatedText'] for t in translations])

            if i + batch_size < len(texts):
                time.sleep(0.1)

        return results


class DeepLAPI(TranslationAPI):
    """DeepL Translation API."""

    def __init__(self, api_key: str, use_free: bool = True):
        self.api_key = api_key
        self.base_url = "https://api-free.deepl.com/v2/translate" if use_free else "https://api.deepl.com/v2/translate"

    def get_name(self) -> str:
        return "DeepL"

    def translate(self, texts: List[str], target_lang: str, source_lang: str = 'en') -> List[str]:
        try:
            import requests
        except ImportError:
            print("Error: 'requests' library required. Install with: pip install requests")
            sys.exit(1)

        deepl_lang_map = {
            'zh': 'ZH',
            'pt': 'PT-BR',
            'en': 'EN',
        }
        target = deepl_lang_map.get(target_lang, target_lang.upper())

        results = []
        batch_size = 50

        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]

            headers = {
                'Authorization': f'DeepL-Auth-Key {self.api_key}',
                'Content-Type': 'application/json'
            }

            data = {
                'text': batch,
                'target_lang': target,
                'source_lang': 'EN',
                'preserve_formatting': True
            }

            response = requests.post(self.base_url, headers=headers, json=data)

            if response.status_code != 200:
                raise Exception(f"DeepL API error: {response.status_code} - {response.text}")

            result = response.json()
            translations = result['translations']
            results.extend([t['text'] for t in translations])

            if i + batch_size < len(texts):
                time.sleep(0.2)

        return results


class OpenAIAPI(TranslationAPI):
    """OpenAI GPT API for translation."""

    def __init__(self, api_key: str, model: str = "gpt-4o"):
        self.api_key = api_key
        self.model = model
        self.base_url = "https://api.openai.com/v1/chat/completions"

    def get_name(self) -> str:
        return f"OpenAI ({self.model})"

    def translate(self, texts: List[str], target_lang: str, source_lang: str = 'en') -> List[str]:
        try:
            import requests
        except ImportError:
            print("Error: 'requests' library required. Install with: pip install requests")
            sys.exit(1)

        lang_name = LANGUAGES.get(target_lang, (target_lang,))[0]

        results = []
        batch_size = 25

        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]

            numbered_texts = "\n".join([f"{idx+1}. {text}" for idx, text in enumerate(batch)])

            system_prompt = f"""You are an expert translator specializing in mobile app localization for {lang_name}.

CONTEXT: NeuroComet is a social media app designed for neurodivergent individuals (ADHD, autism, dyslexia) and LGBTQ+ communities. The tone should be warm, inclusive, supportive, and affirming.

CRITICAL RULES:
1. PRESERVE ALL PLACEHOLDERS EXACTLY: %s, %d, %1$s, %2$d, %1$d, etc. - these MUST appear in the translation
2. PRESERVE ALL EMOJIS in their original positions
3. DO NOT translate: "NeuroComet", "ADHD", "LGBTQ+", "OpenDyslexic", "Lexend"
4. Keep translations natural and fluent in {lang_name} - avoid literal word-for-word translation
5. Match the formality level: casual/friendly for user-facing text
6. Keep UI strings concise - they must fit on buttons and labels
7. For accessibility terms, use the standard {lang_name} terminology

OUTPUT FORMAT:
Return ONLY a JSON array with exactly {len(batch)} translated strings in the same order.
Example: ["translation1", "translation2", ...]"""

            user_prompt = f"""Translate these {len(batch)} English strings to {lang_name}:

{numbered_texts}

Return a JSON array with exactly {len(batch)} translations in order."""

            headers = {
                'Authorization': f'Bearer {self.api_key}',
                'Content-Type': 'application/json'
            }

            data = {
                'model': self.model,
                'messages': [
                    {'role': 'system', 'content': system_prompt},
                    {'role': 'user', 'content': user_prompt}
                ],
                'temperature': 0.2,
                'max_tokens': 4096
            }

            response = requests.post(self.base_url, headers=headers, json=data)

            if response.status_code != 200:
                raise Exception(f"OpenAI API error: {response.status_code} - {response.text}")

            result = response.json()
            content = result['choices'][0]['message']['content']

            try:
                json_match = re.search(r'\[.*\]', content, re.DOTALL)
                if json_match:
                    translations = json.loads(json_match.group())
                else:
                    translations = json.loads(content)

                if len(translations) != len(batch):
                    print(f"Warning: Expected {len(batch)} translations, got {len(translations)}")
                    while len(translations) < len(batch):
                        translations.append(batch[len(translations)])
                    translations = translations[:len(batch)]

                results.extend(translations)
            except json.JSONDecodeError as e:
                print(f"Warning: Failed to parse OpenAI response: {e}")
                results.extend(batch)

            if i + batch_size < len(texts):
                time.sleep(0.5)

        return results


class AzureTranslateAPI(TranslationAPI):
    """Azure Cognitive Services Translator API."""

    def __init__(self, api_key: str, region: str = "global"):
        self.api_key = api_key
        self.region = region
        self.base_url = "https://api.cognitive.microsofttranslator.com/translate"

    def get_name(self) -> str:
        return "Azure Translator"

    def translate(self, texts: List[str], target_lang: str, source_lang: str = 'en') -> List[str]:
        try:
            import requests
        except ImportError:
            print("Error: 'requests' library required. Install with: pip install requests")
            sys.exit(1)

        results = []
        batch_size = 100

        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]

            headers = {
                'Ocp-Apim-Subscription-Key': self.api_key,
                'Ocp-Apim-Subscription-Region': self.region,
                'Content-Type': 'application/json'
            }

            params = {
                'api-version': '3.0',
                'from': source_lang,
                'to': target_lang
            }

            data = [{'text': text} for text in batch]

            response = requests.post(self.base_url, headers=headers, params=params, json=data)

            if response.status_code != 200:
                raise Exception(f"Azure API error: {response.status_code} - {response.text}")

            result = response.json()
            translations = [item['translations'][0]['text'] for item in result]
            results.extend(translations)

            if i + batch_size < len(texts):
                time.sleep(0.1)

        return results


class AnthropicAPI(TranslationAPI):
    """Anthropic Claude API for highest accuracy translations."""

    def __init__(self, api_key: str, model: str = "claude-3-5-sonnet-20240620"):
        self.api_key = api_key
        self.model = model
        self.base_url = "https://api.anthropic.com/v1/messages"

    def get_name(self) -> str:
        if 'sonnet' in self.model.lower():
            return "Anthropic Claude (Sonnet)"
        elif 'opus' in self.model.lower():
            return "Anthropic Claude (Opus)"
        elif 'haiku' in self.model.lower():
            return "Anthropic Claude (Haiku)"
        return f"Anthropic Claude ({self.model})"

    def translate(self, texts: List[str], target_lang: str, source_lang: str = 'en') -> List[str]:
        try:
            import requests
        except ImportError:
            print("Error: 'requests' library required. Install with: pip install requests")
            sys.exit(1)

        lang_name = LANGUAGES.get(target_lang, (target_lang,))[0]

        results = []
        batch_size = 20

        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]

            numbered_texts = "\n".join([f"{idx+1}. \"{text}\"" for idx, text in enumerate(batch)])

            prompt = f"""You are an expert translator for mobile app localization. Translate the following English strings to {lang_name}.

CONTEXT: NeuroComet is a social media app designed for neurodivergent individuals (ADHD, autism, dyslexia) and LGBTQ+ communities. Translations must be warm, inclusive, supportive, and culturally appropriate for {lang_name} speakers.

CRITICAL RULES:
1. PRESERVE ALL PLACEHOLDERS EXACTLY as they appear: %s, %d, %1$s, %2$d, %1$d, etc.
2. PRESERVE ALL EMOJIS in their original positions
3. DO NOT translate these terms: "NeuroComet", "ADHD", "LGBTQ+", "OpenDyslexic", "Lexend", "Atkinson Hyperlegible"
4. Produce natural, fluent {lang_name} - avoid literal word-for-word translation
5. Use informal/friendly tone for user-facing text (tu/vous, du/Sie etc. - use informal)
6. Keep UI strings concise - they must fit on buttons and small UI elements
7. Use standard {lang_name} accessibility and tech terminology

STRINGS TO TRANSLATE:
{numbered_texts}

OUTPUT: Return ONLY a valid JSON array containing exactly {len(batch)} translated strings in the same order as the input. No explanations, no numbering, just the JSON array.
Example format: ["translated string 1", "translated string 2", ...]"""

            headers = {
                'x-api-key': self.api_key,
                'anthropic-version': '2023-06-01',
                'Content-Type': 'application/json'
            }

            data = {
                'model': self.model,
                'max_tokens': 4096,
                'messages': [
                    {'role': 'user', 'content': prompt}
                ]
            }

            response = requests.post(self.base_url, headers=headers, json=data)

            if response.status_code != 200:
                raise Exception(f"Anthropic API error: {response.status_code} - {response.text}")

            result = response.json()
            content = result['content'][0]['text']

            try:
                json_match = re.search(r'\[.*\]', content, re.DOTALL)
                if json_match:
                    translations = json.loads(json_match.group())
                else:
                    translations = json.loads(content)

                if len(translations) != len(batch):
                    print(f"Warning: Expected {len(batch)} translations, got {len(translations)}")
                    while len(translations) < len(batch):
                        translations.append(batch[len(translations)])
                    translations = translations[:len(batch)]

                for idx, (orig, trans) in enumerate(zip(batch, translations)):
                    placeholders = re.findall(r'%\d*\$?[sdfl]|%[sdfl]', orig)
                    trans_placeholders = re.findall(r'%\d*\$?[sdfl]|%[sdfl]', trans)
                    if sorted(placeholders) != sorted(trans_placeholders):
                        print(f"Warning: Placeholder mismatch in translation {i+idx+1}, using original")
                        translations[idx] = orig

                results.extend(translations)
            except json.JSONDecodeError as e:
                print(f"Warning: Failed to parse Claude response: {e}")
                results.extend(batch)

            if i + batch_size < len(texts):
                time.sleep(0.3)

        return results


class TranslationCache:
    """Cache for translations to avoid re-translating the same strings."""

    def __init__(self):
        CACHE_DIR.mkdir(exist_ok=True)

    def _get_cache_file(self, api_name: str, target_lang: str) -> Path:
        safe_name = re.sub(r'[^\w\-]', '_', f"{api_name}_{target_lang}")
        return CACHE_DIR / f"{safe_name}.json"

    def get(self, api_name: str, target_lang: str, source_text: str) -> Optional[str]:
        cache_file = self._get_cache_file(api_name, target_lang)
        if not cache_file.exists():
            return None

        try:
            with open(cache_file, 'r', encoding='utf-8') as f:
                cache = json.load(f)
            return cache.get(source_text)
        except:
            return None

    def set_batch(self, api_name: str, target_lang: str, translations: Dict[str, str]):
        cache_file = self._get_cache_file(api_name, target_lang)

        try:
            if cache_file.exists():
                with open(cache_file, 'r', encoding='utf-8') as f:
                    cache = json.load(f)
            else:
                cache = {}
        except:
            cache = {}

        cache.update(translations)

        with open(cache_file, 'w', encoding='utf-8') as f:
            json.dump(cache, f, ensure_ascii=False, indent=2)


def get_english_strings() -> Dict[str, str]:
    """Get all translatable strings from values/strings.xml."""
    strings_path = RES_DIR / 'values' / 'strings.xml'
    with open(strings_path, 'r', encoding='utf-8') as f:
        content = f.read()

    pattern = r'<string name="([^"]+)"[^>]*>(.*?)</string>'
    matches = re.findall(pattern, content, re.DOTALL)

    result = {}
    for name, value in matches:
        if name not in NON_TRANSLATABLE:
            value = value.strip()
            value = value.replace('&lt;', '<').replace('&gt;', '>').replace('&amp;', '&')
            value = value.replace("\\'", "'")
            result[name] = value

    return result


def get_language_strings(lang_code: str) -> Dict[str, str]:
    """Get existing strings for a language."""
    lang_info = LANGUAGES.get(lang_code)
    if not lang_info:
        return {}

    values_dir = lang_info[1]
    strings_path = RES_DIR / values_dir / 'strings.xml'

    if not strings_path.exists():
        return {}

    with open(strings_path, 'r', encoding='utf-8') as f:
        content = f.read()

    pattern = r'<string name="([^"]+)"[^>]*>(.*?)</string>'
    matches = re.findall(pattern, content, re.DOTALL)

    result = {}
    for name, value in matches:
        value = value.strip()
        value = value.replace('&lt;', '<').replace('&gt;', '>').replace('&amp;', '&')
        value = value.replace("\\'", "'")
        result[name] = value

    return result


def get_untranslated_strings(english: Dict[str, str], current: Dict[str, str]) -> Dict[str, str]:
    """Find strings that need translation."""
    untranslated = {}

    for key, eng_value in english.items():
        if key in current and current[key] == eng_value:
            if re.match(r'^%\d+\$[ds]$', eng_value.strip()):
                continue
            untranslated[key] = eng_value
        elif key not in current:
            untranslated[key] = eng_value

    return untranslated


def escape_for_xml(text: str) -> str:
    """Escape text for Android XML strings."""
    text = text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
    if "\\'" not in text:
        text = text.replace("'", "\\'")
    return text


def update_strings_xml(lang_code: str, translations: Dict[str, str]) -> int:
    """Update the strings.xml file for a language."""
    lang_info = LANGUAGES.get(lang_code)
    if not lang_info:
        return 0

    values_dir = lang_info[1]
    strings_path = RES_DIR / values_dir / 'strings.xml'

    if not strings_path.exists():
        return 0

    with open(strings_path, 'r', encoding='utf-8') as f:
        content = f.read()

    english = get_english_strings()
    updated_count = 0

    for key, translation in translations.items():
        pattern = rf'(<string name="{re.escape(key)}"[^>]*>)(.*?)(</string>)'
        match = re.search(pattern, content, re.DOTALL)

        if match:
            current_value = match.group(2).strip()
            current_clean = current_value.replace("\\'", "'").replace('&amp;', '&').replace('&lt;', '<').replace('&gt;', '>')

            if key in english and current_clean == english[key]:
                escaped_translation = escape_for_xml(translation)
                new_element = f'{match.group(1)}{escaped_translation}{match.group(3)}'
                content = content[:match.start()] + new_element + content[match.end():]
                updated_count += 1
        else:
            # Handle missing string by adding it before </resources>
            escaped_translation = escape_for_xml(translation)
            new_string = f'\n    <string name="{key}">{escaped_translation}</string>'
            insert_point = content.rfind('</resources>')
            if insert_point > 0:
                content = content[:insert_point] + new_string + content[insert_point:]
                updated_count += 1

    with open(strings_path, 'w', encoding='utf-8') as f:
        f.write(content)

    return updated_count


def create_api(api_name: str, api_key: str) -> TranslationAPI:
    """Create a translation API instance."""
    if api_name == 'google':
        return GoogleTranslateAPI(api_key)
    elif api_name == 'deepl':
        return DeepLAPI(api_key)
    elif api_name == 'openai':
        return OpenAIAPI(api_key)
    elif api_name == 'anthropic':
        return AnthropicAPI(api_key)
    elif api_name == 'azure':
        return AzureTranslateAPI(api_key)
    else:
        raise ValueError(f"Unknown API: {api_name}")


def main():
    parser = argparse.ArgumentParser(description='Batch translation script for NeuroComet')

    parser.add_argument('--status', action='store_true', help='Show translation status')
    parser.add_argument('--api', choices=['google', 'deepl', 'openai', 'anthropic', 'azure'], help='API to use')
    parser.add_argument('--api-key', help='API key (or use environment/secrets.properties)')
    parser.add_argument('--target', help='Target language or "all"')
    parser.add_argument('--batch-size', type=int, default=50)
    parser.add_argument('--dry-run', action='store_true')

    args = parser.parse_args()

    if args.status:
        # Simple status print
        english = get_english_strings()
        print(f"English: {len(english)} strings")
        for code in sorted(LANGUAGES.keys()):
            current = get_language_strings(code)
            untranslated = get_untranslated_strings(english, current)
            pct = (1 - len(untranslated)/len(english)) * 100
            print(f"  {code}: {pct:.1f}% ({len(current)} total, {len(untranslated)} untranslated)")
        return

    if not args.api or not args.target:
        parser.error("--api and --target are required")

    # Get API key using the secrets loader
    env_vars = {
        'google': 'GOOGLE_TRANSLATE_API_KEY',
        'deepl': 'DEEPL_API_KEY',
        'openai': 'OPENAI_API_KEY',
        'anthropic': 'ANTHROPIC_API_KEY',
        'azure': 'AZURE_TRANSLATOR_KEY'
    }

    api_key = args.api_key
    if not api_key:
        api_key = get_secret(env_vars[args.api])

    if not api_key:
        print(f"❌ Error: No API key provided for {args.api}")
        print(f"   Set {env_vars[args.api]} in secrets.properties or environment")
        sys.exit(1)

    api = create_api(args.api, api_key)
    print(f"🔑 Using {api.get_name()} API")

    targets = list(LANGUAGES.keys()) if args.target == 'all' else [args.target]

    for target in targets:
        # Perform translation (simplified call for this implementation)
        english = get_english_strings()
        current = get_language_strings(target)
        untranslated = get_untranslated_strings(english, current)

        if not untranslated:
            print(f"✅ {target}: All strings translated!")
            continue

        if args.dry_run:
            print(f"⏭️  [DRY RUN] Would translate {len(untranslated)} strings for {target}")
            continue

        print(f"🔄 Translating {len(untranslated)} strings for {target}...")
        
        keys = list(untranslated.keys())
        texts = list(untranslated.values())
        
        all_translated = []
        for i in range(0, len(texts), args.batch_size):
            batch = texts[i:i + args.batch_size]
            api_lang_code = LANGUAGES[target][2]
            translated_batch = api.translate(batch, api_lang_code)
            all_translated.extend(translated_batch)
            
        results = dict(zip(keys, all_translated))
        updated = update_strings_xml(target, results)
        print(f"   📝 Updated {updated} strings")

    print("\n✅ Done!")


if __name__ == '__main__':
    main()
