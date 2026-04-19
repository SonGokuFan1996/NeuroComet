# NeuroComet Web — `getneurocomet.com`
Static site served via **GitHub Pages** at `https://getneurocomet.com/`.
## Layout
```
web/
  CNAME                    → "getneurocomet.com" (tells GitHub Pages the custom domain)
  index.html               → https://getneurocomet.com/
  privacy/index.html       → https://getneurocomet.com/privacy
  terms/index.html         → https://getneurocomet.com/terms
  .well-known/
    assetlinks.json        → https://getneurocomet.com/.well-known/assetlinks.json
                             (Android App Links proof — MUST be served with
                              Content-Type: application/json and NO redirects.)
```
## Deploying to GitHub Pages (Plan B)
### 1. Push `web/` to GitHub
Commit everything under `web/`. Then in **Repo Settings → Pages**:
- Source: **Deploy from a branch**
- Branch: `main` (or whichever you use), folder: `/web`
- Save.
GitHub Pages will provision a build in ~1 minute.
### 2. DNS at your registrar
Point `getneurocomet.com` at GitHub Pages IPs:
| Type  | Name | Value                                |
|-------|------|--------------------------------------|
| A     | @    | 185.199.108.153                      |
| A     | @    | 185.199.109.153                      |
| A     | @    | 185.199.110.153                      |
| A     | @    | 185.199.111.153                      |
| AAAA  | @    | 2606:50c0:8000::153                  |
| AAAA  | @    | 2606:50c0:8001::153                  |
| AAAA  | @    | 2606:50c0:8002::153                  |
| AAAA  | @    | 2606:50c0:8003::153                  |
| CNAME | www  | `<your-github-username>.github.io`   |
Enable **DNSSEC** at the registrar if offered.
### 3. HTTPS
In **Repo Settings → Pages**, tick **"Enforce HTTPS"** once the cert is
provisioned (5–15 minutes after DNS propagates).
### 4. Verify
```powershell
curl.exe -I https://getneurocomet.com/
curl.exe -I https://getneurocomet.com/privacy
curl.exe -I https://getneurocomet.com/terms
curl.exe -I https://getneurocomet.com/.well-known/assetlinks.json
```
All should return `200 OK`. The `assetlinks.json` response MUST include
`Content-Type: application/json` and MUST NOT be a redirect.
## Before you publish `assetlinks.json`
Replace the three `REPLACE_WITH_*` placeholders with real SHA-256 fingerprints.
### 1. Play App Signing fingerprint (required once you upload to Play)
Play Console → Your app → **Setup → App signing** → copy the **SHA-256 certificate
fingerprint** under "App signing key certificate".
### 2. Upload keystore fingerprint (`release-keystore.jks`)
```powershell
keytool -list -v -keystore app\release-keystore.jks -alias neurocomet | Select-String SHA256
```
Google accepts either `F9:F1:DA:DD:...` or `f9f1dadd...` format.
### 3. Debug keystore (optional — only if you want deep links on local debug builds)
```powershell
keytool -list -v -keystore $env:USERPROFILE\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android | Select-String SHA256
```
## Validate App Links
External validator:
<https://developers.google.com/digital-asset-links/tools/generator>
On device:
```powershell
adb shell pm verify-app-links --re-verify com.kyilmaz.neurocomet
adb shell pm get-app-links com.kyilmaz.neurocomet
```
You should see `getneurocomet.com` listed as **verified**.
