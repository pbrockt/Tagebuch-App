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
![Version](https://img.shields.io/badge/Version-0.1-purple)
![Android](https://img.shields.io/badge/Android-15+-green)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-blue)

</div>

---

## ✨ Features

### 🔒 Sicherheit & Datenschutz
- **4-stelliger PIN-Schutz** — sperrt sich sofort beim Verlassen der App
- **PIN-Versuch-Limit** — nach 5 Fehlversuchen 30 Sekunden Sperre
- **Haptic Feedback** — kurze Vibration bei jeder PIN-Eingabe
- **AES-256-GCM Verschlüsselung** — alle Daten durch Android Keystore geschützt
- **Kein Cloud-Zwang** — vollständig offline nutzbar
- **Kein Backup** — Daten verlassen das Gerät nur über selbst konfigurierten WebDAV

### 📅 Kalender
- Monatsansicht mit Navigation
- **Einträge-Zähler** im Monats-Header (z. B. „Mai · 12 Einträge")
- Farbige Tages-Markierung je nach **Stimmung** (grün → rot)
- Kleines **Wetter-Icon** pro Tag
- Punkt-Indikator für Tage mit Einträgen

### ✏️ Eintrags-Editor
- Mehrere **Seiten pro Tag** (Tabs)
- Freies **Textfeld** für Tagebucheinträge
- **Emoji-Canvas** — Emojis frei platzieren, verschieben & skalieren (Pinch)
- **Bild-Picker** — Fotos aus der Galerie einfügen und frei positionieren
- **Stimmungs-Auswahl**: 😁 Super / 😊 Gut / 😐 Ok / 😔 Schlecht / 😢 Schrecklich
- **Wetter-Auswahl**: ☀️ Sonnig / ☁️ Bewölkt / 🌧️ Regen / ❄️ Schnee / ⛈️ Gewitter

### 📊 Statistiken
- 🔥 Aktueller Streak + 🏆 Längster Streak
- ✍️ Gesamtanzahl Wörter
- Monats-Balkendiagramm (letzte 6 Monate)
- Stimmungsverteilung als Fortschrittsbalken

### 🔍 Suche
- Volltext-Suche über alle Einträge (ab 2 Zeichen)

### 🔔 Erinnerungen
- Tägliche Benachrichtigung zur konfigurierbaren Uhrzeit

### 📄 PDF-Export
- Aktuellen Monat als PDF exportieren und teilen

### ☁️ WebDAV-Synchronisation
- Bidirektionale Sync mit eigenem Server
- Clientseitige Verschlüsselung (PBKDF2 + AES-GCM)
- Offline-first

### 🎨 Design
- 6 Akzentfarben: Lila, Blau, Grün, Orange, Rosa, Türkis
- Light / Dark / Systemstandard Theme
- Material Design 3

---

## 📱 Installation

1. [Neueste APK herunterladen](https://github.com/pbrockt/Tagebuch-App/actions) → Build öffnen → **Artifacts** → `Tagebuch-App-v0.1`
2. `Tagebuch-App-v0.1.apk` auf das Gerät übertragen
3. Einstellungen → Sicherheit → **Unbekannte Quellen** erlauben
4. APK installieren

> **Hinweis:** Beim Update von einer früheren Version die alte App zuerst deinstallieren (einmalig nötig).

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

Diese App wurde **vollständig von Claude (Anthropic)** entwickelt:

- Anforderungsanalyse aus der `ToDo.txt`
- Architekturentscheidungen (MVVM, Room, Hilt, etc.)
- ~40 Kotlin-Quelldateien, ~4.000 Zeilen Code
- Fehlerbehebung anhand von GitHub Actions Build-Logs
- Iterative Verbesserungen auf Nutzerfeedback

**Menschlicher Aufwand:** Anforderungen formulieren + Feedback  
**KI-Aufwand:** Alles andere

---

## 📋 Changelog

### v0.1 (Release)
- Initiale Version mit allen Kern-Features
- PIN-Schutz mit Versuch-Limit (5x → 30s Sperre)
- Haptic Feedback bei PIN-Eingabe
- Einträge-Zähler im Kalender-Header
- Berechtigungen direkt in den Einstellungen verwalten
- Stimmungs- & Wetter-Tracking
- Statistiken mit Streak-Anzeige
- PDF-Export, Suche, WebDAV-Sync
- 6 Farbthemen + Light/Dark Mode

---

<div align="center">

*Version 0.1 — erstellt mit ❤️ und 🤖 von Claude (Anthropic)*

</div>
