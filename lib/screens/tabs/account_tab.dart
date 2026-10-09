import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../models/subscription_info.dart';
import '../../services/vpn_service.dart';
import '../../services/traffic_tracker.dart';
import '../../utils/app_locale.dart';

class AccountTab extends StatelessWidget {
  final VpnService? vpnService;
  final SubscriptionInfo? subscriptionInfo;
  final String? subscriptionUrl;
  final AppLocale locale;
  final bool isDark;
  final VoidCallback onRefresh;
  final VoidCallback onEditUrl;

  const AccountTab({
    super.key,
    this.vpnService,
    required this.subscriptionInfo,
    required this.subscriptionUrl,
    required this.locale,
    required this.isDark,
    required this.onRefresh,
    required this.onEditUrl,
  });

  @override
  Widget build(BuildContext context) {
    final bgCard = isDark ? const Color(0xFF0F172A) : Colors.white;
    final textColor = isDark ? Colors.white : const Color(0xFF0F172A);
    final subTextColor = isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B);
    final borderColor = isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0);

    final info = subscriptionInfo;
    final username = vpnService?.appSettings.profileName ?? info?.username ?? "LinoVPN VIP Account";
    final total = info?.totalFormatted ?? "80 GB";
    final used = info?.usedFormatted ?? "22.4 GB";
    final remaining = info != null ? "${info.remainingGb.toStringAsFixed(1)} GB" : "57.6 GB";
    final days = info != null ? "${info.remainingDays} ${locale.daysRemaining}" : "26 ${locale.daysRemaining}";

    // ترافیک مصرفی نشست فعلی و مجموع مصرف شده کانفیگ با این اپلیکیشن
    final sessionDown = TrafficTracker.formatBytes(vpnService?.currentStatus.download ?? 0);
    final sessionUp = TrafficTracker.formatBytes(vpnService?.currentStatus.upload ?? 0);
    final totalDown = TrafficTracker.formatBytes(vpnService?.trafficTracker.accumulatedDownloadBytes ?? 0);
    final totalUp = TrafficTracker.formatBytes(vpnService?.trafficTracker.accumulatedUploadBytes ?? 0);

    return SingleChildScrollView(
      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // کارت اصلی نام اشتراک و پلن
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [Color(0xFF083344), Color(0xFF1E1B4B)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(22),
              border: Border.all(color: const Color(0xFF06B6D4).withOpacity(0.3)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      locale.planName,
                      style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 12, fontWeight: FontWeight.bold),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: const Color(0xFF10B981).withOpacity(0.2),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text(
                        days,
                        style: const TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  username,
                  style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 4),
                const Text(
                  "LinoVPN High-Speed VIP Network",
                  style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                ),
              ],
            ),
          ),
          const SizedBox(height: 18),

          // ترافیک مصرفی انباشته با کانفیگ (دانلود و آپلود)
          Text(
            locale.isEnglish ? "Config Traffic (App Accumulated)" : "ترافیک مصرفی با این کانفیگ",
            style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(child: _buildMetricCard(locale.isEnglish ? "Total Download" : "مجموع دانلود", totalDown, Icons.arrow_downward_rounded, const Color(0xFF10B981), bgCard, textColor, subTextColor, borderColor)),
              const SizedBox(width: 10),
              Expanded(child: _buildMetricCard(locale.isEnglish ? "Total Upload" : "مجموع آپلود", totalUp, Icons.arrow_upward_rounded, const Color(0xFF06B6D4), bgCard, textColor, subTextColor, borderColor)),
            ],
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(child: _buildMetricCard(locale.isEnglish ? "Session Download" : "دانلود این بار", sessionDown, Icons.downloading_rounded, const Color(0xFFA855F7), bgCard, textColor, subTextColor, borderColor)),
              const SizedBox(width: 10),
              Expanded(child: _buildMetricCard(locale.isEnglish ? "Session Upload" : "آپلود این بار", sessionUp, Icons.upload_file_rounded, const Color(0xFFF59E0B), bgCard, textColor, subTextColor, borderColor)),
            ],
          ),
          const SizedBox(height: 18),

          // حجم کل اشتراک (سهمیه پنل)
          Text(
            locale.isEnglish ? "Subscription Quota (Panel)" : "سهمیه کل اشتراک (پنل سرور)",
            style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(child: _buildMetricCard(locale.totalUsage, "$used / $total", Icons.data_usage_rounded, const Color(0xFF06B6D4), bgCard, textColor, subTextColor, borderColor)),
              const SizedBox(width: 10),
              Expanded(child: _buildMetricCard("Remaining", remaining, Icons.hourglass_top_rounded, const Color(0xFF10B981), bgCard, textColor, subTextColor, borderColor)),
            ],
          ),
          const SizedBox(height: 20),

          // بخش لینک اشتراک
          Text(
            locale.subUrlTitle,
            style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: bgCard,
              borderRadius: BorderRadius.circular(18),
              border: Border.all(color: borderColor),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  subscriptionUrl ?? "https://panel.linovpn.me/sub/...",
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 11, fontFamily: 'monospace'),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        style: OutlinedButton.styleFrom(
                          side: const BorderSide(color: Color(0xFF06B6D4)),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                        onPressed: () {
                          if (subscriptionUrl != null) {
                            Clipboard.setData(ClipboardData(text: subscriptionUrl!));
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text("Subscription URL copied!")),
                            );
                          }
                        },
                        icon: const Icon(Icons.copy_rounded, size: 16, color: Color(0xFF06B6D4)),
                        label: Text(locale.isEnglish ? "Copy Link" : "کپی لینک", style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 12)),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF06B6D4),
                          foregroundColor: Colors.black,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                        onPressed: onEditUrl,
                        icon: const Icon(Icons.edit_rounded, size: 16),
                        label: Text(locale.isEnglish ? "Edit Profile / Link" : "ویرایش نام و لینک", style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMetricCard(String label, String value, IconData icon, Color color, Color bg, Color textColor, Color subColor, Color border) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: border),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: color.withOpacity(0.12),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(icon, color: color, size: 20),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label, style: TextStyle(color: subColor, fontSize: 10)),
                const SizedBox(height: 2),
                Text(value, style: TextStyle(color: textColor, fontSize: 12, fontWeight: FontWeight.bold)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
