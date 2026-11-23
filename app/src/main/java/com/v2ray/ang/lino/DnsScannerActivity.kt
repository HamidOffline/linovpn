package com.v2ray.ang.lino

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.extension.toast
import com.v2ray.ang.ui.base.BaseComponentActivity
import com.v2ray.ang.ui.compose.AppTheme
import com.v2ray.ang.ui.compose.AppTopBar
import com.v2ray.ang.util.Utils

class DnsScannerActivity : BaseComponentActivity() {

    @Composable
    override fun ScreenContent() {
        DnsScannerScreen(onBack = { finish() })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DnsScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scanState by DnsScannerEngine.state.collectAsState()
    var slipSettings by remember { mutableStateOf(LinoSettingsStore.loadSlipNet()) }
    var customCidr by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose {
            DnsScannerEngine.stopScan()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.lino_scanner_title),
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Source picker
            Text(stringResource(R.string.lino_scanner_source), style = MaterialTheme.typography.labelLarge)
            val sources = listOf(
                "unbar_ir" to R.string.lino_scan_src_unbar_ir,
                "ir_lite" to R.string.lino_scan_src_ir_lite,
                "ir_full" to R.string.lino_scan_src_ir_full,
                "custom" to R.string.lino_scan_src_custom
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sources.forEach { (key, resId) ->
                    FilterChip(
                        selected = slipSettings.scannerSource == key,
                        onClick = {
                            slipSettings = slipSettings.copy(scannerSource = key)
                            LinoSettingsStore.saveSlipNet(slipSettings)
                        },
                        label = { Text(stringResource(resId)) }
                    )
                }
            }

            if (slipSettings.scannerSource == "custom") {
                OutlinedTextField(
                    value = customCidr,
                    onValueChange = { customCidr = it },
                    label = { Text(stringResource(R.string.lino_scanner_custom_cidr)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = slipSettings.scannerThreads.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { v ->
                            slipSettings = slipSettings.copy(scannerThreads = v)
                            LinoSettingsStore.saveSlipNet(slipSettings)
                        }
                    },
                    label = { Text(stringResource(R.string.lino_scanner_threads)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = slipSettings.scannerTimeoutMs.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { v ->
                            slipSettings = slipSettings.copy(scannerTimeoutMs = v)
                            LinoSettingsStore.saveSlipNet(slipSettings)
                        }
                    },
                    label = { Text(stringResource(R.string.lino_scanner_timeout)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Start / Stop Scan Button
            Button(
                onClick = {
                    if (scanState.isScanning) {
                        DnsScannerEngine.stopScan()
                    } else {
                        DnsScannerEngine.startScan(
                            context = context,
                            source = slipSettings.scannerSource,
                            customInput = customCidr,
                            testDomain = slipSettings.scannerTestDomain,
                            threads = slipSettings.scannerThreads,
                            timeoutMs = slipSettings.scannerTimeoutMs
                        )
                    }
                },
                colors = if (scanState.isScanning) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        if (scanState.isScanning) R.string.lino_scanner_stop else R.string.lino_scanner_start
                    )
                )
            }

            // Progress & stats
            if (scanState.totalTargets > 0) {
                val frac = (scanState.scannedCount.toFloat() / scanState.totalTargets.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(progress = { frac }, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.lino_scanner_progress, scanState.scannedCount, scanState.totalTargets),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(R.string.lino_scanner_found, scanState.results.size),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // One-tap Apply Actions when results exist
            if (scanState.results.isNotEmpty()) {
                val topCsv = scanState.results
                    .filter { it.cleanNxDomain }
                    .ifEmpty { scanState.results }
                    .take(10)
                    .joinToString(",") { it.address }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            LinoSettingsStore.applyScannedResolversToUnbar(topCsv)
                            context.toast(context.getString(R.string.lino_scanner_applied_unbar))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.lino_apply_unbar), maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            LinoSettingsStore.applyScannedResolversToSlipNet(topCsv)
                            context.toast(context.getString(R.string.lino_scanner_applied_slipnet))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.lino_apply_slipnet), maxLines = 1)
                    }
                }
                OutlinedButton(
                    onClick = {
                        Utils.setClipboard(context, topCsv)
                        context.toast(context.getString(R.string.toast_success))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.lino_copy_top_resolvers))
                }
            }

            // Results list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(scanState.results, key = { it.address }) { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 1.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(item.address, fontWeight = FontWeight.SemiBold)
                                val badge = if (item.cleanNxDomain && item.ednsSupported) {
                                    stringResource(R.string.lino_resolver_clean_edns)
                                } else if (item.cleanNxDomain) {
                                    stringResource(R.string.lino_resolver_clean)
                                } else {
                                    stringResource(R.string.lino_resolver_hijack)
                                }
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (item.cleanNxDomain) Color(0xFF00C88C) else MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = stringResource(R.string.lino_ms_format, item.latencyMs),
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    item.latencyMs < 120 -> Color(0xFF00C88C)
                                    item.latencyMs < 350 -> Color(0xFFFFB020)
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
