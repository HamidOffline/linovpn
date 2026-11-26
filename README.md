<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/logo-dark.png">
    <source media="(prefers-color-scheme: light)" srcset="docs/logo-light.png">
    <img alt="LinoVPN" src="docs/logo-light.png" width="240">
  </picture>

  <h1>LinoVPN برای اندروید و ویندوز</h1>
  <p><b>کلاینت هوشمند، سریع و مقاوم در برابر محدودیت‌های اینترنتی — مجهز به هسته Xray-core، Aether (WARP)، Unbar و SlipNet</b></p>

  <p>
    <a href="README.md"><b>🇮🇷 فارسی (پیش‌فرض)</b></a> •
    <a href="README.en.md">🇬🇧 English</a>
  </p>

  <p>
    <a href="https://github.com/HamidOffline/linovpn/releases/latest">
      <img src="https://img.shields.io/github/v/release/HamidOffline/linovpn?label=%D8%A2%D8%AE%D8%B1%DB%8C%D9%86%20%D9%86%D8%B3%D8%AE%D9%87&color=0084FF&style=for-the-badge" alt="Latest Release">
    </a>
    <a href="https://t.me/LinoVPN">
      <img src="https://img.shields.io/badge/%DA%A9%D8%A7%D9%86%D8%A7%D9%84%20%D8%AA%D9%84%DA%AF%D8%B1%D8%A7%D9%85-%40LinoVPN-00B4E6?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram Channel">
    </a>
    <a href="https://t.me/LinoVPNSup">
      <img src="https://img.shields.io/badge/%D9%BE%D8%B4%D8%AA%DB%8C%D8%A8%D8%A7%D9%86%DB%8C-%40LinoVPNSup-00A878?style=for-the-badge&logo=telegram&logoColor=white" alt="Support">
    </a>
  </p>
</div>

---

<div dir="rtl">

## 📥 دانلود مستقیم آخرین نسخه (`v3.0.5`)

### 📱 نسخه اندروید و اندروید تی‌وی (Android / Android TV)

> **نکته مهم درباره به‌روزرسانی:** تمام نسخه‌های رسمی **LinoVPN** با کلید امضای اختصاصی و دائمی منتشر می‌شوند؛ بنابراین برای نصب نسخه‌های جدیدتر **هیچ نیازی به حذف نسخه قبلی یا وارد کردن دوباره اشتراک نیست** و به‌روزرسانی مستقیم روی نسخه قبلی نصب می‌شود.

| نسخه / معماری پردازنده | توضیح و دستگاه‌های سازگار | لینک دانلود مستقیم |
| :--- | :--- | :---: |
| **ARM64 (`arm64-v8a` — معماری ۸)** | ✅ **پیشنهادی** برای تقریباً تمام گوشی‌ها و تلویزیون‌های جدید اندروید (۶۴ بیتی) — حجم سبک‌تر | [⬇️ دانلود مستقیم (arm64-v8a)](https://github.com/HamidOffline/linovpn/releases/download/v3.0.5/LinoVPN_3.0.5_arm64-v8a.apk) |
| **ARMv7 (`armeabi-v7a` — معماری ۷)** | مناسب گوشی‌ها، تبلت‌ها و اندرویدباکس‌های قدیمی‌تر (۳۲ بیتی) | [⬇️ دانلود مستقیم (armeabi-v7a)](https://github.com/HamidOffline/linovpn/releases/download/v3.0.5/LinoVPN_3.0.5_armeabi-v7a.apk) |
| **Universal (یونیورسال)** | قابل نصب روی هر دو معماری ۳۲ و ۶۴ بیتی (اگر از معماری دستگاه خود مطمئن نیستید) | [⬇️ دانلود مستقیم (Universal)](https://github.com/HamidOffline/linovpn/releases/download/v3.0.5/LinoVPN_3.0.5_universal.apk) |

### 💻 نسخه ویندوز (Windows 10 / 11 — ۶۴ بیتی)

| نسخه ویندوز | توضیح | لینک دانلود مستقیم |
| :--- | :--- | :---: |
| **نسخه نصبی (`Setup.exe`)** | ✅ **پیشنهادی** — نصب کامل همراه با میانبر دسکتاپ، اجرا با ویندوز و آپدیت درون‌برنامه‌ای | [⬇️ دانلود نسخه نصبی ویندوز](https://github.com/HamidOffline/linovpn/releases/download/v3.0.5/LinoVPN_3.0.5_Windows_x64_Setup.exe) |
| **نسخه پورتابل (`Portable.zip`)** | بدون نیاز به نصب — فقط از حالت فشرده خارج و `LinoVPN.exe` را اجرا کنید | [⬇️ دانلود نسخه پورتابل ویندوز](https://github.com/HamidOffline/linovpn/releases/download/v3.0.5/LinoVPN_3.0.5_Windows_x64_Portable.zip) |

> 📦 همچنین می‌توانید تمام نسخه‌ها و جزئیات تغییرات را در صفحه [Releases](https://github.com/HamidOffline/linovpn/releases) مشاهده کنید.

---

## ✨ امکانات و قابلیت‌های کلیدی LinoVPN

### ⚡ اتصال هوشمند و رابط کاربری یکپارچه (اندروید، اندروید تی‌وی و ویندوز)
- **راه‌اندازی سریع در اولین اجرا:** با باز کردن برنامه، لینک اشتراک یا کانفیگ را وارد کنید تا سرورها دریافت، پینگ‌گیری و سریع‌ترین سرور به‌صورت خودکار متصل شود.
- **پینگ خودکار و اتصال به بهترین سرور در شروع برنامه:** تمام سرورهای تب فعال پینگ گرفته می‌شوند و سریع‌ترین سرور پایدار به‌طور خودکار انتخاب و متصل می‌شود؛ در صورت اختلال شدید روی پروتکل‌های اصلی، به‌صورت هوشمند به نودهای اضطراری **Unbar** سوئیچ می‌کند.
- **نوار وضعیت کپسولی پیشرفته:** نمایش پینگ واقعی، **کشور و آی‌پی خروجی**، **سرعت لحظه‌ای دانلود و آپلود** و **حجم مصرفی کل همان اتصال**.
- **کارت اطلاعات اشتراک کامل:** نمایش حجم مصرفی، تاریخ انقضا، نوار پیشرفت حجم، لینک پشتیبانی و دکمه‌های اختصاصی **رفرش اشتراک** و **پینگ یک‌جا**.
- **رابط کاربری اختصاصی Android TV:** ناوبری کامل با کنترل تلویزیون (D-pad)، منوی کشویی، ویرایشگرها و دسترسی کامل به Unbar، SlipNet و اسکنر DNS.
- **تجربه بومی ویندوز:** انتخاب آنی بین **پروکسی سیستم** (بدون نیاز به دسترسی مدیر)، **حالت کل سیستم (TUN / Wintun)** برای عبور ترافیک بازی‌ها و همه برنامه‌ها، و **پورت محلی**؛ به‌همراه آیکون کنار ساعت (System Tray)، سوئیچ لحظه‌ای سرور در حالت متصل و پنجره تمام‌صفحه.
- **تغییر آنی حالت روز و شب (🌙 / ☀️) و دوزبانه کامل (فارسی و انگلیسی):** ترجمه یکپارچه در تمام منوها و تنظیمات همراه با آپدیت خودکار درون‌برنامه‌ای.

### 🚀 هسته قدرتمند Xray-core (`v26.11.24`) و ابزارهای ضدفیلترینگ
- پشتیبانی کامل از پروتکل‌ها و بسترهای جدید:
  - `VLESS` (`Reality`، `XHTTP`، **`xDrive`**، `ML-KEM 768`، `Vision`، `HTTPUpgrade`، `WebSocket`، `gRPC`، `TCP`)
  - `VMess`، `Trojan`، `Shadowsocks`، `Hysteria2` (با بازه جهش پورت سراسری و مبهم‌سازی Gecko/Salamander)، `WireGuard`، `MASQUE` (با IPv6 و دی‌ان‌اس امن)، `SOCKS5`، `HTTP`
- پشتیبانی کامل از **FinalMask** (از جمله **XDNS `alphabet`** ویژه UDP/53 ایران)، **TLS Fragment**، **UDP Noise**، **CurvePreferences**، **Session Resumption**، **ECH**، **ALPN قابل ویرایش** و **TCP User Timeout**.
- **تنظیمات پیشرفته DNS:** اعتبارسنجی منطقه‌ای DNS داخلی (`Validate Region`)، پرس‌وجوی موازی (`Parallel Query`) و استفاده از کش منقضی هنگام اختلال (`Serve Stale`).
- پشتیبانی از **زنجیره پروکسی (Proxy Chains)**، **گروه‌های سیاستی و لودبالانسینگ (Policy Groups)**، **کانفیگ Custom JSON چندخروجی (Multi-Outbound)** و **درون‌ریزی مجموعه قوانین مسیریابی از لینک (URL / JSON)**.
- پشتیبانی داخلی و کامل از **Aether (WARP)**، **Psiphon** و **Tor** همراه با اسکنر نقطه اتصال و دریافت خودکار کلیدهای جدید WARP.
- **تقسیم تونل برنامه‌ها (اندروید)**، **فایل‌های جغرافیایی GeoIP / GeoSite بهینه ایران** و پشتیبان‌گیری/بازیابی کامل تنظیمات.

### 📡 تونل ضدقطعی Unbar (Soar DNS Tunnel) با تنظیمات مجزا
- هسته بومی و مستقل **Unbar (Soar)** برای عبور از شدیدترین محدودیت‌ها و شرایط قطعی از بستر DNS.
- پشتیبانی مستقیم از لینک‌های `unbar://`، ساب‌های JSON آنبار و تزریق خودکار نودهای Unbar داخل لینک اشتراک اصلی.
- صفحه **تنظیمات اختصاصی آنبار** جدا از تنظیمات Xray شامل: کشف خودکار در **۲۳۰۷ ریزالور تاییدشده ایران** یا ریزالورهای دلخواه، حالت **TCP-53 / UDP-53**، تنظیم MTU و Max DNS Answer، محدودیت پهنای باند، عبور مستقیم سایت‌های ایرانی (`.ir`) و گزارش‌های عیب‌یابی.

### 🛡️ موتور کامل SlipNet + اسکنر داخلی ۶تستی DNS
- ادغام کامل تمام پروتکل‌های **SlipNet**:
  - **DNSTT** و **NoizDNS** (با حالت Stealth و Authoritative)
  - **VayDNS** (تنظیم نوع رکورد DNS، سازگاری DNSTT، Max QNAME، RPS و تایم‌اوت‌ها)
  - **Soar** و **Slipstream QUIC** (با کنترل ازدحام `BBR` / `DCUBIC` و `UDP GSO`)
  - **NaiveProxy**، **SSH Tunnel** (TLS / SNI / WebSocket / Payload Injection)، **DoH** و **شبکه Tor** (Snowflake / obfs4 / Meek)
- پشتیبانی بومی از رمزگشایی لینک‌های **`slipnet-enc://`** علاوه بر `slipnet://`.
- **اسکنر حرفه‌ای داخلی DNS:** اسکن چندرشته‌ای با ۶ تست تخصصی سازگاری تونل از میان ۲۳۰۷ ریزالور ایران، رنج‌های CIDR سبک و کامل ایران یا فایل دلخواه، و **اعمال یک‌کلیکی بهترین ریزالورها روی Unbar یا SlipNet**.

---

## 📞 ارتباط و پشتیبانی
- **کانال رسمی تلگرام:** [@LinoVPN](https://t.me/LinoVPN)
- **پشتیبانی آنلاین:** [@LinoVPNSup](https://t.me/LinoVPNSup)
- **گزارش مشکل و پیشنهادها:** [GitHub Issues](https://github.com/HamidOffline/linovpn/issues)

</div>
