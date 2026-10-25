package ir.linovpn.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ir.linovpn.app.ui.theme.LinoPrimaryCyan

data class DnsEntry(
    val name: String,
    val type: String, // DoH, DoT, DoU, Custom
    val address: String,
    val isIpv6: Boolean = false
)

val DEFAULT_DNS_ENTRIES = listOf(
    DnsEntry("Google DoH", "DoH", "8.8.8.8"),
    DnsEntry("Google IPv6 DoH", "DoH", "2001:4860:4860::8888", isIpv6 = true),
    DnsEntry("Google DoT", "DoT", "8.8.8.8"),
    DnsEntry("Google IPv6 DoT", "DoT", "2001:4860:4860::8888", isIpv6 = true),
    DnsEntry("Google DoU", "DoU", "8.8.8.8"),
    DnsEntry("Google IPv6 DoU", "DoU", "2001:4860:4860::8888", isIpv6 = true),
    DnsEntry("Cloudflare DoH", "DoH", "1.1.1.1"),
    DnsEntry("Cloudflare IPv6 DoH", "DoH", "2606:4700:4700::1111", isIpv6 = true),
    DnsEntry("Cloudflare DoT", "DoT", "1.1.1.1"),
    DnsEntry("Cloudflare IPv6 DoT", "DoT", "2606:4700:4700::1111", isIpv6 = true),
    DnsEntry("Cloudflare DoU", "DoU", "1.1.1.1"),
    DnsEntry("Cloudflare IPv6 DoU", "DoU", "2606:4700:4700::1111", isIpv6 = true),
    DnsEntry("Quad9 DoH", "DoH", "9.9.9.9"),
    DnsEntry("AdGuard DoH", "DoH", "94.140.14.14"),
    DnsEntry("Shecan (Anti-Sanction)", "DoU", "178.22.122.100"),
    DnsEntry("Electro (Anti-Sanction)", "DoU", "78.157.42.101"),
    DnsEntry("403.online (Anti-Sanction)", "DoU", "10.202.10.202")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val dnsConfig by viewModel.dnsConfig.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

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
                        text = "DNS Settings",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = primaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Custom DNS",
                            tint = primaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        DEFAULT_DNS_ENTRIES.forEachIndexed { index, entry ->
                            val displayText = "${entry.type}: ${entry.address}"
                            val isSelected = dnsConfig.remoteDnsName == entry.name ||
                                (dnsConfig.remoteDnsName.contains("Cloudflare") && dnsConfig.remoteDnsName.contains("DoH") && entry.name == "Cloudflare DoH")

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val isDoh = entry.type == "DoH"
                                        val dohUrl = if (isDoh) "https://${entry.address}/dns-query" else ""
                                        viewModel.setRemoteDns(
                                            name = entry.name,
                                            primary = entry.address,
                                            secondary = "8.8.8.8",
                                            dohUrl = dohUrl,
                                            isDoh = isDoh
                                        )
                                    }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Radio Selection Circle
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) primaryBlue else Color.Transparent)
                                        .then(
                                            if (!isSelected) Modifier.background(
                                                color = Color.Transparent,
                                                shape = CircleShape
                                            ) else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(dividerColor)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(cardBackground)
                                                    .align(Alignment.Center)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Monospace DNS display
                                Text(
                                    text = displayText,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace,
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

                            if (index < DEFAULT_DNS_ENTRIES.size - 1) {
                                HorizontalDivider(
                                    color = dividerColor,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 52.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
