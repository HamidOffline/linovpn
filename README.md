# 🚀 LinoVPN — کلاینت نسل جدید و پرسرعت اندروید

<div align="center">

![LinoVPN Banner](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

### تجربه‌ای مدرن، امن و مینیمال با رابط کاربری الهام‌گرفته از Streisand سیستم‌عامل iOS  
**مجهز به هسته رسمی و پرقدرت Xray-Core**

[![GitHub release](https://img.shields.io/github/v/release/HamidOffline/linovpn?style=for-the-badge&color=007AFF)](https://github.com/HamidOffline/linovpn/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/HamidOffline/linovpn)
[![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL%20v3-red?style=for-the-badge)](LICENSE)

</div>

---

## 📥 لینک‌های مستقیم دانلود آخرین نسخه (v1.0.0)

تمامی نسخه‌ها به طور رسمی امضا شده (Signed) و برای انواع پردازنده‌ها و گوشی‌های موبایل تفکیک شده‌اند:

| نسخه | معماری پردازنده | مناسب برای چه دستگاه‌هایی؟ | حجم فایل | لینک دانلود مستقیم |
| :--- | :--- | :--- | :---: | :---: |
| **توصیه‌شده (اصلی)** | **`arm64-v8a`** | اکثر گوشی‌های جدید و هوشمند (سامسونگ، شیائومی، پوکو، پیکسل، وان‌پلاس، هواوی و...) | **~۵۲ مگابایت** | [🚀 دانلود مستقیم APK](https://github.com/HamidOffline/linovpn/releases/download/v1.0.0/LinoVPN-v1.0.0-arm64-v8a.apk) |
| **گوشی‌های قدیمی‌تر** | **`armeabi-v7a`** | گوشی‌های قدیمی‌تر ۳۲ بیتی اندروید | **~۵۱ مگابایت** | [📦 دانلود مستقیم APK](https://github.com/HamidOffline/linovpn/releases/download/v1.0.0/LinoVPN-v1.0.0-armeabi-v7a.apk) |
| **نسخه یونیورسال (جامع)** | **`Universal`** | قابل نصب روی **تمامی** دستگاه‌ها بدون در نظر گرفتن نوع پردازنده | **~۱۴۸ مگابایت** | [🌐 دانلود مستقیم APK](https://github.com/HamidOffline/linovpn/releases/download/v1.0.0/LinoVPN-v1.0.0-universal.apk) |
| **شبیه‌ساز و کامپیوتر** | **`x86_64`** | شبیه‌سازهای اندروید روی کامپیوتر (LDPlayer، BlueStacks، WSA) | **~۵۴ مگابایت** | [💻 دانلود مستقیم APK](https://github.com/HamidOffline/linovpn/releases/download/v1.0.0/LinoVPN-v1.0.0-x86_64.apk) |

> 💡 **نمی‌دانید کدام نسخه را دانلود کنید؟**  
> نسخه **`arm64-v8a`** را دانلود کنید؛ این نسخه برای ۹۵٪ گوشی‌های تولید سال ۲۰۱۸ به بعد مناسب و بسیار کم‌حجم است. اگر خطای نصب داد، نسخه **`Universal`** را نصب نمایید.

---

## ✨ ویژگی‌ها و قابلیت‌های برجسته

* 🎨 **طراحی لوکس به سبک Streisand iOS**: رابط کاربری مینیمال، فونت چشم‌نواز وزیرمتن (Vazirmatn)، انیمیشن‌های روان و کارت‌های شیشه‌ای.
* ⚡ **هسته قدرتمند Xray-Core**: پشتیبانی کامل و بومی از جدیدترین پروتکل‌های عبور از فیلترینگ شامل:
  * `VLESS` (همراه با Reality، Vision، Flow)
  * `VMess`
  * `Trojan`
  * `Shadowsocks`
  * پروتکل‌های انتقالی جدید: `XHTTP`، `HTTPUpgrade`، `gRPC`، `WebSocket` و `TCP`
* 🔄 **سوییچ آنی و بدون قطعی (Seamless Server Switching)**: تعویض مستقیم سرورها در کمتر از ۲۰۰ میلی‌ثانیه بدون نیاز به قطع اتصال و اتصال مجدد.
* 🇮🇷 **بای‌پس هوشمند سایت‌های داخلی**: عبور مستقیم بدون فیلترشکن برای وب‌سایت‌های ایرانی، درگاه‌های بانکی، اپلیکیشن‌های پرداخت و دامنه‌های `.ir`.
* 📶 **تست تاخیر واقعی (True Delay Ping)**: ارسال پینگ به آدرس‌های معتبر و نمایش دقیق زمان تاخیر با رنگ‌بندی تفکیک‌شده و برچسب قرمز `TimeOut` در صورت عدم اتصال.
* 📊 **مرتب‌سازی هوشمند سرورها (`Sort by Ping`)**: اولویت‌بندی خودکار سرورهای سریع‌تر و بدون تایم‌اوت در بالای لیست.
* 📋 **پشتیبانی کامل از ساب‌اسکریپشن (Subscription)**:
  * وارد کردن لینک اشتراک با شناسایی خودکار حجم باقی‌مانده و تاریخ انقضا.
  * منوی نگه‌داشتن انگشت (Hold Menu) برای بروزرسانی، ویرایش و تست گروهی سرورها.
  * امکان اضافه کردن کانفیگ از کلیپ‌بورد یا اسکن کد QR.
* 🌍 **دو زبانه کامل**: پشتیبانی بی‌نقص از زبان‌های **فارسی (RTL)** و **انگلیسی (LTR)** با سوییچ لحظه‌ای.

---

## 📱 راهنمای استفاده

1. نسخه متناسب با پردازنده گوشی خود را از جدول بالا دانلود و نصب کنید.
2. اپلیکیشن **LinoVPN** را اجرا کنید.
3. با زدن دکمه `+` در بالای صفحه، لینک اشتراک خود را از کلیپ‌بورد اضافه کنید یا کانفیگ را اسکن نمایید.
4. دکمه بزرگ پاور را در مرکز صفحه لمس کنید و از اینترنت آزاد و پرسرعت لذت ببرید!

---

## 🛠 معماری و تکنولوژی‌های به‌کار رفته

* **UI Framework:** Jetpack Compose (Declarative UI)
* **Design System:** Apple iOS Human Interface Guidelines adapted for Material 3
* **Language:** 100% Kotlin
* **Dependency Injection:** Dagger Hilt
* **Database:** Room Database + StateFlow
* **VPN Cores:** Official Libv2ray / Xray-core (Cgo / Go JNI bindings)
* **Architecture Pattern:** Clean Architecture (Domain - Data - UI Presentation)

---

## 💬 پشتیبانی و جامعه کاربری

برای دریافت آخرین اخبار، بروزرسانی کانفیگ‌ها و نسخه‌های جدید در کانال تلگرام ما عضو شوید:  
📢 **تلگرام:** [@LinoVPN](https://t.me/LinoVPN)

---

<div align="center">
ساخته‌شده با ❤️ برای دسترسی آزاد و امن به اینترنت
</div>