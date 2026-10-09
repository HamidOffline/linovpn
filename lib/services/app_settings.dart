import 'package:shared_preferences/shared_preferences.dart';

/// مدیریت تنظیمات اختصاصی LinoVPN با پشتیبانی از تم روز/شب و زبان انگلیسی
class AppSettings {
  static const String _keyBypassIran = 'setting_bypass_iran';
  static const String _keyAutoConnect = 'setting_auto_connect';
  static const String _keyDnsServer = 'setting_dns_server';
  static const String _keyDarkMode = 'setting_dark_mode';
  static const String _keyIsEnglish = 'setting_is_english';
  static const String _keyTelegramSupport = 'setting_telegram_support';

  bool bypassIran = true;
  bool autoConnectBest = true;
  String dnsServer = "1.1.1.1";
  bool isDarkMode = true;
  bool isEnglish = true; // پیش‌فرض انگلیسی بر اساس درخواست کاربر
  String telegramSupport = "LinoVPN_Admin";

  Future<void> load() async {
    final prefs = await SharedPreferences.getInstance();
    bypassIran = prefs.getBool(_keyBypassIran) ?? true;
    autoConnectBest = prefs.getBool(_keyAutoConnect) ?? true;
    dnsServer = prefs.getString(_keyDnsServer) ?? "1.1.1.1";
    isDarkMode = prefs.getBool(_keyDarkMode) ?? true;
    isEnglish = prefs.getBool(_keyIsEnglish) ?? true;
    telegramSupport = prefs.getString(_keyTelegramSupport) ?? "LinoVPN_Admin";
  }

  Future<void> saveDarkMode(bool value) async {
    isDarkMode = value;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyDarkMode, value);
  }

  Future<void> saveLanguage(bool isEng) async {
    isEnglish = isEng;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyIsEnglish, isEng);
  }

  Future<void> saveBypassIran(bool value) async {
    bypassIran = value;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyBypassIran, value);
  }

  Future<void> saveAutoConnect(bool value) async {
    autoConnectBest = value;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyAutoConnect, value);
  }

  Future<void> saveDns(String value) async {
    dnsServer = value;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyDnsServer, value);
  }
}
