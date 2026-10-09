import 'dart:convert';

/// مدل هر سرور و کانفیگ شبکه LinoVPN
class ServerModel {
  final String id;
  final String name;
  final String rawConfig;
  final String protocol;
  final String host;
  final int port;
  final String flag;
  int ping; // میلی‌ثانیه (-1 به معنی تایم‌اوت یا خطا)

  ServerModel({
    required this.id,
    required this.name,
    required this.rawConfig,
    required this.protocol,
    required this.host,
    required this.port,
    required this.flag,
    this.ping = -1,
  });

  /// تشخیص پرچم کشور از روی نام کانفیگ
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
        final hashIndex = trimmed.indexOf('#');
        if (hashIndex != -1) {
          name = Uri.decodeComponent(trimmed.substring(hashIndex + 1));
        }
      }
    } catch (_) {
      // در صورت وجود کاراکترهای خاص در URI، از مقادیر پیش‌فرض استفاده می‌شود
    }

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
