import 'package:shared_preferences/shared_preferences.dart';

/// مدیریت تنظیمات اختصاصی LinoVPN با آدرس پینگ سفارشی، نام اشتراک و انتخاب دستی/خودکار سرور
class AppSettings {
  static const String _keyBypassIran = 'setting_bypass_iran';
  static const String _keyAutoConnect = 'setting_auto_connect';
  static const String _keyDnsServer = 'setting_dns_server';
  static const String _keyDarkMode = 'setting_dark_mode';
  static const String _keyIsEnglish = 'setting_is_english';
  static const String _keyTelegramSupport = 'setting_telegram_support';
  static const String _keyProfileName = 'setting_profile_name';
  static const String _keyPingTestUrl = 'setting_ping_test_url';

  bool bypassIran = true;
  bool autoConnectBest = true;
  String dnsServer = "1.1.1.1";
  bool isDarkMode = true;
  bool isEnglish = true;
  String telegramSupport = "LinoVPN_Admin";
  String profileName = "LinoVPN VIP Account";
  String pingTestUrl = "http://www.google.com/generate_204";

  Future<void> load() async {
    final prefs = await SharedPreferences.getInstance();
    bypassIran = prefs.getBool(_keyBypassIran) ?? true;
    autoConnectBest = prefs.getBool(_keyAutoConnect) ?? true;
    dnsServer = prefs.getString(_keyDnsServer) ?? "1.1.1.1";
    isDarkMode = prefs.getBool(_keyDarkMode) ?? true;
    isEnglish = prefs.getBool(_keyIsEnglish) ?? true;
    telegramSupport = prefs.getString(_keyTelegramSupport) ?? "LinoVPN_Admin";
    profileName = prefs.getString(_keyProfileName) ?? "LinoVPN VIP Account";
    pingTestUrl = prefs.getString(_keyPingTestUrl) ?? "http://www.google.com/generate_204";
  }

  Future<void> savePingTestUrl(String url) async {
    pingTestUrl = url.trim().isEmpty ? "http://www.google.com/generate_204" : url.trim();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyPingTestUrl, pingTestUrl);
  }

  Future<void> saveProfileName(String name) async {
    profileName = name.trim().isEmpty ? "LinoVPN VIP Account" : name.trim();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyProfileName, profileName);
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
