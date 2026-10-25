package ir.linovpn.app.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.mutableStateMapOf
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
import ir.linovpn.app.domain.model.Profile
import ir.linovpn.app.domain.model.Server
import ir.linovpn.app.domain.model.ServerProtocol
import ir.linovpn.app.domain.model.VpnState
import ir.linovpn.app.ui.navigation.Screen
import ir.linovpn.app.ui.theme.IosBlue
import ir.linovpn.app.ui.theme.IosGreen
import ir.linovpn.app.ui.theme.IosYellow
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    onRequestVpnPermission: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val strings = ir.linovpn.app.ui.theme.LocalStrings.current

    var showAddMenuDialog by remember { mutableStateOf(false) }
    var showImportSubDialog by remember { mutableStateOf(false) }
    var subscriptionUrlInput by remember { mutableStateOf("") }
    var showProfileImportedBanner by remember { mutableStateOf(false) }

    var isEditMode by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<Profile?>(null) }

    var editingProfile by remember { mutableStateOf<Profile?>(null) }
    var editNameInput by remember { mutableStateOf("") }
    var editUrlInput by remember { mutableStateOf("") }

    val sortMode by viewModel.sortMode.collectAsState()
    var selectedServerForInfo by remember { mutableStateOf<Server?>(null) }
    var selectedProfileForMenu by remember { mutableStateOf<Profile?>(null) }
    var isLocalExpanded by remember { mutableStateOf(true) }
    val profileExpandedMap = remember { mutableStateMapOf<String, Boolean>() }

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
                text = if (isEditMode) strings.done else strings.edit,
                color = IosBlue,
                fontSize = 17.sp,
                fontWeight = if (isEditMode) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.clickable {
                    isEditMode = !isEditMode
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
                        text = strings.profileImported,
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
                if (isConnected) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Connect Toggle",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
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
                    isConnected -> strings.connected
                    isConnecting -> strings.connecting
                    else -> strings.notConnected
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isConnected) IosGreen else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "›",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Light
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. DNS & ROUTING CARD (Exact Streisand iOS)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.DnsSettings.route) }
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🌐",
                        fontSize = 18.sp
                    )
                    Text(
                        text = "DNS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "›",
                    fontSize = 20.sp,
                    color = Color(0xFFC7C7CC),
                    fontWeight = FontWeight.Light
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp)
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.RoutingSettings.route) }
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🔀",
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Routing",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "›",
                    fontSize = 20.sp,
                    color = Color(0xFFC7C7CC),
                    fontWeight = FontWeight.Light
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. SORT BY ROW (Streisand Exact: Sort by ping/name & Test all pings)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.isPinging) strings.testing else strings.testAllPings,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.isPinging) IosYellow else IosBlue,
                modifier = Modifier.clickable {
                    if (!state.isPinging) viewModel.pingAllServers()
                }
            )
            Text(
                text = if (sortMode == HomeViewModel.SortMode.PING) strings.sortModePing else strings.sortModeName,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = IosBlue,
                modifier = Modifier.clickable {
                    viewModel.toggleSortMode()
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        fun sortServers(list: List<Server>): List<Server> {
            return if (sortMode == HomeViewModel.SortMode.PING) {
                list.sortedWith(compareBy<Server> {
                    when {
                        it.latency > 0 -> it.latency
                        it.latency < 0 -> 99999L
                        else -> 50000L
                    }
                }.thenBy { it.name })
            } else {
                list.sortedBy { it.name }
            }
        }

        val localServers = sortServers(state.allServers.filter { it.profileId == null })

        // 5. LOCAL PROFILES CARD (Exact Streisand iOS: only shows when single/manual configs exist!)
        if (localServers.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isLocalExpanded = !isLocalExpanded }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF3B30))
                                    .clickable {
                                        localServers.forEach { viewModel.deleteServer(it.id) }
                                        Toast.makeText(context, "کانفیگ‌های محلی حذف شدند", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Delete Local Profiles",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "LOCAL PROFILES",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.2).sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = IosBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (isLocalExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    )
                    localServers.forEachIndexed { index, srv ->
                        val isSelected = srv.id == state.selectedServer?.id
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                            )
                        }
                        ServerRowItem(
                            server = srv,
                            isSelected = isSelected,
                            isEditMode = isEditMode,
                            onDeleteClick = {
                                viewModel.deleteServer(srv.id)
                                Toast.makeText(context, "سرور حذف شد", Toast.LENGTH_SHORT).show()
                            },
                            onSelect = { viewModel.selectServer(srv) },
                            onInfoClick = { selectedServerForInfo = srv }
                        )
                    }
                }
            }
        }

        // 6. SUBSCRIPTION CARDS (Only appears when subscription link is imported!)
        if (state.profiles.isNotEmpty()) {
            state.profiles.forEach { profile ->
                Spacer(modifier = Modifier.height(12.dp))
                val profileServers = sortServers(state.allServers.filter { it.profileId == profile.id })
                val isExpanded = profileExpandedMap[profile.id] ?: true

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // Subscription Header: e.g. "subscription" ∨
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { profileExpandedMap[profile.id] = !isExpanded },
                                onLongClick = { selectedProfileForMenu = profile }
                            )
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isEditMode) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF3B30))
                                        .clickable { profileToDelete = profile },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Delete Subscription",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = profile.name.ifBlank { "subscription" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = (-0.2).sp
                                )
                                val trafficInfo = profile.getTrafficDisplay()
                                if (!trafficInfo.isNullOrBlank()) {
                                    Text(
                                        text = trafficInfo,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = IosBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isExpanded && profileServers.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        )

                        profileServers.forEachIndexed { index, srv ->
                            val isSelected = srv.id == state.selectedServer?.id
                            if (index > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                                )
                            }
                            ServerRowItem(
                                server = srv,
                                isSelected = isSelected,
                                isEditMode = isEditMode,
                                onDeleteClick = {
                                    viewModel.deleteServer(srv.id)
                                    Toast.makeText(context, "سرور حذف شد", Toast.LENGTH_SHORT).show()
                                },
                                onSelect = { viewModel.selectServer(srv) },
                                onInfoClick = { selectedServerForInfo = srv }
                            )
                        }
                    }
                }
            }
        } else if (localServers.isEmpty()) {
            // Clean empty state when no subscription and no local configs yet (no fake servers!)
            Spacer(modifier = Modifier.height(28.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = strings.emptyProfilesNotice,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = strings.tapPlusToAdd,
                    fontSize = 12.5.sp,
                    color = IosBlue,
                    modifier = Modifier.clickable { showAddMenuDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // 6. ADD MENU DIALOG (Streisand Compact Glass Cards)
    if (showAddMenuDialog) {
        AlertDialog(
            onDismissRequest = { showAddMenuDialog = false },
            title = {
                Text(
                    text = strings.addConfigOrSub,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. IMPORT SUBSCRIPTION (Manual Subscription Entry)
                    GlassActionCard(
                        icon = "🔗",
                        title = strings.manualImportSub,
                        onClick = {
                            showAddMenuDialog = false
                            val clip = clipboardManager.getText()?.text ?: ""
                            if (clip.startsWith("http://") || clip.startsWith("https://")) {
                                subscriptionUrlInput = clip
                            }
                            showImportSubDialog = true
                        }
                    )

                    // 2. Import from clipboard
                    GlassActionCard(
                        icon = "📋",
                        title = strings.importClipboard,
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
                        }
                    )

                    // 3. Scan QR Code
                    GlassActionCard(
                        icon = "📷",
                        title = strings.scanQr,
                        onClick = {
                            showAddMenuDialog = false
                            Toast.makeText(context, strings.scanQr, Toast.LENGTH_SHORT).show()
                        }
                    )

                    // 4. Manual Config
                    GlassActionCard(
                        icon = "✍️",
                        title = strings.manualConfig,
                        onClick = {
                            showAddMenuDialog = false
                            navController.navigate(Screen.Servers.route)
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddMenuDialog = false }) {
                    Text(strings.cancel, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // 7. IMPORT SUBSCRIPTION INPUT DIALOG (Manual Entry)
    if (showImportSubDialog) {
        AlertDialog(
            onDismissRequest = { showImportSubDialog = false },
            title = {
                Text(
                    text = strings.manualImportSub,
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
                        text = strings.subscriptionUrl,
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
                    Text(strings.saveChanges, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportSubDialog = false }) {
                    Text(strings.cancel)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 8. SUBSCRIPTION ACTIONS DIALOG (Streisand Hold Menu: Update / Ping / Edit / Delete)
    if (selectedProfileForMenu != null) {
        val profile = selectedProfileForMenu!!
        AlertDialog(
            onDismissRequest = { selectedProfileForMenu = null },
            title = {
                Text(
                    text = profile.name.ifBlank { "subscription" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Option 1: Update Subscription
                    GlassActionCard(
                        icon = "🔄",
                        title = strings.updateSubscription,
                        onClick = {
                            selectedProfileForMenu = null
                            viewModel.updateSubscription(profile.id) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    // Option 2: Test Server Pings
                    GlassActionCard(
                        icon = "⚡",
                        title = strings.testPings,
                        onClick = {
                            selectedProfileForMenu = null
                            viewModel.pingAllServers()
                        }
                    )

                    // Option 3: Edit Subscription (Name & Link)
                    GlassActionCard(
                        icon = "✏️",
                        title = strings.editSubscription,
                        onClick = {
                            selectedProfileForMenu = null
                            editingProfile = profile
                            editNameInput = profile.name
                            editUrlInput = profile.subscriptionUrl
                        }
                    )

                    // Option 4: Delete Subscription
                    GlassActionCard(
                        icon = "🗑️",
                        title = strings.deleteSubscription,
                        onClick = {
                            val id = profile.id
                            selectedProfileForMenu = null
                            viewModel.deleteProfile(id)
                            Toast.makeText(context, strings.subscriptionDeleted, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedProfileForMenu = null }) {
                    Text(strings.cancel, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // 8.1 EDIT SUBSCRIPTION DIALOG (Edit Name & Subscription Link)
    if (editingProfile != null) {
        val profile = editingProfile!!
        AlertDialog(
            onDismissRequest = { editingProfile = null },
            title = {
                Text(
                    text = strings.editSubscription,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.subscriptionName,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        placeholder = { Text("subscription") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = strings.subscriptionUrl,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editUrlInput,
                        onValueChange = { editUrlInput = it },
                        placeholder = { Text("https://example.com/sub/...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = profile.id
                        editingProfile = null
                        viewModel.editSubscription(id, editNameInput, editUrlInput) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.saveChanges, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProfile = null }) {
                    Text(strings.cancel)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // PROFILE DELETE CONFIRMATION DIALOG (Streisand Edit Mode Flow)
    if (profileToDelete != null) {
        val prof = profileToDelete!!
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = {
                Text(
                    text = strings.deleteSubscription,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "${prof.name.ifBlank { "subscription" }}",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = prof.id
                        profileToDelete = null
                        viewModel.deleteProfile(id)
                        Toast.makeText(context, strings.subscriptionDeleted, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.deleteSubscription, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text(strings.cancel)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 9. SERVER DETAILS & EDIT DIALOG
    if (selectedServerForInfo != null) {
        val srv = selectedServerForInfo!!
        var isEditingThisServer by remember(srv.id) { mutableStateOf(false) }

        var editName by remember(srv.id) { mutableStateOf(srv.name) }
        var editHost by remember(srv.id) { mutableStateOf(srv.host) }
        var editPort by remember(srv.id) { mutableStateOf(srv.port.toString()) }
        var editSni by remember(srv.id) { mutableStateOf(srv.serverName ?: "") }
        var editHostHeader by remember(srv.id) { mutableStateOf(srv.hostHeader ?: "") }
        var editPath by remember(srv.id) { mutableStateOf(srv.path ?: "") }
        var editUuid by remember(srv.id) { mutableStateOf(srv.uuid ?: srv.password ?: "") }

        AlertDialog(
            onDismissRequest = {
                selectedServerForInfo = null
                isEditingThisServer = false
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditingThisServer) strings.serverDetails else srv.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    TextButton(onClick = { isEditingThisServer = !isEditingThisServer }) {
                        Text(
                            text = if (isEditingThisServer) strings.serverDetails else strings.edit,
                            color = IosBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            text = {
                if (isEditingThisServer) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("نام سرور") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = editHost,
                            onValueChange = { editHost = it },
                            label = { Text("آدرس سرور (Host / IP)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = editPort,
                            onValueChange = { editPort = it },
                            label = { Text("پورت (Port)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = editSni,
                            onValueChange = { editSni = it },
                            label = { Text("SNI (Server Name)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = editHostHeader,
                            onValueChange = { editHostHeader = it },
                            label = { Text("Host Header") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = editPath,
                            onValueChange = { editPath = it },
                            label = { Text("مسیر (Path)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = editUuid,
                            onValueChange = { editUuid = it },
                            label = { Text("UUID یا پسورد") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("پروتکل: ${srv.protocol.name}", fontSize = 13.sp)
                        Text("آدرس: ${srv.host}:${srv.port}", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Text("نوع شبکه: ${srv.network.uppercase()}", fontSize = 13.sp)
                        Text("امنیت: ${srv.security.uppercase()}", fontSize = 13.sp)
                        if (!srv.serverName.isNullOrBlank()) {
                            Text("SNI: ${srv.serverName}", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                        if (!srv.hostHeader.isNullOrBlank()) {
                            Text("Host Header: ${srv.hostHeader}", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                        if (!srv.path.isNullOrBlank()) {
                            Text("Path: ${srv.path}", fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                        if (srv.latency > 0) {
                            Text("تاخیر واقعی: ${srv.latency}ms", fontSize = 13.sp, color = IosGreen, fontWeight = FontWeight.Bold)
                        } else if (srv.latency < 0) {
                            Text("تاخیر واقعی: TimeOut", fontSize = 13.sp, color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.pingServer(srv)
                                    Toast.makeText(context, "در حال تست تاخیر واقعی...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("⚡ تست تاخیر", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    val copyText = "${srv.name}\n${srv.host}:${srv.port}"
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(copyText))
                                    Toast.makeText(context, "مشخصات سرور کپی شد", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("📋 کپی", fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.deleteServer(srv.id)
                                selectedServerForInfo = null
                                Toast.makeText(context, "سرور حذف شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF3B30)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🗑️ حذف این سرور")
                        }
                    }
                }
            },
            confirmButton = {
                if (isEditingThisServer) {
                    Button(
                        onClick = {
                            val updated = srv.copy(
                                name = editName.trim().ifBlank { srv.name },
                                host = editHost.trim().ifBlank { srv.host },
                                port = editPort.trim().toIntOrNull() ?: srv.port,
                                serverName = editSni.trim().ifBlank { null },
                                hostHeader = editHostHeader.trim().ifBlank { null },
                                path = editPath.trim().ifBlank { null },
                                uuid = if (srv.protocol == ServerProtocol.VLESS || srv.protocol == ServerProtocol.VMESS) editUuid.trim().ifBlank { srv.uuid } else srv.uuid,
                                password = if (srv.protocol == ServerProtocol.TROJAN || srv.protocol == ServerProtocol.SHADOWSOCKS) editUuid.trim().ifBlank { srv.password } else srv.password
                            )
                            viewModel.updateServer(updated)
                            selectedServerForInfo = updated
                            isEditingThisServer = false
                            Toast.makeText(context, strings.serverSaved, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(strings.saveChanges, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            selectedServerForInfo = null
                            viewModel.selectServer(srv)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(srv.name, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (isEditingThisServer) {
                        isEditingThisServer = false
                    } else {
                        selectedServerForInfo = null
                    }
                }) {
                    Text(strings.cancel)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}


@Composable
private fun ServerRowItem(
    server: Server,
    isSelected: Boolean,
    isEditMode: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onSelect: () -> Unit,
    onInfoClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = if (isEditMode) onDeleteClick else onSelect)
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
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30))
                        .clickable(onClick = onDeleteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Delete Server",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
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
            }

            Text(text = "🚀", fontSize = 14.sp)

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = server.name,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${server.protocol} / ${server.network.uppercase()} / ${server.security.uppercase()}",
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
            if (server.latency > 0) {
                val pingColor = when {
                    server.latency < 300 -> IosGreen
                    server.latency < 800 -> IosYellow
                    else -> Color(0xFFFF3B30)
                }
                Text(
                    text = "${server.latency}ms",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = pingColor
                )
            } else if (server.latency < 0) {
                Text(
                    text = "TimeOut",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFFF3B30)
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onInfoClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ⓘ",
                    color = IosBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hrs, mins, secs)
}

@Composable
private fun GlassActionCard(
    icon: String,
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 15.sp
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "›",
                fontSize = 17.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
        }
    }
}

