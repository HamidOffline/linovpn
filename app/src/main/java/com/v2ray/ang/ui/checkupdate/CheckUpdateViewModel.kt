package com.v2ray.ang.ui.checkupdate

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.AppConfig
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.R
import com.v2ray.ang.dto.CheckUpdateResult
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsManager
import com.v2ray.ang.handler.UpdateCheckerManager
import com.v2ray.ang.ui.base.BaseViewModel
import com.v2ray.ang.util.LogUtil
import com.v2ray.ang.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class CheckUpdateViewModel(application: Application) : BaseViewModel(application) {

    private val _checkPreRelease = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_CHECK_UPDATE_PRE_RELEASE, false)
    )
    val checkPreRelease: StateFlow<Boolean> = _checkPreRelease.asStateFlow()

    private val _updateResult = MutableStateFlow<CheckUpdateResult?>(null)
    val updateResult: StateFlow<CheckUpdateResult?> = _updateResult.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Int?>(null)
    val downloadProgress: StateFlow<Int?> = _downloadProgress.asStateFlow()

    fun toggleCheckPreRelease(enabled: Boolean) {
        _checkPreRelease.value = enabled
        MmkvManager.encodeSettings(AppConfig.PREF_CHECK_UPDATE_PRE_RELEASE, enabled)
    }

    fun checkForUpdates() {
        launchLoading {
            try {
                val result = UpdateCheckerManager.checkForUpdate(_checkPreRelease.value)
                if (result.hasUpdate) {
                    _updateResult.value = result
                    _showUpdateDialog.value = true
                } else {
                    toastSuccess(R.string.update_already_latest_version)
                }
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Failed to check for updates", e)
                toastError(R.string.toast_failure)
            }
        }
    }

    fun downloadAndInstall(context: Context, downloadUrl: String) {
        if (_downloadProgress.value != null) return
        _showUpdateDialog.value = false
        _downloadProgress.value = 0

        viewModelScope.launch(Dispatchers.IO) {
            val partFile = File(context.cacheDir, "linovpn_update.apk.part")
            val outFile = File(context.cacheDir, "linovpn_update.apk")
            try {
                if (partFile.exists()) partFile.delete()
                if (outFile.exists()) outFile.delete()

                val httpPort = SettingsManager.getHttpPort()
                val proxyActive = UpdateCheckerManager.isLocalProxyReachable(httpPort)
                // Because VPN excludes our own app via addDisallowedApplication, use local proxy FIRST when connected!
                val attempts = if (proxyActive) listOf(true, false) else listOf(false, true)

                var downloaded = false
                for (useProxy in attempts) {
                    if (partFile.exists()) partFile.delete()
                    _downloadProgress.value = 0
                    val ok = runCatching {
                        downloadFile(downloadUrl, partFile, useLocalProxy = useProxy)
                    }.getOrElse { err ->
                        LogUtil.w(AppConfig.TAG, "Update download attempt (proxy=$useProxy) failed: ${err.message}")
                        false
                    }
                    if (ok && partFile.exists() && partFile.length() >= 100_000L) {
                        downloaded = true
                        break
                    }
                }

                if (!downloaded || !partFile.renameTo(outFile)) {
                    if (partFile.exists()) partFile.delete()
                    withContext(Dispatchers.Main) {
                        _downloadProgress.value = null
                        toastError(R.string.lino_update_download_failed)
                        Utils.openUri(context, downloadUrl)
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    _downloadProgress.value = null
                    toastSuccess(R.string.lino_update_installing)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${BuildConfig.APPLICATION_ID}.cache",
                        outFile
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                if (partFile.exists()) partFile.delete()
                LogUtil.e(AppConfig.TAG, "In-app update download/install failed", e)
                withContext(Dispatchers.Main) {
                    _downloadProgress.value = null
                    toastError(R.string.lino_update_download_failed)
                    Utils.openUri(context, downloadUrl)
                }
            }
        }
    }

    private fun downloadFile(urlStr: String, target: File, useLocalProxy: Boolean): Boolean {
        val builder = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)

        val proxyUsername = SettingsManager.getSocksUsername()
        val proxyPassword = SettingsManager.getSocksPassword()

        if (useLocalProxy) {
            val httpPort = SettingsManager.getHttpPort()
            if (httpPort <= 0) return false
            builder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(AppConfig.LOOPBACK, httpPort)))
            if (!proxyUsername.isNullOrBlank() && !proxyPassword.isNullOrBlank()) {
                builder.proxyAuthenticator { _, response ->
                    if (response.request.header("Proxy-Authorization") != null) {
                        null
                    } else {
                        response.request.newBuilder()
                            .header("Proxy-Authorization", Credentials.basic(proxyUsername, proxyPassword))
                            .build()
                    }
                }
            }
        }

        val client = builder.build()
        val request = Request.Builder()
            .url(urlStr)
            .get()
            .header("User-Agent", "LinoVPN/${BuildConfig.VERSION_NAME}")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                LogUtil.w(AppConfig.TAG, "Update download HTTP ${response.code} (proxy=$useLocalProxy)")
                return false
            }
            val body = response.body ?: return false
            val total = body.contentLength()
            var lastPct = -1
            var readTotal = 0L

            body.byteStream().use { input ->
                FileOutputStream(target).use { output ->
                    val buf = ByteArray(32 * 1024)
                    var read = input.read(buf)
                    while (read != -1) {
                        output.write(buf, 0, read)
                        readTotal += read
                        if (total > 0L) {
                            val pct = ((readTotal * 100L) / total).toInt().coerceIn(1, 100)
                            if (pct != lastPct) {
                                lastPct = pct
                                _downloadProgress.value = pct
                            }
                        }
                        read = input.read(buf)
                    }
                    output.flush()
                }
            }

            if (total > 0L && readTotal < total) {
                LogUtil.w(AppConfig.TAG, "Update download truncated ($readTotal/$total)")
                return false
            }
            return readTotal >= 100_000L
        }
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }
}
