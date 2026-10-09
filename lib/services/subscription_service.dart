import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/subscription_info.dart';
import '../models/server_model.dart';

/// سرویس دریافت، بروزرسانی و استخراج کانفیگ‌های لینک ساب LinoVPN
class SubscriptionService {
  static const String _prefSubKey = 'linovpn_subscription_url';
  static const String _prefCachedConfigsKey = 'linovpn_cached_configs';
  static const String _prefCachedHeaderKey = 'linovpn_cached_header';

  /// دریافت لینک ساب ذخیره شده در حافظه گوشی
  Future<String?> getSavedSubscriptionUrl() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_prefSubKey);
  }

  /// ذخیره لینک ساب جدید در حافظه گوشی
  Future<void> saveSubscriptionUrl(String url) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_prefSubKey, url.trim());
  }

  /// دریافت و بروزرسانی کانفیگ‌ها از روی لینک ساب مشتری
  Future<SubscriptionResult> fetchSubscription(String subUrl) async {
    try {
      final uri = Uri.parse(subUrl.trim());
      final response = await http.get(
        uri,
        headers: {
          'User-Agent': 'v2rayNG/1.8.5 (LinoVPN Android Client)',
        },
      ).timeout(const Duration(seconds: 15));

      if (response.statusCode != 200) {
        throw Exception("خطا در برقراری ارتباط با سرور اشتراک (کد: ${response.statusCode})");
      }

      // استخراج اطلاعات هدر حجم و انقضا
      String? userInfoHeader = response.headers['subscription-userinfo'] ??
          response.headers['Subscription-Userinfo'];
      final subInfo = SubscriptionInfo.fromHeader(userInfoHeader);

      // تجزیه محتوای بدنه (معمولاً Base64 رمزنگاری شده است)
      final body = response.body.trim();
      List<String> rawLines = _decodeSubscriptionBody(body);

      // تبدیل خطوط به مدل سرورها
      List<ServerModel> servers = [];
      int index = 1;
      for (var line in rawLines) {
        final trimmed = line.trim();
        if (trimmed.isEmpty) continue;
        if (trimmed.startsWith('vless://') ||
            trimmed.startsWith('vmess://') ||
            trimmed.startsWith('trojan://') ||
            trimmed.startsWith('ss://')) {
          servers.add(ServerModel.fromRawUri(trimmed, index++));
        }
      }

      if (servers.isEmpty) {
        throw Exception("هیچ سرور معتبری در لینک اشتراک یافت نشد.");
      }

      // کش کردن برای دسترسی آفلاین
      final prefs = await SharedPreferences.getInstance();
      await prefs.setStringList(_prefCachedConfigsKey, servers.map((s) => s.rawConfig).toList());
      if (userInfoHeader != null) {
        await prefs.setString(_prefCachedHeaderKey, userInfoHeader);
      }

      return SubscriptionResult(
        isSuccess: true,
        servers: servers,
        subscriptionInfo: subInfo,
      );
    } catch (e) {
      // در صورت بروز خطا در شبکه، تلاش برای بارگذاری از کش آفلاین
      final cachedResult = await loadCachedSubscription();
      if (cachedResult != null && cachedResult.servers.isNotEmpty) {
        return cachedResult;
      }
      return SubscriptionResult(
        isSuccess: false,
        errorMessage: e.toString(),
        servers: [],
        subscriptionInfo: SubscriptionInfo(uploadBytes: 0, downloadBytes: 0, totalBytes: 0),
      );
    }
  }

  /// بارگذاری سرورهای ذخیره شده از کش گوشی (در حالت قطعی اینترنت)
  Future<SubscriptionResult?> loadCachedSubscription() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final cachedList = prefs.getStringList(_prefCachedConfigsKey);
      final cachedHeader = prefs.getString(_prefCachedHeaderKey);

      if (cachedList != null && cachedList.isNotEmpty) {
        List<ServerModel> servers = [];
        int index = 1;
        for (var raw in cachedList) {
          servers.add(ServerModel.fromRawUri(raw, index++));
        }
        return SubscriptionResult(
          isSuccess: true,
          servers: servers,
          subscriptionInfo: SubscriptionInfo.fromHeader(cachedHeader),
        );
      }
    } catch (_) {}
    return null;
  }

  /// دیکود کردن امن Base64 در لینک‌های ساب
  List<String> _decodeSubscriptionBody(String content) {
    try {
      // نرمال‌سازی پدینگ Base64 در صورت ناقص بودن
      String normalized = content.replaceAll('\n', '').replaceAll('\r', '').trim();
      while (normalized.length % 4 != 0) {
        normalized += '=';
      }
      final decodedBytes = base64Decode(normalized);
      final decodedText = utf8.decode(decodedBytes);
      return decodedText.split(RegExp(r'[\r\n]+'));
    } catch (_) {
      // اگر فرمت متنی بدون Base64 بود، مستقیماً بر اساس خطوط تقسیم شود
      return content.split(RegExp(r'[\r\n]+'));
    }
  }
}

/// نتیجه دریافت اشتراک
class SubscriptionResult {
  final bool isSuccess;
  final String? errorMessage;
  final List<ServerModel> servers;
  final SubscriptionInfo subscriptionInfo;

  SubscriptionResult({
    required this.isSuccess,
    this.errorMessage,
    required this.servers,
    required this.subscriptionInfo,
  });
}
