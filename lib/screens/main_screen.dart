import 'dart:async';
import 'package:flutter/material.dart';
import '../models/subscription_info.dart';
import '../services/app_settings.dart';
import '../services/subscription_service.dart';
import '../services/vpn_service.dart';
import '../utils/app_locale.dart';
import 'tabs/account_tab.dart';
import 'tabs/home_tab.dart';
import 'tabs/servers_tab.dart';
import 'tabs/settings_tab.dart';

class MainScreen extends StatefulWidget {
  final Function(bool isDark) onThemeToggle;

  const MainScreen({super.key, required this.onThemeToggle});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  final VpnService _vpnService = VpnService();
  final SubscriptionService _subscriptionService = SubscriptionService();
  final AppSettings _appSettings = AppSettings();

  int _currentIndex = 0;
  SubscriptionInfo? _subscriptionInfo;
  String? _savedSubUrl;
  int _secondsUntilNextRefresh = 3600;
  Timer? _countdownTimer;

  bool _isDark = true;
  bool _isEnglish = true;

  @override
  void initState() {
    super.initState();
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    await _appSettings.load();
    setState(() {
      _isDark = _appSettings.isDarkMode;
      _isEnglish = _appSettings.isEnglish;
    });

    await _vpnService.initialize();

    _vpnService.onStatusChanged = (_) {
      if (mounted) setState(() {});
    };

    _vpnService.onServersUpdated = (_, __) {
      if (mounted) setState(() {});
    };

    _vpnService.onPingTestingStateChanged = (_) {
      if (mounted) setState(() {});
    };

    _startCountdown();

    _savedSubUrl = await _subscriptionService.getSavedSubscriptionUrl();
    if (_savedSubUrl == null || _savedSubUrl!.isEmpty) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _showSubscriptionDialog(isFirstTime: true);
      });
    } else {
      await _runFullCycle(_savedSubUrl!, autoConnect: true);
    }
  }

  void _startCountdown() {
    _countdownTimer?.cancel();
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
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

  Future<void> _runFullCycle(String url, {bool autoConnect = true}) async {
    setState(() => _secondsUntilNextRefresh = 3600);

    final result = await _subscriptionService.fetchSubscription(url);
    if (result.isSuccess) {
      setState(() => _subscriptionInfo = result.subscriptionInfo);
    }

    await _vpnService.refreshSubscriptionAndOptimize(url, autoConnectAfter: autoConnect);
  }

  void _toggleConnection() async {
    if (_vpnService.currentStatus.state == 'CONNECTED') {
      await _vpnService.disconnect();
    } else {
      if (_vpnService.activeServer != null) {
        await _vpnService.connectToServer(_vpnService.activeServer!);
      } else if (_savedSubUrl != null) {
        await _runFullCycle(_savedSubUrl!, autoConnect: true);
      } else {
        _showSubscriptionDialog();
      }
    }
  }

  void _showSubscriptionDialog({bool isFirstTime = false}) {
    final controller = TextEditingController(text: _savedSubUrl ?? "");
    final locale = AppLocale(isEnglish: _isEnglish);

    showDialog(
      context: context,
      barrierDismissible: !isFirstTime,
      builder: (ctx) => AlertDialog(
        backgroundColor: _isDark ? const Color(0xFF0F172A) : Colors.white,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(24),
          side: BorderSide(color: _isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0)),
        ),
        title: Row(
          children: [
            const Icon(Icons.vpn_key_rounded, color: Color(0xFF06B6D4)),
            const SizedBox(width: 8),
            Text(
              locale.subUrlTitle,
              style: TextStyle(color: _isDark ? Colors.white : Colors.black, fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              locale.subUrlDesc,
              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: controller,
              style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 12, fontFamily: 'monospace'),
              decoration: InputDecoration(
                hintText: "https://.../sub/...",
                hintStyle: const TextStyle(color: Color(0xFF475569)),
                filled: true,
                fillColor: _isDark ? const Color(0xFF020617) : const Color(0xFFF1F5F9),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(16)),
              ),
            ),
          ],
        ),
        actions: [
          if (!isFirstTime)
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: Text(locale.cancel, style: const TextStyle(color: Color(0xFF64748B))),
            ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF06B6D4),
              foregroundColor: Colors.black,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
            ),
            onPressed: () async {
              final newUrl = controller.text.trim();
              if (newUrl.isNotEmpty) {
                Navigator.pop(ctx);
                await _subscriptionService.saveSubscriptionUrl(newUrl);
                _savedSubUrl = newUrl;
                await _runFullCycle(newUrl, autoConnect: true);
              }
            },
            child: Text(locale.save, style: const TextStyle(fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _countdownTimer?.cancel();
    _vpnService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final locale = AppLocale(isEnglish: _isEnglish);
    final scaffoldBg = _isDark ? const Color(0xFF020617) : const Color(0xFFF8FAFC);
    final navBg = _isDark ? const Color(0xFF0B1120) : Colors.white;
    final borderColor = _isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0);

    return Directionality(
      textDirection: _isEnglish ? TextDirection.ltr : TextDirection.rtl,
      child: Scaffold(
        backgroundColor: scaffoldBg,
        appBar: AppBar(
          backgroundColor: scaffoldBg,
          elevation: 0,
          title: Row(
            children: [
              Container(
                width: 36,
                height: 36,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: const Color(0xFF06B6D4).withOpacity(0.5)),
                  image: const DecorationImage(
                    image: AssetImage('assets/images/logo.jpg'),
                    fit: BoxFit.cover,
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    "LinoVPN Pro",
                    style: TextStyle(
                      color: _isDark ? Colors.white : Colors.black,
                      fontSize: 16,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                  const Text(
                    "VIP Ultra Speed",
                    style: TextStyle(color: Color(0xFF06B6D4), fontSize: 10, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
            ],
          ),
          actions: [
            IconButton(
              icon: Icon(
                _isDark ? Icons.light_mode_rounded : Icons.dark_mode_rounded,
                color: const Color(0xFF06B6D4),
                size: 20,
              ),
              tooltip: "Theme",
              onPressed: () {
                final newMode = !_isDark;
                setState(() => _isDark = newMode);
                _appSettings.saveDarkMode(newMode);
                widget.onThemeToggle(newMode);
              },
            ),
            IconButton(
              icon: const Icon(Icons.vpn_key_rounded, color: Color(0xFF06B6D4), size: 20),
              tooltip: "Subscription",
              onPressed: () => _showSubscriptionDialog(),
            ),
            const SizedBox(width: 6),
          ],
        ),
        body: IndexedStack(
          index: _currentIndex,
          children: [
            HomeTab(
              vpnService: _vpnService,
              subscriptionInfo: _subscriptionInfo,
              locale: locale,
              isDark: _isDark,
              countdownText: _formatCountdown(),
              onToggleConnect: _toggleConnection,
              onManualRefresh: () {
                if (_savedSubUrl != null) _runFullCycle(_savedSubUrl!, autoConnect: false);
              },
              onPingAll: () => _vpnService.testAllServersPing(),
              onOpenServersTab: () => setState(() => _currentIndex = 1),
            ),
            ServersTab(
              vpnService: _vpnService,
              locale: locale,
              isDark: _isDark,
              onPingAll: () => _vpnService.testAllServersPing(),
            ),
            AccountTab(
              subscriptionInfo: _subscriptionInfo,
              subscriptionUrl: _savedSubUrl,
              locale: locale,
              isDark: _isDark,
              onRefresh: () {
                if (_savedSubUrl != null) _runFullCycle(_savedSubUrl!, autoConnect: false);
              },
              onEditUrl: () => _showSubscriptionDialog(),
            ),
            SettingsTab(
              appSettings: _appSettings,
              locale: locale,
              isDark: _isDark,
              onThemeChanged: (val) {
                setState(() => _isDark = val);
                widget.onThemeToggle(val);
              },
              onLanguageChanged: (val) {
                setState(() => _isEnglish = val);
              },
            ),
          ],
        ),
        bottomNavigationBar: Container(
          decoration: BoxDecoration(
            color: navBg,
            border: Border(top: BorderSide(color: borderColor)),
          ),
          child: BottomNavigationBar(
            currentIndex: _currentIndex,
            onTap: (index) => setState(() => _currentIndex = index),
            backgroundColor: navBg,
            selectedItemColor: const Color(0xFF06B6D4),
            unselectedItemColor: const Color(0xFF64748B),
            type: BottomNavigationBarType.fixed,
            selectedFontSize: 11,
            unselectedFontSize: 11,
            elevation: 0,
            items: [
              BottomNavigationBarItem(
                icon: const Icon(Icons.home_rounded),
                label: locale.home,
              ),
              BottomNavigationBarItem(
                icon: const Icon(Icons.public_rounded),
                label: locale.servers,
              ),
              BottomNavigationBarItem(
                icon: const Icon(Icons.person_rounded),
                label: locale.account,
              ),
              BottomNavigationBarItem(
                icon: const Icon(Icons.settings_rounded),
                label: locale.settings,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
