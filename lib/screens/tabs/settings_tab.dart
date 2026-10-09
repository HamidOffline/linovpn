import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../services/app_settings.dart';
import '../../utils/app_locale.dart';

class SettingsTab extends StatelessWidget {
  final AppSettings appSettings;
  final AppLocale locale;
  final bool isDark;
  final Function(bool isDark) onThemeChanged;
  final Function(bool isEnglish) onLanguageChanged;

  const SettingsTab({
    super.key,
    required this.appSettings,
    required this.locale,
    required this.isDark,
    required this.onThemeChanged,
    required this.onLanguageChanged,
  });

  @override
  Widget build(BuildContext context) {
    final bgCard = isDark ? const Color(0xFF0F172A) : Colors.white;
    final textColor = isDark ? Colors.white : const Color(0xFF0F172A);
    final subTextColor = isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B);
    final borderColor = isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0);

    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 10),
      children: [
        // تنظیمات ظاهری (تم شب و روز + زبان)
        _buildSectionHeader(locale.appSettings),
        _buildCard(bgCard, borderColor, [
          SwitchListTile(
            title: Text(locale.darkMode, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text(isDark ? "Dark theme active" : "Light theme active", style: TextStyle(color: subTextColor, fontSize: 11)),
            secondary: Icon(isDark ? Icons.dark_mode_rounded : Icons.light_mode_rounded, color: const Color(0xFF06B6D4)),
            activeColor: const Color(0xFF06B6D4),
            value: isDark,
            onChanged: (val) async {
              await appSettings.saveDarkMode(val);
              onThemeChanged(val);
            },
          ),
          Divider(color: borderColor),
          SwitchListTile(
            title: Text(locale.languageTitle, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text(appSettings.isEnglish ? "English Language (Full English)" : "زبان فارسی", style: TextStyle(color: subTextColor, fontSize: 11)),
            secondary: const Icon(Icons.language_rounded, color: Color(0xFF06B6D4)),
            activeColor: const Color(0xFF06B6D4),
            value: appSettings.isEnglish,
            onChanged: (val) async {
              await appSettings.saveLanguage(val);
              onLanguageChanged(val);
            },
          ),
        ]),
        const SizedBox(height: 18),

        // دور زدن فیلترینگ و اتصال هوشمند
        _buildSectionHeader(locale.isEnglish ? "Network & Routing" : "تنظیمات شبکه و دور زدن فیلترینگ"),
        _buildCard(bgCard, borderColor, [
          SwitchListTile(
            title: Text(locale.bypassIran, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text(locale.bypassIranDesc, style: TextStyle(color: subTextColor, fontSize: 11)),
            secondary: const Icon(Icons.alt_route_rounded, color: Color(0xFF06B6D4)),
            activeColor: const Color(0xFF06B6D4),
            value: appSettings.bypassIran,
            onChanged: (val) async {
              await appSettings.saveBypassIran(val);
            },
          ),
          Divider(color: borderColor),
          SwitchListTile(
            title: Text(locale.autoConnectTitle, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text(locale.autoConnectDesc, style: TextStyle(color: subTextColor, fontSize: 11)),
            secondary: const Icon(Icons.bolt_rounded, color: Color(0xFF06B6D4)),
            activeColor: const Color(0xFF06B6D4),
            value: appSettings.autoConnectBest,
            onChanged: (val) async {
              await appSettings.saveAutoConnect(val);
            },
          ),
          Divider(color: borderColor),
          ListTile(
            leading: const Icon(Icons.dns_rounded, color: Color(0xFF06B6D4)),
            title: Text(locale.secureDns, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text(appSettings.dnsServer, style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 11)),
            trailing: DropdownButton<String>(
              value: appSettings.dnsServer,
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
                  await appSettings.saveDns(val);
                }
              },
            ),
          ),
        ]),
        const SizedBox(height: 18),

        // پشتیبانی تلگرام
        _buildSectionHeader(locale.telegramSupport),
        _buildCard(bgCard, borderColor, [
          ListTile(
            leading: const Icon(Icons.send_rounded, color: Color(0xFF06B6D4)),
            title: Text(locale.telegramSupport, style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold)),
            subtitle: Text("@${appSettings.telegramSupport} (${locale.copyId})", style: TextStyle(color: subTextColor, fontSize: 11)),
            onTap: () {
              Clipboard.setData(ClipboardData(text: "@${appSettings.telegramSupport}"));
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text("Telegram ID copied: @${appSettings.telegramSupport}")),
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
