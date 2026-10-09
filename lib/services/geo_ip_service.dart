import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;

/// سرویس استخراج لوکیشن دقیق واقعی سرورها بر اساس آی‌پی و حذف نام‌های اشتباه
class GeoIpService {
  static final Map<String, GeoLocation> _cache = {};

  /// تبدیل کد دوحرفی کشور (مانند DE, FR, US) به ایموجی رسمی پرچم
  static String countryCodeToFlag(String countryCode) {
    if (countryCode.length != 2) return "🌐";
    final clean = countryCode.toUpperCase();
    final first = clean.codeUnitAt(0) - 0x41 + 0x1F1E6;
    final second = clean.codeUnitAt(1) - 0x41 + 0x1F1E6;
    return String.fromCharCode(first) + String.fromCharCode(second);
  }

  /// دریافت لوکیشن واقعی سرور (کشور، شهر، پرچم و آی‌پی دقیق)
  static Future<GeoLocation?> lookup(String host) async {
    final cleanHost = host.trim();
    if (cleanHost.isEmpty) return null;

    if (_cache.containsKey(cleanHost)) {
      return _cache[cleanHost];
    }

    try {
      String ip = cleanHost;

      // اگر هاست دامنه بود، ابتدا آی‌پی آن را با DNS کشف می‌کنیم
      if (!RegExp(r'^\d+\.\d+\.\d+\.\d+$').hasMatch(cleanHost)) {
        final addresses = await InternetAddress.lookup(cleanHost).timeout(const Duration(seconds: 3));
        if (addresses.isNotEmpty) {
          ip = addresses.first.address;
        }
      }

      // استعلام از API رایگان و پرسرعت IP-API
      final url = Uri.parse("http://ip-api.com/json/$ip?fields=status,country,countryCode,city,query");
      final response = await http.get(url).timeout(const Duration(seconds: 3));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        if (data['status'] == 'success') {
          final geo = GeoLocation(
            ip: data['query'] ?? ip,
            country: data['country'] ?? 'Unknown',
            countryCode: data['countryCode'] ?? '',
            city: data['city'] ?? '',
            flag: countryCodeToFlag(data['countryCode'] ?? ''),
          );
          _cache[cleanHost] = geo;
          return geo;
        }
      }
    } catch (_) {
      // در صورت قطعی اینترنت، تلاش جایگزین
    }

    return null;
  }
}

class GeoLocation {
  final String ip;
  final String country;
  final String countryCode;
  final String city;
  final String flag;

  GeoLocation({
    required this.ip,
    required this.country,
    required this.countryCode,
    required this.city,
    required this.flag,
  });

  String get displayName {
    if (city.isNotEmpty && country.isNotEmpty) {
      return "$country ($city)";
    }
    return country.isNotEmpty ? country : "Unknown Location";
  }
}
