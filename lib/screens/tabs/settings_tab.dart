import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../services/app_settings.dart';
import '../../services/traffic_tracker.dart';
import '../../utils/app_locale.dart';

class SettingsTab extends StatefulWidget {
  final AppSettings appSettings;
  final TrafficTracker? trafficTracker;
  final AppLocale locale;
  final bool isDark;
  final Function(bool isDark) onThemeChanged;
  final Function(bool isEnglish) onLanguageChanged;

  const SettingsTab({
    super.key,
    required this.appSettings,
    this.trafficTracker,
    required this.locale,
    required this.isDark,
    required this.onThemeChanged,
    required this.onLanguageChanged,
  });

  @override
  State<SettingsTab> createState() => _SettingsTabState();
}

class _SettingsTabState extends State<SettingsTab> {
  final TextEditingController _pingUrlController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _pingUrlController.text = widget.appSettings.pingTestUrl;
  }

  @override
  Widget build(BuildContext context) {
    final bgCard = widget.isDark ? const Color(0xFF0F172A) : Colors.white;
    final textColor = widget.isDark ? Colors.white : const Color(0xFF0F172A);
    final subTextColor = widget.isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B);
    final borderColor = widget.isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0);

    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 10),
      children: [
        // تنظیمات پینگ و آدرس تست پینگ طبق درخواست کاربر
        _buildSectionHeader(widget.locale.isEnglish ? "Ping & Latency Testing" : "تنظیمات تست پینگ"),
        _buildCard(bgCard, borderColor, [
          Padding(
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  widget.locale.isEnglish ? "Ping Test URL:" : "آدرس تست پینگ (Delay URL):",
                  style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 4),
                Text(
                  widget.locale.isEnglish
                      ? "Custom URL used for measuring server real delay"
                      : "آدرس استاندارد جهت اندازه‌گیری تاخیر واقعی سرورها",
                  style: TextStyle(color: subTextColor, fontSize: 11),
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _pingUrlController,
                        style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 12, fontFamily: 'monospace'),
                        decoration: InputDecoration(
                          hintText: "http://www.google.com/generate_204",
                          hintStyle: const TextStyle(color: Color(0xFF64748B), fontSize: 11),
                          filled: true,
                          fillColor: widget.isDark ? const Color(0xFF020617) : const Color(0xFFF1F5F9),
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: borderColor)),
                          contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFF06B6D4),
                        foregroundColor: Colors.black,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                      ),
                      onPressed: () async {
                        final newUrl = _pingUrlController.text.trim();
                        await widget.appSettings.savePingTestUrl(newUrl);
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text("Ping URL updated successfully!")),
                          );
                        }
                      },
                      child: Text(widget.locale.isEnglish ? "Save" : "ذخیره", style: const TextStyle(fontWeight: FontWeight.bold)),
                    ),
                  ],
                ),
              ],
            ),
          ),
          Divider(color: borderColor),
          SwitchListTile(
            title: Text(widget.locale.autoConnectTitle, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text(widget.locale.autoConnectDesc, style: TextStyle(color: subTextColor, fontSize: 11)),
            secondary: const Icon(Icons.bolt_rounded, color: Color(0xFF06B6D4)),
            activeColor: const Color(0xFF06B6D4),
            value: widget.appSettings.autoConnectBest,
            onChanged: (val) async {
              setState(() => widget.appSettings.autoConnectBest = val);
              await widget.appSettings.saveAutoConnect(val);
            },
          ),
        ]),
        const SizedBox(height: 18),

        // تنظیمات تم روز و شب و زبان
        _buildSectionHeader(widget.locale.appSettings),
        _buildCard(bgCard, borderColor, [
          SwitchListTile(
            title: Text(widget.locale.darkMode, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text(widget.isDark ? "Dark theme active" : "Light theme active", style: TextStyle(color: subTextColor, fontSize: 11)),
            secondary: Icon(widget.isDark ? Icons.dark_mode_rounded : Icons.light_mode_rounded, color: const Color(0xFF06B6D4)),
            activeColor: const Color(0xFF06B6D4),
            value: widget.isDark,
            onChanged: (val) async {
              await widget.appSettings.saveDarkMode(val);
              widget.onThemeChanged(val);
            },
          ),
          Divider(color: borderColor),
          SwitchListTile(
            title: Text(widget.locale.languageTitle, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text(widget.appSettings.isEnglish ? "English Language (Full English)" : "زبان فارسی", style: TextStyle(color: subTextColor, fontSize: 11)),
            secondary: const Icon(Icons.language_rounded, color: Color(0xFF06B6D4)),
            activeColor: const Color(0xFF06B6D4),
            value: widget.appSettings.isEnglish,
            onChanged: (val) async {
              await widget.appSettings.saveLanguage(val);
              widget.onLanguageChanged(val);
            },
          ),
        ]),
        const SizedBox(height: 18),

        // مسیریابی و DNS
        _buildSectionHeader(widget.locale.isEnglish ? "Network & Routing" : "تنظیمات شبکه و دور زدن فیلترینگ"),
        _buildCard(bgCard, borderColor, [
          SwitchListTile(
            title: Text(widget.locale.bypassIran, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text(widget.locale.bypassIranDesc, style: TextStyle(color: subTextColor, fontSize: 11)),
            secondary: const Icon(Icons.alt_route_rounded, color: Color(0xFF06B6D4)),
            activeColor: const Color(0xFF06B6D4),
            value: widget.appSettings.bypassIran,
            onChanged: (val) async {
              await widget.appSettings.saveBypassIran(val);
            },
          ),
          Divider(color: borderColor),
          ListTile(
            leading: const Icon(Icons.dns_rounded, color: Color(0xFF06B6D4)),
            title: Text(widget.locale.secureDns, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text(widget.appSettings.dnsServer, style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 11)),
            trailing: DropdownButton<String>(
              value: widget.appSettings.dnsServer,
              dropdownColor: bgCard,
              underline: const SizedBox(),
              icon: const Icon(Icons.arrow_drop_down, color: Color(0xFF06B6D4)),
              items: const [
                DropdownMenuItem(value: "1.1.1.1", child: Text("Cloudflare 1.1.1.1", style: TextStyle(fontSize: 12))),
                DropdownMenuItem(value: "8.8.8.8", child: Text("Google 8.8.8.8", style: TextStyle(fontSize: 12))),
                DropdownMenuItem(value: "9.9.9.9", child: Text("Quad9 9.9.9.9", style: TextStyle(fontSize: 12))),
              ],
              onChanged: (val) async {
                if (val != null) {
                  setState(() => widget.appSettings.dnsServer = val);
                  await widget.appSettings.saveDns(val);
                }
              },
            ),
          ),
        ]),
        const SizedBox(height: 18),

        // پشتیبانی تلگرام
        _buildSectionHeader(widget.locale.telegramSupport),
        _buildCard(bgCard, borderColor, [
          ListTile(
            leading: const Icon(Icons.send_rounded, color: Color(0xFF06B6D4)),
            title: Text(widget.locale.telegramSupport, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text("@${widget.appSettings.telegramSupport} (${widget.locale.copyId})", style: TextStyle(color: subTextColor, fontSize: 11)),
            onTap: () {
              Clipboard.setData(ClipboardData(text: "@${widget.appSettings.telegramSupport}"));
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text("Telegram ID copied: @${widget.appSettings.telegramSupport}")),
              );
            },
          ),
        ]),
      ],
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 8),
      child: Text(
        title,
        style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 12, fontWeight: FontWeight.bold),
      ),
    );
  }

  Widget _buildCard(Color bg, Color border, List<Widget> children) {
    return Container(
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: border),
      ),
      child: Column(children: children),
    );
  }
}
