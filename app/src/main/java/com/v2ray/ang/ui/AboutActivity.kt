package com.v2ray.ang.ui

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.v2ray.ang.AppConfig
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.R
import com.v2ray.ang.core.CoreNativeManager
import com.v2ray.ang.ui.base.BaseComponentActivity
import com.v2ray.ang.ui.checkupdate.CheckUpdateActivity
import com.v2ray.ang.ui.compose.AppDivider
import com.v2ray.ang.ui.compose.AppTopBar
import com.v2ray.ang.ui.compose.LocalDarkTheme
import com.v2ray.ang.ui.compose.NavigationBarsSpacer
import com.v2ray.ang.ui.compose.SettingsMenuItem
import com.v2ray.ang.ui.compose.VersionInfoBlock
import com.v2ray.ang.util.Utils

class AboutActivity : BaseComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun ScreenContent() {
        AboutScreen(
            onBackClick = { finish() },
            onCheckUpdateClick = {
                startActivity(Intent(this, CheckUpdateActivity::class.java))
            }
        )
    }
}

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onCheckUpdateClick: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalDarkTheme.current
    var showOssDialog by remember { mutableStateOf(false) }

    val libVersion = CoreNativeManager.getLibVersion()
    val versionText = "v${BuildConfig.VERSION_NAME} ($libVersion)"
    val appIdText = BuildConfig.APPLICATION_ID

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_about),
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(
                            if (isDark) R.drawable.ic_lino_logo_dark else R.drawable.ic_lino_logo_light
                        ),
                        contentDescription = stringResource(R.string.app_name),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(82.dp)
                            .fillMaxWidth(0.68f)
                    )
                    Text(
                        text = stringResource(R.string.lino_version_badge, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.lino_about_tagline),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            SettingsMenuItem(
                icon = painterResource(R.drawable.ic_check_update_24dp),
                title = stringResource(R.string.lino_about_update),
                subtitle = stringResource(R.string.lino_about_update_sub),
                onClick = onCheckUpdateClick
            )
            AppDivider()
            SettingsMenuItem(
                icon = painterResource(R.drawable.ic_telegram_24dp),
                title = stringResource(R.string.lino_about_channel),
                subtitle = stringResource(R.string.lino_about_channel_sub),
                onClick = { Utils.openUri(context, AppConfig.TG_CHANNEL_URL) }
            )
            SettingsMenuItem(
                icon = painterResource(R.drawable.ic_feedback_24dp),
                title = stringResource(R.string.lino_about_support),
                subtitle = stringResource(R.string.lino_about_support_sub),
                onClick = { Utils.openUri(context, AppConfig.SUPPORT_TELEGRAM_URL) }
            )
            SettingsMenuItem(
                icon = painterResource(R.drawable.ic_source_code_24dp),
                title = stringResource(R.string.lino_about_github),
                subtitle = stringResource(R.string.lino_about_github_sub),
                onClick = { Utils.openUri(context, AppConfig.APP_RELEASES_URL) }
            )
            AppDivider()
            SettingsMenuItem(
                icon = painterResource(R.drawable.license_24px),
                title = stringResource(R.string.title_oss_license),
                onClick = { showOssDialog = true }
            )
            Spacer(Modifier.height(8.dp))
            VersionInfoBlock(
                versionText = versionText,
                appIdText = appIdText
            )
            NavigationBarsSpacer()
        }
    }

    if (showOssDialog) {
        AlertDialog(
            onDismissRequest = { showOssDialog = false },
            title = { Text(stringResource(R.string.title_oss_license)) },
            text = {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            loadUrl("file:///android_asset/open_source_licenses.html")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = { showOssDialog = false }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(bottom = 60.dp)
        )
    }
}
