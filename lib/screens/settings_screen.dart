import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../services/app_settings.dart';
import '../services/subscription_service.dart';

class SettingsScreen extends StatefulWidget {
  final AppSettings appSettings;
  final String? currentSubUrl;
  final Function(String newUrl)? onSubUrlChanged;

  const SettingsScreen({
    super.key,
    required this.appSettings,
    this.currentSubUrl,
    this.onSubUrlChanged,
  });

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  late bool _bypassIran;
  late bool _autoConnect;
  late String _dns;
  final TextEditingController _subController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _bypassIran = widget.appSettings.bypassIran;
    _autoConnect = widget.appSettings.autoConnectBest;
    _dns = widget.appSettings.dnsServer;
    _subController.text = widget.currentSubUrl ?? "";
  }

  @override
  Widget build(BuildContext context) {
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        backgroundColor: const Color(0xFF020617),
        appBar: AppBar(
          backgroundColor: const Color(0xFF0F172A),
          title: const Text(
            "تنظیمات LinoVPN",
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
          ),
          leading: IconButton(
            icon: const Icon(Icons.arrow_back_ios_new_rounded, color: Color(0xFF06B6D4), size: 20),
            onPressed: () => Navigator.pop(context),
          ),
        ),
        body: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // بخش تنظیمات اتصال و فیلترینگ
            _buildSectionHeader("تنظیمات دور زدن فیلترینگ و شبکه"),
            _buildCard([
              SwitchListTile(
                title: const Text(
                  "دور زدن سایت‌های ایرانی (Bypass Iran)",
                  style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold),
                ),
                subtitle: const Text(
                  "سایت‌های بانکی و ایرانی بدون فیلترشکن باز شوند تا ترافیک مصرف نشود.",
                  style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                ),
                activeColor: const Color(0xFF06B6D4),
                value: _bypassIran,
                onChanged: (val) async {
                  setState(() => _bypassIran = val);
                  await widget.appSettings.saveBypassIran(val);
                },
              ),
              const Divider(color: Color(0xFF1E293B)),
              SwitchListTile(
                title: const Text(
                  "اتصال هوشمند خودکار",
                  style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold),
                ),
                subtitle: const Text(
                  "انتخاب و اتصال خودکار به سروری که کمترین پینگ را دارد.",
                  style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                ),
                activeColor: const Color(0xFF06B6D4),
                value: _autoConnect,
                onChanged: (val) async {
                  setState(() => _autoConnect = val);
                  await widget.appSettings.saveAutoConnect(val);
                },
              ),
              const Divider(color: Color(0xFF1E293B)),
              ListTile(
                title: const Text(
                  "سرور DNS امن",
                  style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold),
                ),
                subtitle: Text(
                  _dns == "1.1.1.1" ? "کلودفلر (Cloudflare)" : "گوگل (Google 8.8.8.8)",
                  style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 11),
                ),
                trailing: DropdownButton<String>(
                  value: _dns,
                  dropdownColor: const Color(0xFF0F172A),
                  underline: const SizedBox(),
                  icon: const Icon(Icons.arrow_drop_down, color: Color(0xFF06B6D4)),
                  items: const [
                    DropdownMenuItem(value: "1.1.1.1", child: Text("Cloudflare 1.1.1.1", style: TextStyle(color: Colors.white, fontSize: 12))),
                    DropdownMenuItem(value: "8.8.8.8", child: Text("Google 8.8.8.8", style: TextStyle(color: Colors.white, fontSize: 12))),
                    DropdownMenuItem(value: "9.9.9.9", child: Text("Quad9 9.9.9.9", style: TextStyle(color: Colors.white, fontSize: 12))),
                  ],
                  onChanged: (val) async {
                    if (val != null) {
                      setState(() => _dns = val);
                      await widget.appSettings.saveDns(val);
                    }
                  },
                ),
              ),
            ]),
            const SizedBox(height: 20),

            // بخش تنظیم لینک اشتراک
            _buildSectionHeader("لینک اشتراک LinoVPN"),
            _buildCard([
              Padding(
                padding: const EdgeInsets.all(14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      "لینک اشتراک خود را می‌توانید در کادر زیر ویرایش یا بروزرسانی کنید:",
                      style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12),
                    ),
                    const SizedBox(height: 10),
                    TextField(
                      controller: _subController,
                      style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 12, fontFamily: 'monospace'),
                      decoration: InputDecoration(
                        hintText: "https://.../sub/...",
                        hintStyle: const TextStyle(color: Color(0xFF475569)),
                        filled: true,
                        fillColor: const Color(0xFF020617),
                        border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: const BorderSide(color: Color(0xFF334155))),
                        focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: const BorderSide(color: Color(0xFF06B6D4))),
                      ),
                    ),
                    const SizedBox(height: 10),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF06B6D4),
                          foregroundColor: Colors.black,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                        onPressed: () async {
                          final newUrl = _subController.text.trim();
                          if (newUrl.isNotEmpty) {
                            await SubscriptionService().saveSubscriptionUrl(newUrl);
                            widget.onSubUrlChanged?.call(newUrl);
                            if (context.mounted) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text("لینک اشتراک با موفقیت ذخیره شد.")),
                              );
                            }
                          }
                        },
                        child: const Text("ذخیره لینک اشتراک", style: TextStyle(fontWeight: FontWeight.bold)),
                      ),
                    ),
                  ],
                ),
              ),
            ]),
            const SizedBox(height: 20),

            // پشتیبانی تلگرام LinoVPN
            _buildSectionHeader("پشتیبانی و کانال تلگرام"),
            _buildCard([
              ListTile(
                leading: const Icon(Icons.send_rounded, color: Color(0xFF06B6D4)),
                title: const Text("پشتیبانی تلگرام LinoVPN", style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold)),
                subtitle: const Text("@LinoVPN_Support (لمس برای کپی آیدی)", style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11)),
                onTap: () {
                  Clipboard.setData(const ClipboardData(text: "@LinoVPN_Support"));
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text("آیدی تلگرام کپی شد: @LinoVPN_Support")),
                  );
                },
              ),
            ]),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(right: 6, bottom: 8),
      child: Text(
        title,
        style: const TextStyle(color: Color(0xFF06B6D4), fontSize: 12, fontWeight: FontWeight.bold),
      ),
    );
  }

  Widget _buildCard(List<Widget> children) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF0F172A),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFF1E293B)),
      ),
      child: Column(children: children),
    );
  }
}
