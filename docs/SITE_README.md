# NeuroComet Web — `getneurocomet.com`
Static site served via **GitHub Pages** from the repo's `docs/` folder.
## Layout
```
docs/
  CNAME                    → "getneurocomet.com" (custom domain binding)
  .nojekyll                → disables Jekyll so /.well-known/* is served verbatim
  index.html               → https://getneurocomet.com/
  privacy.html             → https://getneurocomet.com/privacy.html  (legacy)
  privacy/index.html       → https://getneurocomet.com/privacy
  terms.html               → https://getneurocomet.com/terms.html    (legacy)
  terms/index.html         → https://getneurocomet.com/terms
  delete-account/index.html → https://getneurocomet.com/delete-account
  .well-known/
    assetlinks.json        → https://getneurocomet.com/.well-known/assetlinks.json
                             (Android App Links proof — MUST be served with
                              Content-Type: application/json and NO redirects.)
```
## Deploying to GitHub Pages
### 1. Push this commit
```powershell
git add docs
git commit -m "Add getneurocomet.com site"
git push
```
### 2. Enable GitHub Pages
**Repo Settings → Pages**
- Source: **Deploy from a branch**
- Branch: `main` (or whichever you use)
- Folder: **`/docs`**  ← now selectable because the folder exists on the branch
- Save.
### 3. DNS at your registrar
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
### 4. Tick "Enforce HTTPS" once the cert is provisioned (5–15 min after DNS).
### 5. Verify
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
### 3. Debug keystore (optional — only if you want deep links on local debug builds)
```powershell
keytool -list -v -keystore $env:USERPROFILE\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android | Select-String SHA256
```
## Validate App Links on device
```powershell
adb shell pm verify-app-links --re-verify com.kyilmaz.neurocomet
adb shell pm get-app-links com.kyilmaz.neurocomet
```
You should see `getneurocomet.com` listed as **verified**.
