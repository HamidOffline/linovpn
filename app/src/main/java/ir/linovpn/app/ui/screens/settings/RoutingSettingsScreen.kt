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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ir.linovpn.app.domain.model.RoutingMode

data class RouteItem(
    val id: String,
    val name: String,
    val description: String,
    val mode: RoutingMode
)

val DEFAULT_ROUTES = listOf(
    RouteItem("route_1", "Route 1", "Bypass LAN & Local Private Addresses", RoutingMode.BYPASS_LAN),
    RouteItem("route_2", "Route 2", "Bypass Iranian Sites & Domains (.ir)", RoutingMode.BYPASS_IRAN),
    RouteItem("route_3", "Route 3", "Bypass LAN + Iran + Block Advertisements", RoutingMode.BYPASS_LAN_AND_IRAN),
    RouteItem("route_4", "Route 4", "Global Proxy (All Traffic through VPN)", RoutingMode.GLOBAL)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutingSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val routingMode by viewModel.routingMode.collectAsState()
    val isEnabled by viewModel.isRoutingEnabled.collectAsState()

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val backgroundColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardBackground = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val primaryBlue = Color(0xFF007AFF)
    val iosGreen = Color(0xFF34C759)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Routing",
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
                    IconButton(onClick = { /* Add custom route */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Route",
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
            // Card 1: Enable Switch
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Enable",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { viewModel.setRoutingEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = iosGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)
                            )
                        )
                    }
                }

                // Streisand note below switch card
                Text(
                    text = "In order for the Routing settings to take effect, it is necessary to restart the tunnel.",
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
                    lineHeight = 18.sp
                )
            }

            // Card 2: Routes List
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        DEFAULT_ROUTES.forEachIndexed { index, route ->
                            val isSelected = routingMode == route.mode

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = isEnabled) {
                                        viewModel.setRoutingMode(route.mode)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Selection Circle
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected && isEnabled) primaryBlue else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected && isEnabled) {
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

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = route.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface else Color(0xFF8E8E93)
                                    )
                                    Text(
                                        text = route.description,
                                        fontSize = 12.sp,
                                        color = Color(0xFF8E8E93)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFFC7C7CC),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (index < DEFAULT_ROUTES.size - 1) {
                                HorizontalDivider(
                                    color = dividerColor,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 52.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
