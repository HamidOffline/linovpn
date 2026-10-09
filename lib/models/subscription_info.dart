/// مدل نگهداری و تجزیه اطلاعات اشتراک، حجم دانلود، آپلود و نام کاربری تمیز
class SubscriptionInfo {
  final String username;
  final int uploadBytes;
  final int downloadBytes;
  final int totalBytes;
  final DateTime? expireDate;

  SubscriptionInfo({
    this.username = "LinoVPN VIP Account",
    required this.uploadBytes,
    required this.downloadBytes,
    required this.totalBytes,
    this.expireDate,
  });

  int get usedBytes => uploadBytes + downloadBytes;

  double get uploadGb => uploadBytes / (1024 * 1024 * 1024);
  double get downloadGb => downloadBytes / (1024 * 1024 * 1024);
  double get usedGb => usedBytes / (1024 * 1024 * 1024);
  double get totalGb => totalBytes / (1024 * 1024 * 1024);

  double get remainingGb {
    final rem = totalGb - usedGb;
    return rem > 0 ? rem : 0.0;
  }

  String get uploadFormatted => "${uploadGb.toStringAsFixed(2)} GB";
  String get downloadFormatted => "${downloadGb.toStringAsFixed(2)} GB";
  String get usedFormatted => "${usedGb.toStringAsFixed(1)} GB";
  String get totalFormatted => "${totalGb.toStringAsFixed(0)} GB";
  String get remainingFormatted => "${remainingGb.toStringAsFixed(1)} GB";

  double get usageRatio {
    if (totalBytes <= 0) return 0.0;
    return (usedBytes / totalBytes).clamp(0.0, 1.0);
  }

  int get remainingDays {
    if (expireDate == null) return -1;
    final difference = expireDate!.difference(DateTime.now()).inDays;
    return difference > 0 ? difference : 0;
  }

  bool get isExpired {
    if (totalBytes > 0 && usedBytes >= totalBytes) return true;
    if (expireDate != null && expireDate!.isBefore(DateTime.now())) return true;
    return false;
  }

  factory SubscriptionInfo.fromHeader(String? headerValue, [String profileName = "LinoVPN VIP Account"]) {
    return SubscriptionInfo.fromHeaderAndName(headerValue, profileName);
  }

  factory SubscriptionInfo.fromHeaderAndUrl(String? headerValue, String subUrl, [String profileName = "LinoVPN VIP Account"]) {
    return SubscriptionInfo.fromHeaderAndName(headerValue, profileName);
  }

  factory SubscriptionInfo.fromHeaderAndName(String? headerValue, String profileName) {
    final cleanName = profileName.trim().isEmpty ? "LinoVPN VIP Account" : profileName.trim();

    if (headerValue == null || headerValue.isEmpty) {
      return SubscriptionInfo(
        username: cleanName,
        uploadBytes: 0,
        downloadBytes: 0,
        totalBytes: 0,
      );
    }

    int upload = 0;
    int download = 0;
    int total = 0;
    DateTime? expire;

    final parts = headerValue.split(';');
    for (var part in parts) {
      final pair = part.trim().split('=');
      if (pair.length == 2) {
        final key = pair[0].trim().toLowerCase();
        final value = int.tryParse(pair[1].trim()) ?? 0;

        switch (key) {
          case 'upload':
            upload = value;
            break;
          case 'download':
            download = value;
            break;
          case 'total':
            total = value;
            break;
          case 'expire':
            if (value > 0) {
              expire = DateTime.fromMillisecondsSinceEpoch(value * 1000);
            }
            break;
        }
      }
    }

    return SubscriptionInfo(
      username: cleanName,
      uploadBytes: upload,
      downloadBytes: download,
      totalBytes: total,
      expireDate: expire,
    );
  }
}
