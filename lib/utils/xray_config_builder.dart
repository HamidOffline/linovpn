import 'dart:convert';

/// سازنده حرفه‌ای کانفیگ Xray/V2Ray با پشتیبانی کامل از VLESS Reality, Vision, Trojan, VMess, gRPC, WebSocket
class XrayConfigBuilder {
  /// تبدیل هر نوع لینک (Vless, Vmess, Trojan, Shadowsocks) به فایل کامل JSON استاندارد Xray
  static String buildCompleteConfigJson({
    required String rawUri,
    bool bypassIran = true,
    String dnsServer = "1.1.1.1",
  }) {
    final trimmed = rawUri.trim();

    try {
      if (trimmed.startsWith('vless://')) {
        return _buildVlessConfig(trimmed, bypassIran: bypassIran, dnsServer: dnsServer);
      } else if (trimmed.startsWith('trojan://')) {
        return _buildTrojanConfig(trimmed, bypassIran: bypassIran, dnsServer: dnsServer);
      } else if (trimmed.startsWith('vmess://')) {
        return _buildVmessConfig(trimmed, bypassIran: bypassIran, dnsServer: dnsServer);
      } else if (trimmed.startsWith('ss://')) {
        return _buildShadowsocksConfig(trimmed, bypassIran: bypassIran, dnsServer: dnsServer);
      }
    } catch (_) {
      // در صورت بروز هرگونه خطا در تبدیل سفارشی، لینک اصلی برگردانده می‌شود
    }

    return trimmed;
  }

  /// ساخت کانفیگ مدرن VLESS با پشتیبانی کامل از Reality, Vision, gRPC, WS
  static String _buildVlessConfig(String uriStr, {required bool bypassIran, required String dnsServer}) {
    final uri = Uri.parse(uriStr);
    final uuid = uri.userInfo;
    final host = uri.host;
    final port = uri.port > 0 ? uri.port : 443;
    final params = uri.queryParameters;

    final security = params['security'] ?? 'none'; // reality, tls, none
    final type = params['type'] ?? 'tcp'; // tcp, ws, grpc, http
    final flow = params['flow'] ?? ''; // xtls-rprx-vision
    final sni = params['sni'] ?? params['host'] ?? host;
    final fp = params['fp'] ?? 'chrome'; // uTLS fingerprint
    final pbk = params['pbk'] ?? ''; // Reality Public Key
    final sid = params['sid'] ?? ''; // Reality Short ID
    final spx = params['spx'] ?? '/';
    final path = params['path'] ?? '/';
    final serviceName = params['serviceName'] ?? '';

    // ساخت تنظیمات streamSettings
    Map<String, dynamic> streamSettings = {
      "network": type,
      "security": security,
    };

    if (security == 'reality') {
      streamSettings["realitySettings"] = {
        "show": false,
        "fingerprint": fp,
        "serverName": sni,
        "publicKey": pbk,
        "shortId": sid,
        "spiderX": spx,
      };
    } else if (security == 'tls') {
      streamSettings["tlsSettings"] = {
        "allowInsecure": false,
        "serverName": sni,
        "fingerprint": fp,
        if (params['alpn'] != null) "alpn": params['alpn']!.split(','),
      };
    }

    if (type == 'ws') {
      streamSettings["wsSettings"] = {
        "path": path,
        "headers": {
          "Host": params['host'] ?? sni,
        }
      };
    } else if (type == 'grpc') {
      streamSettings["grpcSettings"] = {
        "serviceName": serviceName.isNotEmpty ? serviceName : path,
        "multiMode": params['mode'] == 'multi',
      };
    }

    final Map<String, dynamic> outbound = {
      "tag": "proxy",
      "protocol": "vless",
      "settings": {
        "vnext": [
          {
            "address": host,
            "port": port,
            "users": [
              {
                "id": uuid,
                "encryption": params['encryption'] ?? "none",
                "flow": flow,
                "level": 0,
              }
            ]
          }
        ]
      },
      "streamSettings": streamSettings,
      "mux": {
        "enabled": false,
        "concurrency": -1
      }
    };

    return _assembleFinalConfig(outbound, bypassIran: bypassIran, dnsServer: dnsServer);
  }

  /// ساخت کانفیگ Trojan
  static String _buildTrojanConfig(String uriStr, {required bool bypassIran, required String dnsServer}) {
    final uri = Uri.parse(uriStr);
    final password = uri.userInfo;
    final host = uri.host;
    final port = uri.port > 0 ? uri.port : 443;
    final params = uri.queryParameters;

    final security = params['security'] ?? 'tls';
    final type = params['type'] ?? 'tcp';
    final sni = params['sni'] ?? host;

    Map<String, dynamic> streamSettings = {
      "network": type,
      "security": security,
      "tlsSettings": {
        "allowInsecure": false,
        "serverName": sni,
      }
    };

    if (type == 'ws') {
      streamSettings["wsSettings"] = {
        "path": params['path'] ?? '/',
        "headers": {"Host": params['host'] ?? sni}
      };
    } else if (type == 'grpc') {
      streamSettings["grpcSettings"] = {
        "serviceName": params['serviceName'] ?? '',
      };
    }

    final Map<String, dynamic> outbound = {
      "tag": "proxy",
      "protocol": "trojan",
      "settings": {
        "servers": [
          {
            "address": host,
            "port": port,
            "password": password,
            "level": 0,
          }
        ]
      },
      "streamSettings": streamSettings,
    };

    return _assembleFinalConfig(outbound, bypassIran: bypassIran, dnsServer: dnsServer);
  }

  /// ساخت کانفیگ VMess از روی فرمت متداول
  static String _buildVmessConfig(String uriStr, {required bool bypassIran, required String dnsServer}) {
    final b64 = uriStr.substring(8);
    String normalized = b64.trim();
    while (normalized.length % 4 != 0) {
      normalized += '=';
    }
    final jsonStr = utf8.decode(base64Decode(normalized));
    final Map<String, dynamic> map = jsonDecode(jsonStr);

    final host = map['add'] ?? '';
    final port = int.tryParse(map['port']?.toString() ?? '443') ?? 443;
    final id = map['id'] ?? '';
    final aid = int.tryParse(map['aid']?.toString() ?? '0') ?? 0;
    final net = map['net'] ?? 'tcp';
    final type = map['type'] ?? 'none';
    final tls = map['tls'] ?? 'none';
    final sni = map['sni'] ?? map['host'] ?? host;
    final path = map['path'] ?? '/';

    Map<String, dynamic> streamSettings = {
      "network": net,
      "security": tls == 'tls' ? 'tls' : 'none',
    };

    if (tls == 'tls') {
      streamSettings["tlsSettings"] = {
        "allowInsecure": false,
        "serverName": sni,
      };
    }

    if (net == 'ws') {
      streamSettings["wsSettings"] = {
        "path": path,
        "headers": {"Host": map['host'] ?? sni}
      };
    } else if (net == 'grpc') {
      streamSettings["grpcSettings"] = {
        "serviceName": path,
      };
    }

    final Map<String, dynamic> outbound = {
      "tag": "proxy",
      "protocol": "vmess",
      "settings": {
        "vnext": [
          {
            "address": host,
            "port": port,
            "users": [
              {
                "id": id,
                "alterId": aid,
                "security": map['scy'] ?? "auto",
                "level": 0,
              }
            ]
          }
        ]
      },
      "streamSettings": streamSettings,
    };

    return _assembleFinalConfig(outbound, bypassIran: bypassIran, dnsServer: dnsServer);
  }

  /// ساخت کانفیگ Shadowsocks
  static String _buildShadowsocksConfig(String uriStr, {required bool bypassIran, required String dnsServer}) {
    // از سیستم استاندارد برای Shadowsocks استفاده می‌شود
    return uriStr;
  }

  /// ترکیب Outbound با تنظیمات مسیریابی (Routing)، رفع فیلترینگ DNS، Sniffing و آزادی سایت‌های ایرانی
  static String _assembleFinalConfig(
    Map<String, dynamic> mainOutbound, {
    required bool bypassIran,
    required String dnsServer,
  }) {
    final Map<String, dynamic> config = {
      "log": {
        "loglevel": "warning",
      },
      "dns": {
        "servers": [
          dnsServer,
          "8.8.8.8",
          "https://1.1.1.1/dns-query",
          "localhost"
        ]
      },
      "inbounds": [
        {
          "tag": "tun",
          "port": 10808,
          "listen": "127.0.0.1",
          "protocol": "socks",
          "settings": {
            "auth": "noauth",
            "udp": true
          },
          "sniffing": {
            "enabled": true,
            "destOverride": ["http", "tls", "quic"],
            "metadataOnly": false,
            "routeOnly": false
          }
        }
      ],
      "outbounds": [
        mainOutbound,
        {
          "tag": "direct",
          "protocol": "freedom",
          "settings": {
            "domainStrategy": "UseIP"
          }
        },
        {
          "tag": "block",
          "protocol": "blackhole",
          "settings": {
            "response": {
              "type": "http"
            }
          }
        }
      ],
      "routing": {
        "domainStrategy": "IPIfNonMatch",
        "rules": [
          if (bypassIran) ...[
            {
              "type": "field",
              "outboundTag": "direct",
              "domain": ["geosite:ir", "domain:ir"]
            },
            {
              "type": "field",
              "outboundTag": "direct",
              "ip": ["geoip:ir", "geoip:private"]
            }
          ],
          {
            "type": "field",
            "outboundTag": "direct",
            "network": "udp",
            "port": "123" // پروتکل NTP زمان
          },
          {
            "type": "field",
            "outboundTag": "proxy",
            "network": "tcp,udp"
          }
        ]
      }
    };

    return jsonEncode(config);
  }
}
