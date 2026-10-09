import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_v2ray/flutter_v2ray.dart';
import 'package:percent_indicator/linear_percent_indicator.dart';
import '../models/server_model.dart';
import '../models/subscription_info.dart';
import '../services/subscription_service.dart';
import '../services/vpn_service.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with SingleTickerProviderStateMixin {
  final VpnService _vpnService = VpnService();
  final SubscriptionService _subscriptionService = SubscriptionService();

  SubscriptionInfo? _subscriptionInfo;
  String? _savedSubUrl;
  bool _isLoading = false;
  String _statusMessage = "در حال آماده‌سازی...";

  // تایمر ۶۰ دقیقه‌ای برای نمایش شمارشگر معکوس روی UI
  int _secondsUntilNextRefresh = 3600;
  Timer? _uiCountdownTimer;

  late AnimationController _pulseController;

  @override
  void initState() {
    super.initState();

    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat(reverse: true);

    _initApp();
    _startUiCountdown();
  }

  void _startUiCountdown() {
    _uiCountdownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (mounted) {
        setState(() {
          if (_secondsUntilNextRefresh > 0) {
            _secondsUntilNextRefresh--;
          } else {
            _secondsUntilNextRefresh = 3600;
          }
        });
      }
    });
  }

  String _formatCountdown() {
    final m = (_secondsUntilNextRefresh ~/ 60).toString().padLeft(2, '0');
    final s = (_secondsUntilNextRefresh % 60).toString().padLeft(2, '0');
    return "$m:$s";
  }

  Future<void> _initApp() async {
    setState(() => _isLoading = true);

    // مقداردهی سرویس VPN
    await _vpnService.initialize();

    _vpnService.onStatusChanged = (status) {
      if (mounted) setState(() {});
    };

    _vpnService.onServersUpdated = (servers, best) {
      if (mounted) setState(() {});
    };

    _vpnService.onLogMessage = (msg) {
      if (mounted) {
        setState(() => _statusMessage = msg);
      }
    };

    // بررسی وجود لینک ساب در حافظه
    _savedSubUrl = await _subscriptionService.getSavedSubscriptionUrl();

    if (_savedSubUrl == null || _savedSubUrl!.isEmpty) {
      setState(() => _isLoading = false);
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _showSubscriptionDialog(isFirstTime: true);
      });
    } else {
      // اجرای خودکار در اولین اجرا: بروزرسانی ساب + تست پینگ + اتصال به بهترین سرور
      await _runFullRefreshCycle(_savedSubUrl!, autoConnect: true);
    }
  }

  Future<void> _runFullRefreshCycle(String url, {bool autoConnect = true}) async {
    setState(() {
      _isLoading = true;
      _secondsUntilNextRefresh = 3600;
    });

    final subResult = await _subscriptionService.fetchSubscription(url);
    if (subResult.isSuccess) {
      _subscriptionInfo = subResult.subscriptionInfo;
    }

    await _vpnService.refreshSubscriptionAndOptimize(url, autoConnectAfter: autoConnect);

    if (mounted) {
      setState(() => _isLoading = false);
    }
  }

  void _toggleConnection() async {
    if (_vpnService.currentStatus.state == 'CONNECTED') {
      await _vpnService.disconnect();
    } else {
      if (_vpnService.activeServer != null) {
        await _vpnService.connectToServer(_vpnService.activeServer!);
      } else if (_savedSubUrl != null) {
        await _runFullRefreshCycle(_savedSubUrl!, autoConnect: true);
      } else {
        _showSubscriptionDialog();
      }
    }
  }

  void _showSubscriptionDialog({bool isFirstTime = false}) {
    final controller = TextEditingController(text: _savedSubUrl ?? "");

    showDialog(
      context: context,
      barrierDismissible: !isFirstTime,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF0F172A),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(24),
          side: const BorderSide(color: Color(0xFF1E293B)),
        ),
        title: Row(
          children: [
            const Icon(Icons.vpn_key_rounded, color: Color(0xFF06B6D4)),
            const SizedBox(width: 8),
            Text(
              isFirstTime ? "ورود به LinoVPN" : "تنظیم لینک اشتراک",
              style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "لطفاً لینک اشتراک اختصاصی خود را که از ربات یا پشتیبانی دریافت کرده‌اید وارد نمایید:",
              style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13, height: 1.5),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: controller,
              style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 13, fontFamily: 'monospace'),
              decoration: InputDecoration(
                hintText: "https://.../sub/...",
                hintStyle: const TextStyle(color: Color(0xFF475569)),
                filled: true,
                fillColor: const Color(0xFF020617),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(16),
                  borderSide: const BorderSide(color: Color(0xFF334155)),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(16),
                  borderSide: const BorderSide(color: Color(0xFF06B6D4)),
                ),
              ),
            ),
          ],
        ),
        actions: [
          if (!isFirstTime)
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text("انصراف", style: TextStyle(color: Color(0xFF64748B))),
            ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF06B6D4),
              foregroundColor: Colors.black,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
            ),
            onPressed: () async {
              final newUrl = controller.text.trim();
              if (newUrl.isNotEmpty) {
                Navigator.pop(ctx);
                await _subscriptionService.saveSubscriptionUrl(newUrl);
                _savedSubUrl = newUrl;
                await _runFullRefreshCycle(newUrl, autoConnect: true);
              }
            },
            child: const Text("ذخیره و اتصال خودکار", style: TextStyle(fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _pulseController.dispose();
    _uiCountdownTimer?.cancel();
    _vpnService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isConnected = _vpnService.currentStatus.state == 'CONNECTED';
    final isConnecting = _vpnService.currentStatus.state == 'CONNECTING';

    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        backgroundColor: const Color(0xFF020617),
        body: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
            child: Column(
              children: [
                // هدر برنامه و لوگوی LinoVPN
                _buildHeader(),
                const SizedBox(height: 16),

                // کارت وضعیت اشتراک (حجم مصرفی و روزشمار انقضا)
                _buildSubscriptionCard(),
                const SizedBox(height: 24),

                // دکمه بزرگ اتصال مرکزی
                Expanded(
                  child: Center(
                    child: _buildPowerButton(isConnected, isConnecting),
                  ),
                ),

                // نمایش وضعیت اتصال و سرعت لحظه‌ای
                _buildSpeedAndStatus(isConnected, isConnecting),
                const SizedBox(height: 16),

                // کارت بهترین سرور انتخاب شده
                _buildBestServerCard(),
                const SizedBox(height: 12),

                // دکمه مشاهده تمام سرورها
                _buildServerListButton(),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: const Color(0xFF06B6D4).withOpacity(0.4)),
                image: const DecorationImage(
                  image: AssetImage('assets/images/logo.jpg'),
                  fit: BoxFit.cover,
                ),
              ),
            ),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: const [
                Text(
                  "LinoVPN Pro",
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.w900,
                    letterSpacing: 0.5,
                  ),
                ),
                Text(
                  "سرویس ضد فیلتر پرسرعت",
                  style: TextStyle(color: Color(0xFF06B6D4), fontSize: 11, fontWeight: FontWeight.w500),
                ),
              ],
            ),
          ],
        ),
        IconButton(
          onPressed: () => _showSubscriptionDialog(),
          icon: Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: const Color(0xFF0F172A),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: const Color(0xFF1E293B)),
            ),
            child: const Icon(Icons.settings_outlined, color: Color(0xFF06B6D4), size: 20),
          ),
        ),
      ],
    );
  }

  Widget _buildSubscriptionCard() {
    final info = _subscriptionInfo;
    final usageRatio = info?.usageRatio ?? 0.28;
    final usageText = info?.formattedUsage ?? "22.4 GB از 80 GB";
    final daysText = info?.formattedRemainingDays ?? "26 روز باقی‌مانده";

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF0F172A).withOpacity(0.85),
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: const Color(0xFF1E293B)),
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    width: 8,
                    height: 8,
                    decoration: const BoxDecoration(
                      color: Color(0xFF06B6D4),
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 8),
                  const Text(
                    "وضعیت اشتراک LinoVPN",
                    style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: const Color(0xFF10B981).withOpacity(0.15),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: const Color(0xFF10B981).withOpacity(0.3)),
                ),
                child: Text(
                  daysText,
                  style: const TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text("حجم مصرفی:", style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
              Text(
                usageText,
                style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
              ),
            ],
          ),
          const SizedBox(height: 8),
          LinearPercentIndicator(
            lineHeight: 8.0,
            percent: usageRatio,
            backgroundColor: const Color(0xFF1E293B),
            linearGradient: const LinearGradient(
              colors: [Color(0xFF06B6D4), Color(0xFFA855F7)],
            ),
            barRadius: const Radius.circular(10),
            padding: EdgeInsets.zero,
          ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                "بروزرسانی بعدی در: ${_formatCountdown()}",
                style: const TextStyle(color: Color(0xFF64748B), fontSize: 11),
              ),
              InkWell(
                onTap: _isLoading
                    ? null
                    : () {
                        if (_savedSubUrl != null) {
                          _runFullRefreshCycle(_savedSubUrl!, autoConnect: false);
                        }
                      },
                child: Row(
                  children: const [
                    Icon(Icons.refresh_rounded, color: Color(0xFF06B6D4), size: 14),
                    SizedBox(width: 4),
                    Text(
                      "بروزرسانی دستی",
                      style: TextStyle(color: Color(0xFF06B6D4), fontSize: 11, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildPowerButton(bool isConnected, bool isConnecting) {
    return GestureDetector(
      onTap: _toggleConnection,
      child: Stack(
        alignment: Alignment.center,
        children: [
          // حلقه درخشان بیرونی
          Container(
            width: 190,
            height: 190,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              boxShadow: isConnected
                  ? [
                      BoxShadow(
                        color: const Color(0xFF06B6D4).withOpacity(0.4),
                        blurRadius: 40,
                        spreadRadius: 10,
                      ),
                      BoxShadow(
                        color: const Color(0xFFA855F7).withOpacity(0.3),
                        blurRadius: 60,
                        spreadRadius: 5,
                      ),
                    ]
                  : [],
            ),
          ),
          // دکمه اصلی
          Container(
            width: 155,
            height: 155,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: RadialGradient(
                colors: isConnected
                    ? [const Color(0xFF083344), const Color(0xFF020617)]
                    : [const Color(0xFF1E293B), const Color(0xFF0F172A)],
              ),
              border: Border.all(
                color: isConnected
                    ? const Color(0xFF06B6D4)
                    : (isConnecting ? const Color(0xFFF59E0B) : const Color(0xFF334155)),
                width: 4,
              ),
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(
                  Icons.power_settings_new_rounded,
                  size: 54,
                  color: isConnected
                      ? const Color(0xFF06B6D4)
                      : (isConnecting ? const Color(0xFFF59E0B) : const Color(0xFF64748B)),
                ),
                const SizedBox(height: 8),
                Text(
                  isConnecting
                      ? "در حال اتصال..."
                      : (isConnected ? "متصل شد" : "لمس برای اتصال"),
                  style: TextStyle(
                    color: isConnected ? const Color(0xFF06B6D4) : Colors.white70,
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSpeedAndStatus(bool isConnected, bool isConnecting) {
    return Column(
      children: [
        Text(
          isConnecting
              ? "در حال برقراری ارتباط ایمن..."
              : (isConnected ? "اتصال ایمن برقرار است" : "اتصال قطع است"),
          style: TextStyle(
            color: isConnected
                ? const Color(0xFF10B981)
                : (isConnecting ? const Color(0xFFF59E0B) : const Color(0xFF64748B)),
            fontSize: 13,
            fontWeight: FontWeight.bold,
          ),
        ),
        if (isConnected) ...[
          const SizedBox(height: 6),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                "⬇️ ${_vpnService.currentStatus.downloadSpeed} / ⬆️ ${_vpnService.currentStatus.uploadSpeed}",
                style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 11, fontFamily: 'monospace'),
              ),
            ],
          ),
        ],
      ],
    );
  }

  Widget _buildBestServerCard() {
    final server = _vpnService.activeServer;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: const Color(0xFF0F172A),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFF1E293B)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Text(
                server?.flag ?? "🌐",
                style: const TextStyle(fontSize: 24),
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        server?.name ?? "سرور هوشمند LinoVPN",
                        style: const TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold),
                      ),
                      const SizedBox(width: 6),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 1),
                        decoration: BoxDecoration(
                          color: const Color(0xFF06B6D4).withOpacity(0.15),
                          borderRadius: BorderRadius.circular(6),
                        ),
                        child: const Text("سریع‌ترین", style: TextStyle(color: Color(0xFF06B6D4), fontSize: 9)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 2),
                  Text(
                    server?.host ?? "خودکار بر اساس پینگ",
                    style: const TextStyle(color: Color(0xFF64748B), fontSize: 10, fontFamily: 'monospace'),
                  ),
                ],
              ),
            ],
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            decoration: BoxDecoration(
              color: const Color(0xFF10B981).withOpacity(0.12),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: const Color(0xFF10B981).withOpacity(0.3)),
            ),
            child: Text(
              server != null && server.ping > 0 ? "${server.ping}ms" : "98ms",
              style: const TextStyle(color: Color(0xFF10B981), fontSize: 12, fontWeight: FontWeight.bold),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildServerListButton() {
    final count = _vpnService.serverList.length;

    return InkWell(
      onTap: () {
        _showServerListModal();
      },
      borderRadius: BorderRadius.circular(14),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 16),
        decoration: BoxDecoration(
          color: const Color(0xFF090D1A),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: const Color(0xFF1E293B)),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              "مشاهده لیست سرورها ($count سرور دریافت شده)",
              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12),
            ),
            const Icon(Icons.keyboard_arrow_down_rounded, color: Color(0xFF64748B), size: 20),
          ],
        ),
      ),
    );
  }

  void _showServerListModal() {
    showModalBottomSheet(
      context: context,
      backgroundColor: const Color(0xFF0F172A),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) {
        return Directionality(
          textDirection: TextDirection.rtl,
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      "لیست سرورهای LinoVPN",
                      style: TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.bold),
                    ),
                    IconButton(
                      icon: const Icon(Icons.refresh, color: Color(0xFF06B6D4)),
                      onPressed: () async {
                        await _vpnService.testAllServersPing();
                        setState(() {});
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Expanded(
                  child: ListView.separated(
                    itemCount: _vpnService.serverList.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 8),
                    itemBuilder: (context, index) {
                      final item = _vpnService.serverList[index];
                      final isSelected = _vpnService.activeServer?.id == item.id;

                      return ListTile(
                        tileColor: isSelected ? const Color(0xFF06B6D4).withOpacity(0.1) : const Color(0xFF020617),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                          side: BorderSide(
                            color: isSelected ? const Color(0xFF06B6D4) : const Color(0xFF1E293B),
                          ),
                        ),
                        leading: Text(item.flag, style: const TextStyle(fontSize: 22)),
                        title: Text(item.name, style: const TextStyle(color: Colors.white, fontSize: 13)),
                        subtitle: Text(item.protocol, style: const TextStyle(color: Color(0xFF64748B), fontSize: 10)),
                        trailing: Text(
                          item.ping > 0 ? "${item.ping}ms" : "تست نشده",
                          style: TextStyle(
                            color: item.ping > 0 && item.ping < 200 ? const Color(0xFF10B981) : const Color(0xFFF59E0B),
                            fontSize: 12,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        onTap: () async {
                          Navigator.pop(ctx);
                          await _vpnService.connectToServer(item);
                          setState(() {});
                        },
                      );
                    },
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
