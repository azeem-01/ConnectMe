# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Security Architecture

ConnectMe is engineered with offline-first security:
- **Hardware-Backed Cryptography**: Student credentials are encrypted with AES-256-GCM using keys stored exclusively within Android Keystore hardware.
- **Zero Plaintext Fallback**: Passwords are never persisted in plaintext, shared preferences, or unencrypted SQLite databases.
- **Backup & Transfer Exclusion**: Both `backup_rules.xml` and `data_extraction_rules.xml` ensure credentials never leave the device through Google Cloud Backup or ADB transfer.
- **Direct Portal Communication**: Network requests for authentication are bound strictly to the physical Wi-Fi network interface using `ConnectivityManager.requestNetwork` and custom DNS routing.

## Reporting a Vulnerability

If you discover a security vulnerability in ConnectMe:
1. Please do **not** open a public issue.
2. Email details of the vulnerability or open a private GitHub security advisory.
3. Include a description of the issue, affected versions, and steps to reproduce.
4. We will review and address the report promptly.
