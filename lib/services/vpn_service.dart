import 'dart:async';
import 'package:flutter_v2ray/flutter_v2ray.dart';
import '../models/server_model.dart';
import 'subscription_service.dart';
import 'app_settings.dart';
import 'traffic_tracker.dart';

/// سرویس مدیریت اتصال هسته ویتوری، پینگ سریع موازی با آدرس دلخواه و شمارشگر دقیق حجم
class VpnService {
  late final FlutterV2ray _flutterV2ray;
  final SubscriptionService _subscriptionService = SubscriptionService();
  final AppSettings appSettings = AppSettings();
  final TrafficTracker trafficTracker = TrafficTracker();

  Timer? _hourlyTimer;
  bool _isInitialized = false;
  bool isPingTesting = false;
  String? testingServerId; // آیدی سروری که به صورت تک‌تک در حال تست پینگ است

  // کال‌بک‌ها برای بروزرسانی رابط کاربری
  Function(V2RayStatus status)? onStatusChanged;
  Function(List<ServerModel> updatedServers, ServerModel? bestServer)? onServersUpdated;
  Function(String message)? onLogMessage;
  Function(bool isTesting)? onPingTestingStateChanged;

  V2RayStatus currentStatus = V2RayStatus();
  List<ServerModel> serverList = [];
  ServerModel? activeServer;
  bool isManualSelected = false; // آیا کاربر سرور را به صورت دستی انتخاب کرده است؟

  VpnService() {
    _flutterV2ray = FlutterV2ray(
      onStatusChanged: (status) {
        currentStatus = status;
        // بروزرسانی و جمع ترافیک مصرفی نشست جاری
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

  /// تست پینگ تکی و فوری برای یک سرور خاص (دستی)
  Future<int> testSingleServerPing(ServerModel server) async {
    testingServerId = server.id;
    onServersUpdated?.call(serverList, activeServer);

    try {
      final config = _getNativeConfig(server.rawConfig);
      final delay = await _flutterV2ray
          .getServerDelay(
            config: config,
            url: appSettings.pingTestUrl,
          )
          .timeout(const Duration(milliseconds: 4000), onTimeout: () => -1);
      server.ping = delay;
    } catch (_) {
      server.ping = -1;
    }

    testingServerId = null;
    onServersUpdated?.call(serverList, activeServer);
    return server.ping;
  }

  /// فرآیند کامل بروزرسانی، تست پینگ سریع موازی و اتصال خودکار
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

    // شروع تست پینگ سریع موازی
    await testAllServersPing(autoConnectBest: autoConnectAfter && !isManualSelected);
  }

  /// تست پینگ سریع و همزمان با دسته‌های ۸ تایی برای سرعت فوق‌العاده
  Future<void> testAllServersPing({bool autoConnectBest = false}) async {
    if (isPingTesting || serverList.isEmpty) return;

    isPingTesting = true;
    onPingTestingStateChanged?.call(true);
    onLogMessage?.call("⚡ Testing ping for ${serverList.length} servers via ${appSettings.pingTestUrl}...");

    // اجرای همزمان در دسته‌های ۸ تایی برای پایان در کمتر از ۲ الی ۳ ثانیه
    const int batchSize = 8;
    for (int i = 0; i < serverList.length; i += batchSize) {
      final batch = serverList.skip(i).take(batchSize).toList();
      await Future.wait(batch.map((server) async {
        try {
          final config = _getNativeConfig(server.rawConfig);
          final delay = await _flutterV2ray
              .getServerDelay(
                config: config,
                url: appSettings.pingTestUrl,
              )
              .timeout(const Duration(milliseconds: 3500), onTimeout: () => -1);
          server.ping = delay;
        } catch (_) {
          server.ping = -1;
        }
      }));

      onServersUpdated?.call(serverList, activeServer);
    }

    isPingTesting = false;
    onPingTestingStateChanged?.call(false);

    // انتخاب هوشمند سریع‌ترین سرور در صورت مجاز بودن
    final validServers = serverList.where((s) => s.ping > 0).toList();
    if (validServers.isNotEmpty) {
      validServers.sort((a, b) => a.ping.compareTo(b.ping));

      if (!isManualSelected) {
        activeServer = validServers.first;
        onLogMessage?.call("⚡ Lowest ping server: ${activeServer!.name} (${activeServer!.ping}ms)");
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
        onLogMessage?.call("VPN Permission Denied by user");
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
