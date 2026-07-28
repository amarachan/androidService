# androidService 📱🔒

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg)](https://kotlinlang.org)

**androidService** is a security-focused background call recording application for Android. It is designed to operate using app masking techniques while ensuring all recordings are encrypted and securely delivered to your private email or cloud relay.

---

## ✨ Features

- **🛡️ Secure Storage**: All recordings are encrypted with **AES-256 GCM** via the Jetpack Security library. Keys are stored in the hardware-backed Android Keystore.
- **🎭 App Masking**: Hide the application behind a functional **Calculator decoy**. Use a secret code (`123456 =`) to unlock the real app.
- **🎙️ Automated Monitoring**: Foreground service with `BOOT_COMPLETED` persistence ensure calls are recorded automatically.
- **📧 Delivery Options**:
  - Automatic ZIP compression.
  - **SMTP Support** (Gmail, Outlook, etc.).
  - **Hyvor Relay API** integration for reliable delivery.
- **🕵️ Stealth Features**: Minimized notification priority and generic system labeling ("System Sync") to blend in with OS processes.
- **🔐 PIN Lock**: Built-in PIN lock screen protects your settings and recordings list.

---

## 📸 Screenshots

| Decoy (Calculator) | Secret Unlock | Main Monitor | Secure Settings |
| :---: | :---: | :---: | :---: |
| *Functional Calculator* | *Trigger: 123456 =* | *Service Status* | *Encrypted Config* |

---

## 🚀 Getting Started (Android Only)

> [!IMPORTANT]
> **Compatibility**: This application is exclusively for Android devices (API 23 and above). It will not work on iOS or other platforms.

### Prerequisites

- Android Device (API 23+)
- SMTP Server credentials OR a Hyvor Relay API Key.

### Installation

#### Option 1: Download APK (Recommended for Users)
1. Download the latest `androidService.apk` from the [Releases](https://github.com/amarachan/androidService/releases) section.
2. Transfer the APK to your Android device.
3. Enable "Install from Unknown Sources" in your device settings.
4. Tap the APK to install.

#### Option 2: Build from Source (For Developers)
1. Clone the repository:
   ```bash
   git clone https://github.com/youruser/androidService.git
   ```
2. Open the project in **Android Studio**.
3. Build and deploy the `app` module to your device via ADB.

### Initial Setup

1. **Grant Permissions**: Microphone, Phone State, and Notifications.
2. **Configure Delivery**: Enter your email and SMTP/Hyvor details in the **Settings** menu.
3. **Enable Monitor**: Toggle the "Background Monitor" on the dashboard.
4. **Mask the App**: Select your preferred decoy in Settings to hide the app from the launcher.

---

## 🛠️ Technology Stack

- **UI**: Jetpack Compose (Material 3)
- **Background Work**: WorkManager & Foreground Services
- **Security**: Jetpack Security (Security-Crypto)
- **Networking**: OkHttp & Jakarta Mail
- **Architecture**: MVVM with Clean Architecture principles

---

## 🛡️ Security Architecture

The app follows a "Zero Knowledge" approach for your recordings:
- **No Third-Party Servers**: Unless you use a relay, your data moves directly from your device to your inbox.
- **Encrypted at Rest**: Files are unreadable even if extracted from the device without the hardware-backed keys.
- **Decoy Persistence**: The masking system uses `PackageManager` component states, meaning the "Real" app icon is completely removed from the system launcher when masked.

---

## ⚖️ Disclaimer

Recording phone calls without the consent of all parties may be illegal in your jurisdiction. The developers assume no liability for misuse of this tool. Use responsibly.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
