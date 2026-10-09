/// سیستم دوزبانه انگلیسی و فارسی برای تمام بخش‌های اپلیکیشن LinoVPN
class AppLocale {
  final bool isEnglish;

  AppLocale({required this.isEnglish});

  // عمومی و تب‌ها
  String get home => isEnglish ? "Home" : "خانه";
  String get servers => isEnglish ? "Servers" : "سرورها";
  String get account => isEnglish ? "Account" : "اشتراک";
  String get settings => isEnglish ? "Settings" : "تنظیمات";

  // دکمه اتصال و وضعیت
  String get tapToConnect => isEnglish ? "TAP TO CONNECT" : "لمس برای اتصال";
  String get connecting => isEnglish ? "CONNECTING..." : "در حال اتصال...";
  String get connected => isEnglish ? "CONNECTED" : "متصل شد";
  String get disconnected => isEnglish ? "DISCONNECTED" : "قطع شده";
  String get secureConnection => isEnglish ? "Secure & Encrypted Connection" : "ارتباط امن و رمزنگاری شده";
  String get notConnected => isEnglish ? "Not Connected" : "اتصال قطع است";

  // کارت اشتراک
  String get subStatus => isEnglish ? "Subscription Status" : "وضعیت اشتراک";
  String get planName => isEnglish ? "Plan / Username" : "نام اشتراک";
  String get daysRemaining => isEnglish ? "Days Remaining" : "روز باقی‌مانده";
  String get expired => isEnglish ? "Expired" : "منقضی شده";
  String get unlimited => isEnglish ? "Unlimited" : "نامحدود";
  String get totalUsage => isEnglish ? "Total Usage" : "حجم مصرفی";
  String get totalDownload => isEnglish ? "Total Download" : "دانلود کلی";
  String get totalUpload => isEnglish ? "Total Upload" : "آپلود کلی";
  String get sessionTraffic => isEnglish ? "Live Session Traffic" : "مصرف نشست فعلی";
  String get nextRefresh => isEnglish ? "Next refresh in" : "بروزرسانی بعدی در";
  String get refreshNow => isEnglish ? "Refresh" : "بروزرسانی";

  // سرورها
  String get bestServer => isEnglish ? "BEST SERVER" : "بهترین سرور";
  String get pingAll => isEnglish ? "Test All Ping" : "تست پینگ همه سرورها";
  String get pingTesting => isEnglish ? "Testing Ping..." : "در حال تست پینگ...";
  String get searchServer => isEnglish ? "Search servers..." : "جستجوی سرورها...";
  String get noServers => isEnglish ? "No servers found" : "هیچ سروری یافت نشد";

  // تنظیمات
  String get appSettings => isEnglish ? "App Preferences" : "تنظیمات برنامه";
  String get darkMode => isEnglish ? "Night / Dark Mode" : "حالت شب (تم تیره)";
  String get languageTitle => isEnglish ? "Application Language" : "زبان برنامه";
  String get bypassIran => isEnglish ? "Bypass Local / Iran Sites" : "دور زدن سایت‌های ایرانی";
  String get bypassIranDesc => isEnglish ? "Do not proxy local banking & domestic websites" : "سایت‌های بانکی و ایرانی بدون فیلترشکن باز شوند";
  String get autoConnectTitle => isEnglish ? "Auto Connect to Best Server" : "اتصال خودکار به سریع‌ترین سرور";
  String get autoConnectDesc => isEnglish ? "Automatically select & connect to lowest ping server" : "انتخاب و اتصال خودکار به سروری که کمترین پینگ را دارد";
  String get secureDns => isEnglish ? "Secure DNS Server" : "سرور DNS امن";
  String get subUrlTitle => isEnglish ? "Subscription Link" : "لینک اشتراک";
  String get subUrlDesc => isEnglish ? "Paste your LinoVPN subscription URL here" : "لینک اشتراک دریافتی از پشتیبانی را وارد کنید";
  String get save => isEnglish ? "Save & Connect" : "ذخیره و اتصال";
  String get cancel => isEnglish ? "Cancel" : "انصراف";
  String get telegramSupport => isEnglish ? "Telegram Support Channel" : "پشتیبانی تلگرام LinoVPN";
  String get copyId => isEnglish ? "Tap to copy ID" : "لمس برای کپی آیدی";
}
