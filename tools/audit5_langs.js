const fs = require('fs');
const path = require('path');
const resDir = path.join('C:', 'Users', 'bkyil', 'AndroidStudioProjects', 'NeuroComet', 'app', 'src', 'main', 'res');
const dartFile = path.join('C:', 'Users', 'bkyil', 'AndroidStudioProjects', 'NeuroComet', 'flutter_app', 'lib', 'l10n', 'app_localizations.dart');
const outFile = path.join('C:', 'Users', 'bkyil', 'AndroidStudioProjects', 'NeuroComet', 'tools', 'audit5.txt');

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

const baseFile = path.join(resDir, 'values', 'strings.xml');
const baseKeys = extractXmlKeys(baseFile);
const baseEntries = extractXmlStringEntries(baseFile);

let out = 'NATIVE ANDROID\nBase: ' + baseKeys.length + '\n\n';

const langs = ['ja', 'ko', 'zh', 'ar', 'hi'];
const nativeMissing = {};

for (const lang of langs) {
    const f = path.join(resDir, 'values-' + lang, 'strings.xml');
    if (!fs.existsSync(f)) { out += lang + ': FILE NOT FOUND\n'; continue; }
    const lk = new Set(extractXmlKeys(f));
    const missing = baseKeys.filter(k => !lk.has(k));
    nativeMissing[lang] = missing;
    out += lang + ': ' + lk.size + '/' + baseKeys.length + ' (missing: ' + missing.length + ')\n';
    for (const k of missing) {
        out += '  ' + k + ' = ' + (baseEntries[k] || '???') + '\n';
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

function extractDartKeyValues(langCode) {
    const re = new RegExp("'" + langCode + "':\\s*\\{");
    const match = re.exec(dartContent);
    if (!match) return {};
    let bc = 0, si = match.index + match[0].length - 1, ei = si;
    for (let i = si; i < dartContent.length; i++) {
        if (dartContent[i] === '{') bc++;
        if (dartContent[i] === '}') bc--;
        if (bc === 0) { ei = i; break; }
    }
    const block = dartContent.substring(si, ei);
    const result = {};
    const pr = /'([a-zA-Z_][a-zA-Z0-9_]*)'\s*:\s*'((?:[^'\\]|\\.)*)'/g;
    let pm;
    while ((pm = pr.exec(block)) !== null) result[pm[1]] = pm[2];
    return result;
}

const enDartKeys = extractDartKeys('en');
const enDartValues = extractDartKeyValues('en');
const flutterMissing = {};

out += '\n\nFLUTTER APP\nBase: ' + enDartKeys.size + '\n\n';
for (const lang of langs) {
    const lk = extractDartKeys(lang);
    const missing = [...enDartKeys].filter(k => !lk.has(k));
    flutterMissing[lang] = missing;
    out += lang + ': ' + lk.size + '/' + enDartKeys.size + ' (missing: ' + missing.length + ')\n';
    for (const k of missing) {
        out += '  ' + k + ' = ' + (enDartValues[k] || '???') + '\n';
    }
    out += '\n';
}

fs.writeFileSync(outFile, out, 'utf8');
console.log('Done: ' + outFile);

