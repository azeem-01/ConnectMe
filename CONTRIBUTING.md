# Contributing to ConnectMe

Thank you for your interest in contributing to ConnectMe! We welcome bug reports, feature suggestions, code contributions, and UI enhancements.

## Getting Started

1. **Fork the Repository** on GitHub.
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/<your-username>/ConnectMe.git
   cd ConnectMe
   ```
3. **Open the project in Android Studio** (Ladybug / Koala / Hedgehog or newer).
4. Ensure you have **JDK 17** and **Android SDK 35** installed.

## Development Workflow

1. Create a feature branch:
   ```bash
   git checkout -b feature/my-new-feature
   ```
2. Make your modifications, adhering to:
   - Modern Kotlin idioms and Jetpack Compose best practices.
   - Material 3 design guidelines and Google Sans typography.
   - Offline privacy: no user credentials should ever be transmitted outside the local captive portal handshake or logged in plain text.
3. Build and test the release build:
   ```bash
   ./gradlew assembleRelease
   ```
4. Commit your changes with clear, descriptive commit messages.
5. Push to your fork and submit a Pull Request.

## Reporting Bugs

Please open an issue on GitHub detailing:
- Device model and Android version
- Steps to reproduce the bug
- Expected vs. actual behavior
- Relevant log output (ensure no passwords or private credentials are included in logs!)

## Community

Be kind, constructive, and respectful to all contributors. See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for details.
