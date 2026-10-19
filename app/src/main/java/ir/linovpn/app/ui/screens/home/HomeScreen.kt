package ir.linovpn.app.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PowerSettingsNew
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ir.linovpn.app.R
import ir.linovpn.app.domain.model.VpnState
import ir.linovpn.app.ui.navigation.Screen
import ir.linovpn.app.ui.theme.IosBlue
import ir.linovpn.app.ui.theme.IosGreen
import ir.linovpn.app.ui.theme.IosYellow
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    onRequestVpnPermission: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showAddMenuDialog by remember { mutableStateOf(false) }
    var showImportSubDialog by remember { mutableStateOf(false) }
    var subscriptionUrlInput by remember { mutableStateOf("") }
    var showProfileImportedBanner by remember { mutableStateOf(false) }

    LaunchedEffect(showProfileImportedBanner) {
        if (showProfileImportedBanner) {
            delay(2000)
            showProfileImportedBanner = false
        }
    }

    val isConnected = state.vpnState is VpnState.Connected
    val isConnecting = state.vpnState is VpnState.Connecting

    var durationSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isConnected) {
        if (isConnected) {
            val connectedAt = (state.vpnState as? VpnState.Connected)?.connectedAt ?: System.currentTimeMillis()
            while (true) {
                durationSeconds = (System.currentTimeMillis() - connectedAt) / 1000
                delay(1000)
            }
        } else {
            durationSeconds = 0L
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. TOP BAR (Streisand Exact: Edit | Profile imported (temporary 2s) | +)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit",
                color = IosBlue,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Servers.route)
                }
            )

            if (showProfileImportedBanner) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(IosGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = "Profile imported",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        showAddMenuDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Profile",
                    tint = IosBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. HERO AREA (Connection Time | 00:00:00 | Squircle Power Button | Not Connected >)
        Text(
            text = "Connection Time",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatDuration(durationSeconds),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Iconic Streisand Squircle + Circular Power Button
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    if (!viewModel.isPermissionGranted()) {
                        onRequestVpnPermission()
                    } else {
                        viewModel.toggleConnection()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) IosGreen else IosBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Connect Toggle",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Pill: Not Connected > / Connected >
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    if (!viewModel.isPermissionGranted()) {
                        onRequestVpnPermission()
                    } else {
                        viewModel.toggleConnection()
                    }
                }
                .padding(horizontal = 18.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = when {
                    isConnected -> "Connected"
                    isConnecting -> "Connecting..."
                    else -> "Not Connected"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isConnected) IosGreen else MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. SORT BY ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "SORT BY:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = IosBlue,
                modifier = Modifier.clickable {
                    viewModel.pingSelectedServer()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. PROFILES & SERVERS INSET CARD
        val server = state.selectedServer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Card Header: LINOVPN PROFILES ∨
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.Servers.route) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LINOVPN PROFILES",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.2).sp
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = IosBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Hiddify Subscription Info Bar inside Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IosBlue.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⏳ ۱۹ روز باقی‌مانده",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "۲۱.۴ GB / ۵۰ GB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.42f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(IosBlue)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )

            // Server List (VPNPanels Exact Layout with Yellow Selection Dot)
            if (state.allServers.isNotEmpty()) {
                state.allServers.forEachIndexed { index, srv ->
                    val isSelected = srv.id == state.selectedServer?.id
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectServer(srv) }
                            .background(if (isSelected) IosYellow.copy(alpha = 0.08f) else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Yellow Dot Container: ONLY appears on the selected server!
                            Box(
                                modifier = Modifier.size(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(IosYellow)
                                    )
                                }
                            }

                            Text(text = "🚀", fontSize = 14.sp)

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = srv.name,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${srv.protocol} / TCP / NONE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (srv.latency > 0) {
                                Text(
                                    text = "${srv.latency}ms",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = IosGreen
                                )
                            }
                            Text(
                                text = "ⓘ",
                                color = IosBlue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Default server row when empty
                val isSelected = true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.Servers.route) }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.size(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(IosYellow)
                                )
                            }
                        }

                        Text(text = "🚀", fontSize = 14.sp)

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = server?.name ?: "🇩🇪 DE-Server1X (Linovpn VIP)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (server != null) "${server.protocol} / TCP / NONE" else "VLESS / TCP / NONE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "670ms",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = IosGreen
                        )
                        Text(
                            text = "ⓘ",
                            color = IosBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. TELEGRAM COMMUNITY & SUPPORT CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TelegramRow(
                emoji = "📢",
                title = "کانال تلگرام (@LinoVPN)",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/LinoVPN"))
                    context.startActivity(intent)
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            TelegramRow(
                emoji = "🛒",
                title = "ربات خرید (@Linovpnshop_bot)",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Linovpnshop_bot"))
                    context.startActivity(intent)
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            TelegramRow(
                emoji = "💬",
                title = "پشتیبانی آنلاین (@LinoVPNSup)",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/LinoVPNSup"))
                    context.startActivity(intent)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // 6. ADD MENU DIALOG (Streisand Exact with Import Subscription)
    if (showAddMenuDialog) {
        AlertDialog(
            onDismissRequest = { showAddMenuDialog = false },
            title = {
                Text(
                    text = "افزودن کانفیگ و اشتراک جدید",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. IMPORT SUBSCRIPTION (Manual Subscription Entry)
                    Button(
                        onClick = {
                            showAddMenuDialog = false
                            val clip = clipboardManager.getText()?.text ?: ""
                            if (clip.startsWith("http://") || clip.startsWith("https://")) {
                                subscriptionUrlInput = clip
                            }
                            showImportSubDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📥 افزودن دستی لینک اشتراک", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // 2. Import from clipboard
                    OutlinedButton(
                        onClick = {
                            showAddMenuDialog = false
                            val clip = clipboardManager.getText()?.text ?: ""
                            viewModel.importFromClipboard(clip) { success, msg ->
                                if (success) {
                                    showProfileImportedBanner = true
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📋 افزودن از کلیپ‌بورد", fontWeight = FontWeight.Medium)
                    }

                    // 3. Scan QR Code
                    OutlinedButton(
                        onClick = {
                            showAddMenuDialog = false
                            Toast.makeText(context, "اسکن بارکد QR", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📷 اسکن کد QR", fontWeight = FontWeight.Medium)
                    }

                    // 4. Manual Config
                    OutlinedButton(
                        onClick = {
                            showAddMenuDialog = false
                            navController.navigate(Screen.Servers.route)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("✍️ تنظیم دستی کانفیگ", fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddMenuDialog = false }) {
                    Text("انصراف", color = MaterialTheme.colorScheme.error)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 7. IMPORT SUBSCRIPTION INPUT DIALOG (Manual Entry)
    if (showImportSubDialog) {
        AlertDialog(
            onDismissRequest = { showImportSubDialog = false },
            title = {
                Text(
                    text = "افزودن دستی لینک اشتراک",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "لینک ساب‌اسکریپشن را در کادر زیر وارد یا جای‌گذاری کنید:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = subscriptionUrlInput,
                        onValueChange = { subscriptionUrlInput = it },
                        placeholder = { Text("https://example.com/sub/...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportSubDialog = false
                        viewModel.importSubscription(subscriptionUrlInput) { success, msg ->
                            if (success) {
                                showProfileImportedBanner = true
                                subscriptionUrlInput = ""
                            } else {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("بارگذاری", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportSubDialog = false }) {
                    Text("انصراف")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun TelegramRow(
    emoji: String,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hrs, mins, secs)
}
