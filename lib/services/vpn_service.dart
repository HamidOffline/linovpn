import 'dart:async';
import 'package:flutter_v2ray/flutter_v2ray.dart';
import '../models/server_model.dart';
import 'subscription_service.dart';
import 'app_settings.dart';

/// سرویس مدیریت اتصال هسته ویتوری، پینگ دقیق ۵ ثانیه‌ای و دور زدن قطعی فیلترینگ
class VpnService {
  late final FlutterV2ray _flutterV2ray;
  final SubscriptionService _subscriptionService = SubscriptionService();
  final AppSettings appSettings = AppSettings();

  Timer? _hourlyTimer;
  bool _isInitialized = false;
  bool isPingTesting = false;

  // کال‌بک‌ها برای بروزرسانی رابط کاربری
  Function(V2RayStatus status)? onStatusChanged;
  Function(List<ServerModel> updatedServers, ServerModel? bestServer)? onServersUpdated;
  Function(String message)? onLogMessage;
  Function(bool isTesting)? onPingTestingStateChanged;

  V2RayStatus currentStatus = V2RayStatus();
  List<ServerModel> serverList = [];
  ServerModel? activeServer;

  VpnService() {
    _flutterV2ray = FlutterV2ray(
      onStatusChanged: (status) {
        currentStatus = status;
        onStatusChanged?.call(status);
      },
    );
  }

  /// مقداردهی اولیه هسته ویتوری و بارگذاری تنظیمات
  Future<void> initialize() async {
    if (_isInitialized) return;

    await appSettings.load();
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
        await refreshSubscriptionAndOptimize(savedUrl, autoConnectAfter: true);
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
    } catch (_) {
      // در صورت بروز هرگونه خطا، از لینک خام استفاده می‌شود
    }
    return rawConfig.trim();
  }

  /// فرآیند کامل بروزرسانی، تست پینگ دقیق و اتصال خودکار
  Future<void> refreshSubscriptionAndOptimize(String subUrl, {bool autoConnectAfter = true}) async {
    onLogMessage?.call("Fetching updated servers...");
    final result = await _subscriptionService.fetchSubscription(subUrl);

    if (!result.isSuccess || result.servers.isEmpty) {
      onLogMessage?.call("Failed to fetch servers: ${result.errorMessage}");
      return;
    }

    serverList = result.servers;
    activeServer ??= serverList.first;
    onServersUpdated?.call(serverList, activeServer);

    // شروع تست پینگ همگانی با تایم‌اوت بهینه
    await testAllServersPing(autoConnectBest: autoConnectAfter);
  }

  /// تست پینگ دقیق و موازی با تایم‌اوت ۵ ثانیه‌ای برای شناسایی تمام سرورهای سالم
  Future<void> testAllServersPing({bool autoConnectBest = false}) async {
    if (isPingTesting || serverList.isEmpty) return;

    isPingTesting = true;
    onPingTestingStateChanged?.call(true);
    onLogMessage?.call("⚡ Testing ping for ${serverList.length} servers...");

    // اجرای تست در دسته‌های ۳ تایی برای دقت حداکثری در شبکه ایران
    const int batchSize = 3;
    for (int i = 0; i < serverList.length; i += batchSize) {
      final batch = serverList.skip(i).take(batchSize).toList();
      await Future.wait(batch.map((server) async {
        try {
          final config = _getNativeConfig(server.rawConfig);
          // مهلت تست پینگ ۵ ثانیه برای ثبت دقیق پینگ تمام سرورهای سالم
          final delay = await _flutterV2ray
              .getServerDelay(config: config)
              .timeout(const Duration(milliseconds: 5000), onTimeout: () => -1);
          server.ping = delay;
        } catch (_) {
          server.ping = -1;
        }
      }));

      // بروزرسانی زنده UI برای نمایش پینگ‌های کشف‌شده
      onServersUpdated?.call(serverList, activeServer);
    }

    isPingTesting = false;
    onPingTestingStateChanged?.call(false);

    // انتخاب بهترین سرور (کمترین پینگ مثبت)
    final validServers = serverList.where((s) => s.ping > 0).toList();
    if (validServers.isNotEmpty) {
      validServers.sort((a, b) => a.ping.compareTo(b.ping));
      activeServer = validServers.first;
      onLogMessage?.call("⚡ Best server selected: ${activeServer!.name} (${activeServer!.ping}ms)");
      onServersUpdated?.call(serverList, activeServer);

      if (autoConnectBest && appSettings.autoConnectBest) {
        await connectToServer(activeServer!);
      }
    } else {
      activeServer = serverList.first;
      onServersUpdated?.call(serverList, activeServer);
      if (autoConnectBest && appSettings.autoConnectBest) {
        await connectToServer(activeServer!);
      }
    }
  }

  /// اتصال به سرور انتخابی در حالت کامل VPN (هدایت ۱۰۰٪ ترافیک گوشی از فیلترشکن)
  Future<void> connectToServer(ServerModel server) async {
    try {
      activeServer = server;
      if (await _flutterV2ray.requestPermission()) {
        final config = _getNativeConfig(server.rawConfig);
        await _flutterV2ray.startV2Ray(
          remark: "LinoVPN: ${server.name}",
          config: config,
          proxyOnly: false, // حالت کامل VPN برای دور زدن فیلترینگ تمام اپ‌ها
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
