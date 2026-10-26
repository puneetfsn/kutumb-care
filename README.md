# Kutumb Care

A private, 100% offline Android emergency dossier for Indian families.

Kutumb Care helps you catalogue **where** identity papers, bank accounts, insurance policies, EPF / UAN, property deeds and trusted contacts live — so your people can find them in an emergency. It never asks for net-banking passwords, UPI PINs, ATM PINs, OTPs or CVVs.

When the time comes, print the **Family Emergency Dossier** PDF, seal it, and keep a copy with trusted kin or in a bank locker.

## Privacy contract

- **No internet.** `android.permission.INTERNET` is forbidden and stripped from the manifest.
- **No account, no cloud, no ads, no analytics.**
- All records stay in a local Room database on the phone.
- Optional 4-digit PIN is hashed with SHA-256 on device. It is never written to a backup and the dossier re-locks after 30 seconds in the background.
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

## India-specific checklist (auto-seeded)

Identity & civil · Banking & funds · Investments (Demat, EPF/UAN, PPF, NPS, gold) · Insurance · Property & loans · Digital legacy · Trusted contacts (CA, lawyer, agent).

## Features

1. **Family readiness score** on the home screen and a Glance home-screen widget.
2. **Missing nominee alerts** driven by what you actually recorded on each record.
3. **On-device A4 PDF dossier** shared through FileProvider (print, Drive, Bluetooth — your choice). Busy categories spill onto continuation pages instead of truncating.
4. **Optional PIN lock** that re-locks 30 seconds after the app goes to the background, dark / light theme, family profile.
5. **Search** across institution, reference, location, contact and notes.
6. **JSON backup & restore** via the system file picker. The PIN is never exported.

## Project structure

```
app/src/main/java/com/puneetfsn/kutumbcare/
  data/       Room entities, DAO, seeder, DataStore, JSON backup
  pdf/        Family Emergency Dossier generator
  ui/         Jetpack Compose screens (dashboard, records, PIN, settings)
  widget/     Glance readiness widget
  di/         Manual app container (no Hilt, no network)
.github/workflows/build.yml   Cloud APK build
app/src/test/                 JVM unit tests (readiness, PIN hash, backup format, re-lock)
```

## Build an APK (GitHub Actions)

This repository is set up so you never need Android Studio on a slow laptop.

1. Push to `main` or any `arena/**` branch, or run **Actions → Build APK → Run workflow**.
2. When the job finishes, open the run and download the `kutumb-care-debug` artifact.
3. Install the APK on your phone (you will need to allow unknown sources).

The debug APK is signed with the public debug keystore. It is installable for testing, not for Play Store.

The workflow also runs the unit tests and then **fails the build if the assembled APK requests
`INTERNET`, `ACCESS_NETWORK_STATE` or `ACCESS_WIFI_STATE`**, so the privacy claim is checked on every push.

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
