import 'dart:async';
import 'package:flutter_v2ray/flutter_v2ray.dart';
import '../models/server_model.dart';
import '../utils/xray_config_builder.dart';
import 'subscription_service.dart';
import 'app_settings.dart';

/// سرویس مدیریت اتصال هسته ویتوری، پینگ سریع موازی، اتصال خودکار و رفع فیلترینگ
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
      onLogMessage?.call("⏰ موعد ۶۰ دقیقه: بروزرسانی خودکار اشتراک LinoVPN...");
      final savedUrl = await _subscriptionService.getSavedSubscriptionUrl();
      if (savedUrl != null && savedUrl.isNotEmpty) {
        await refreshSubscriptionAndOptimize(savedUrl, autoConnectAfter: true);
      }
    });
  }

  /// ساخت کانفیگ نهایی Xray با دور زدن هوشمند سایت‌های داخلی و DNS
  String _prepareConfig(String rawConfig) {
    return XrayConfigBuilder.buildCompleteConfigJson(
      rawUri: rawConfig,
      bypassIran: appSettings.bypassIran,
      dnsServer: appSettings.dnsServer,
    );
  }

  /// فرآیند کامل بروزرسانی، تست پینگ موازی سریع و اتصال خودکار
  Future<void> refreshSubscriptionAndOptimize(String subUrl, {bool autoConnectAfter = true}) async {
    onLogMessage?.call("در حال دریافت سرورها از لینک ساب...");
    final result = await _subscriptionService.fetchSubscription(subUrl);

    if (!result.isSuccess || result.servers.isEmpty) {
      onLogMessage?.call("خطا: ${result.errorMessage}");
      return;
    }

    serverList = result.servers;
    activeServer ??= serverList.first;
    onServersUpdated?.call(serverList, activeServer);

    // شروع تست پینگ سریع و موازی
    await testAllServersPing(autoConnectBest: autoConnectAfter);
  }

  /// تست پینگ فوق‌العاده سریع به صورت موازی (Parallel) برای حل کندی برنامه
  Future<void> testAllServersPing({bool autoConnectBest = false}) async {
    if (isPingTesting || serverList.isEmpty) return;

    isPingTesting = true;
    onPingTestingStateChanged?.call(true);
    onLogMessage?.call("⚡ در حال تست پینگ همزمان سرورها...");

    // اجرای پینگ‌ها در دسته‌های ۴ تایی برای عدم اشغال بیش‌از‌حد شبکه و سرعت حداکثری
    const int batchSize = 4;
    for (int i = 0; i < serverList.length; i += batchSize) {
      final batch = serverList.skip(i).take(batchSize).toList();
      await Future.wait(batch.map((server) async {
        try {
          final config = _prepareConfig(server.rawConfig);
          final delay = await _flutterV2ray
              .getServerDelay(config: config)
              .timeout(const Duration(milliseconds: 2500), onTimeout: () => -1);
          server.ping = delay;
        } catch (_) {
          server.ping = -1;
        }
      }));

      // بروزرسانی لحظه‌ای UI تا کاربر معطل اتمام تست نماند
      onServersUpdated?.call(serverList, activeServer);
    }

    isPingTesting = false;
    onPingTestingStateChanged?.call(false);

    // انتخاب هوشمند بهترین سرور با کمترین پینگ
    final validServers = serverList.where((s) => s.ping > 0).toList();
    if (validServers.isNotEmpty) {
      validServers.sort((a, b) => a.ping.compareTo(b.ping));
      activeServer = validServers.first;
      onLogMessage?.call("⚡ بهترین سرور: ${activeServer!.name} (${activeServer!.ping}ms)");
      onServersUpdated?.call(serverList, activeServer);

      if (autoConnectBest && appSettings.autoConnectBest) {
        await connectToServer(activeServer!);
      }
    } else {
      activeServer = serverList.first;
      onLogMessage?.call("سرور اول انتخاب شد.");
      onServersUpdated?.call(serverList, activeServer);

      if (autoConnectBest && appSettings.autoConnectBest) {
        await connectToServer(activeServer!);
      }
    }
  }

  /// اتصال به سرور مشخص با آماده‌سازی کانفیگ Xray
  Future<void> connectToServer(ServerModel server) async {
    try {
      activeServer = server;
      if (await _flutterV2ray.requestPermission()) {
        final fullConfig = _prepareConfig(server.rawConfig);
        await _flutterV2ray.startV2Ray(
          remark: "LinoVPN: ${server.name}",
          config: fullConfig,
          proxyOnly: false,
          notificationDisconnectButtonName: "قطع اتصال",
        );
        onLogMessage?.call("✅ متصل به ${server.name}");
      } else {
        onLogMessage?.call("مجوز اتصال داده نشد.");
      }
    } catch (e) {
      onLogMessage?.call("خطا در اتصال: $e");
    }
  }

  /// قطع اتصال
  Future<void> disconnect() async {
    try {
      await _flutterV2ray.stopV2Ray();
      onLogMessage?.call("اتصال قطع شد.");
    } catch (e) {
      onLogMessage?.call("خطا در قطع اتصال: $e");
    }
  }

  void dispose() {
    _hourlyTimer?.cancel();
  }
}
