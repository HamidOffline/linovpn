import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'screens/main_screen.dart';
import 'services/app_settings.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final settings = AppSettings();
  await settings.load();

  runApp(LinoVpnApp(initialDarkMode: settings.isDarkMode));
}

class LinoVpnApp extends StatefulWidget {
  final bool initialDarkMode;

  const LinoVpnApp({super.key, required this.initialDarkMode});

  @override
  State<LinoVpnApp> createState() => _LinoVpnAppState();
}

class _LinoVpnAppState extends State<LinoVpnApp> {
  late bool _isDark;

  @override
  void initState() {
    super.initState();
    _isDark = widget.initialDarkMode;
    _updateSystemUI();
  }

  void _updateSystemUI() {
    SystemChrome.setSystemUIOverlayStyle(
      SystemUiOverlayStyle(
        statusBarColor: Colors.transparent,
        statusBarIconBrightness: _isDark ? Brightness.light : Brightness.dark,
        systemNavigationBarColor: _isDark ? const Color(0xFF020617) : Colors.white,
        systemNavigationBarIconBrightness: _isDark ? Brightness.light : Brightness.dark,
      ),
    );
  }

  void toggleTheme(bool isDark) {
    setState(() {
      _isDark = isDark;
      _updateSystemUI();
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'LinoVPN Pro',
      debugShowCheckedModeBanner: false,
      themeMode: _isDark ? ThemeMode.dark : ThemeMode.light,
      theme: ThemeData(
        brightness: Brightness.light,
        scaffoldBackgroundColor: const Color(0xFFF8FAFC),
        primaryColor: const Color(0xFF0891B2),
        colorScheme: const ColorScheme.light(
          primary: Color(0xFF0891B2),
          secondary: Color(0xFF9333EA),
          surface: Colors.white,
        ),
        fontFamily: 'sans-serif',
      ),
      darkTheme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF020617),
        primaryColor: const Color(0xFF06B6D4),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF06B6D4),
          secondary: Color(0xFFA855F7),
          surface: Color(0xFF0F172A),
        ),
        fontFamily: 'sans-serif',
      ),
      home: MainScreen(onThemeToggle: toggleTheme),
    );
  }
}
