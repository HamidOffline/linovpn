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
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

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
            try {
                val outFile = File(context.cacheDir, "linovpn_update.apk")
                if (outFile.exists()) outFile.delete()

                val downloaded = downloadFile(downloadUrl, outFile, useLocalProxy = false)
                    || downloadFile(downloadUrl, outFile, useLocalProxy = true)

                if (!downloaded || !outFile.exists() || outFile.length() < 1024L) {
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
        var currentUrl = urlStr
        repeat(6) {
            val url = URL(currentUrl)
            val conn = if (useLocalProxy) {
                val httpPort = SettingsManager.getHttpPort()
                val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(AppConfig.LOOPBACK, httpPort))
                url.openConnection(proxy) as HttpURLConnection
            } else {
                url.openConnection() as HttpURLConnection
            }
            conn.instanceFollowRedirects = false
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.setRequestProperty("User-Agent", "LinoVPN/${BuildConfig.VERSION_NAME}")
            val code = conn.responseCode
            if (code in 300..399) {
                val loc = conn.getHeaderField("Location") ?: return false
                conn.disconnect()
                currentUrl = loc
                return@repeat
            }
            if (code != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                return false
            }
            val total = conn.contentLengthLong
            conn.inputStream.use { input ->
                FileOutputStream(target).use { output ->
                    val buf = ByteArray(16 * 1024)
                    var readTotal = 0L
                    var read = input.read(buf)
                    while (read != -1) {
                        output.write(buf, 0, read)
                        readTotal += read
                        if (total > 0L) {
                            _downloadProgress.value = ((readTotal * 100L) / total).toInt().coerceIn(0, 100)
                        }
                        read = input.read(buf)
                    }
                }
            }
            conn.disconnect()
            return true
        }
        return false
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }
}
