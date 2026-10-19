package ir.linovpn.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ir.linovpn.app.R
import ir.linovpn.app.domain.model.AppThemeMode
import ir.linovpn.app.domain.model.VpnCore
import ir.linovpn.app.ui.navigation.Screen
import ir.linovpn.app.ui.theme.LinoCyan
import ir.linovpn.app.ui.theme.LinoGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferredCore by viewModel.preferredCore.collectAsState()
    val dnsConfig by viewModel.dnsConfig.collectAsState()
    val isSplitTunneling by viewModel.isSplitTunneling.collectAsState()
    val bypassApps by viewModel.bypassApps.collectAsState()
    val isKillSwitch by viewModel.isKillSwitch.collectAsState()
    val isAutoConnect by viewModel.isAutoConnect.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Mode Section
            SettingsSectionHeader(title = "پوسته‌ی برنامه (Theme Mode)")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val themes = listOf(
                        AppThemeMode.SYSTEM to "پیروی از حالت سیستم (خودکار)",
                        AppThemeMode.DARK to "تم تیره (Dark Theme)",
                        AppThemeMode.LIGHT to "تم روشن (Light Theme)"
                    )
                    themes.forEachIndexed { index, (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = LinoCyan,
                                    unselectedColor = LinoGray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (themeMode == mode) MaterialTheme.colorScheme.onSurface else LinoGray,
                                    fontWeight = if (themeMode == mode) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                        if (index < themes.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }

            // Core selection section
            SettingsSectionHeader(title = "هسته پیش‌فرض (Engine Core)")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val cores = listOf(
                        VpnCore.XRAY to "Xray-core (پیشنهادی برای VLESS/Reality و دور زدن فیلترینگ)",
                        VpnCore.SINGBOX to "sing-box (سبک، سریع و مدرن)",
                        VpnCore.CLASH to "Clash Meta / Mihomo (مسیریابی هوشمند)",
                        VpnCore.V2RAY to "v2ray-core (پایدار)"
                    )
                    cores.forEachIndexed { index, (core, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setPreferredCore(core) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferredCore == core,
                                onClick = { viewModel.setPreferredCore(core) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = LinoCyan,
                                    unselectedColor = LinoGray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (preferredCore == core) MaterialTheme.colorScheme.onSurface else LinoGray,
                                    fontWeight = if (preferredCore == core) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                        if (index < cores.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }

            // Routing & DNS Section
            SettingsSectionHeader(title = "مسیریابی و شبکه (Routing & DNS)")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingSwitchItem(
                        icon = Icons.Default.Route,
                        title = "عبور سایت‌های ایرانی (Bypass Iran)",
                        subtitle = "سایت‌های داخلی و بانکی بدون VPN و با سرعت بالا باز شوند",
                        checked = dnsConfig.bypassIran,
                        onCheckedChange = {
                            viewModel.setDnsConfig(dnsConfig.copy(bypassIran = it))
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    SettingSwitchItem(
                        icon = Icons.Default.Dns,
                        title = "DNS امن (DoH - DNS over HTTPS)",
                        subtitle = "رمزگذاری درخواست‌های DNS برای مقابله با فیلترینگ DNS",
                        checked = dnsConfig.dnsOverHttps,
                        onCheckedChange = {
                            viewModel.setDnsConfig(dnsConfig.copy(dnsOverHttps = it))
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Split Tunneling Navigation Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { navController.navigate(Screen.SplitTunneling.route) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallSplit,
                            contentDescription = null,
                            tint = LinoCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مستثنی کردن برنامه‌ها (Split Tunneling)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = if (isSplitTunneling) {
                                    "${bypassApps.size} برنامه مستثنی شده (فعال)"
                                } else {
                                    "غیرفعال - لمس برای انتخاب برنامه‌ها"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSplitTunneling) LinoCyan else LinoGray,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Navigate",
                            tint = LinoGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Security Section
            SettingsSectionHeader(title = "امنیت و اتصال (Security & Auto-Connect)")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingSwitchItem(
                        icon = Icons.Default.Lock,
                        title = "کیل سوییچ (Kill Switch)",
                        subtitle = "قطع کامل اینترنت در صورت قطع ارتباط VPN",
                        checked = isKillSwitch,
                        onCheckedChange = { viewModel.setKillSwitch(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    SettingSwitchItem(
                        icon = Icons.Default.PowerSettingsNew,
                        title = "اتصال خودکار پس از روشن شدن دستگاه",
                        subtitle = "به محض بالا آمدن اندروید اتصال برقرار گردد",
                        checked = isAutoConnect,
                        onCheckedChange = { viewModel.setAutoConnect(it) }
                    )
                }
            }

            // About & Telegram Channel Section
            SettingsSectionHeader(title = "پشتیبانی و تلگرام")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://t.me/LinoVPN")
                                )
                                context.startActivity(intent)
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Telegram",
                            tint = LinoCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "کانال رسمی تلگرام: @LinoVPN",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "دریافت آخرین سرورها و اخبار به‌روزرسانی",
                                style = MaterialTheme.typography.bodySmall.copy(color = LinoGray)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open",
                            tint = LinoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نسخه برنامه",
                            style = MaterialTheme.typography.bodyMedium.copy(color = LinoGray)
                        )
                        Text(
                            text = "1.0.0 (Multi-Core Edition)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LinoCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            color = LinoCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        ),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LinoCyan,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LinoGray,
                    fontSize = 11.sp
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = LinoCyan,
                uncheckedThumbColor = LinoGray
            )
        )
    }
}
