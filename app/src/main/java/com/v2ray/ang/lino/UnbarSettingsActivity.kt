package com.v2ray.ang.lino

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsChangeManager
import com.v2ray.ang.ui.base.BaseComponentActivity
import com.v2ray.ang.ui.compose.AppTheme
import com.v2ray.ang.ui.compose.AppTopBar

class UnbarSettingsActivity : BaseComponentActivity() {

    @Composable
    override fun ScreenContent() {
        UnbarSettingsScreen(
            onBack = { finish() },
            onOpenScanner = { startActivity(Intent(this, DnsScannerActivity::class.java)) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UnbarSettingsScreen(
    onBack: () -> Unit,
    onOpenScanner: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(LinoSettingsStore.loadUnbar()) }
    var showAddServerDialog by remember { mutableStateOf(false) }

    fun update(newVal: UnbarSettings) {
        settings = newVal
        LinoSettingsStore.saveUnbar(newVal)
        SettingsChangeManager.makeRestartService()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.lino_unbar_settings_title),
                onBackClick = onBack,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back_24dp), contentDescription = stringResource(R.string.acc_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick actions row: Add Unbar Server + Open DNS Scanner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddServerDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.lino_unbar_add_server))
                }
                OutlinedButton(
                    onClick = onOpenScanner,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.lino_open_dns_scanner))
                }
            }

            // Section 1: Connection Mode
            SectionHeader(stringResource(R.string.lino_unbar_sec_connection))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = settings.connectionMode == "vpn",
                    onClick = { update(settings.copy(connectionMode = "vpn")) },
                    label = { Text(stringResource(R.string.lino_mode_vpn)) }
                )
                FilterChip(
                    selected = settings.connectionMode == "socks",
                    onClick = { update(settings.copy(connectionMode = "socks")) },
                    label = { Text(stringResource(R.string.lino_mode_socks)) }
                )
            }

            // Section 2: Resolver Discovery & Custom Resolvers
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_unbar_sec_resolvers))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = settings.discoveryMode == "builtin",
                    onClick = { update(settings.copy(discoveryMode = "builtin")) },
                    label = { Text(stringResource(R.string.lino_unbar_res_builtin)) }
                )
                FilterChip(
                    selected = settings.discoveryMode == "custom",
                    onClick = { update(settings.copy(discoveryMode = "custom")) },
                    label = { Text(stringResource(R.string.lino_unbar_res_custom)) }
                )
            }
            if (settings.discoveryMode == "custom") {
                OutlinedTextField(
                    value = settings.customResolvers,
                    onValueChange = { update(settings.copy(customResolvers = it)) },
                    label = { Text(stringResource(R.string.lino_custom_resolvers_label)) },
                    placeholder = { Text(stringResource(R.string.lino_custom_resolvers_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            SwitchRow(
                title = stringResource(R.string.lino_unbar_tcp_mode),
                subtitle = stringResource(R.string.lino_unbar_tcp_mode_desc),
                checked = settings.tcpMode,
                onCheckedChange = { update(settings.copy(tcpMode = it)) }
            )

            // Section 3: Packet Tuning (MTU & Max Answer)
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_unbar_sec_tuning))
            Text(stringResource(R.string.lino_unbar_mtu_label), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1280, 1400, 1500).forEach { v ->
                    FilterChip(
                        selected = settings.mtu == v,
                        onClick = { update(settings.copy(mtu = v)) },
                        label = { Text(if (v == 0) stringResource(R.string.lino_auto) else v.toString()) }
                    )
                }
            }

            Text(stringResource(R.string.lino_unbar_max_answer_label), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 512, 768, 1024, 1232).forEach { v ->
                    FilterChip(
                        selected = settings.maxAnswer == v,
                        onClick = { update(settings.copy(maxAnswer = v)) },
                        label = { Text(if (v == 0) stringResource(R.string.lino_auto) else v.toString()) }
                    )
                }
            }

            // Section 4: Bandwidth Limits
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_unbar_sec_bandwidth))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = if (settings.downloadLimitKbps == 0) "" else settings.downloadLimitKbps.toString(),
                    onValueChange = { update(settings.copy(downloadLimitKbps = it.toIntOrNull() ?: 0)) },
                    label = { Text(stringResource(R.string.lino_download_limit)) },
                    placeholder = { Text(stringResource(R.string.lino_unlimited)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (settings.uploadLimitKbps == 0) "" else settings.uploadLimitKbps.toString(),
                    onValueChange = { update(settings.copy(uploadLimitKbps = it.toIntOrNull() ?: 0)) },
                    label = { Text(stringResource(R.string.lino_upload_limit)) },
                    placeholder = { Text(stringResource(R.string.lino_unlimited)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Section 5: Geo Bypass & Custom Bypass Domains
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_unbar_sec_routing))
            SwitchRow(
                title = stringResource(R.string.lino_unbar_geo_bypass),
                subtitle = stringResource(R.string.lino_unbar_geo_bypass_desc),
                checked = settings.geoBypassIr,
                onCheckedChange = { update(settings.copy(geoBypassIr = it)) }
            )
            OutlinedTextField(
                value = settings.customBypassDomains,
                onValueChange = { update(settings.copy(customBypassDomains = it)) },
                label = { Text(stringResource(R.string.lino_unbar_bypass_domains)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Section 6: Local Proxy & Diagnostics
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_unbar_sec_proxy_diag))
            OutlinedTextField(
                value = settings.socksPort.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { update(settings.copy(socksPort = it)) } },
                label = { Text(stringResource(R.string.lino_local_port)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = settings.socksUsername,
                    onValueChange = { update(settings.copy(socksUsername = it)) },
                    label = { Text(stringResource(R.string.lino_proxy_username)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = settings.socksPassword,
                    onValueChange = { update(settings.copy(socksPassword = it)) },
                    label = { Text(stringResource(R.string.lino_proxy_password)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            SwitchRow(
                title = stringResource(R.string.lino_unbar_notif_speed),
                subtitle = "",
                checked = settings.showSpeedInNotification,
                onCheckedChange = { update(settings.copy(showSpeedInNotification = it)) }
            )
            SwitchRow(
                title = stringResource(R.string.lino_unbar_diag_logs),
                subtitle = "",
                checked = settings.diagnosticLogs,
                onCheckedChange = { update(settings.copy(diagnosticLogs = it)) }
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showAddServerDialog) {
        AddUnbarServerDialog(
            onDismiss = { showAddServerDialog = false },
            onAdded = {
                showAddServerDialog = false
                context.toast(context.getString(R.string.lino_server_added_ok))
            }
        )
    }
}

@Composable
private fun AddUnbarServerDialog(
    onDismiss: () -> Unit,
    onAdded: () -> Unit
) {
    var uriOrName by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("") }
    var pubkey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.lino_unbar_add_server)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uriOrName,
                    onValueChange = { uriOrName = it },
                    label = { Text(stringResource(R.string.lino_unbar_uri_or_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = zone,
                    onValueChange = { zone = it },
                    label = { Text(stringResource(R.string.lino_unbar_zone_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pubkey,
                    onValueChange = { pubkey = it },
                    label = { Text(stringResource(R.string.lino_unbar_pubkey_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsed = if (uriOrName.trim().startsWith("unbar://", true)) {
                    UnbarFmt.parse(uriOrName.trim())
                } else if (zone.isNotBlank() && pubkey.isNotBlank()) {
                    ProfileItem.create(EConfigType.UNBAR).apply {
                        remarks = uriOrName.trim().ifBlank { zone.trim() }
                        server = zone.trim()
                        serverPort = "53"
                        publicKey = pubkey.trim()
                        mode = "DISCOVERY"
                        host = "IR"
                    }
                } else null

                if (parsed != null) {
                    MmkvManager.encodeServerConfig("", parsed)
                    SettingsChangeManager.makeSetupGroupTab()
                    onAdded()
                }
            }) {
                Text(stringResource(R.string.lino_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.lino_cancel))
            }
        }
    )
}

@Composable
internal fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
internal fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
