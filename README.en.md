<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/logo-dark.png">
    <source media="(prefers-color-scheme: light)" srcset="docs/logo-light.png">
    <img alt="LinoVPN" src="docs/logo-light.png" width="240">
  </picture>

  <h1>LinoVPN for Android</h1>
  <p><b>Fast, Smart & Censorship-Resistant Android VPN — Powered by Xray-core, PattNG, Unbar & SlipNet</b></p>

  <p>
    <a href="README.md">🇮🇷 فارسی (Default)</a> •
    <a href="README.en.md"><b>🇬🇧 English</b></a>
  </p>

  <p>
    <a href="https://github.com/HamidOffline/linovpn/releases/latest">
      <img src="https://img.shields.io/github/v/release/HamidOffline/linovpn?label=Latest%20Release&color=0084FF&style=for-the-badge" alt="Latest Release">
    </a>
    <a href="https://t.me/MojAzadNet">
      <img src="https://img.shields.io/badge/Telegram%20Channel-%40MojAzadNet-00B4E6?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram Channel">
    </a>
    <a href="https://t.me/linovpnsup">
      <img src="https://img.shields.io/badge/Support-%40linovpnsup-00A878?style=for-the-badge&logo=telegram&logoColor=white" alt="Support">
    </a>
  </p>
</div>

---

## 📥 Direct Download (`v3.0.4`)

> **Seamless In-Place Upgrades:** All official **LinoVPN** builds are signed with our permanent dedicated release keystore. Future updates install directly over your existing installation **without uninstalling or losing your subscriptions**.

| Architecture / Variant | Recommended For | Direct Download |
| :--- | :--- | :---: |
| **ARM64 (`arm64-v8a` — v8)** | ✅ **Recommended** for almost all modern 64-bit Android phones (smaller size) | [⬇️ Download (arm64-v8a)](https://github.com/HamidOffline/linovpn/releases/download/v3.0.4/LinoVPN_3.0.4_arm64-v8a.apk) |
| **ARMv7 (`armeabi-v7a` — v7)** | Older 32-bit Android phones and tablets | [⬇️ Download (armeabi-v7a)](https://github.com/HamidOffline/linovpn/releases/download/v3.0.4/LinoVPN_3.0.4_armeabi-v7a.apk) |
| **Universal** | Works on both 64-bit & 32-bit ARM Android devices (if you are unsure) | [⬇️ Download (Universal)](https://github.com/HamidOffline/linovpn/releases/download/v3.0.4/LinoVPN_3.0.4_universal.apk) |

> 📦 Browse all releases and changelogs on the [GitHub Releases](https://github.com/HamidOffline/linovpn/releases) page.

---

## ✨ Key Features

### ⚡ Smart Connection & Modern UI
- **Zero-Friction Onboarding:** Paste your subscription link or config on first launch to automatically import, test ping, and connect to the fastest server.
- **Auto-Ping & Smart Auto-Connect on Launch:** Automatically tests all servers in the active tab on startup and connects to the lowest-ping primary server—with intelligent automatic fallback to **Unbar** DNS tunnels if regular protocols are blocked.
- **Floating Capsule Status Bar:** Clean pill bar showing real ping latency, **exit country & IP**, **real-time Download/Upload speeds**, and **total session traffic volume**.
- **Subscription Usage Card:** Displays data usage, remaining quota bar, expiry date, support shortcut, **subscription refresh**, and **one-tap server ping**.
- **Compact 2-Column & 1-Column Cards:** Clean, dense grid layout with automatic ping sorting and organized subscription tabs.
- **Instant Light & Dark Mode Toggle (🌙 / ☀️):** One-tap theme switcher right on the main toolbar with LinoVPN cyan-to-sapphire brand palette and adaptive Light/Dark logos.
- **100% Pure Bilingual (Persian & English):** Clean, consistent localization across every menu, setting, and speed/volume/latency unit.
- **Automatic Release Notifications & In-App Updater:** Automatically checks GitHub Releases in the background, alerts you via system notification & an in-app banner whenever a new version drops, and downloads + installs the exact matching ABI APK directly inside the app.

### 🚀 Latest Xray-core & Full PattNG Capabilities
- Powered by **Xray-core v26.11.23** supporting:
  - `VLESS` (`Reality`, `XHTTP`, `ML-KEM 768`, `Vision`, `HTTPUpgrade`, `WebSocket`, `gRPC`, `TCP`)
  - `VMess`, `Trojan`, `Shadowsocks`, `Hysteria2`, `WireGuard`, `SOCKS5`, `HTTP`
- Full support for **FinalMask**, **TLS Fragment**, **CurvePreferences**, **Session Resumption**, **ECH**, and **TCP User Timeout**.
- **Proxy Chains**, **Policy Groups** (`LeastPing` / `LeastLoad`), and **Custom JSON Outbound** editor.
- Built-in **Aether (WARP)**, **Psiphon**, and **Lyrebird** cores.
- **Per-App Proxy (Split Tunneling)**, **Custom Routing Rules**, **GeoIP / GeoSite** assets, full Backup & Restore, and Quick Settings Tile.

### 📡 Unbar (Soar DNS Tunnel) with Dedicated Settings
- Native **Unbar (Soar)** engine for bypassing extreme censorship and blackouts over DNS.
- Native support for `unbar://` URIs, Unbar JSON arrays, and automatic subscription header injection (cleanly placed at the bottom of your server list).
- Dedicated **Unbar Settings** screen separate from Xray settings: VPN / SOCKS5 mode, **Built-in Discovery (2,307 verified Iranian resolvers)** or custom resolvers, **TCP-53 mode**, MTU & Max DNS Answer tuning, download/upload bandwidth limits, domestic `.ir` geo-bypass, custom bypass domains, and diagnostic logs.

### 🛡️ Full SlipNet Engine + Built-In DNS Scanner
- Complete **SlipNet Full** protocol suite:
  - **DNSTT** & **NoizDNS** (Stealth & Authoritative modes)
  - **VayDNS** (DNS record type, DNSTT compatibility, Max QNAME, RPS limit & timeout tuning)
  - **Soar** & **Slipstream QUIC** (`BBR` / `DCUBIC` congestion control & `UDP GSO`)
  - **NaiveProxy**, **SSH Tunnel** (TLS / SNI / WebSocket / Custom Payload Injection), **DoH**
  - **Tor Network** with **Snowflake**, **obfs4**, **Meek**, and custom bridges
- Native decryption of **`slipnet-enc://`** encrypted configs alongside `slipnet://`.
- **Built-in Multi-Threaded DNS Scanner:** Scans 2,307 verified resolvers, Iran Lite/Full CIDR pools, or custom IP/CIDR lists, detects clean EDNS0-capable resolvers and hijacks, and applies the fastest clean resolvers to **Unbar** or **SlipNet** in one tap.

---

## 🛠️ Building from Source

- Requirements: **JDK 17+** and **Android SDK (API 37)**.
- Create `keystore.properties` in the project root (or supply `LINO_KEYSTORE_PATH`, `LINO_KEYSTORE_PASSWORD`, `LINO_KEY_ALIAS`, `LINO_KEY_PASSWORD` environment variables):

```properties
storeFile=/path/to/linovpn-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=linovpn_release
keyPassword=YOUR_KEY_PASSWORD
```

Build all three release variants (`arm64-v8a`, `armeabi-v7a`, and `universal`) together:

```bash
./gradlew assemblePlaystoreRelease
```

---

## 📞 Community & Support
- **Official Telegram Channel:** [@MojAzadNet](https://t.me/MojAzadNet)
- **Customer Support:** [@linovpnsup](https://t.me/linovpnsup)
- **Bug Reports & Feature Requests:** [GitHub Issues](https://github.com/HamidOffline/linovpn/issues)
