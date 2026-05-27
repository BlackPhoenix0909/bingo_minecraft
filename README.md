# 🎰 Minecraft Bingo Mod (Fabric 1.21.1)

Ein vollständiges Bingo-Minispiel für Minecraft Java Edition mit Fabric Mod Loader.

---

## 🎮 Features

- **3×3 Bingo-Karte** mit zufälligen Minecraft Items
- **Automatische Erkennung**: Sobald ein Item im Inventar landet, wird es automatisch markiert
- **Echtzeit-Sync**: Alle Spieler sehen ihre eigene Karte live aktualisiert
- **3 in einer Reihe** (horizontal, vertikal, diagonal) = BINGO und Gewinn
- **Minecraft-Design**: Dunkles GUI im Stil von Minecraft Inventaren
- **Durchgestrichen-Effekt**: Gesammelte Items werden mit grünem ✔ und diagonalen Linien markiert
- **Win-Overlay**: Goldenes animiertes BINGO-Banner bei Gewinn
- **Sound-Effekte**: Pickup-Ton bei Item-Sammlung, Sieger-Fanfare bei Gewinn
- **Mehrsprachig**: Deutsch + Englisch

---

## 📋 Commands

| Command | Beschreibung | Berechtigung |
|---------|-------------|--------------|
| `/bingo` | Öffnet deine Bingo-Karte (GUI) | Alle Spieler |
| `/bingo start` | Startet ein neues Bingo-Spiel mit zufälligem Board | OP (Level 2) |
| `/bingo stop` | Beendet das aktuelle Spiel | OP (Level 2) |

---

## 🏗️ Projekt-Struktur

```
minecraft-bingo-mod/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── generate_textures.py          ← Generiert Texturen (optional)
└── src/
    ├── main/
    │   ├── java/de/bingo/
    │   │   ├── BingoMod.java           ← Server-Einstiegspunkt + Commands
    │   │   ├── network/
    │   │   │   └── BingoNetworking.java ← Server-Netzwerk-Pakete
    │   │   └── util/
    │   │       └── BingoGame.java       ← Spiellogik (Board, Bingo-Check)
    │   └── resources/
    │       ├── fabric.mod.json
    │       └── assets/bingo/
    │           ├── icon.png
    │           ├── sounds.json
    │           ├── lang/
    │           │   ├── de_de.json
    │           │   └── en_us.json
    │           └── textures/gui/
    │               ├── bingo_icon.png
    │               └── checkmark.png
    └── client/
        └── java/de/bingo/
            ├── BingoModClient.java           ← Client-Einstiegspunkt
            ├── network/
            │   └── BingoNetworkingClient.java ← Client-Netzwerk + State
            └── screen/
                └── BingoScreen.java           ← Das komplette GUI ⭐
```

---

## 🔧 Installation & Build

### Voraussetzungen
- Java 21
- Gradle (oder nutze den mitgelieferten Gradle Wrapper)

### Bauen

```bash
# In den Projektordner wechseln
cd minecraft-bingo-mod

# Entwicklungsumgebung einrichten (IntelliJ/Eclipse)
./gradlew genSources

# Mod kompilieren
./gradlew build

# Die fertige .jar findest du in:
# build/libs/minecraft-bingo-1.0.0.jar
```

### Installieren

1. [Fabric Loader](https://fabricmc.net/use/installer/) installieren (für MC 1.21.1)
2. [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) herunterladen
3. Beide `.jar` Dateien in den `mods/` Ordner legen
4. `minecraft-bingo-1.0.0.jar` auch in den `mods/` Ordner

---

## 🎲 Spielablauf

```
Admin:  /bingo start
        → Zufälliges 3×3 Board wird generiert
        → Alle Spieler werden benachrichtigt

Spieler: /bingo
        → Öffnet die Bingo-Karte im GUI

        [Sammeln...]
        → Jedes gesammelte Item wird automatisch erkannt
        → Grünes ✔ + Diagonal-Linien erscheinen auf der Karte
        → Pickup-Sound

        [3 in einer Reihe!]
        → 🎉 BINGO! Banner erscheint
        → Server-Ankündigung im Chat
        → Win-Sound

Admin:  /bingo stop
        → Spiel beenden (optional)
```

---

## 🎨 GUI Design

Das GUI folgt dem **Minecraft-Designprinzip**:

- **Dunkelgraue Panels** (RGB: 55, 55, 55) wie Inventare
- **Gold-Akzente** für Titel und Gewinn-Overlay
- **Pixel-genaue Borders** im typischen Minecraft-Stil
- **Item-Rendering** direkt mit Minecraft's eigenem Renderer
- **3×3 Grid** mit je 54×54 Pixel Zellen
- **Animiertes Win-Banner** mit pulsierendem Goldrand

---

## 📦 Mögliche Bingo-Items (60+)

Diamant, Golderz, Eisenbarren, Smaragd, Kohle, Redstone, Lapislazuli, Quarz,
Obsidian, Lohenruten, Enderperlen, Schleimbälle, Spinnenauge, Faden, Federn, Knochen,
Schießpulver, Zucker, Tintenbeutel, Glühsteinstaub, Leder, Kaninchenbalg,
Eichenholz, Sand, Kies, Lehm, Feuerstein, Papier, Buch,
Brot, Apfel, Karotte, Kartoffel, Ei, Melonenscheibe, Kürbis, Bambus, Kaktus,
Eisenschwert, Bogen, Pfeil, Schild, Eisenspitzhacke, Eisenaxt, Eisenschaufel,
Werktisch, Ofen, Kiste, Fackel, Leiter, Schild, Eimer, Angel, Uhr, Kompass, Karte

---

## 🔌 Netzwerk-Pakete

| Paket | Richtung | Inhalt |
|-------|----------|--------|
| `bingo:open_gui` | Server → Client | Öffnet GUI + Board-Daten |
| `bingo:sync_board` | Server → Client | Neues Board beim Spielstart |
| `bingo:sync_progress` | Server → Client | Item-Fortschritt Update |
| `bingo:bingo_win` | Server → Client | Gewinner-Name |
| `bingo:game_stop` | Server → Client | Spiel beendet |

---

## 🛠️ Erweitern

### Mehr Items hinzufügen
In `BingoGame.java` das `BINGO_ITEMS` Array erweitern:
```java
private static final Item[] BINGO_ITEMS = {
    Items.NETHERITE_INGOT, // hinzufügen
    // ...
};
```

### Board-Größe ändern (z.B. 5×5)
In `BingoGame.java`:
```java
public static final int SIZE = 5; // von 3 auf 5
```

### Sounds anpassen
Eigene `.ogg` Dateien in `src/main/resources/assets/bingo/sounds/` legen
und `sounds.json` entsprechend anpassen.

---

## 📄 Lizenz

MIT License - Frei verwendbar und modifizierbar.
