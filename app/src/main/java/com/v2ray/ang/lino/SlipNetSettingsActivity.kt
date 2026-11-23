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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import org.json.JSONObject

class SlipNetSettingsActivity : BaseComponentActivity() {

    @Composable
    override fun ScreenContent() {
        SlipNetSettingsScreen(
            onBack = { finish() },
            onOpenScanner = { startActivity(Intent(this, DnsScannerActivity::class.java)) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SlipNetSettingsScreen(
    onBack: () -> Unit,
    onOpenScanner: () -> Unit
) {
    val context = LocalContext.current
    var s by remember { mutableStateOf(LinoSettingsStore.loadSlipNet()) }
    var showAddDialog by remember { mutableStateOf(false) }

    fun update(newVal: SlipNetSettings) {
        s = newVal
        LinoSettingsStore.saveSlipNet(newVal)
        SettingsChangeManager.makeRestartService()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.lino_slipnet_settings_title),
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { showAddDialog = true }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.lino_slipnet_add_server))
                }
                OutlinedButton(onClick = onOpenScanner, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.lino_open_dns_scanner))
                }
            }

            // Section 1: Tunnel Type & DNS Transport
            SectionHeader(stringResource(R.string.lino_slip_sec_tunnel))
            val tunnelTypes = listOf(
                "dnstt" to R.string.lino_proto_dnstt,
                "noizdns" to R.string.lino_proto_noizdns,
                "vaydns" to R.string.lino_proto_vaydns,
                "soar" to R.string.lino_proto_soar,
                "slipstream" to R.string.lino_proto_slipstream,
                "naive" to R.string.lino_proto_naive,
                "ssh" to R.string.lino_proto_ssh,
                "tor" to R.string.lino_proto_tor,
                "doh" to R.string.lino_proto_doh
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tunnelTypes.forEach { (key, resId) ->
                    FilterChip(
                        selected = s.defaultTunnelType == key,
                        onClick = { update(s.copy(defaultTunnelType = key)) },
                        label = { Text(stringResource(resId)) }
                    )
                }
            }

            Text(stringResource(R.string.lino_slip_dns_transport), style = MaterialTheme.typography.labelLarge)
            val transports = listOf(
                "udp" to R.string.lino_trans_udp,
                "tcp" to R.string.lino_trans_tcp,
                "dot" to R.string.lino_trans_dot,
                "doh" to R.string.lino_trans_doh
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                transports.forEach { (key, resId) ->
                    FilterChip(
                        selected = s.dnsTransport == key,
                        onClick = { update(s.copy(dnsTransport = key)) },
                        label = { Text(stringResource(resId)) }
                    )
                }
            }
            if (s.dnsTransport == "doh") {
                OutlinedTextField(
                    value = s.dohUrl,
                    onValueChange = { update(s.copy(dohUrl = it)) },
                    label = { Text(stringResource(R.string.lino_slip_doh_url)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Section 2: Resolver Strategy & Custom Resolvers
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_slip_sec_resolvers))
            val resModes = listOf(
                "fanout" to R.string.lino_res_mode_fanout,
                "round_robin" to R.string.lino_res_mode_rr,
                "fastest" to R.string.lino_res_mode_fastest
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                resModes.forEach { (key, resId) ->
                    FilterChip(
                        selected = s.resolverMode == key,
                        onClick = { update(s.copy(resolverMode = key)) },
                        label = { Text(stringResource(resId)) }
                    )
                }
            }
            OutlinedTextField(
                value = s.customResolvers,
                onValueChange = { update(s.copy(customResolvers = it)) },
                label = { Text(stringResource(R.string.lino_custom_resolvers_label)) },
                placeholder = { Text(stringResource(R.string.lino_custom_resolvers_hint)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = s.rrSpreadCount.toString(),
                    onValueChange = { it.toIntOrNull()?.let { v -> update(s.copy(rrSpreadCount = v)) } },
                    label = { Text(stringResource(R.string.lino_slip_rr_spread)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (s.maxPayload == 0) "" else s.maxPayload.toString(),
                    onValueChange = { update(s.copy(maxPayload = it.toIntOrNull() ?: 0)) },
                    label = { Text(stringResource(R.string.lino_slip_max_payload)) },
                    placeholder = { Text(stringResource(R.string.lino_auto)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Section 3: DNSTT & NoizDNS
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_slip_sec_dnstt_noiz))
            SwitchRow(
                title = stringResource(R.string.lino_slip_auth_mode),
                subtitle = stringResource(R.string.lino_slip_auth_mode_desc),
                checked = s.authoritativeMode,
                onCheckedChange = { update(s.copy(authoritativeMode = it)) }
            )
            SwitchRow(
                title = stringResource(R.string.lino_slip_stealth_mode),
                subtitle = stringResource(R.string.lino_slip_stealth_mode_desc),
                checked = s.noizStealthMode,
                onCheckedChange = { update(s.copy(noizStealthMode = it)) }
            )

            // Section 4: VayDNS Advanced Tuning
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_slip_sec_vaydns))
            Text(stringResource(R.string.lino_slip_vay_record), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("txt", "a", "aaaa", "mx", "cname").forEach { rt ->
                    FilterChip(
                        selected = s.vayRecordType == rt,
                        onClick = { update(s.copy(vayRecordType = rt)) },
                        label = { Text(rt.uppercase()) }
                    )
                }
            }
            SwitchRow(
                title = stringResource(R.string.lino_slip_vay_compat),
                subtitle = "",
                checked = s.vayDnsttCompat,
                onCheckedChange = { update(s.copy(vayDnsttCompat = it)) }
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = s.vayMaxQnameLen.toString(),
                    onValueChange = { it.toIntOrNull()?.let { v -> update(s.copy(vayMaxQnameLen = v)) } },
                    label = { Text(stringResource(R.string.lino_slip_vay_qname)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (s.vayRps == 0.0) "" else s.vayRps.toString(),
                    onValueChange = { update(s.copy(vayRps = it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text(stringResource(R.string.lino_slip_vay_rps)) },
                    placeholder = { Text(stringResource(R.string.lino_unlimited)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = if (s.vayKeepalive == 0) "" else s.vayKeepalive.toString(),
                    onValueChange = { update(s.copy(vayKeepalive = it.toIntOrNull() ?: 0)) },
                    label = { Text(stringResource(R.string.lino_slip_keepalive)) },
                    placeholder = { Text(stringResource(R.string.lino_auto)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (s.vayIdleTimeout == 0) "" else s.vayIdleTimeout.toString(),
                    onValueChange = { update(s.copy(vayIdleTimeout = it.toIntOrNull() ?: 0)) },
                    label = { Text(stringResource(R.string.lino_slip_idle_timeout)) },
                    placeholder = { Text(stringResource(R.string.lino_auto)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Section 5: Slipstream QUIC
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_slip_sec_slipstream))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = s.slipstreamCongestion == "bbr",
                    onClick = { update(s.copy(slipstreamCongestion = "bbr")) },
                    label = { Text(stringResource(R.string.lino_cc_bbr)) }
                )
                FilterChip(
                    selected = s.slipstreamCongestion == "dcubic",
                    onClick = { update(s.copy(slipstreamCongestion = "dcubic")) },
                    label = { Text(stringResource(R.string.lino_cc_dcubic)) }
                )
            }
            SwitchRow(
                title = stringResource(R.string.lino_slip_gso),
                subtitle = "",
                checked = s.slipstreamGso,
                onCheckedChange = { update(s.copy(slipstreamGso = it)) }
            )

            // Section 6: SSH Tunnel & Obfuscation
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_slip_sec_ssh))
            SwitchRow(
                title = stringResource(R.string.lino_slip_ssh_tls),
                subtitle = "",
                checked = s.sshOverTls,
                onCheckedChange = { update(s.copy(sshOverTls = it)) }
            )
            if (s.sshOverTls) {
                OutlinedTextField(
                    value = s.sshSni,
                    onValueChange = { update(s.copy(sshSni = it)) },
                    label = { Text(stringResource(R.string.lino_slip_ssh_sni)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            SwitchRow(
                title = stringResource(R.string.lino_slip_ssh_ws),
                subtitle = "",
                checked = s.sshOverWs,
                onCheckedChange = { update(s.copy(sshOverWs = it)) }
            )
            if (s.sshOverWs) {
                OutlinedTextField(
                    value = s.sshWsPath,
                    onValueChange = { update(s.copy(sshWsPath = it)) },
                    label = { Text(stringResource(R.string.lino_slip_ssh_ws_path)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            OutlinedTextField(
                value = s.sshPayload,
                onValueChange = { update(s.copy(sshPayload = it)) },
                label = { Text(stringResource(R.string.lino_slip_ssh_payload)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            // Section 7: Tor & Pluggable Transports (Snowflake / obfs4 / Meek)
            HorizontalDivider()
            SectionHeader(stringResource(R.string.lino_slip_sec_tor))
            val torModes = listOf(
                "snowflake" to R.string.lino_tor_snowflake,
                "obfs4" to R.string.lino_tor_obfs4,
                "meek" to R.string.lino_tor_meek,
                "custom" to R.string.lino_tor_custom,
                "direct" to R.string.lino_tor_direct
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                torModes.forEach { (key, resId) ->
                    FilterChip(
                        selected = s.torBridgeMode == key,
                        onClick = { update(s.copy(torBridgeMode = key)) },
                        label = { Text(stringResource(resId)) }
                    )
                }
            }
            if (s.torBridgeMode in listOf("obfs4", "meek", "custom")) {
                OutlinedTextField(
                    value = s.torCustomBridges,
                    onValueChange = { update(s.copy(torCustomBridges = it)) },
                    label = { Text(stringResource(R.string.lino_slip_tor_bridges)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            }

            // Section 8: Local Port
            HorizontalDivider()
            OutlinedTextField(
                value = s.listenPort.toString(),
                onValueChange = { it.toIntOrNull()?.let { v -> update(s.copy(listenPort = v)) } },
                label = { Text(stringResource(R.string.lino_local_port)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showAddDialog) {
        AddSlipNetServerDialog(
            defaultType = s.defaultTunnelType,
            onDismiss = { showAddDialog = false },
            onAdded = {
                showAddDialog = false
                context.toast(context.getString(R.string.lino_server_added_ok))
            }
        )
    }
}

@Composable
private fun AddSlipNetServerDialog(
    defaultType: String,
    onDismiss: () -> Unit,
    onAdded: () -> Unit
) {
    var uriOrName by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var pubkey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.lino_slipnet_add_server)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uriOrName,
                    onValueChange = { uriOrName = it },
                    label = { Text(stringResource(R.string.lino_slipnet_uri_or_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text(stringResource(R.string.lino_slip_domain_label)) },
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
                val input = uriOrName.trim()
                val parsed = if (input.startsWith("slipnet://", true) || input.startsWith("slipnet-enc://", true)) {
                    SlipNetFmt.parse(input)
                } else if (domain.isNotBlank() || defaultType == "tor") {
                    ProfileItem.create(EConfigType.SLIPNET).apply {
                        remarks = input.ifBlank { domain.trim().ifBlank { defaultType.uppercase() } }
                        method = defaultType
                        server = domain.trim().ifBlank { "tor" }
                        serverPort = "53"
                        publicKey = pubkey.trim()
                        xhttpExtra = JSONObject()
                            .put("tunnelType", defaultType)
                            .put("domain", domain.trim())
                            .put("pubkey", pubkey.trim())
                            .toString()
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
