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

> 🤖 *Diese App wurde vollständig durch künstliche Intelligenz ([Claude](https://claude.ai) von Anthropic) entwickelt — von der Architektur über den Code bis hin zu diesem README.*

[![GitHub Release](https://img.shields.io/github/v/release/pbrockt/Tagebuch-App?label=Release&color=purple)](https://github.com/pbrockt/Tagebuch-App/releases/latest)
[![Build APK](https://github.com/pbrockt/Tagebuch-App/actions/workflows/build.yml/badge.svg)](https://github.com/pbrockt/Tagebuch-App/actions/workflows/build.yml)
![Android](https://img.shields.io/badge/Android-15+-green)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-blue)

</div>

---

## 📦 Version 0.1 — Was diese Version bietet

Dies ist die erste vollständige Veröffentlichung der Tagebuch-App. Alle unten genannten Features sind in **Version 0.1** enthalten und funktionsfähig.

### 🔒 Sicherheit & Datenschutz

Die App legt höchsten Wert auf Privatsphäre. Alle Daten bleiben auf dem Gerät — kein Account, kein Cloud-Zwang, keine Telemetrie.

- **4-stelliger PIN** schützt den Zugang zur App
- **Automatische Sperre** beim Verlassen der App — beim Zurückkehren ist sofort der PIN-Screen aktiv
- **PIN-Versuch-Limit:** Nach 5 falschen Eingaben wird die App 30 Sekunden gesperrt (mit Countdown-Anzeige)
- **Haptic Feedback** bei jeder PIN-Ziffer (spürbares Tipp-Gefühl)
- **AES-256-GCM Verschlüsselung** — die Datenbank ist durch den Android Keystore geschützt
- Keine Backups, kein Cloud-Upload ohne explizite Konfiguration

### 📅 Kalender

Der Hauptbildschirm zeigt eine Monatsübersicht.

- Monatsansicht mit Vor-/Zurück-Navigation
- **Einträge-Zähler** im Header (z. B. „Mai · 7 Einträge")
- Tage mit Einträgen haben einen farbigen Punkt
- **Stimmungs-Farbe** pro Tag: Grün (super) → Gelb (okay) → Rot (schrecklich)
- Kleines **Wetter-Icon** auf dem jeweiligen Tag
- Versionsnummer rechts unten

### ✏️ Eintrags-Editor

Klick auf einen Tag öffnet ein großes Popup (~90 % Bildschirmhöhe).

- **Stimmung wählen:** 😁 Super / 😊 Gut / 😐 Ok / 😔 Schlecht / 😢 Schrecklich
- **Wetter wählen:** ☀️ Sonnig / ☁️ Bewölkt / 🌧️ Regen / ❄️ Schnee / ⛈️ Gewitter
- **Mehrere Seiten pro Tag** — Tabs zum Wechseln, neue Seite hinzufügen, Seite löschen
- **Freitextfeld** — schreibe deinen Tagebucheintrag
- **Emoji-Canvas** — wähle Emojis aus der Leiste, platziere sie frei auf der Seite, verschiebe und skaliere sie per Pinch
- **Bilder einfügen** — öffne die Galerie, wähle ein Foto, platziere es frei auf der Seite

### 📊 Statistiken

Ein eigener Statistik-Screen zeigt Übersichten über deine Schreibgewohnheiten.

- 🔥 **Aktueller Streak** — wie viele Tage in Folge du geschrieben hast
- 🏆 **Längster Streak** aller Zeiten
- ✍️ **Gesamtzahl Wörter** über alle Einträge
- 📅 **Gesamtzahl Tage** mit Einträgen
- **Monats-Balkendiagramm** — Einträge der letzten 6 Monate auf einen Blick
- **Stimmungsverteilung** — wie oft war welche Stimmung die häufigste

### 🔍 Suche

- Volltext-Suche über alle Einträge (ab 2 Zeichen Eingabe)
- Ergebnisse zeigen Datum und Textvorschau
- Debounced — sucht automatisch beim Tippen

### 🔔 Erinnerungen

- Tägliche Push-Benachrichtigung zur selbst gewählten Uhrzeit
- „Hast du deinen Tag schon festgehalten? ✏️"
- In Einstellungen aktivierbar/deaktivierbar + Uhrzeit konfigurierbar

### 📄 PDF-Export

- Aktuellen Monat als PDF exportieren
- Enthält: alle Einträge mit Datum, Stimmung, Wetter und Seiteninhalt
- Teilen über beliebige App (WhatsApp, E-Mail, Signal, etc.)

### ☁️ WebDAV-Synchronisation

- Sync mit eigenem WebDAV-Server (Nextcloud, ownCloud, etc.)
- **Clientseitige Verschlüsselung** — der Server sieht nur verschlüsselte Dateien
- Schlüssel wird aus deiner Passphrase via PBKDF2 abgeleitet — ohne Passphrase keine Entschlüsselung
- Bidirektional: Geräte- und Serveränderungen werden zusammengeführt
- **Offline-first** — App funktioniert ohne Verbindung, Sync erfolgt beim nächsten Start

### 🎨 Design & Anpassung

- **6 Akzentfarben** wählbar: Lila, Blau, Grün, Orange, Rosa, Türkis
- **Light / Dark / Systemstandard** Theme
- Material Design 3
- Florale Hintergrund-Dekoration auf dem PIN-Screen

### ⚙️ Einstellungen

Alle relevanten Optionen sind in einem Einstellungs-Screen zusammengefasst:

- PIN ändern / entfernen
- Akzentfarbe & Theme wählen
- Erinnerungszeit konfigurieren
- WebDAV-Server einrichten
- **Berechtigungen direkt in der App** verwalten (Benachrichtigungen, Fotos)

---

## 📱 Installation

### Voraussetzungen
- Android 15 oder höher
- Ca. 100 MB freier Speicherplatz

### Schritte
1. **[Neueste Version herunterladen →](https://github.com/pbrockt/Tagebuch-App/releases/latest)**
2. `Tagebuch-App-v0.1.apk` auf das Android-Gerät übertragen (z. B. per USB oder Cloud)
3. Auf dem Gerät: **Einstellungen → Sicherheit → Unbekannte Quellen erlauben**
4. APK antippen → Installieren

> **Hinweis bei Updates:** Beim ersten Update von einer älteren Version muss die alte App ggf. zuerst deinstalliert werden. Danach sind direkte Updates möglich.

---

## 🏗️ Technischer Stack

| Bereich | Technologie |
|---|---|
| Sprache | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architektur | MVVM + Repository Pattern |
| Datenbank | Room + SQLCipher (AES-256) |
| Dependency Injection | Hilt |
| Netzwerk | OkHttp (WebDAV) |
| Bilder laden | Coil |
| Hintergrundaufgaben | WorkManager |
| Verschlüsselung | Android Keystore + AES-GCM + PBKDF2 |
| Build-System | Gradle 8.9 + KSP |
| CI/CD | GitHub Actions |

---

## 🤖 KI-Entwicklung

Diese App wurde **vollständig von [Claude](https://claude.ai) (Anthropic)** entwickelt — einem großen Sprachmodell der Firma Anthropic.

Der gesamte Entwicklungsprozess lief in einer einzigen Konversation:

1. Anforderungen aus `ToDo.txt` analysiert
2. Architektur entworfen (MVVM, Room, Hilt, Compose)
3. ~40 Kotlin-Quelldateien geschrieben (~4.000 Zeilen Code)
4. Build-Fehler aus GitHub Actions Logs analysiert und behoben
5. Features iterativ auf Nutzerfeedback erweitert

| | |
|---|---|
| **Menschlicher Aufwand** | Anforderungen formulieren + Feedback geben |
| **KI-Aufwand** | Architektur, Code, Debugging, CI/CD, README, Release |

---

## 📋 Changelog

### v0.1 — Erste Veröffentlichung
**Kern-Features:**
- Kalenderansicht mit Monatsnavigation und Einträge-Zähler
- Tagebucheditor mit freiem Emoji/Bild-Canvas
- Stimmungs- und Wetter-Tracking pro Tag
- 4-stelliger PIN mit automatischer Sperre und Versuch-Limit
- Volltext-Suche, Statistiken, PDF-Export
- WebDAV-Sync mit clientseitiger Verschlüsselung
- 6 Farbthemen + Light/Dark Mode
- Tägliche Erinnerungs-Benachrichtigung
- Berechtigungsverwaltung direkt in der App

---

<div align="center">

*Version 0.1 · erstellt mit ❤️ und 🤖 von [Claude](https://claude.ai) (Anthropic)*

</div>
