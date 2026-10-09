import 'package:intl/intl.dart';

/// مدل نگهداری و تجزیه اطلاعات اشتراک و حجم مشتری
class SubscriptionInfo {
  final int uploadBytes;
  final int downloadBytes;
  final int totalBytes;
  final DateTime? expireDate;

  SubscriptionInfo({
    required this.uploadBytes,
    required this.downloadBytes,
    required this.totalBytes,
    this.expireDate,
  });

  /// حجم مصرف شده در قالب بایت
  int get usedBytes => uploadBytes + downloadBytes;

  /// حجم مصرف شده در قالب گیگابایت (با یک رقم اعشار)
  double get usedGb => (usedBytes / (1024 * 1024 * 1024));

  /// حجم کل در قالب گیگابایت
  double get totalGb => (totalBytes / (1024 * 1024 * 1024));

  /// حجم باقیمانده در قالب گیگابایت
  double get remainingGb {
    final rem = totalGb - usedGb;
    return rem > 0 ? rem : 0.0;
  }

  /// درصد مصرف شده (عددی بین 0.0 تا 1.0)
  double get usageRatio {
    if (totalBytes <= 0) return 0.0;
    final ratio = usedBytes / totalBytes;
    return ratio.clamp(0.0, 1.0);
  }

  /// تعداد روزهای باقیمانده تا انقضا
  int get remainingDays {
    if (expireDate == null) return -1;
    final difference = expireDate!.difference(DateTime.now()).inDays;
    return difference > 0 ? difference : 0;
  }

  /// آیا اشتراک منقضی شده است؟
  bool get isExpired {
    if (totalBytes > 0 && usedBytes >= totalBytes) return true;
    if (expireDate != null && expireDate!.isBefore(DateTime.now())) return true;
    return false;
  }

  /// فرمت متنی حجم برای نمایش در کارت وضعیت
  String get formattedUsage {
    if (totalBytes <= 0) return "${usedGb.toStringAsFixed(1)} GB مصرف شده";
    return "${usedGb.toStringAsFixed(1)} GB از ${totalGb.toStringAsFixed(0)} GB";
  }

  /// فرمت متنی روزهای باقیمانده
  String get formattedRemainingDays {
    if (expireDate == null) return "نامحدود";
    final days = remainingDays;
    if (days <= 0) return "منقضی شده";
    return "$days روز باقی‌مانده";
  }

  /// پارس کردن هدر استاندارد subscription-userinfo از پنل‌های مرزبان و ویتوری
  /// مثال: upload=1073741824; download=4294967296; total=53687091200; expire=1735689600
  factory SubscriptionInfo.fromHeader(String? headerValue) {
    if (headerValue == null || headerValue.isEmpty) {
      return SubscriptionInfo(
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
      uploadBytes: upload,
      downloadBytes: download,
      totalBytes: total,
      expireDate: expire,
    );
  }
}
