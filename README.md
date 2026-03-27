<div align="center">

# EarlySleep (Hytale Mod)

</div>

<div align="center">

![Version](https://img.shields.io/badge/version-2.0.0-green?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge)

</div>

---

## Overview

EarlySleep is a server-side mod for Hytale that provides control over when players are allowed to sleep and wake up, as well as how many players are required to skip the night.

By default, sleep behavior is restricted to predefined in-game hours. This mod extends that functionality by allowing server administrators to define custom sleep and wake times, making the experience more flexible and better suited to different gameplay styles, while integrating seamlessly with Hytale's native sleep system across all active worlds.

The default sleep window is set from 19:30 (7:30 PM) to 05:30 (5:30 AM), requiring 50% of online players to be in bed. All values are fully customizable via commands.

## Features

- Configure when players can start sleeping
- Configure when players wake up
- Configure minimum players required to skip the night
- Support for percentage (%) and fixed player counts
- Settings persist after server restart (JSON storage)
- Apply changes globally to all worlds
- Simple command-based control
- Lightweight and server-side only

## Commands

All commands require operator permissions (`earlysleep.admin`).

```bash
/sleeptime set <HH:mm>
/waketime set <HH:mm>
/sm minset <value>
/sm minget
```

### Examples

```bash
/sleeptime set 19:00
/waketime set 05:30
/sm minset 50%
/sm minset 3
/sm minget
```

### Notes

- Time must be in `HH:mm` format (24-hour)
- Invalid input will be rejected with a message
- `/sm minset` accepts:
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

### 2.0.0
- Added Sleep Manager system
- Added `/sm minset` and `/sm minget` commands
- Support for percentage-based and fixed player requirements
- Integrated with Hytale's native sleep window

## Author

**Elyon Oliveira dos Santos**  
Software Developer

## License

This project is licensed under the MIT License.