import 'dart:async';
import 'package:flutter_v2ray/flutter_v2ray.dart';
import '../models/server_model.dart';
import 'subscription_service.dart';

/// سرویس مدیریت اتصال هسته ویتوری، پینگ هوشمند، اتصال خودکار و تایمر ۶۰ دقیقه‌ای
class VpnService {
  final FlutterV2ray _flutterV2ray = FlutterV2ray();
  final SubscriptionService _subscriptionService = SubscriptionService();

  Timer? _hourlyTimer;
  bool _isInitialized = false;

  // کال‌بک‌ها برای بروزرسانی رابط کاربری
  Function(V2RayStatus status)? onStatusChanged;
  Function(List<ServerModel> updatedServers, ServerModel? bestServer)? onServersUpdated;
  Function(String message)? onLogMessage;

  V2RayStatus currentStatus = V2RayStatus();
  List<ServerModel> serverList = [];
  ServerModel? activeServer;

  /// مقداردهی اولیه هسته ویتوری و شنونده وضعیت
  Future<void> initialize() async {
    if (_isInitialized) return;

    await _flutterV2ray.initializeV2Ray();
    _flutterV2ray.registerStatusObserver((status) {
      currentStatus = status;
      if (onStatusChanged != null) {
        onStatusChanged!(status);
      }
    });

    _isInitialized = true;
    _start60MinutePeriodicTimer();
  }

  /// تایمر بروزرسانی خودکار هر ۶۰ دقیقه در زمان باز بودن برنامه
  void _start60MinutePeriodicTimer() {
    _hourlyTimer?.cancel();
    _hourlyTimer = Timer.periodic(const Duration(minutes: 60), (timer) async {
      onLogMessage?.call("⏰ موعد ۶۰ دقیقه فرا رسید: بروزرسانی خودکار اشتراک LinoVPN...");
      final savedUrl = await _subscriptionService.getSavedSubscriptionUrl();
      if (savedUrl != null && savedUrl.isNotEmpty) {
        await refreshSubscriptionAndOptimize(savedUrl, autoConnectAfter: true);
      }
    });
  }

  /// فرآیند کامل بروزرسانی، تست پینگ از همه سرورها و اتصال خودکار به بهترین سرور
  Future<void> refreshSubscriptionAndOptimize(String subUrl, {bool autoConnectAfter = true}) async {
    onLogMessage?.call("در حال دریافت آخرین لیست سرورها از لینک ساب...");
    final result = await _subscriptionService.fetchSubscription(subUrl);

    if (!result.isSuccess || result.servers.isEmpty) {
      onLogMessage?.call("خطا در دریافت سرورها: ${result.errorMessage}");
      return;
    }

    serverList = result.servers;
    onLogMessage?.call("دریافت ${serverList.length} سرور با موفقیت انجام شد. شروع تست پینگ هوشمند...");

    // اجرای تست پینگ از همه سرورها
    await testAllServersPing();

    // انتخاب بهترین سرور (کمترین پینگ مثبت)
    final sorted = serverList.where((s) => s.ping > 0).toList();
    if (sorted.isNotEmpty) {
      sorted.sort((a, b) => a.ping.compareTo(b.ping));
      activeServer = sorted.first;
      onLogMessage?.call("⚡ بهترین سرور شناسایی شد: ${activeServer!.name} با پینگ ${activeServer!.ping}ms");

      onServersUpdated?.call(serverList, activeServer);

      // در صورت درخواست، اتصال خودکار به بهترین سرور
      if (autoConnectAfter) {
        await connectToServer(activeServer!);
      }
    } else {
      // اگر همه پینگ‌ها منفی شد، اولین سرور را برای اتصال انتخاب کن
      activeServer = serverList.first;
      onServersUpdated?.call(serverList, activeServer);
      if (autoConnectAfter) {
        await connectToServer(activeServer!);
      }
    }
  }

  /// تست پینگ واقعی از تمام سرورها
  Future<void> testAllServersPing() async {
    for (var server in serverList) {
      try {
        final delay = await _flutterV2ray.getServerDelay(config: server.rawConfig);
        server.ping = delay;
      } catch (_) {
        server.ping = -1;
      }
    }
    // بروزرسانی UI پس از تست
    onServersUpdated?.call(serverList, activeServer);
  }

  /// اتصال به یک سرور مشخص
  Future<void> connectToServer(ServerModel server) async {
    try {
      activeServer = server;
      // بررسی مجوز اتصال VPN اندروید
      if (await _flutterV2ray.requestPermission()) {
        await _flutterV2ray.startV2Ray(
          remark: "LinoVPN: ${server.name}",
          config: server.rawConfig,
          proxyOnly: false,
        );
        onLogMessage?.call("✅ متصل به ${server.name}");
      } else {
        onLogMessage?.call("کاربر مجوز فعال‌سازی VPN را رد کرد.");
      }
    } catch (e) {
      onLogMessage?.call("خطا در شروع اتصال: $e");
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

  /// آزادسازی منابع
  void dispose() {
    _hourlyTimer?.cancel();
  }
}
