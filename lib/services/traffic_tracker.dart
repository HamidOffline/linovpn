import 'package:shared_preferences/shared_preferences.dart';

/// ردیاب و ذخیره‌ساز ترافیک دانلود و آپلود مصرف‌شده با کانفیگ‌ها
class TrafficTracker {
  static const String _keyTotalDownload = 'traffic_accumulated_download';
  static const String _keyTotalUpload = 'traffic_accumulated_upload';

  int accumulatedDownloadBytes = 0;
  int accumulatedUploadBytes = 0;

  int lastReportedDownload = 0;
  int lastReportedUpload = 0;

  Future<void> load() async {
    final prefs = await SharedPreferences.getInstance();
    accumulatedDownloadBytes = prefs.getInt(_keyTotalDownload) ?? 0;
    accumulatedUploadBytes = prefs.getInt(_keyTotalUpload) ?? 0;
  }

  /// بروزرسانی مصرف لحظه‌ای نشست جاری و جمع بستن آن در مجموع
  void updateSession(int currentDownload, int currentUpload) {
    if (currentDownload > lastReportedDownload) {
      final delta = currentDownload - lastReportedDownload;
      accumulatedDownloadBytes += delta;
      lastReportedDownload = currentDownload;
    }
    if (currentUpload > lastReportedUpload) {
      final delta = currentUpload - lastReportedUpload;
      accumulatedUploadBytes += delta;
      lastReportedUpload = currentUpload;
    }
    _persist();
  }

  /// ریست کردن شمارشگر نشست در زمان شروع اتصال جدید
  void resetSession() {
    lastReportedDownload = 0;
    lastReportedUpload = 0;
  }

  Future<void> _persist() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_keyTotalDownload, accumulatedDownloadBytes);
    await prefs.setInt(_keyTotalUpload, accumulatedUploadBytes);
  }

  /// ریست کردن کل مصرف انباشته
  Future<void> resetAll() async {
    accumulatedDownloadBytes = 0;
    accumulatedUploadBytes = 0;
    lastReportedDownload = 0;
    lastReportedUpload = 0;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_keyTotalDownload);
    await prefs.remove(_keyTotalUpload);
  }

  /// فرمت متنی خوانا برای حجم
  static String formatBytes(int bytes) {
    if (bytes <= 0) return "0 MB";
    final kb = bytes / 1024;
    final mb = kb / 1024;
    final gb = mb / 1024;

    if (gb >= 1.0) {
      return "${gb.toStringAsFixed(2)} GB";
    } else if (mb >= 1.0) {
      return "${mb.toStringAsFixed(1)} MB";
    } else {
      return "${kb.toStringAsFixed(0)} KB";
    }
  }
}
