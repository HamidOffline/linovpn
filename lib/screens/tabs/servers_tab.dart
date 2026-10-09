import 'package:flutter/material.dart';
import '../../models/server_model.dart';
import '../../services/vpn_service.dart';
import '../../utils/app_locale.dart';

class ServersTab extends StatefulWidget {
  final VpnService vpnService;
  final AppLocale locale;
  final bool isDark;
  final VoidCallback onPingAll;

  const ServersTab({
    super.key,
    required this.vpnService,
    required this.locale,
    required this.isDark,
    required this.onPingAll,
  });

  @override
  State<ServersTab> createState() => _ServersTabState();
}

class _ServersTabState extends State<ServersTab> {
  String _searchQuery = "";

  @override
  Widget build(BuildContext context) {
    final bgCard = widget.isDark ? const Color(0xFF0F172A) : Colors.white;
    final textColor = widget.isDark ? Colors.white : const Color(0xFF0F172A);
    final subTextColor = widget.isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B);
    final borderColor = widget.isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0);

    final filteredServers = widget.vpnService.serverList.where((s) {
      final q = _searchQuery.toLowerCase();
      return s.name.toLowerCase().contains(q) ||
          s.protocol.toLowerCase().contains(q) ||
          s.host.toLowerCase().contains(q);
    }).toList();

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 10),
      child: Column(
        children: [
          // نوار جستجو و دکمه تست پینگ همه سرورها
          Row(
            children: [
              Expanded(
                child: TextField(
                  onChanged: (val) => setState(() => _searchQuery = val),
                  style: TextStyle(color: textColor, fontSize: 13),
                  decoration: InputDecoration(
                    hintText: widget.locale.searchServer,
                    hintStyle: TextStyle(color: subTextColor, fontSize: 12),
                    prefixIcon: Icon(Icons.search_rounded, color: subTextColor, size: 20),
                    filled: true,
                    fillColor: bgCard,
                    contentPadding: const EdgeInsets.symmetric(vertical: 10),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: BorderSide(color: borderColor)),
                    enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: BorderSide(color: borderColor)),
                  ),
                ),
              ),
              const SizedBox(width: 10),
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF06B6D4),
                  foregroundColor: Colors.black,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                ),
                onPressed: widget.vpnService.isPingTesting ? null : widget.onPingAll,
                icon: widget.vpnService.isPingTesting
                    ? const SizedBox(width: 14, height: 14, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.black))
                    : const Icon(Icons.bolt_rounded, size: 18),
                label: Text(
                  widget.vpnService.isPingTesting ? widget.locale.pingTesting : widget.locale.pingAll,
                  style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),

          // لیست سرورها
          Expanded(
            child: filteredServers.isEmpty
                ? Center(
                    child: Text(
                      widget.locale.noServers,
                      style: TextStyle(color: subTextColor, fontSize: 13),
                    ),
                  )
                : ListView.separated(
                    itemCount: filteredServers.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 8),
                    itemBuilder: (context, index) {
                      final item = filteredServers[index];
                      final isSelected = widget.vpnService.activeServer?.id == item.id;
                      final isConnected = widget.vpnService.currentStatus.state == 'CONNECTED' && isSelected;

                      Color pingColor = const Color(0xFF64748B);
                      if (item.ping > 0) {
                        if (item.ping < 140) {
                          pingColor = const Color(0xFF10B981); // سبز
                        } else if (item.ping < 260) {
                          pingColor = const Color(0xFFF59E0B); // نارنجی
                        } else {
                          pingColor = const Color(0xFFEF4444); // قرمز
                        }
                      }

                      return InkWell(
                        onTap: () async {
                          await widget.vpnService.connectToServer(item);
                          setState(() {});
                        },
                        borderRadius: BorderRadius.circular(16),
                        child: Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: isSelected ? const Color(0xFF06B6D4).withOpacity(0.12) : bgCard,
                            borderRadius: BorderRadius.circular(16),
                            border: Border.all(
                              color: isSelected ? const Color(0xFF06B6D4) : borderColor,
                              width: isSelected ? 1.5 : 1.0,
                            ),
                          ),
                          child: Row(
                            children: [
                              Text(item.flag, style: const TextStyle(fontSize: 26)),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: [
                                        Text(
                                          item.name,
                                          style: TextStyle(color: textColor, fontSize: 13, fontWeight: FontWeight.bold),
                                        ),
                                        if (isConnected) ...[
                                          const SizedBox(width: 6),
                                          Container(
                                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                            decoration: BoxDecoration(
                                              color: const Color(0xFF10B981).withOpacity(0.2),
                                              borderRadius: BorderRadius.circular(6),
                                            ),
                                            child: Text(
                                              widget.locale.connected,
                                              style: const TextStyle(color: Color(0xFF10B981), fontSize: 9, fontWeight: FontWeight.bold),
                                            ),
                                          ),
                                        ],
                                      ],
                                    ),
                                    const SizedBox(height: 2),
                                    Text(
                                      "${item.protocol} • ${item.host}",
                                      style: TextStyle(color: subTextColor, fontSize: 10, fontFamily: 'monospace'),
                                    ),
                                  ],
                                ),
                              ),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                                decoration: BoxDecoration(
                                  color: pingColor.withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(10),
                                  border: Border.all(color: pingColor.withOpacity(0.3)),
                                ),
                                child: Text(
                                  item.ping > 0 ? "${item.ping}ms" : "--",
                                  style: TextStyle(color: pingColor, fontSize: 12, fontWeight: FontWeight.bold, fontFamily: 'monospace'),
                                ),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
