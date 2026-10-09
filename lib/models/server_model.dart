import 'dart:convert';

/// مدل هر سرور و کانفیگ شبکه LinoVPN با قابلیت ذخیره لوکیشن واقعی
class ServerModel {
  final String id;
  final String name;
  final String rawConfig;
  final String protocol;
  final String host;
  final int port;
  String flag;
  int ping; // میلی‌ثانیه (-1 به معنی تایم‌اوت یا خطا)

  // اطلاعات لوکیشن دقیق کشف‌شده از روی آی‌پی واقعی
  String? realCountry;
  String? realCity;
  String? realCountryCode;
  String? realFlag;

  ServerModel({
    required this.id,
    required this.name,
    required this.rawConfig,
    required this.protocol,
    required this.host,
    required this.port,
    required this.flag,
    this.ping = -1,
    this.realCountry,
    this.realCity,
    this.realCountryCode,
    this.realFlag,
  });

  /// پرچم نمایشی نهایی (ابتدا پرچم واقعی آی‌پی، سپس پرچم حدسی)
  String get displayFlag => realFlag ?? flag;

  /// نام نمایشی لوکیشن واقعی سرور
  String get displayLocation {
    if (realCountry != null && realCountry!.isNotEmpty) {
      if (realCity != null && realCity!.isNotEmpty) {
        return "$realCountry, $realCity";
      }
      return realCountry!;
    }
    return name;
  }

  /// تشخیص پرچم از روی نام کانفیگ به عنوان پیش‌فرض اولیه
  static String extractFlag(String name) {
    final lower = name.toLowerCase();
    if (lower.contains('de') || lower.contains('germany') || lower.contains('آلمان')) return '🇩🇪';
    if (lower.contains('fi') || lower.contains('finland') || lower.contains('فنلاند')) return '🇫🇮';
    if (lower.contains('nl') || lower.contains('netherlands') || lower.contains('هلند')) return '🇳🇱';
    if (lower.contains('fr') || lower.contains('france') || lower.contains('فرانسه')) return '🇫🇷';
    if (lower.contains('us') || lower.contains('usa') || lower.contains('آمریکا')) return '🇺🇸';
    if (lower.contains('uk') || lower.contains('england') || lower.contains('انگلیس')) return '🇬🇧';
    if (lower.contains('tr') || lower.contains('turkey') || lower.contains('ترکیه')) return '🇹🇷';
    if (lower.contains('ae') || lower.contains('dubai') || lower.contains('امارات')) return '🇦🇪';
    return '⚡';
  }

  /// ایجاد شیء سرور از روی یک خط کانفیگ
  factory ServerModel.fromRawUri(String raw, int index) {
    final trimmed = raw.trim();
    String protocol = 'V2RAY';
    String name = 'LinoVPN سرور $index';
    String host = 'server.linovpn.me';
    int port = 443;

    try {
      if (trimmed.startsWith('vless://') || trimmed.startsWith('trojan://')) {
        final uri = Uri.parse(trimmed);
        protocol = uri.scheme.toUpperCase();
        host = uri.host;
        port = uri.port > 0 ? uri.port : 443;
        if (uri.fragment.isNotEmpty) {
          name = Uri.decodeComponent(uri.fragment);
        }
      } else if (trimmed.startsWith('vmess://')) {
        protocol = 'VMESS';
        final b64 = trimmed.substring(8);
        final jsonStr = utf8.decode(base64Decode(base64.normalize(b64)));
        final data = jsonDecode(jsonStr);
        if (data is Map) {
          name = data['ps'] ?? name;
          host = data['add'] ?? host;
          port = int.tryParse(data['port']?.toString() ?? '443') ?? 443;
        }
      } else if (trimmed.startsWith('ss://')) {
        protocol = 'SHADOWSOCKS';
        var mainPart = trimmed.substring(5);
        final hashIndex = mainPart.indexOf('#');
        if (hashIndex != -1) {
          name = Uri.decodeComponent(mainPart.substring(hashIndex + 1));
          mainPart = mainPart.substring(0, hashIndex);
        }
        if (mainPart.contains('@')) {
          final atIndex = mainPart.lastIndexOf('@');
          final hostPort = mainPart.substring(atIndex + 1);
          final hpParts = hostPort.split(':');
          if (hpParts.isNotEmpty) host = hpParts[0];
          if (hpParts.length > 1) port = int.tryParse(hpParts[1].split('/')[0]) ?? 443;
        } else {
          try {
            var b64 = mainPart.split('/')[0];
            while (b64.length % 4 != 0) {
              b64 += '=';
            }
            final decoded = utf8.decode(base64Decode(b64));
            if (decoded.contains('@')) {
              final atIndex = decoded.lastIndexOf('@');
              final hostPort = decoded.substring(atIndex + 1);
              final hpParts = hostPort.split(':');
              if (hpParts.isNotEmpty) host = hpParts[0];
              if (hpParts.length > 1) port = int.tryParse(hpParts[1]) ?? 443;
            }
          } catch (_) {}
        }
      } else if (trimmed.startsWith('hysteria2://') || trimmed.startsWith('hy2://') || trimmed.startsWith('tuic://')) {
        final uri = Uri.tryParse(trimmed);
        if (uri != null) {
          protocol = uri.scheme.toUpperCase();
          host = uri.host;
          port = uri.port > 0 ? uri.port : 443;
          if (uri.fragment.isNotEmpty) {
            name = Uri.decodeComponent(uri.fragment);
          }
        }
      }
    } catch (_) {}

    return ServerModel(
      id: 'srv_$index',
      name: name,
      rawConfig: trimmed,
      protocol: protocol,
      host: host,
      port: port,
      flag: extractFlag(name),
      ping: -1,
    );
  }
}
