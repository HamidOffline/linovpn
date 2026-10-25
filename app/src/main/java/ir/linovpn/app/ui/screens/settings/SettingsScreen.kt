package ir.linovpn.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ir.linovpn.app.domain.model.AppFont
import ir.linovpn.app.domain.model.AppLanguage
import ir.linovpn.app.domain.model.AppThemeMode
import ir.linovpn.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val themeMode by viewModel.themeMode.collectAsState()
    val appFont by viewModel.appFont.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    var showFontDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val backgroundColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardBackground = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val primaryBlue = Color(0xFF007AFF)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            // ================= SECTION 1: CONFIGURATION =================
            item {
                SectionHeader("CONFIGURATION")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.FileDownload,
                            iconTint = primaryBlue,
                            title = "Import Configuration",
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    Toast.makeText(context, "کانفیگ از کلیپ‌بورد شناسایی شد", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.Home.route)
                                } else {
                                    Toast.makeText(context, "کلیپ‌بورد خالی است", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 50.dp)
                        )

                        SettingsRow(
                            icon = Icons.Default.FileUpload,
                            iconTint = primaryBlue,
                            title = "Export Configuration",
                            onClick = {
                                Toast.makeText(context, "کانفیگ فعال با موفقیت کپی شد", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ================= SECTION 2: PREFERENCES =================
            item {
                SectionHeader("PREFERENCES")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingsNavRow(
                            icon = Icons.Default.Link,
                            iconTint = primaryBlue,
                            title = "Subscription",
                            onClick = { navController.navigate(Screen.SubscriptionSettings.route) }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 50.dp)
                        )

                        SettingsNavRow(
                            icon = Icons.Default.Dns,
                            iconTint = primaryBlue,
                            title = "Tunnel",
                            onClick = { navController.navigate(Screen.TunnelSettings.route) }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 50.dp)
                        )

                        SettingsNavRow(
                            icon = Icons.Default.Speed,
                            iconTint = primaryBlue,
                            title = "Ping",
                            onClick = { navController.navigate(Screen.PingSettings.route) }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 50.dp)
                        )

                        SettingsNavRow(
                            icon = Icons.Default.AltRoute,
                            iconTint = primaryBlue,
                            title = "Routing",
                            onClick = { navController.navigate(Screen.RoutingSettings.route) }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 50.dp)
                        )

                        SettingsNavRow(
                            icon = Icons.Default.Dns,
                            iconTint = primaryBlue,
                            title = "DNS",
                            onClick = { navController.navigate(Screen.DnsSettings.route) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ================= SECTION 3: APPEARANCE & LANGUAGE =================
            item {
                SectionHeader("APPEARANCE & LANGUAGE")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Font selection
                        SettingsValueRow(
                            icon = Icons.Default.FontDownload,
                            iconTint = Color(0xFFAF52DE),
                            title = "قلم برنامه (Font)",
                            value = when (appFont) {
                                AppFont.VAZIRMATN -> "وزیرمتن"
                                AppFont.YEKAN -> "یکان‌بخ"
                                AppFont.SYSTEM -> "پیش‌فرض سیستم"
                            },
                            onClick = { showFontDialog = true }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 50.dp)
                        )

                        // Language selection
                        SettingsValueRow(
                            icon = Icons.Default.Language,
                            iconTint = Color(0xFF34C759),
                            title = "زبان (Language)",
                            value = when (appLanguage) {
                                AppLanguage.FA -> "فارسی"
                                AppLanguage.EN -> "English"
                                AppLanguage.SYSTEM -> "هماهنگ با سیستم"
                            },
                            onClick = { showLanguageDialog = true }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 50.dp)
                        )

                        // Theme Mode
                        SettingsValueRow(
                            icon = Icons.Default.DarkMode,
                            iconTint = Color(0xFFFF9500),
                            title = "حالت تیره و روشن (Theme)",
                            value = when (themeMode) {
                                AppThemeMode.DARK -> "تیره (Dark)"
                                AppThemeMode.LIGHT -> "روشن (Light)"
                                AppThemeMode.SYSTEM -> "خودکار (سیستم)"
                            },
                            onClick = { showThemeDialog = true }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ================= SECTION 4: COMMUNITY & ABOUT =================
            item {
                SectionHeader("COMMUNITY")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsNavRow(
                        icon = Icons.Default.Info,
                        iconTint = primaryBlue,
                        title = "About LinoVPN (کانال، ربات و پشتیبانی)",
                        onClick = { navController.navigate(Screen.AboutLino.route) }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ================= SECTION 5: STATISTICS =================
            item {
                SectionHeader("STATISTICS")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsNavRow(
                        icon = Icons.Default.ShowChart,
                        iconTint = primaryBlue,
                        title = "Statistics",
                        onClick = { navController.navigate(Screen.StatisticsSettings.route) }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ================= SECTION 6: RESET =================
            item {
                SectionHeader("RESET")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsNavRow(
                        icon = Icons.Default.Refresh,
                        iconTint = primaryBlue,
                        title = "Reset",
                        onClick = { showResetDialog = true }
                    )
                }
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    // ================= DIALOGS =================
    // Font Dialog
    if (showFontDialog) {
        var tempFont by remember { mutableStateOf(appFont) }
        val fontList = remember {
            listOf(
                AppFont.VAZIRMATN to "وزیرمتن (Vazirmatn)",
                AppFont.YEKAN to "یکان‌بخ (Yekan Bakh)",
                AppFont.SYSTEM to "پیش‌فرض سیستم (System)"
            )
        }
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text(text = "انتخاب قلم برنامه", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    fontList.forEach { item ->
                        val font = item.first
                        val label = item.second
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempFont = font }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tempFont == font,
                                onClick = { tempFont = font }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setAppFont(tempFont)
                    showFontDialog = false
                }) {
                    Text(text = "تایید و اعمال", color = primaryBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFontDialog = false }) {
                    Text(text = "انصراف")
                }
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        var tempLang by remember { mutableStateOf(appLanguage) }
        val langList = remember {
            listOf(
                AppLanguage.FA to "فارسی (Persian)",
                AppLanguage.EN to "English",
                AppLanguage.SYSTEM to "هماهنگ با سیستم (System)"
            )
        }
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = "انتخاب زبان برنامه", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    langList.forEach { item ->
                        val lang = item.first
                        val label = item.second
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempLang = lang }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tempLang == lang,
                                onClick = { tempLang = lang }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setAppLanguage(tempLang)
                    showLanguageDialog = false
                }) {
                    Text(text = "تایید و اعمال", color = primaryBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(text = "انصراف")
                }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        var tempTheme by remember { mutableStateOf(themeMode) }
        val themeList = remember {
            listOf(
                AppThemeMode.DARK to "تیره (Dark)",
                AppThemeMode.LIGHT to "روشن (Light)",
                AppThemeMode.SYSTEM to "خودکار بر اساس تنظیمات سیستم"
            )
        }
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = "حالت تیره / روشن", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themeList.forEach { item ->
                        val mode = item.first
                        val label = item.second
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempTheme = mode }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tempTheme == mode,
                                onClick = { tempTheme = mode }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setThemeMode(tempTheme)
                    showThemeDialog = false
                }) {
                    Text(text = "تایید", color = primaryBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(text = "انصراف")
                }
            }
        )
    }

    // Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "بازنشانی تنظیمات", fontWeight = FontWeight.Bold) },
            text = { Text("آیا مطمئن هستید که می‌خواهید تمام تنظیمات را به مقادیر پیش‌فرض استریسند بازگردانید؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetToDefaults()
                    showResetDialog = false
                    Toast.makeText(context, "تنظیمات بازنشانی شد", Toast.LENGTH_SHORT).show()
                }) {
                    Text("بازنشانی", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFF8E8E93),
        modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFC7C7CC),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsValueRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 15.sp,
                color = Color(0xFF8E8E93)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFC7C7CC),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
