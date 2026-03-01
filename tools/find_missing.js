const fs = require('fs');
const path = require('path');

const resDir = path.join(__dirname, '..', 'app', 'src', 'main', 'res');
const outFile = path.join(__dirname, 'missing_keys.txt');

function extractKeys(filePath) {
    const content = fs.readFileSync(filePath, 'utf8');
    const keys = [];
    const regex = /name="([^"]+)"/g;
    let match;
    while ((match = regex.exec(content)) !== null) {
        keys.push(match[1]);
    }
    return keys;
}

function extractStringEntries(filePath) {
    const content = fs.readFileSync(filePath, 'utf8');
    const entries = {};
    // Match <string name="key">value</string> (including multiline)
    const regex = /<string\s+name="([^"]+)"[^>]*>([\s\S]*?)<\/string>/g;
    let match;
    while ((match = regex.exec(content)) !== null) {
        entries[match[1]] = match[2];
    }
    return entries;
}

const baseFile = path.join(resDir, 'values', 'strings.xml');
const baseKeys = extractKeys(baseFile);
const baseEntries = extractStringEntries(baseFile);

let output = `Base English has ${baseKeys.length} entries\n\n`;

const langs = ['tr', 'es', 'de', 'fr', 'pt'];
const allMissing = {};

for (const lang of langs) {
    const langFile = path.join(resDir, `values-${lang}`, 'strings.xml');
    const langKeys = new Set(extractKeys(langFile));

    const missing = baseKeys.filter(k => !langKeys.has(k));
    allMissing[lang] = missing;

    output += `=== ${lang} === ${langKeys.size}/${baseKeys.length} (missing: ${missing.length})\n`;
    for (const k of missing) {
        const engVal = baseEntries[k] || '(non-string entry)';
        output += `  ${k} = ${engVal}\n`;
    }
    output += '\n';
}

fs.writeFileSync(outFile, output, 'utf8');
console.log('Done. Output written to ' + outFile);

