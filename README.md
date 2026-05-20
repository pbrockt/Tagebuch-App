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

> 🤖 *Diese App wurde vollständig durch künstliche Intelligenz (Claude von Anthropic) entwickelt — von der Architektur über den Code bis hin zu diesem README.*

[![Build APK](https://github.com/pbrockt/Tagebuch-App/actions/workflows/build.yml/badge.svg)](https://github.com/pbrockt/Tagebuch-App/actions/workflows/build.yml)
![Version](https://img.shields.io/badge/Version-0.1a-purple)
![Android](https://img.shields.io/badge/Android-15+-green)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-blue)

</div>

---

## ✨ Features

### 🔒 Sicherheit & Datenschutz
- **4-stelliger PIN-Schutz** — App sperrt sich sofort beim Verlassen, PIN-Abfrage beim Zurückkehren
- **AES-256-GCM Verschlüsselung** — alle lokalen Daten sind mit Android Keystore verschlüsselt
- **Kein Cloud-Zwang** — funktioniert vollständig offline
- **Kein Backup** — Daten verlassen das Gerät nur über WebDAV, das du selbst konfigurierst

### 📅 Kalender
- Monatsansicht mit Navigation
- Farbige Tages-Markierung je nach **Stimmung** (grün → rot)
- Kleines **Wetter-Icon** pro Tag
- Punkt-Indikator für Tage mit Einträgen
- Florale Hintergrund-Dekoration in der Akzentfarbe

### ✏️ Eintrags-Editor
- Mehrere **Seiten pro Tag** (Tabs zum Durchschalten)
- Freies **Textfeld** für Tagebucheinträge
- **Emoji-Canvas** — Emojis frei auf der Seite platzieren, verschieben & skalieren
- **Bild-Picker** — Fotos aus der Galerie einfügen und frei positionieren
- **Stimmungs-Auswahl**: 😁 Super / 😊 Gut / 😐 Ok / 😔 Schlecht / 😢 Schrecklich
- **Wetter-Auswahl**: ☀️ Sonnig / ☁️ Bewölkt / 🌧️ Regen / ❄️ Schnee / ⛈️ Gewitter

### 📊 Statistiken
- 🔥 Aktueller Streak (Tage in Folge)
- 🏆 Längster Streak aller Zeiten
- ✍️ Gesamtanzahl geschriebener Wörter
- Monats-Balkendiagramm (letzte 6 Monate)
- Stimmungsverteilung als Fortschrittsbalken

### 🔍 Suche
- Volltext-Suche über alle Einträge (ab 2 Zeichen)
- Ergebnisse mit Datum und Textvorschau

### 🔔 Erinnerungen
- Tägliche Benachrichtigung zur konfigurierbaren Uhrzeit
- „Hast du deinen Tag schon festgehalten?"

### 📄 PDF-Export
- Aktuellen Monat als PDF exportieren
- Mit Stimmung, Wetter und allen Seiteneinträgen
- Teilen via beliebige App (WhatsApp, E-Mail, etc.)

### ☁️ WebDAV-Synchronisation
- Bidirektionale Sync mit eigenem Server (Nextcloud, ownCloud, etc.)
- **Clientseitige Verschlüsselung** — Server sieht nur verschlüsselte Dateien
- Schlüssel wird aus deiner Passphrase via PBKDF2 abgeleitet
- Offline-first: funktioniert ohne Verbindung, synct beim nächsten Start

### 🎨 Design
- **6 Akzentfarben**: Lila, Blau, Grün, Orange, Rosa, Türkis
- **Light / Dark / Systemstandard** Theme
- Material Design 3
- Florale Hintergrund-Muster in der gewählten Farbe

---

## 📱 Installation

1. [Neueste APK herunterladen](https://github.com/pbrockt/Tagebuch-App/actions) → neuesten Build öffnen → **Artifacts** → `tagebuch-debug`
2. ZIP entpacken
3. `app-debug.apk` auf das Android-Gerät übertragen
4. Einstellungen → Sicherheit → **Unbekannte Quellen** erlauben
5. APK installieren

> ⚠️ **Hinweis bei Updates:** Da dies ein Debug-Build ist, muss beim ersten Update-Versuch die alte Version ggf. zuerst deinstalliert werden. Ab dem zweiten Update sollte eine Direkt-Installation funktionieren (solange der CI-Cache aktiv ist).

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
| Bilder | Coil |
| Background | WorkManager |
| Verschlüsselung | Android Keystore + AES-GCM + PBKDF2 |
| Build | Gradle 8.9 + KSP |

---

## 🤖 KI-Entwicklung

Diese App wurde **vollständig von Claude (Anthropic)** entwickelt — einem großen Sprachmodell. Der gesamte Prozess lief über Claude Code:

- Anforderungsanalyse aus der `ToDo.txt`
- Architekturentscheidungen (MVVM, Room, Hilt, etc.)
- Schreiben aller Kotlin-Quelldateien (~40 Dateien, ~3.500 Zeilen Code)
- Fehlerbehebung anhand von GitHub Actions Build-Logs
- Iterative Verbesserungen auf Nutzerfeedback

**Entwicklungszeit:** Eine Konversation  
**Menschlicher Aufwand:** Anforderungen formulieren, Feedback geben  
**KI-Aufwand:** Alles andere

---

## 📁 Projektstruktur

```
app/src/main/java/com/pbrockt/tagebuch/
├── data/
│   ├── local/          # Room DB, Crypto, SecurePrefs
│   ├── model/          # DiaryDay, DiaryPage, PageMedia
│   ├── remote/         # WebDAV Client & SyncManager
│   └── repository/     # DiaryRepository, SyncRepository
├── export/             # PDF-Exporter
├── notifications/      # ReminderWorker, NotificationHelper
├── ui/
│   ├── auth/           # PIN-Screen
│   ├── calendar/       # Kalender + Popup-Editor
│   ├── editor/         # Canvas-Editor
│   ├── search/         # Volltext-Suche
│   ├── settings/       # Einstellungen
│   ├── stats/          # Statistiken
│   └── theme/          # Light/Dark + Farben + Blüten-Hintergrund
├── AppLockManager.kt   # App-Sperre bei Hintergrund
├── MainActivity.kt
└── TagebuchApplication.kt
```

---

<div align="center">

*Version 0.1a — erstellt mit ❤️ und 🤖*

</div>
