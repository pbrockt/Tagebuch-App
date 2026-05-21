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

[![GitHub Release](https://img.shields.io/github/v/release/pbrockt/Tagebuch-App?label=Release&color=purple)](https://github.com/pbrockt/Tagebuch-App/releases/latest)
[![Build APK](https://github.com/pbrockt/Tagebuch-App/actions/workflows/build.yml/badge.svg)](https://github.com/pbrockt/Tagebuch-App/actions/workflows/build.yml)
![Android](https://img.shields.io/badge/Android-15+-green)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-blue)

</div>

---

## 💜 Die Geschichte hinter dieser App

Diese App entstand aus einer ganz persönlichen Idee: Ich wollte für **meine Tochter** ein digitales Tagebuch entwickeln — eines, das genau so funktioniert wie wir es uns wünschen. Kein Kompromiss mit fremden Apps, keine unnötigen Features, keine Datenweitergabe. Einfach ihr ganz eigenes, privates Tagebuch.

Da ich kein erfahrener Entwickler bin, habe ich die App **gemeinsam mit der KI [Claude](https://claude.ai) von Anthropic** entwickelt. Claude hat den gesamten Code geschrieben, Fehler behoben und neue Features nach meinen Wünschen umgesetzt — ich habe die Ideen geliefert und Feedback gegeben.

Dieses Projekt ist für mich gleichzeitig eine **Lernreise in die Programmierung**. Ich nutze den gemeinsam entstandenen Code, um Kotlin und Android-Entwicklung zu verstehen und peu à peu selbst darin einzutauchen.

> *„Ich bin der KI sehr dankbar für diese Möglichkeit — eine App nach unseren ganz eigenen Wünschen zu bauen, die meine Tochter jeden Tag nutzt."*

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
- Clientseitige Verschlüsselung (PBKDF2 + AES-GCM)
- Verbindungs-Test-Button in den Einstellungen
- Offline-first

### 🎨 Design & Anpassung
- **4 Schriftarten:** Standard · Serif · Schreibmaschine · Kursiv
- **6 Akzentfarben:** Lila, Blau, Grün, Orange, Rosa, Türkis
- **Light / Dark / Systemstandard** Theme
- Material Design 3

### ⚙️ Einstellungen
- **Versions-Check** — prüft ob eine neue Version auf GitHub verfügbar ist
- Geburtstage aus Kontakten + eigener Geburtstag (TT.MM)
- Kalender-Ansicht wählen: Stimmung / Wetter / Beides
- Berechtigungen direkt in der App verwalten

---

## 📱 Installation

1. **[Neueste Version herunterladen →](https://github.com/pbrockt/Tagebuch-App/releases/latest)**
2. APK auf das Android-Gerät übertragen
3. **Einstellungen → Sicherheit → Unbekannte Quellen** erlauben
4. APK installieren

> Direktes Update über ältere Versionen möglich (kein Deinstallieren nötig).

**Anforderungen:** Android 15 (API 35) oder höher

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

## 🤖 Entwicklung mit KI

Der gesamte Code dieser App wurde von [Claude](https://claude.ai) (Anthropic) geschrieben. Die Zusammenarbeit lief so:

1. Ich habe Anforderungen und Ideen formuliert
2. Claude hat den Code implementiert, Fehler behoben und erklärt
3. Ich habe getestet, Feedback gegeben und neue Wünsche geäußert
4. Wir haben iterativ verbessert — von v0.1 bis heute

Dieses Projekt zeigt, was heute mit KI-Unterstützung möglich ist: Eine vollständige, sichere Android-App — maßgeschneidert für eine ganz persönliche Verwendung.

---

## 📋 Changelog

### v0.2b (9)
- Versions-Check zeigt jetzt Versionscode in Klammern: `v0.2b (9)`
- Version aus Kalender und Einstellungen entfernt
- Repo öffentlich gestellt → Versions-Check funktioniert

### v0.2a (8)
- Fix: Bottom-NavigationBar überdeckte Inhalte
- Neu: Versions-Check in Einstellungen

### v0.2 (7) — Visuelles Redesign + Geburtstage
- 4 wählbare Schriftarten, Gradient-Hintergrund, Slide-Animation
- Geburtstage aus Kontakten (🎈 / 👑), Bottom Navigation Bar
- Animierte PIN-Dots, Rich-Text-Editor, Gradient-Statistiken

### v0.1e (6) — v0.1b (3)
- Diverse Bugfixes: Stimmungs-Icon, WebDAV-Crash, Serialisierung

### v0.1 (1) — Erste Veröffentlichung
- Kalender, Editor, PIN-Schutz, WebDAV-Sync, Statistiken, PDF-Export, Suche

---

<div align="center">

*Entwickelt mit ❤️ für meine Tochter — mit Hilfe von 🤖 [Claude](https://claude.ai) (Anthropic)*

</div>
