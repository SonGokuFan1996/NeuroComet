const fs = require('fs');
const path = require('path');

const resDir = path.join('C:', 'Users', 'bkyil', 'AndroidStudioProjects', 'NeuroComet', 'app', 'src', 'main', 'res');
const outFile = path.join('C:', 'Users', 'bkyil', 'AndroidStudioProjects', 'NeuroComet', 'tools', 'full_audit.txt');

function extractKeys(filePath) {
    const content = fs.readFileSync(filePath, 'utf8');
    const keys = [];
    const regex = /name="([^"]+)"/g;
    let m;
    while ((m = regex.exec(content)) !== null) {
        keys.push(m[1]);
    }
    return keys;
}

const baseFile = path.join(resDir, 'values', 'strings.xml');
const baseKeys = extractKeys(baseFile);

let out = `Base English: ${baseKeys.length} entries\n\n`;

const langs = ['tr', 'es', 'de', 'fr', 'pt'];
for (const lang of langs) {
    const langFile = path.join(resDir, `values-${lang}`, 'strings.xml');
    const langKeys = new Set(extractKeys(langFile));
    const missing = baseKeys.filter(k => !langKeys.has(k));
    out += `=== ${lang} === ${langKeys.size}/${baseKeys.length} (missing: ${missing.length})\n`;
    if (missing.length > 0) {
        for (const k of missing) {
            out += `  ${k}\n`;
        }
    }
    out += '\n';
}

fs.writeFileSync(outFile, out, 'utf8');
console.log('Written to ' + outFile);

