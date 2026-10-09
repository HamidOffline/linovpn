import 'package:flutter/material.dart';
import 'package:percent_indicator/linear_percent_indicator.dart';
import '../../models/subscription_info.dart';
import '../../services/vpn_service.dart';
import '../../utils/app_locale.dart';

class HomeTab extends StatelessWidget {
  final VpnService vpnService;
  final SubscriptionInfo? subscriptionInfo;
  final AppLocale locale;
  final bool isDark;
  final String countdownText;
  final VoidCallback onToggleConnect;
  final VoidCallback onManualRefresh;
  final VoidCallback onPingAll;
  final VoidCallback onOpenServersTab;

  const HomeTab({
    super.key,
    required this.vpnService,
    required this.subscriptionInfo,
    required this.locale,
    required this.isDark,
    required this.countdownText,
    required this.onToggleConnect,
    required this.onManualRefresh,
    required this.onPingAll,
    required this.onOpenServersTab,
  });

  @override
  Widget build(BuildContext context) {
    final isConnected = vpnService.currentStatus.state == 'CONNECTED';
    final isConnecting = vpnService.currentStatus.state == 'CONNECTING';

    final bgCard = isDark ? const Color(0xFF0F172A) : Colors.white;
    final textColor = isDark ? Colors.white : const Color(0xFF0F172A);
    final subTextColor = isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B);
    final borderColor = isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0);

    final info = subscriptionInfo;
    final usageRatio = info?.usageRatio ?? 0.28;
    final usedText = info?.usedFormatted ?? "22.4 GB";
    final totalText = info?.totalFormatted ?? "80 GB";
    final daysText = info != null ? "${info.remainingDays} ${locale.daysRemaining}" : "26 ${locale.daysRemaining}";
    final username = info?.username ?? "LinoVPN User";
    final uploadText = info?.uploadFormatted ?? "3.4 GB";
    final downloadText = info?.downloadFormatted ?? "19.0 GB";

    return SingleChildScrollView(
      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 10),
      child: Column(
        children: [
          // کارت وضعیت اشتراک مطابق درخواست
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: bgCard,
              borderRadius: BorderRadius.circular(22),
              border: Border.all(color: borderColor),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(isDark ? 0.3 : 0.05),
                  blurRadius: 10,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // نام اشتراک و روزشمار
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
                        Text(
                          username,
                          style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold),
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
                const SizedBox(height: 12),

                // نوار پیشرفت مصرف حجم
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text("${locale.totalUsage}:", style: TextStyle(color: subTextColor, fontSize: 12)),
                    Text(
                      "$usedText / $totalText",
                      style: TextStyle(color: textColor, fontSize: 12, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                LinearPercentIndicator(
                  lineHeight: 8.0,
                  percent: usageRatio,
                  backgroundColor: isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0),
                  linearGradient: const LinearGradient(
                    colors: [Color(0xFF06B6D4), Color(0xFFA855F7)],
                  ),
                  barRadius: const Radius.circular(10),
                  padding: EdgeInsets.zero,
                ),
                const SizedBox(height: 12),

                // نمایش تفکیک دانلود و آپلود کلی طبق درخواست
                Container(
                  padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                  decoration: BoxDecoration(
                    color: isDark ? const Color(0xFF020617) : const Color(0xFFF1F5F9),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      Row(
                        children: [
                          const Icon(Icons.arrow_downward_rounded, color: Color(0xFF10B981), size: 14),
                          const SizedBox(width: 4),
                          Text("${locale.totalDownload}: ", style: TextStyle(color: subTextColor, fontSize: 11)),
                          Text(downloadText, style: TextStyle(color: textColor, fontSize: 11, fontWeight: FontWeight.bold)),
                        ],
                      ),
                      Container(height: 14, width: 1, color: borderColor),
                      Row(
                        children: [
                          const Icon(Icons.arrow_upward_rounded, color: Color(0xFF06B6D4), size: 14),
                          const SizedBox(width: 4),
                          Text("${locale.totalUpload}: ", style: TextStyle(color: subTextColor, fontSize: 11)),
                          Text(uploadText, style: TextStyle(color: textColor, fontSize: 11, fontWeight: FontWeight.bold)),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 10),

                // شمارشگر ۶۰ دقیقه و دکمه بروزرسانی
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      "${locale.nextRefresh}: $countdownText",
                      style: TextStyle(color: subTextColor, fontSize: 10),
                    ),
                    InkWell(
                      onTap: onManualRefresh,
                      child: Row(
                        children: [
                          const Icon(Icons.refresh_rounded, color: Color(0xFF06B6D4), size: 14),
                          const SizedBox(width: 4),
                          Text(
                            locale.refreshNow,
                            style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 11, fontWeight: FontWeight.bold),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // دکمه بزرگ پاور مرکزی (مطابق موکاپ)
          GestureDetector(
            onTap: onToggleConnect,
            child: Stack(
              alignment: Alignment.center,
              children: [
                // هاله نئونی در وضعیت متصل
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
                // بدنه دکمه
                Container(
                  width: 155,
                  height: 155,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    gradient: RadialGradient(
                      colors: isConnected
                          ? [const Color(0xFF083344), const Color(0xFF020617)]
                          : (isDark
                              ? [const Color(0xFF1E293B), const Color(0xFF0F172A)]
                              : [const Color(0xFFF1F5F9), const Color(0xFFE2E8F0)]),
                    ),
                    border: Border.all(
                      color: isConnected
                          ? const Color(0xFF06B6D4)
                          : (isConnecting ? const Color(0xFFF59E0B) : const Color(0xFF06B6D4).withOpacity(0.4)),
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
                            : (isConnecting ? const Color(0xFFF59E0B) : const Color(0xFF06B6D4)),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        isConnecting
                            ? locale.connecting
                            : (isConnected ? locale.connected : locale.tapToConnect),
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: isConnected ? const Color(0xFF06B6D4) : textColor,
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                          letterSpacing: 0.5,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),

          // وضعیت و ترافیک لایو نشست متصل
          Text(
            isConnecting
                ? locale.connecting
                : (isConnected ? locale.secureConnection : locale.notConnected),
            style: TextStyle(
              color: isConnected
                  ? const Color(0xFF10B981)
                  : (isConnecting ? const Color(0xFFF59E0B) : subTextColor),
              fontSize: 12,
              fontWeight: FontWeight.bold,
            ),
          ),
          if (isConnected) ...[
            const SizedBox(height: 6),
            Text(
              "⬇️ ${vpnService.currentStatus.downloadSpeed}   ⬆️ ${vpnService.currentStatus.uploadSpeed}",
              style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 11, fontFamily: 'monospace', fontWeight: FontWeight.bold),
            ),
          ],
          const SizedBox(height: 20),

          // کارت وضعیت بهترین سرور انتخاب شده با دکمه تست پینگ
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: bgCard,
              borderRadius: BorderRadius.circular(18),
              border: Border.all(color: borderColor),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Text(
                      vpnService.activeServer?.flag ?? "🇩🇪",
                      style: const TextStyle(fontSize: 24),
                    ),
                    const SizedBox(width: 12),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Text(
                              vpnService.activeServer?.name ?? "LinoVPN Germany",
                              style: TextStyle(color: textColor, fontSize: 12, fontWeight: FontWeight.bold),
                            ),
                            const SizedBox(width: 6),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 1),
                              decoration: BoxDecoration(
                                color: const Color(0xFF06B6D4).withOpacity(0.15),
                                borderRadius: BorderRadius.circular(6),
                              ),
                              child: Text(locale.bestServer, style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 8, fontWeight: FontWeight.bold)),
                            ),
                          ],
                        ),
                        const SizedBox(height: 2),
                        Text(
                          vpnService.activeServer?.host ?? "fast.linovpn.me",
                          style: TextStyle(color: subTextColor, fontSize: 10, fontFamily: 'monospace'),
                        ),
                      ],
                    ),
                  ],
                ),
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: const Color(0xFF10B981).withOpacity(0.12),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: const Color(0xFF10B981).withOpacity(0.3)),
                      ),
                      child: Text(
                        vpnService.activeServer != null && vpnService.activeServer!.ping > 0
                            ? "${vpnService.activeServer!.ping}ms"
                            : "95ms",
                        style: const TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold),
                      ),
                    ),
                    const SizedBox(width: 6),
                    IconButton(
                      icon: Icon(
                        Icons.bolt_rounded,
                        color: vpnService.isPingTesting ? const Color(0xFFF59E0B) : const Color(0xFF06B6D4),
                        size: 22,
                      ),
                      tooltip: locale.pingAll,
                      onPressed: onPingAll,
                    ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),

          // دکمه دسترسی به لیست همه سرورها
          InkWell(
            onTap: onOpenServersTab,
            borderRadius: BorderRadius.circular(14),
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 16),
              decoration: BoxDecoration(
                color: isDark ? const Color(0xFF090D1A) : const Color(0xFFF1F5F9),
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: borderColor),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    "${locale.servers} (${vpnService.serverList.length})",
                    style: TextStyle(color: subTextColor, fontSize: 12),
                  ),
                  Icon(Icons.arrow_forward_ios_rounded, color: subTextColor, size: 14),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
