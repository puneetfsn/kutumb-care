# Kutumb Care — Web App

A 100% offline, bilingual (English / हिन्दी) companion to the Android app.

## Run it

- **Just open it:** double-click `index.html` (works from a USB stick, no internet).
- **Or serve it:** `python3 -m http.server 8080` in this folder, then visit
  `http://localhost:8080`. Serving over HTTP(S) enables the installable PWA
  (service worker + "Add to Home Screen").

## What it does

- First-launch English / हिन्दी chooser, remembered on the device.
- 24 seeded India-specific records, readiness %, nominee alerts, search, editor.
- Optional **passphrase lock (6+ characters)**: with it set, all records are stored in this browser as an encrypted vault (WebCrypto PBKDF2-HMAC-SHA256, 150k rounds → AES-256-GCM; the key exists only in memory). Existing 4-digit PINs still unlock, wrong tries are throttled, and the app re-locks after 30 s in background. **No recovery backdoor** — if the passphrase is forgotten, only a backup file can restore the data.
- JSON backup/restore **compatible with the Android app**, plus **passphrase-encrypted backups** (same PBKDF2 + AES-256-GCM envelope as Android) — the unlock secret is never exported.
- **Download my dossier (PDF)** and **Download blank form to fill by hand (PDF)**,
  generated locally in the chosen language (A4, print-ready).
- No network: CSP `connect-src 'none'`, no CDNs, no analytics. Data is stored only
  in this browser (`localStorage`) — export a backup before clearing browser data.
