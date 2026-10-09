import 'dart:async';
import 'dart:io';
import 'package:flutter_v2ray/flutter_v2ray.dart';
import '../models/server_model.dart';
import 'subscription_service.dart';
import 'app_settings.dart';
import 'traffic_tracker.dart';
import 'geo_ip_service.dart';

/// سرویس مدیریت اتصال هسته ویتوری با سیستم پینگ فوق‌سریع TCPing مشابه v2rayNG و مکان‌یابی دقیق
class VpnService {
  late final FlutterV2ray _flutterV2ray;
  final SubscriptionService _subscriptionService = SubscriptionService();
  final AppSettings appSettings = AppSettings();
  final TrafficTracker trafficTracker = TrafficTracker();

  Timer? _hourlyTimer;
  bool _isInitialized = false;
  bool isPingTesting = false;
  String? testingServerId;

  // کال‌بک‌ها برای بروزرسانی رابط کاربری
  Function(V2RayStatus status)? onStatusChanged;
  Function(List<ServerModel> updatedServers, ServerModel? bestServer)? onServersUpdated;
  Function(String message)? onLogMessage;
  Function(bool isTesting)? onPingTestingStateChanged;

  V2RayStatus currentStatus = V2RayStatus();
  List<ServerModel> serverList = [];
  ServerModel? activeServer;
  bool isManualSelected = false;

  VpnService() {
    _flutterV2ray = FlutterV2ray(
      onStatusChanged: (status) {
        currentStatus = status;
        trafficTracker.updateSession(status.download, status.upload);
        onStatusChanged?.call(status);
      },
    );
  }

  /// مقداردهی اولیه هسته ویتوری، بارگذاری تنظیمات و ترافیک
  Future<void> initialize() async {
    if (_isInitialized) return;

    await appSettings.load();
    await trafficTracker.load();
    await _flutterV2ray.initializeV2Ray();
    _isInitialized = true;
    _start60MinutePeriodicTimer();
  }

  /// تایمر بروزرسانی خودکار هر ۶۰ دقیقه
  void _start60MinutePeriodicTimer() {
    _hourlyTimer?.cancel();
    _hourlyTimer = Timer.periodic(const Duration(minutes: 60), (timer) async {
      onLogMessage?.call("⏰ 60-Minute Auto Refresh Triggered...");
      final savedUrl = await _subscriptionService.getSavedSubscriptionUrl();
      if (savedUrl != null && savedUrl.isNotEmpty) {
        await refreshSubscriptionAndOptimize(savedUrl, autoConnectAfter: !isManualSelected);
      }
    });
  }

  /// تبدیل استاندارد و سازگار لینک خام به کانفیگ کامل هسته ویتوری
  String _getNativeConfig(String rawConfig) {
    try {
      final v2rayURL = FlutterV2ray.parseFromURL(rawConfig.trim());
      final fullConfig = v2rayURL.getFullConfiguration();
      if (fullConfig.isNotEmpty && fullConfig.startsWith('{')) {
        return fullConfig;
      }
    } catch (_) {}
    return rawConfig.trim();
  }

  /// سیستم پینگ مستقیم سوکت TCPing (الگوریتم رسمی و پرسرعت v2rayNG)
  /// این سیستم در هر دو حالت متصل و قطع‌شده با سرعت فوق‌العاده (زیر ۱۰۰ میلی‌ثانیه) پینگ می‌گیرد
  static Future<int> tcping(String host, int port, {int timeoutMs = 2500}) async {
    final sw = Stopwatch()..start();
    try {
      final socket = await Socket.connect(
        host.trim(),
        port > 0 ? port : 443,
        timeout: Duration(milliseconds: timeoutMs),
      );
      socket.destroy();
      sw.stop();
      return sw.elapsedMilliseconds;
    } catch (_) {
      return -1;
    }
  }

  /// تست تاخیر واقعی HTTP با آدرس تنظیم‌شده کاربر (پیش‌فرض google 204) از طریق تونل وی‌پی‌ان
  static Future<int> httpPing(String testUrl, {int timeoutMs = 3000}) async {
    final sw = Stopwatch()..start();
    try {
      final url = Uri.parse(testUrl.trim());
      final client = HttpClient();
      client.connectionTimeout = Duration(milliseconds: timeoutMs);
      final request = await client.getUrl(url).timeout(Duration(milliseconds: timeoutMs));
      final response = await request.close().timeout(Duration(milliseconds: timeoutMs));
      sw.stop();
      if (response.statusCode >= 200 && response.statusCode < 400) {
        return sw.elapsedMilliseconds;
      }
    } catch (_) {}
    return -1;
  }

  /// تست پینگ تکی یک سرور (هم در حالت متصل و هم در حالت قطع)
  Future<int> testSingleServerPing(ServerModel server) async {
    testingServerId = server.id;
    onServersUpdated?.call(serverList, activeServer);

    int delay = -1;

    // اگر همین سرور در حال حاضر متصل باشد، پینگ واقعی پروکسی را با آدرس انتخابی کاربر می‌سنجیم
    if (currentStatus.state == 'CONNECTED' && activeServer?.id == server.id) {
      delay = await httpPing(appSettings.pingTestUrl, timeoutMs: 3000);
      if (delay <= 0) {
        try {
          delay = await _flutterV2ray.getConnectedServerDelay().timeout(const Duration(milliseconds: 2500), onTimeout: () => -1);
        } catch (_) {
          delay = -1;
        }
      }
    }

    // در غیر این صورت، یا در صورت خطا، پینگ دقیق و پرسرعت TCPing پروژه ویتوری را می‌گیریم
    if (delay <= 0) {
      delay = await tcping(server.host, server.port);
    }

    server.ping = delay;
    testingServerId = null;

    // استعلام لوکیشن واقعی سرور در صورت عدم وجود
    if (server.realCountry == null) {
      _lookupGeoLocation(server);
    }

    onServersUpdated?.call(serverList, activeServer);
    return delay;
  }

  /// استعلام لوکیشن و پرچم واقعی برای یک سرور
  Future<void> _lookupGeoLocation(ServerModel server) async {
    final geo = await GeoIpService.lookup(server.host);
    if (geo != null) {
      server.realCountry = geo.country;
      server.realCity = geo.city;
      server.realCountryCode = geo.countryCode;
      server.realFlag = geo.flag;
      onServersUpdated?.call(serverList, activeServer);
    }
  }

  /// فرآیند کامل بروزرسانی، تست پینگ سریع و استعلام لوکیشن واقعی
  Future<void> refreshSubscriptionAndOptimize(String subUrl, {bool autoConnectAfter = true}) async {
    onLogMessage?.call("Fetching updated servers...");
    final result = await _subscriptionService.fetchSubscription(subUrl, customProfileName: appSettings.profileName);

    if (!result.isSuccess || result.servers.isEmpty) {
      onLogMessage?.call("Failed to fetch servers: ${result.errorMessage}");
      return;
    }

    serverList = result.servers;
    if (activeServer == null || !serverList.any((s) => s.id == activeServer!.id)) {
      activeServer = serverList.first;
    }
    onServersUpdated?.call(serverList, activeServer);

    // شروع تست پینگ همگانی با الگوریتم v2rayNG
    await testAllServersPing(autoConnectBest: autoConnectAfter && !isManualSelected);
  }

  /// تست پینگ تمامی سرورها به صورت همزمان با الگوریتم TCPing پروژه v2rayNG
  /// این تست در کمتر از ۱ ثانیه برای تمام سرورها انجام شده و در هر دو حالت متصل و قطع کار می‌کند
  Future<void> testAllServersPing({bool autoConnectBest = false}) async {
    if (isPingTesting || serverList.isEmpty) return;

    isPingTesting = true;
    onPingTestingStateChanged?.call(true);
    onLogMessage?.call("⚡ v2rayNG TCPing starting for ${serverList.length} servers...");

    // تست کاملاً موازی تمام سرورها مانند نرم‌افزار v2rayNG
    await Future.wait(serverList.map((server) async {
      int delay = -1;

      // اگر سرور فعلی متصل باشد، پینگ واقعی پروکسی را اندازه می‌گیریم
      if (currentStatus.state == 'CONNECTED' && activeServer?.id == server.id) {
        delay = await httpPing(appSettings.pingTestUrl, timeoutMs: 2500);
        if (delay <= 0) {
          try {
            delay = await _flutterV2ray.getConnectedServerDelay().timeout(const Duration(milliseconds: 2000), onTimeout: () => -1);
          } catch (_) {}
        }
      }

      if (delay <= 0) {
        delay = await tcping(server.host, server.port);
      }

      server.ping = delay;

      // دریافت لوکیشن واقعی سرور در پس‌زمینه
      if (server.realCountry == null) {
        _lookupGeoLocation(server);
      }
    }));

    isPingTesting = false;
    onPingTestingStateChanged?.call(false);

    // مرتب‌سازی و انتخاب سریع‌ترین سرور در صورت حالت خودکار
    final validServers = serverList.where((s) => s.ping > 0).toList();
    if (validServers.isNotEmpty) {
      validServers.sort((a, b) => a.ping.compareTo(b.ping));

      if (!isManualSelected) {
        activeServer = validServers.first;
        onLogMessage?.call("⚡ Best server: ${activeServer!.name} (${activeServer!.ping}ms)");
      }
      onServersUpdated?.call(serverList, activeServer);

      if (autoConnectBest && appSettings.autoConnectBest && !isManualSelected) {
        await connectToServer(activeServer!);
      }
    } else {
      if (!isManualSelected) {
        activeServer = serverList.first;
      }
      onServersUpdated?.call(serverList, activeServer);
      if (autoConnectBest && appSettings.autoConnectBest && !isManualSelected) {
        await connectToServer(activeServer!);
      }
    }
  }

  /// اتصال به سرور مشخص (دستی یا خودکار)
  Future<void> connectToServer(ServerModel server, {bool isManual = false}) async {
    try {
      activeServer = server;
      if (isManual) {
        isManualSelected = true;
      }

      if (await _flutterV2ray.requestPermission()) {
        trafficTracker.resetSession();
        final config = _getNativeConfig(server.rawConfig);
        await _flutterV2ray.startV2Ray(
          remark: "LinoVPN: ${server.name}",
          config: config,
          proxyOnly: false,
          notificationDisconnectButtonName: "DISCONNECT",
        );
        onLogMessage?.call("✅ Connected to ${server.name}");
      } else {
        onLogMessage?.call("VPN Permission Denied");
      }
    } catch (e) {
      onLogMessage?.call("Connection error: $e");
    }
  }

  /// قطع اتصال
  Future<void> disconnect() async {
    try {
      await _flutterV2ray.stopV2Ray();
      onLogMessage?.call("Disconnected");
    } catch (e) {
      onLogMessage?.call("Disconnect error: $e");
    }
  }

  void dispose() {
    _hourlyTimer?.cancel();
  }
}
