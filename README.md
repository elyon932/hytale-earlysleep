<div align="center">

# EarlySleep (Hytale Mod)

</div>

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0--beta-purple?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge)

</div>

---

## Overview

EarlySleep is a server-side mod for Hytale that provides control over when players are allowed to sleep and wake up.

By default, sleep behavior is restricted to predefined in-game hours. This mod extends that functionality by allowing server administrators to define custom sleep and wake times, making the experience more flexible and better suited to different gameplay styles.

The mod applies these changes across all active worlds and updates the internal sleep configuration at runtime.

---

## Features

- Configure when players can start sleeping
- Configure when players wake up
- Apply changes globally to all worlds
- Simple command-based control
- Lightweight and server-side only

---

## Commands

All commands require operator permissions (`earlysleep.admin`).

```bash
/sleeptime set <HH:mm>
/waketime set <HH:mm>
```

---

### Examples

```bash
/sleeptime set 19:00
/waketime set 05:30
```

---

### Notes

- Time must be in `HH:mm` format (24-hour)
- Invalid input will be rejected with a message

---

## How It Works

The mod modifies the internal `SleepConfig` of each active world using reflection.  
Changes are applied dynamically, without requiring a server restart.

---

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

---

## Development Setup

This project depends on the Hytale server `.jar`, which is not included in the repository.

Place the required file at:

```bash
libs/HytaleServer.jar
```

---

## Notes

- The Hytale server jar is excluded due to size and licensing constraints
- Reflection is used to modify internal behavior, which may break with future updates

---

## Author

**Elyon Oliveira dos Santos**  
Software Developer

---

## License

This project is licensed under the MIT License.