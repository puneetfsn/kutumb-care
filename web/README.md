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
- Optional 4-digit PIN (SHA-256 hashed on-device; re-locks after 30 s in background).
- JSON backup/restore **compatible with the Android app** — the PIN is never exported.
- **Download my dossier (PDF)** and **Download blank form to fill by hand (PDF)**,
  generated locally in the chosen language (A4, print-ready).
- No network: CSP `connect-src 'none'`, no CDNs, no analytics. Data is stored only
  in this browser (`localStorage`) — export a backup before clearing browser data.
