# Kutumb Care · कुटुम्ब केयर

A private, 100% offline family emergency dossier for Indian families — as an **Android app** and a **web app**, in **English and हिन्दी**.

Kutumb Care helps you catalogue **where** identity papers, bank accounts, insurance policies, EPF / UAN, property deeds and trusted contacts live — so your people can find them in an emergency. It never asks for net-banking passwords, UPI PINs, ATM PINs, OTPs or CVVs.

When the time comes, print the **Family Emergency Dossier** PDF, seal it, and keep a copy with trusted kin or in a bank locker.

## Try it

- **Web app (phone or computer):** https://puneetfsn.github.io/kutumb-care/
  Open it once, then use **Install app on Home Screen** inside the page (or the
  browser menu → *Add to Home screen*) — it then works like an app, fully offline.
- **Android app (APK):** download the `kutumb-care-debug` artifact from the latest
  green *Build APK* run at https://github.com/puneetfsn/kutumb-care/actions
  (GitHub login required), install it, and the Kutumb Care icon appears in your
  app drawer — long-press it to place it on the Home Screen. A home-screen
  *Family readiness* widget is also included.

## Privacy contract

- **No internet.** `android.permission.INTERNET` is forbidden and stripped from the manifest.
- **No account, no cloud, no ads, no analytics.**
- All records stay in a local Room database on the phone. Record content, nominee cards and document photos are **encrypted at rest** (AES-256-GCM with an Android Keystore key that never leaves the device).
- Unlock with a **passphrase (6+ characters, or a longer PIN)** — never a short 4-digit code for new users (existing 4-digit PINs keep working until replaced). Android unlock also offers **fingerprint/face** with the passphrase as fallback. Wrong tries are throttled (escalating lockouts), screenshots/recent-apps previews are blocked while the lock is on (`FLAG_SECURE`), and the dossier re-locks after 30 seconds in the background. The unlock secret is never written to a backup.
- Cloud backup of app data is disabled. You can export your own JSON file.
- The printable PDF is generated on-device with Android’s `PdfDocument` API.

## What the app records (metadata only)

| Field | Example |
| --- | --- |
| Institution | SBI — MG Road branch |
| Nominee status | Verified / Needs nominee / Not applicable |
| Physical location | Master bedroom almirah / blue folder |
| Reference | Policy, account, folio or UAN number |
| Contact | LIC agent name and phone |

## India-specific checklist — 31 records (auto-seeded, English + हिन्दी)

Identity & civil · Banking & funds · Investments (Demat, EPF/UAN, PPF, NPS, gold) · Insurance · Property & loans · Digital legacy · Trusted contacts (CA, lawyer, agent).

## Features

1. **Family readiness score** on the home screen and a Glance home-screen widget.
2. **Missing nominee alerts** driven by what you actually recorded on each record.
3. **On-device A4 PDF dossier** shared through FileProvider (print, Drive, Bluetooth — your choice). Busy categories spill onto continuation pages instead of truncating.
4. **Optional passphrase lock** (fingerprint unlock on Android, attempt throttling, screenshot protection) that re-locks 30 seconds after the app goes to the background, dark / light theme, family profile.
5. **Search** across institution, reference, location, contact and notes.
6. **JSON backup & restore** via the system file picker, plus an optional **passphrase-encrypted backup** (PBKDF2-HMAC-SHA256, 150k rounds + AES-256-GCM) that opens in both the Android and web apps. The unlock secret is never exported. **There is no recovery backdoor:** write the passphrase on paper and seal it with your will — if it is forgotten, only a backup file can restore the data.

## Project structure

```
app/src/main/java/com/puneetfsn/kutumbcare/
  data/       Room entities, DAO, seeder, DataStore, JSON backup
  pdf/        Family Emergency Dossier generator
  ui/         Jetpack Compose screens (dashboard, records, PIN, settings)
  widget/     Glance readiness widget
  di/         Manual app container (no Hilt, no network)
.github/workflows/build.yml   Cloud APK build
app/src/test/                 JVM unit tests (readiness, field/backup encryption, backup format, re-lock)
```

## Web app (English + हिन्दी)

`web/` is a self-contained offline web app — no build step, no CDN, no network calls
(`connect-src 'none'`). Open `web/index.html` in any browser, or serve the folder:

```bash
cd web && python3 -m http.server 8080   # then open http://localhost:8080
```

- First launch asks **English / हिन्दी**; the choice is remembered and can be changed anytime.
- Same 24 records, readiness score, nominee alerts, search and record editor as Android.
- Data lives only in that browser on that device (`localStorage`). Clearing browser data
  erases it — keep a JSON backup or a printed dossier.
- **JSON backup is interchangeable with the Android app** (same format; the unlock secret is never exported). With a passphrase set, everything in this browser is stored as an encrypted WebCrypto vault (PBKDF2 + AES-256-GCM), and encrypted backups open in both apps.
- Two PDF downloads are generated on-device in the chosen language:
  **“Download my dossier”** (filled with your records) and
  **“Download blank form to fill by hand”** (an empty A4 booklet to print and complete with a pen).
- Installable as a PWA (“Add to Home Screen”) with the shielded-home icon; the service
  worker caches only the app shell so it keeps working with no connection.

## Per-entry fields (not one generic form)

Every record asks only for what that record needs, in the language chosen — an
Aadhaar card never asks for a "branch". Labels follow official guidance:

- **Aadhaar & PAN:** the full numbers are recorded (the family's own sealed copy — forms and logins need them); only OTPs and PINs are never written.
- **Demat/MF (SEBI 2026 nomination framework):** nominee name + relationship is the
  mandatory core; share % and contacts are optional.
- **EPF (EPFO):** e-nomination covers PF + EPS pension + EDLI; shares total 100%,
  a minor nominee needs a guardian.
- **Insurance (IRDAI 2024):** nominee name, relationship, contact; appointee for minors.
- **Digital online assets:** Gmail (Google Inactive Account Manager — 3–18 months,
  up to 10 phone-verified trusted contacts), Apple ID (Legacy Contact needs the
  access key + death certificate; Keychain passwords are never shared), other email,
  Income Tax account, password manager (emergency access), govt portals, social.
  The app stores **the map, never the keys**: account IDs, registered mobile,
  recovery email and *where the password is kept*.
- Navigation: the bottom bar now stays visible inside a record, with a
  discard-changes prompt for unsaved edits.
- The printed dossier turns blank fields into writing lines with extra space,
  so it can be completed by hand, and carries a fine-print privacy note on
  every page.

## Languages

Both apps ask **English / हिन्दी** on first launch and remember the choice (changeable in
Settings). On Android the 24 seeded records carry Hindi titles/descriptions in the database
(Room v2 migration — existing family data is preserved), and the PDF dossier and widget
follow the chosen language.

## App icon

The shielded-home emblem lives in `assets/kutumb-care-icon.png` and is generated into the
Android launcher mipmaps / adaptive icon and the web favicons + PWA icons (`web/icons/`).

## Build an APK (GitHub Actions)

This repository is set up so you never need Android Studio on a slow laptop.

1. Push to `main` or any `arena/**` branch, or run **Actions → Build APK → Run workflow**.
2. When the job finishes, open the run and download the `kutumb-care-debug` artifact.
3. Install the APK on your phone (you will need to allow unknown sources).

The debug APK is signed with the public debug keystore. It is installable for testing, not for Play Store.

The workflow also runs the unit tests and then **fails the build if the assembled APK requests
`INTERNET`, `ACCESS_NETWORK_STATE` or `ACCESS_WIFI_STATE`**, so the privacy claim is checked on every push.

## Web app (English + हिन्दी)

`web/` is a self-contained offline web app — no build step, no CDN, no network calls
(`connect-src 'none'`). Open `web/index.html` in any browser, or serve the folder:

```bash
cd web && python3 -m http.server 8080   # then open http://localhost:8080
```

- First launch asks **English / हिन्दी**; the choice is remembered and can be changed anytime.
- Same 24 records, readiness score, nominee alerts, search and record editor as Android.
- Data lives only in that browser on that device (`localStorage`). Clearing browser data
  erases it — keep a JSON backup or a printed dossier.
- **JSON backup is interchangeable with the Android app** (same format; the unlock secret is never exported). With a passphrase set, everything in this browser is stored as an encrypted WebCrypto vault (PBKDF2 + AES-256-GCM), and encrypted backups open in both apps.
- Two PDF downloads are generated on-device in the chosen language:
  **“Download my dossier”** (filled with your records) and
  **“Download blank form to fill by hand”** (an empty A4 booklet to print and complete with a pen).
- Installable as a PWA (“Add to Home Screen”) with the shielded-home icon; the service
  worker caches only the app shell so it keeps working with no connection.

## Per-entry fields (not one generic form)

Every record asks only for what that record needs, in the language chosen — an
Aadhaar card never asks for a "branch". Labels follow official guidance:

- **Aadhaar & PAN:** the full numbers are recorded (the family's own sealed copy — forms and logins need them); only OTPs and PINs are never written.
- **Demat/MF (SEBI 2026 nomination framework):** nominee name + relationship is the
  mandatory core; share % and contacts are optional.
- **EPF (EPFO):** e-nomination covers PF + EPS pension + EDLI; shares total 100%,
  a minor nominee needs a guardian.
- **Insurance (IRDAI 2024):** nominee name, relationship, contact; appointee for minors.
- **Digital online assets:** Gmail (Google Inactive Account Manager — 3–18 months,
  up to 10 phone-verified trusted contacts), Apple ID (Legacy Contact needs the
  access key + death certificate; Keychain passwords are never shared), other email,
  Income Tax account, password manager (emergency access), govt portals, social.
  The app stores **the map, never the keys**: account IDs, registered mobile,
  recovery email and *where the password is kept*.
- Navigation: the bottom bar now stays visible inside a record, with a
  discard-changes prompt for unsaved edits.
- The printed dossier turns blank fields into writing lines with extra space,
  so it can be completed by hand, and carries a fine-print privacy note on
  every page.

## Languages

Both apps ask **English / हिन्दी** on first launch and remember the choice (changeable in
Settings). On Android the 24 seeded records carry Hindi titles/descriptions in the database
(Room v2 migration — existing family data is preserved), and the PDF dossier and widget
follow the chosen language.

## App icon

The shielded-home emblem lives in `assets/kutumb-care-icon.png` and is generated into the
Android launcher mipmaps / adaptive icon and the web favicons + PWA icons (`web/icons/`).

## Build locally

Requires JDK 17 and Android SDK 34.

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest   # JVM tests, no device needed
```

## Tech stack

Kotlin · Jetpack Compose (Material 3) · Room · DataStore · Coroutines / Flow · Glance · native `PdfDocument` · Gradle 8.7 · AGP 8.5.2

## Safety reminder

Never type a password, UPI PIN, ATM PIN, OTP or CVV into Kutumb Care. Write a **location note** instead (“screen-unlock note is in the Godrej safe”).
