<div align="center">

# EarlySleep (Hytale Mod)

</div>

<div align="center">

![Version](https://img.shields.io/badge/version-3.0.5-LTS-brightgreen?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge)

</div>

---

## Overview

EarlySleep is a server-side mod for Hytale that controls when players can sleep, wake up, and how night skipping behaves.

In addition to configurable sleep schedules and player requirements, the mod enhances the wake-up experience by fully restoring player health and stamina, and applying temporary wake-up boosts.

The system includes a dynamic night transition, adapting delay automatically based on player count, while remaining fully synchronized with Hytale's native sleep mechanics across all active worlds.

## Features

- Unified command system
- Custom sleep and wake configuration (`HH:mm`)
- Configurable player requirement (percentage or fixed)
- Dynamic night skip system
- Wake-up boosts (health, stamina, regeneration)

- Persistent configuration (`config.json`)
- Real-time updates (no restart required)
- Multi-world support
- Multiplayer sleep/wake notifications

- Improved stability and performance
- Automatic cleanup of offline player data
- Enhanced command validation
- Lightweight and server-side only

## Commands

All commands require operator permissions (`earlysleep.admin`).

```bash
/sm sleep <HH:mm>
/sm wake <HH:mm>
/sm delay <ms>
/sm player <value>
/sm status
/sm help
```

### Examples

```bash
/sm sleep 19:00
/sm wake 05:30
/sm delay 2000
/sm player 50%
/sm player 3
/sm status
```

### Notes

- Time must be in `HH:mm` format (24-hour)
- Invalid input will be rejected with a message
- `/sm player` accepts:
  - Percentage (e.g., `50%`)
  - Fixed player amount (e.g., `3`)

## How It Works

The mod modifies the internal `SleepConfig` of each active world using reflection and extends its behavior through a custom Sleep Manager.

Sleep requirements are dynamically calculated based on either a percentage of online players or a fixed value, and are fully synchronized with the game's native sleep window.

Changes are applied at runtime and persist through restarts.

## Installation

1. Build the project:

```bash
mvn clean package
```

2. Locate the generated file in:

```bash
target/earlysleep-*.jar
```

3. Place the `.jar` into your Hytale server's plugin/mod directory.

## Development Setup

This project depends on the Hytale server `.jar`, which is not included in the repository.

Place the required file at:

```bash
libs/HytaleServer.jar
```

## Technical Notes

- The Hytale server jar is excluded due to size and licensing constraints
- Reflection is used to modify internal behavior, which may break with future updates

## Changelog

### 3.0.5-LTS
- Fixed an issue that allowed players to sleep at any time

## Author

**Elyon Oliveira dos Santos**  
Software Developer

## License

This project is licensed under the MIT License.