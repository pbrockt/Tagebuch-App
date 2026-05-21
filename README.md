<div align="center">

<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120" viewBox="0 0 108 108">
  <rect width="108" height="108" rx="22" fill="#6650A4"/>
  <rect x="65" y="17" width="7" height="74" rx="1" fill="#E0E0E0"/>
  <rect x="67" y="17" width="7" height="74" rx="1" fill="#EEEEEE"/>
  <path d="M24,15 L65,15 Q69,15 69,19 L69,89 Q69,93 65,93 L24,93 Q20,93 20,89 L20,19 Q20,15 24,15 Z" fill="#FFFFFF"/>
  <path d="M57,15 L67,15 L67,42 L62,37 L57,42 Z" fill="#D0BCFF"/>
  <rect x="27" y="35" width="35" height="2" rx="1" fill="#CCCCCC"/>
  <rect x="27" y="44" width="35" height="2" rx="1" fill="#CCCCCC"/>
  <rect x="27" y="53" width="35" height="2" rx="1" fill="#CCCCCC"/>
  <rect x="27" y="62" width="23" height="2" rx="1" fill="#CCCCCC"/>
  <path d="M44,75 C44,73 42,71 40,73 L38,75 C38,77 40,79 44,82 C48,79 50,77 50,75 L48,73 C46,71 44,73 44,75 Z" fill="#EFB8C8"/>
</svg>

# 📔 Tagebuch-App

**Eine private, verschlüsselte Tagebuch-App für Android 15+**

> 🤖 *Diese App wurde vollständig durch künstliche Intelligenz ([Claude](https://claude.ai) von Anthropic) entwickelt.*

[![GitHub Release](https://img.shields.io/github/v/release/pbrockt/Tagebuch-App?label=Release&color=purple)](https://github.com/pbrockt/Tagebuch-App/releases/latest)
[![Build APK](https://github.com/pbrockt/Tagebuch-App/actions/workflows/build.yml/badge.svg)](https://github.com/pbrockt/Tagebuch-App/actions/workflows/build.yml)
![Android](https://img.shields.io/badge/Android-15+-green)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-blue)

</div>

---

## 📦 Aktuelle Version: 0.2a

---

## ✨ Features

### 🔒 Sicherheit & Datenschutz
- **4-stelliger PIN** mit automatischer Sperre beim Verlassen der App
- **PIN-Versuch-Limit:** 5 Fehlversuche → 30 Sekunden Sperre mit Countdown
- **Animierte PIN-Dots:** Bounce beim Eingeben, Shake-Animation bei falschem PIN
- **Haptic Feedback** bei jeder PIN-Ziffer
- **AES-256-GCM Verschlüsselung** — Datenbank durch Android Keystore geschützt
- Kein Cloud-Zwang, kein Account, keine Telemetrie

### 📅 Kalender
- Monatsansicht mit Vor-/Zurück-Navigation und Slide-Animation
- **Einträge-Zähler** im Monats-Header
- **Stimmungs-Emoji** unter der Tageszahl: 😁 😊 😐 😔 😢
- **Wetter-Emoji** über der Tageszahl: ☀ ☁ 🌧 ❄ ⛈
- **1–3 Punkte** je nach Anzahl der Seiten pro Tag
- **🎈 Geburtstags-Icon** für Kontakt-Geburtstage
- **👑 Krone** auf dem eigenen Geburtstag
- Gradient-Hintergrund in der gewählten Akzentfarbe

### ✏️ Eintrags-Editor
- Mehrere **Seiten pro Tag** (Tabs)
- **Rich-Text-Formatierung:** Fett, Kursiv, 6 Textfarben, Formatierung löschen
- Stimmungs- und Wetter-Auswahl direkt im Popup
- Wochentag groß als Popup-Header

### 📊 Statistiken
- 🔥 Aktueller Streak & 🏆 Längster Streak
- ✍️ Gesamtanzahl Wörter & 📅 Tage mit Einträgen
- Monats-Balkendiagramm (letzte 6 Monate) mit Gradient
- Stimmungsverteilung als Fortschrittsbalken

### 🔍 Suche
- Volltext-Suche über alle Einträge (ab 2 Zeichen, Live-Suche)

### 🔔 Erinnerungen
- Tägliche Push-Benachrichtigung zur konfigurierbaren Uhrzeit

### 📄 PDF-Export
- Aktuellen Monat exportieren & teilen (WhatsApp, E-Mail, etc.)

### ☁️ WebDAV-Synchronisation
- Sync mit eigenem Server (Nextcloud, ownCloud, etc.)
- Clientseitige Verschlüsselung (PBKDF2 + AES-GCM) — Server sieht nur verschlüsselte Daten
- Verbindungs-Test-Button in den Einstellungen
- Offline-first — funktioniert ohne Verbindung

### 🎨 Design & Anpassung
- **4 Schriftarten:** Standard (Roboto) · Serif · Schreibmaschine · Kursiv
- **6 Akzentfarben:** Lila, Blau, Grün, Orange, Rosa, Türkis
- **Light / Dark / Systemstandard** Theme
- Material Design 3

### ⚙️ Einstellungen
- **Versions-Check** ganz oben — prüft automatisch ob eine neue Version auf GitHub verfügbar ist
- PIN-Verwaltung, Schriftart, Farbthema, Kalender-Ansicht (Stimmung / Wetter / Beides)
- Tägliche Erinnerung konfigurieren
- WebDAV-Server einrichten & testen
- **Geburtstage:** Kontakte-Berechtigung erteilen + eigenen Geburtstag (TT.MM) eintragen
- Berechtigungen direkt in der App verwalten (Benachrichtigungen, Kontakte)

### 🗂️ Navigation
- Bottom Navigation Bar: 🏠 Kalender · 🔍 Suche · 📊 Statistiken · ⚙️ Einstellungen

---

## 📱 Installation

### Voraussetzungen
- Android 15 (API 35) oder höher

### Schritte
1. **[Neueste Version herunterladen →](https://github.com/pbrockt/Tagebuch-App/releases/latest)**
2. APK auf das Gerät übertragen
3. **Einstellungen → Sicherheit → Unbekannte Quellen** erlauben
4. APK installieren

> Direktes Update über ältere Versionen möglich (solange versionCode größer ist).

---

## 🏗️ Technischer Stack

| Bereich | Technologie |
|---|---|
| Sprache | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architektur | MVVM + Repository Pattern |
| Datenbank | Room + SQLCipher (AES-256) |
| Dependency Injection | Hilt |
| Netzwerk | OkHttp (WebDAV + Update-Check) |
| Bilder laden | Coil |
| Hintergrundaufgaben | WorkManager |
| Verschlüsselung | Android Keystore + AES-GCM + PBKDF2 |
| Build | Gradle 8.9 + KSP + R8 |
| CI/CD | GitHub Actions |

---

## 🤖 KI-Entwicklung

Diese App wurde **vollständig von [Claude](https://claude.ai) (Anthropic)** entwickelt.

| | |
|---|---|
| **Menschlicher Aufwand** | Anforderungen formulieren + Feedback geben |
| **KI-Aufwand** | Architektur, Code, Debugging, CI/CD, README, Releases |

---

## 📋 Changelog

### v0.2a
- Fix: Bottom-NavigationBar überdeckte Inhalte (NavigationBar-Padding)
- Neu: Versions-Check in Einstellungen (prüft GitHub Releases API)

### v0.2 — Visuelles Redesign + Geburtstage
- 4 wählbare Schriftarten
- Gradient-Hintergrund im Kalender, Slide-Animation beim Monatswechsel
- Geburtstage aus Kontakten (🎈), eigener Geburtstag (👑)
- Animierte PIN-Dots (Bounce + Shake)
- Bottom Navigation Bar
- Gradient-Statistik-Karten
- 1–3 Dots je nach Seitenanzahl
- Rich-Text-Formatierung im Editor (Fett, Kursiv, Farben)
- Foto-Berechtigung entfernt (Feature nicht aktiv)

### v0.1e
- Fix: Stimmungs-Emoji als sichtbares Icon im Kalender (statt unsichtbarer Hintergrundfarbe)

### v0.1d
- Fix: Kalender-Ansicht (Stimmung/Wetter) wurde nach Einstellungsänderung nicht aktualisiert

### v0.1c
- Fix: `@Serializable` fehlte auf `DiaryPage` + `DiaryDay` → WebDAV-Sync schlug fehl
- Fix: ProGuard-Regeln für kotlinx.serialization

### v0.1b
- Fix: App-Crash bei WebDAV (CancellationException, Response-Leaks, URL-Parsing)
- Neu: „Verbindung testen"-Button in WebDAV-Einstellungen

### v0.1a
- Rich-Text-Editor (Canvas entfernt, Fett/Kursiv/Farbe)
- Stimmung/Wetter-Labels bündig ausgerichtet
- Kalender-Ansicht umschaltbar (Stimmung / Wetter / Beides) in Einstellungen
- versionCode = 2

### v0.1 — Erste Veröffentlichung
- Kalender, Editor, Stimmung & Wetter, PIN-Schutz, WebDAV-Sync, Statistiken, PDF-Export, Suche

---

<div align="center">

*Version 0.2a · erstellt mit ❤️ und 🤖 von [Claude](https://claude.ai) (Anthropic)*

</div>
