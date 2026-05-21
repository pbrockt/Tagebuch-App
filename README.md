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

> ❤️ *Entwickelt für meine Tochter — mit Hilfe von [Claude](https://claude.ai) (Anthropic)*

[![GitHub Release](https://img.shields.io/github/v/release/pbrockt/Tagebuch-App?label=Release&color=purple)](https://github.com/pbrockt/Tagebuch-App/releases/latest)
[![Build APK](https://github.com/pbrockt/Tagebuch-App/actions/workflows/build.yml/badge.svg)](https://github.com/pbrockt/Tagebuch-App/actions/workflows/build.yml)
![Android](https://img.shields.io/badge/Android-15+-green)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-blue)

</div>

---

## 💜 Die Geschichte hinter dieser App

Diese App entstand aus einer ganz persönlichen Idee: Ich wollte für **meine Tochter** ein digitales Tagebuch entwickeln — eines, das genau so funktioniert wie wir es uns wünschen. Kein Kompromiss mit fremden Apps, keine unnötigen Features, keine Datenweitergabe.

Da ich kein erfahrener Entwickler bin, habe ich die App **gemeinsam mit der KI [Claude](https://claude.ai) von Anthropic** entwickelt. Dieses Projekt ist für mich gleichzeitig eine **Lernreise in die Programmierung** — ich nutze den gemeinsam entstandenen Code um Kotlin und Android-Entwicklung zu verstehen.

> *„Ich bin der KI sehr dankbar für diese Möglichkeit — eine App nach unseren ganz eigenen Wünschen zu bauen."*

---

## ✨ Features

### 🔒 Sicherheit & Datenschutz
- **4-stelliger PIN** — App schließt sich beim Verlassen **komplett** (kein Durchschauen im Recents-Screen)
- **PIN-Versuch-Limit:** 5 Fehlversuche → 30 Sekunden Sperre
- **Animierte PIN-Dots:** Bounce beim Eingeben, Shake bei falschem PIN
- **Haptic Feedback** bei jeder Ziffer
- **Schloss für alte Einträge:** 🔒 Ältere Tage sind gesperrt — PIN nötig zum Bearbeiten. Heutiger Tag immer offen
- **AES-256-GCM Verschlüsselung** — Datenbank durch Android Keystore geschützt
- Kein Cloud-Zwang, kein Account, keine Telemetrie

### 📅 Kalender
- Monatsansicht mit Slide-Animation beim Wechsel
- **Monats-Zusammenfassung:** „12 Einträge · 😊 meist gute Laune · 🔥 5 Tage Streak"
- **Tages-Motivationsspruch** — täglich wechselnd (105 Sprüche offline)
- Stimmungs-Emoji und Wetter-Icon pro Tag (umschaltbar in Einstellungen)
- 1–3 Punkte je nach Anzahl der Seiten
- **🎈 Geburtstags-Icons** aus Kontakten, **👑 Krone** auf dem eigenen Geburtstag

### ✏️ Eintrags-Editor
- Mehrere **Seiten pro Tag** (Tabs)
- **Rich-Text:** Fett, Kursiv, 6 Textfarben, Formatierung löschen
- **Seitenfarbe** wählbar (8 Farben)
- Stimmungs- und Wetter-Auswahl + optionales **Perioden-Tracking**
- „**An diesem Tag vor X Jahren**" — zeigt alte Einträge vom selben Datum

### 🔍 Suche
- Volltext-Suche mit Live-Ergebnissen (ab 2 Zeichen)
- Ergebnisse mit formatiertem Datum und Textvorschau
- **Klick öffnet den Eintrag direkt** im Kalender

### 📊 Statistiken
- 🔥 Streak + 🏆 Rekord + ✍️ Wörter + 📅 Tage
- Monats-Balkendiagramm + Stimmungsverteilung
- **Konfetti-Belohnung** bei 7 🌟 / 30 🏅 / 100 🏆 Tagen Streak

### 🔔 Erinnerungen
- Tägliche Push-Benachrichtigung zur konfigurierbaren Uhrzeit

### 📄 PDF-Export
- Aktuellen Monat exportieren & teilen

### ☁️ WebDAV-Synchronisation
- Sync mit eigenem Server (Nextcloud, ownCloud, etc.)
- Clientseitige Verschlüsselung — Server sieht nur verschlüsselte Daten
- Verbindungs-Test direkt in den Einstellungen

### 🎨 Design
- **4 Schriftarten:** Standard · Serif · Schreibmaschine · Kursiv
- **6 Akzentfarben:** Lila, Blau, Grün, Orange, Rosa, Türkis
- **Light / Dark / Systemstandard** Theme
- Material Design 3

### ⚙️ Einstellungen
- **Versions-Check** — prüft ob eine neue Version auf GitHub verfügbar ist
- Kalender-Anzeige: Stimmung / Wetter / Beides
- Geburtstage aus Kontakten + eigener Geburtstag (TT.MM)
- Perioden-Tracking aktivieren/deaktivieren
- Berechtigungen direkt in der App verwalten
- Tägliche Erinnerung konfigurieren

---

## 📱 Installation

1. **[Neueste Version herunterladen →](https://github.com/pbrockt/Tagebuch-App/releases/latest)**
2. APK auf das Android-Gerät übertragen
3. **Einstellungen → Sicherheit → Unbekannte Quellen** erlauben
4. APK installieren — direktes Update über ältere Versionen möglich

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
| Bilder | Coil |
| Hintergrundaufgaben | WorkManager |
| Verschlüsselung | Android Keystore + AES-GCM + PBKDF2 |
| Build | Gradle 8.9 + KSP + R8 |
| CI/CD | GitHub Actions |

---

## 🤖 Entwicklung mit KI

Der gesamte Code wurde von [Claude](https://claude.ai) (Anthropic) geschrieben und erklärt. Alle Quelldateien sind auf **Deutsch kommentiert** damit der Lernende die Architektur nachvollziehen kann.

---

## 📋 Vollständiger Changelog

### v0.3c — Hotfix (13)
**UX-Verbesserungen & Bugfix**
- 🔒 **Schloss für alte Einträge** — ältere Tage standardmäßig gesperrt, PIN nötig zum Bearbeiten (heutiger Tag immer offen)
- 🔍 **Suche verbessert** — lesbarer Text statt JSON-Code, formatiertes Datum, Klick öffnet Eintrag direkt im Kalender
- ◀️ **Zurück-Pfeile entfernt** — Suche, Statistiken und Einstellungen brauchen keinen Zurück-Pfeil mehr (Bottom Nav reicht)
- 📐 **Lücke behoben** — Einstellungen, Statistiken, Suche und Kalender liefen nicht bis zur Navigationsleiste (doppelte Inset-Berechnung bei verschachtelten Scaffolds)

---

### v0.3b (12)
**PIN-Schutz komplett überarbeitet**
- App schließt sich beim Verlassen **vollständig** (`finishAndRemoveTask`) — 100% zuverlässig, kein Timing-Problem mehr
- App nicht mehr in Recents sichtbar → kein Screenshot sensibler Daten

---

### v0.3a (11)
**Sicherheit & Popup**
- Perioden-Tracking erweitert: 🩸 Start / 🩸 Aktiv / 🔴 Stark / 🩸 Ende
- Popup läuft jetzt über den gesamten Bildschirm (keine Navigationsleiste unten sichtbar)

---

### v0.3 (10)
**Neue Features**
- 📊 **Monats-Zusammenfassung** über dem Kalender: „12 Einträge · 😊 meist gut · 🔥 5 Tage Streak"
- 💬 **Tages-Motivationsspruch** unter dem Kalender (105 Sprüche, 100% offline)
- 🎉 **Streak-Belohnungs-Konfetti** bei 7 🌟 / 30 🏅 / 100 🏆 Tagen in Folge
- 🩸 **Perioden-Tracking** (optional, in Einstellungen deaktivierbar)
- 🎨 **Seitenfarbe** pro Seite wählbar (8 Farben)
- 📅 **„An diesem Tag vor X Jahren"** — Banner wenn Einträge aus Vorjahren existieren
- 📤 **Seite als Text teilen** — Share-Button im Popup
- Privatsphäre: Bildschirm-Aus sperrt die App sofort (BroadcastReceiver)
- Timeout-Einstellung für Sperre (wurde später vereinfacht)

---

### v0.2b (9)
- Versions-Check zeigt Versionscode in Klammern: `v0.2b (9)`
- Repo öffentlich gestellt → Versions-Check funktioniert

### v0.2a (8)
- Fix: NavigationBar überdeckte Inhalte in allen Screens
- Versions-Check in Einstellungen (prüft GitHub Releases API)

### v0.2 (7)
**Großes visuelles Redesign + neue Features**
- 🔤 **4 Schriftarten** wählbar: Standard / Serif / Schreibmaschine / Kursiv
- 🌈 **Gradient-Hintergrund** im Kalender
- ↔️ **Slide-Animation** beim Monatswechsel (links/rechts)
- 🎈 **Geburtstage aus Kontakten** im Kalender (🎈 / 👑)
- 👑 Eigener Geburtstag (TT.MM in Einstellungen)
- 📳 **Bounce + Shake** beim PIN-Eingeben
- 🗂️ **Bottom Navigation** (4 Tabs: Kalender · Suche · Statistiken · Einstellungen)
- 📊 Gradient-Statistik-Karten mit Balkendiagramm
- 📅 Wochentag groß im Popup-Header
- ●●● 1–3 Punkte je nach Seitenanzahl im Kalender
- Kalender-Ansicht umschaltbar: Stimmung / Wetter / Beides

---

### v0.1e (6)
- Fix: Stimmungs-Emoji als sichtbares Icon im Kalender (war nur als unsichtbare Hintergrundfarbe)

### v0.1d (5)
- Fix: Kalender-Ansicht aktualisierte sich nach Einstellungsänderung nicht (Lifecycle-Observer)

### v0.1c (4)
- Fix: `@Serializable` fehlte auf `DiaryPage` und `DiaryDay` → WebDAV-Sync schlug fehl
- Fix: ProGuard-Regeln für kotlinx.serialization im Release-Build

### v0.1b (3)
- Fix: App-Crash bei WebDAV (`CancellationException` verschluckt, Response-Leaks, URL-Parsing)
- Neu: „Verbindung testen"-Button in WebDAV-Einstellungen

### v0.1a (2)
- Rich-Text-Editor (Fett, Kursiv, 6 Farben — Canvas entfernt)
- Stimmung/Wetter-Labels bündig ausgerichtet
- Kalender-Ansicht umschaltbar in Einstellungen
- versionCode = 2

### v0.1 (1) — Erste Veröffentlichung
**Komplette App von Grund auf**
- 📅 Monatskalender mit Eintrags-Markierung
- ✏️ Editor mit mehreren Seiten pro Tag
- 😊 Stimmungs- und Wetter-Tracking
- 🔒 PIN-Schutz mit Versuch-Limit
- ☁️ WebDAV-Synchronisation mit clientseitiger Verschlüsselung
- 📊 Statistiken (Streak, Wörter, Monats-Chart)
- 📄 PDF-Export des aktuellen Monats
- 🔍 Volltext-Suche
- 🔔 Tägliche Erinnerungs-Benachrichtigung
- 6 Akzentfarben + Light/Dark Theme
- Florale Hintergrund-Dekoration

---

<div align="center">

*Entwickelt mit ❤️ für meine Tochter — mit 🤖 [Claude](https://claude.ai) (Anthropic)*

</div>
