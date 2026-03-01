const fs = require('fs');
const path = require('path');

const dartFile = path.join('C:', 'Users', 'bkyil', 'AndroidStudioProjects', 'NeuroComet', 'flutter_app', 'lib', 'l10n', 'app_localizations.dart');
const outFile = path.join('C:', 'Users', 'bkyil', 'AndroidStudioProjects', 'NeuroComet', 'tools', 'flutter_audit.txt');

const content = fs.readFileSync(dartFile, 'utf8');

// Extract all language blocks from _localizedStrings map
// Pattern: 'xx': { ... keys ... }
// We need to find each language code and its keys

const langs = ['en', 'tr', 'es', 'de', 'fr', 'pt'];
let out = '';

for (const lang of langs) {
    // Find the start of this language's map
    const langPattern = new RegExp(`'${lang}':\\s*\\{`, 'g');
    const match = langPattern.exec(content);
    if (!match) {
        out += `${lang}: NOT FOUND\n`;
        continue;
    }

    // Count keys within this language block by finding all 'key': 'value' pairs
    // We need to find the matching closing brace
    let braceCount = 0;
    let startIdx = match.index + match[0].length - 1; // position of opening {
    let endIdx = startIdx;

    for (let i = startIdx; i < content.length; i++) {
        if (content[i] === '{') braceCount++;
        if (content[i] === '}') braceCount--;
        if (braceCount === 0) {
            endIdx = i;
            break;
        }
    }

    const block = content.substring(startIdx, endIdx + 1);
    // Count key entries - match 'key_name':
    const keyMatches = block.match(/'([a-zA-Z_][a-zA-Z0-9_]*)'\s*:/g);
    const keyCount = keyMatches ? keyMatches.length : 0;

    // Extract the actual key names
    const keys = new Set();
    if (keyMatches) {
        for (const km of keyMatches) {
            const kn = km.match(/'([^']+)'/);
            if (kn) keys.add(kn[1]);
        }
    }

    out += `${lang}: ${keyCount} keys\n`;

    if (lang !== 'en') {
        // Store for comparison
    }
}

// Now do a detailed comparison
out += '\n--- Detailed missing keys ---\n\n';

// Extract English keys
function extractLangKeys(langCode) {
    const langPattern = new RegExp(`'${langCode}':\\s*\\{`);
    const match = langPattern.exec(content);
    if (!match) return new Set();

    let braceCount = 0;
    let startIdx = match.index + match[0].length - 1;
    let endIdx = startIdx;

    for (let i = startIdx; i < content.length; i++) {
        if (content[i] === '{') braceCount++;
        if (content[i] === '}') braceCount--;
        if (braceCount === 0) {
            endIdx = i;
            break;
        }
    }

    const block = content.substring(startIdx, endIdx + 1);
    const keyMatches = block.match(/'([a-zA-Z_][a-zA-Z0-9_]*)'\s*:/g);
    const keys = new Set();
    if (keyMatches) {
        for (const km of keyMatches) {
            const kn = km.match(/'([^']+)'/);
            if (kn) keys.add(kn[1]);
        }
    }
    return keys;
}

function extractLangBlock(langCode) {
    const langPattern = new RegExp(`'${langCode}':\\s*\\{`);
    const match = langPattern.exec(content);
    if (!match) return '';

    let braceCount = 0;
    let startIdx = match.index + match[0].length - 1;
    let endIdx = startIdx;

    for (let i = startIdx; i < content.length; i++) {
        if (content[i] === '{') braceCount++;
        if (content[i] === '}') braceCount--;
        if (braceCount === 0) {
            endIdx = i;
            break;
        }
    }

    return content.substring(startIdx, endIdx + 1);
}

function extractKeyValues(langCode) {
    const langPattern = new RegExp(`'${langCode}':\\s*\\{`);
    const match = langPattern.exec(content);
    if (!match) return {};

    let braceCount = 0;
    let startIdx = match.index + match[0].length - 1;
    let endIdx = startIdx;

    for (let i = startIdx; i < content.length; i++) {
        if (content[i] === '{') braceCount++;
        if (content[i] === '}') braceCount--;
        if (braceCount === 0) {
            endIdx = i;
            break;
        }
    }

    const block = content.substring(startIdx, endIdx + 1);
    const result = {};
    // Match 'key': 'value' or 'key': "value"
    const pairRegex = /'([a-zA-Z_][a-zA-Z0-9_]*)'\s*:\s*'((?:[^'\\]|\\.)*)'/g;
    let pm;
    while ((pm = pairRegex.exec(block)) !== null) {
        result[pm[1]] = pm[2];
    }
    return result;
}

const enKeys = extractLangKeys('en');
const enValues = extractKeyValues('en');

out += `English base: ${enKeys.size} keys\n\n`;

for (const lang of ['tr', 'es', 'de', 'fr', 'pt']) {
    const langKeys = extractLangKeys(lang);
    const missing = [...enKeys].filter(k => !langKeys.has(k));
    out += `=== ${lang} === ${langKeys.size}/${enKeys.size} (missing: ${missing.length})\n`;
    for (const k of missing) {
        out += `  ${k} = ${enValues[k] || '???'}\n`;
    }
    out += '\n';
}

fs.writeFileSync(outFile, out, 'utf8');
console.log('Done: ' + outFile);

