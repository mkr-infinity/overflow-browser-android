# Security Policy

## Supported Versions

Only the latest release of Overflow Browser receives active security updates and vulnerability fixes.

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Security Philosophy

Overflow Browser prioritizes user security and privacy through conservative defaults:
- **Zero Remote Telemetry**: No browsing data, search queries, or analytics leave the user's device.
- **Process Isolation**: WebView runs with sandboxed multi-process security wherever supported by Android.
- **HTTPS Enforcement**: HTTPS-only mode upgrades insecure connections and alerts users before submitting unencrypted requests.
- **Ad & Tracker Blocking**: Built-in host filter blocks known malware, tracking, and advertising domains before network requests execute.

## Reporting a Vulnerability

If you discover a security vulnerability in Overflow Browser, please follow responsible disclosure guidelines:

1. **Do not open a public GitHub issue.**
2. Send an email to the project maintainers with details, steps to reproduce, and affected Android versions.
3. You will receive an acknowledgment within 48 hours.
4. We ask that you give us reasonable time to investigate and issue a patch before any public disclosure.
