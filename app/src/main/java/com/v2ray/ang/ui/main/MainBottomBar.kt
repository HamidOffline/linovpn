package com.v2ray.ang.ui.main

import android.net.TrafficStats
import android.os.Process
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.colorFabActive
import com.v2ray.ang.ui.compose.colorFabInactiveDark
import com.v2ray.ang.ui.compose.colorFabInactiveLight
import com.v2ray.ang.ui.compose.colorPing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private val KNOB_SIZE = 54.dp
private val BAR_MAX_WIDTH = 480.dp
private const val TEXT_MAX_SP = 13f
private const val TEXT_MIN_SP = 9.5f
private const val TEXT_LINE_HEIGHT_FACTOR = 1.25f

/**
 * Floating semi-rounded capsule connection status bar aligned cleanly with the card grid:
 * - Shows connection/ping status + exit country & IP (up to 2 crisp lines, no server name clutter)
 * - Shows real-time Download/Upload speeds AND total session downloaded/uploaded traffic when connected
 * - Fixed right-side circular Connect/Disconnect FAB for effortless one-hand reach
 */
@Composable
fun MainBottomBar(
    displayText: String,
    isRunning: Boolean,
    isDarkTheme: Boolean,
    onAction: (MainAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val rotationAnim = remember { Animatable(0f) }
    val textLayoutDirection = LocalLayoutDirection.current

    var rxSpeedBps by remember { mutableLongStateOf(0L) }
    var txSpeedBps by remember { mutableLongStateOf(0L) }
    var sessionRxBytes by remember { mutableLongStateOf(0L) }
    var sessionTxBytes by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (!isRunning) {
            rotationAnim.snapTo(0f)
            rxSpeedBps = 0L
            txSpeedBps = 0L
            sessionRxBytes = 0L
            sessionTxBytes = 0L
        } else {
            val uid = Process.myUid()
            var lastUidRx = TrafficStats.getUidRxBytes(uid)
            var lastUidTx = TrafficStats.getUidTxBytes(uid)
            var lastTotRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
            var lastTotTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
            sessionRxBytes = 0L
            sessionTxBytes = 0L

            while (true) {
                delay(1000L)
                val curUidRx = TrafficStats.getUidRxBytes(uid)
                val curUidTx = TrafficStats.getUidTxBytes(uid)
                val curTotRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
                val curTotTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)

                val dUidRx = if (curUidRx >= 0L && lastUidRx >= 0L) (curUidRx - lastUidRx).coerceAtLeast(0L) else -1L
                val dUidTx = if (curUidTx >= 0L && lastUidTx >= 0L) (curUidTx - lastUidTx).coerceAtLeast(0L) else -1L
                val dTotRx = ((curTotRx - lastTotRx).coerceAtLeast(0L)) / 2L
                val dTotTx = ((curTotTx - lastTotTx).coerceAtLeast(0L)) / 2L

                val deltaRx = if (dUidRx > 0L) dUidRx else dTotRx
                val deltaTx = if (dUidTx > 0L) dUidTx else dTotTx

                rxSpeedBps = deltaRx
                txSpeedBps = deltaTx
                sessionRxBytes += deltaRx
                sessionTxBytes += deltaTx

                lastUidRx = curUidRx
                lastUidTx = curUidTx
                lastTotRx = curTotRx
                lastTotTx = curTotTx
            }
        }
    }

    val downSpeedText = formatCompactSpeed(rxSpeedBps)
    val upSpeedText = formatCompactSpeed(txSpeedBps)
    val downVolText = formatLocalizedVolume(sessionRxBytes)
    val upVolText = formatLocalizedVolume(sessionTxBytes)

    val trackHeight = if (isRunning) 74.dp else 62.dp
    val knobInset = (trackHeight - KNOB_SIZE) / 2

    Box(
        modifier = modifier
            .widthIn(max = BAR_MAX_WIDTH)
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
            ) {
                val pillBorder = BorderStroke(
                    width = 1.dp,
                    color = if (isRunning) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.70f)
                    }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .shadow(elevation = 4.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(pillBorder, CircleShape)
                        .clickable { onAction(MainAction.TestCurrentServer) }
                )

                CompositionLocalProvider(LocalLayoutDirection provides textLayoutDirection) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth()
                            .absolutePadding(left = 20.dp, right = KNOB_SIZE + knobInset + 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        var fontSizeSp by remember(displayText, isRunning) {
                            mutableFloatStateOf(if (isRunning) 12.2f else TEXT_MAX_SP)
                        }
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp * TEXT_LINE_HEIGHT_FACTOR).sp,
                                fontWeight = FontWeight.Medium,
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.None
                                )
                            ),
                            color = if (isRunning) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Start,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { layout ->
                                if (layout.hasVisualOverflow && fontSizeSp > TEXT_MIN_SP) {
                                    fontSizeSp = (fontSizeSp - 0.8f).coerceAtLeast(TEXT_MIN_SP)
                                }
                            },
                            modifier = Modifier.semantics { contentDescription = displayText }
                        )

                        if (isRunning) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "↓ $downSpeedText ($downVolText)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textDirection = TextDirection.Ltr
                                    ),
                                    color = colorPing,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Text(
                                    text = "↑ $upSpeedText ($upVolText)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textDirection = TextDirection.Ltr
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        if (!isRunning) {
                            scope.launch {
                                rotationAnim.animateTo(
                                    targetValue = 720f,
                                    animationSpec = tween(durationMillis = 2400)
                                )
                            }
                        }
                        onAction(MainAction.ToggleService)
                    },
                    shape = CircleShape,
                    containerColor = if (isRunning) {
                        colorFabActive
                    } else if (isDarkTheme) {
                        colorFabInactiveDark
                    } else {
                        colorFabInactiveLight
                    },
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = knobInset)
                        .size(KNOB_SIZE)
                ) {
                    Icon(
                        painter = painterResource(
                            if (isRunning) R.drawable.ic_stop_24dp else R.drawable.ic_play_24dp
                        ),
                        contentDescription = stringResource(
                            if (isRunning) R.string.acc_stop else R.string.acc_start
                        ),
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotationAnim.value }
                    )
                }
            }
        }
    }
}

@Composable
private fun formatCompactSpeed(bps: Long): String {
    val kb = 1024.0
    val mb = kb * 1024.0
    return when {
        bps >= mb -> stringResource(R.string.lino_speed_short_mbps, String.format(Locale.US, "%.1f", bps / mb))
        bps >= kb -> stringResource(R.string.lino_speed_short_kbps, String.format(Locale.US, "%.0f", bps / kb))
        else -> stringResource(R.string.lino_speed_short_bps, bps)
    }
}

@Composable
private fun formatLocalizedVolume(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    return when {
        bytes >= gb -> stringResource(R.string.lino_vol_gb, String.format(Locale.US, "%.2f", bytes / gb))
        bytes >= mb -> stringResource(R.string.lino_vol_mb, String.format(Locale.US, "%.1f", bytes / mb))
        bytes >= kb -> stringResource(R.string.lino_vol_kb, String.format(Locale.US, "%.0f", bytes / kb))
        else -> stringResource(R.string.lino_vol_b, bytes)
    }
}
