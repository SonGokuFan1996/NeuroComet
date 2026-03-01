const fs = require('fs');
const path = require('path');

const root = 'C:/Users/bkyil/AndroidStudioProjects/NeuroComet';
const resDir = path.join(root, 'app/src/main/res');
const dartFile = path.join(root, 'flutter_app/lib/l10n/app_localizations.dart');
const outFile = path.join(root, 'audit5_output.txt');

function extractXmlKeys(filePath) {
    const content = fs.readFileSync(filePath, 'utf8');
    const keys = [];
    const regex = /name="([^"]+)"/g;
    let m;
    while ((m = regex.exec(content)) !== null) keys.push(m[1]);
    return keys;
}

function extractXmlStringEntries(filePath) {
    const content = fs.readFileSync(filePath, 'utf8');
    const entries = {};
    const regex = /<string\s+name="([^"]+)"[^>]*>([\s\S]*?)<\/string>/g;
    let m;
    while ((m = regex.exec(content)) !== null) entries[m[1]] = m[2];
    return entries;
}

const baseFile = path.join(resDir, 'values/strings.xml');
const baseKeys = extractXmlKeys(baseFile);
const baseEntries = extractXmlStringEntries(baseFile);

let out = 'NATIVE ANDROID\nBase: ' + baseKeys.length + '\n\n';

const langs = ['ja', 'ko', 'zh', 'ar', 'hi'];

for (const lang of langs) {
    const f = path.join(resDir, 'values-' + lang, 'strings.xml');
    if (!fs.existsSync(f)) { out += lang + ': FILE NOT FOUND\n\n'; continue; }
    const lk = new Set(extractXmlKeys(f));
    const missing = baseKeys.filter(k => !lk.has(k));
    out += lang + ': ' + lk.size + '/' + baseKeys.length + ' (missing: ' + missing.length + ')\n';
    for (const k of missing) {
        out += '  ' + k + '\n';
    }
    out += '\n';
}

// Flutter
const dartContent = fs.readFileSync(dartFile, 'utf8');
function extractDartKeys(langCode) {
    const re = new RegExp("'" + langCode + "':\\s*\\{");
    const match = re.exec(dartContent);
    if (!match) return new Set();
    let bc = 0, si = match.index + match[0].length - 1, ei = si;
    for (let i = si; i < dartContent.length; i++) {
        if (dartContent[i] === '{') bc++;
        if (dartContent[i] === '}') bc--;
        if (bc === 0) { ei = i; break; }
    }
    const block = dartContent.substring(si, ei);
    const keys = new Set();
    const kr = /'([a-zA-Z_][a-zA-Z0-9_]*)'\s*:/g;
    let km;
    while ((km = kr.exec(block)) !== null) keys.add(km[1]);
    return keys;
}

const enDartKeys = extractDartKeys('en');

out += '\n\nFLUTTER APP\nBase: ' + enDartKeys.size + '\n\n';
for (const lang of langs) {
    const lk = extractDartKeys(lang);
    const missing = [...enDartKeys].filter(k => !lk.has(k));
    out += lang + ': ' + lk.size + '/' + enDartKeys.size + ' (missing: ' + missing.length + ')\n';
    for (const k of missing) {
        out += '  ' + k + '\n';
    }
    out += '\n';
}

fs.writeFileSync(outFile, out, 'utf8');

