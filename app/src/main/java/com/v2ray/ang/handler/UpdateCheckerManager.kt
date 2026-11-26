package com.v2ray.ang.handler

import android.os.Build
import com.v2ray.ang.AppConfig
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.dto.CheckUpdateResult
import com.v2ray.ang.dto.GitHubRelease
import com.v2ray.ang.dto.UrlContentRequest
import com.v2ray.ang.extension.concatUrl
import com.v2ray.ang.util.HttpUtil
import com.v2ray.ang.util.JsonUtil
import com.v2ray.ang.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateCheckerManager {
    suspend fun checkForUpdate(includePreRelease: Boolean = false): CheckUpdateResult = withContext(Dispatchers.IO) {
        val url = if (includePreRelease) {
            AppConfig.APP_API_URL
        } else {
            AppConfig.APP_API_URL.concatUrl("latest")
        }

        val proxyUsername = SettingsManager.getSocksUsername()
        val proxyPassword = SettingsManager.getSocksPassword()
        val httpPort = SettingsManager.getHttpPort()
        val localProxyActive = isLocalProxyReachable(httpPort)

        var response: String? = null
        val tryProxyFirst = localProxyActive && httpPort > 0
        val order = if (tryProxyFirst) listOf(httpPort, 0) else listOf(0, httpPort)
        for (port in order) {
            if (port == 0 && !tryProxyFirst && response != null) break
            if (port > 0 && !localProxyActive) continue
            response = HttpUtil.getUrlContent(
                UrlContentRequest(
                    url = url,
                    timeout = 8000,
                    httpPort = port,
                    proxyUsername = if (port > 0) proxyUsername else null,
                    proxyPassword = if (port > 0) proxyPassword else null
                )
            )
            if (!response.isNullOrEmpty()) break
        }
        if (response.isNullOrEmpty()) {
            throw IllegalStateException("Failed to get response")
        }

        val latestRelease = if (includePreRelease) {
            JsonUtil.fromJsonSafe(response, Array<GitHubRelease>::class.java)
                ?.firstOrNull()
                ?: throw IllegalStateException("No pre-release found")
        } else {
            JsonUtil.fromJsonSafe(response, GitHubRelease::class.java)
        }
        if (latestRelease == null) {
            return@withContext CheckUpdateResult(hasUpdate = false)
        }

        val latestVersion = latestRelease.tagName.removePrefix("v")
        LogUtil.i(
            AppConfig.TAG,
            "Found new version: $latestVersion (current: ${BuildConfig.VERSION_NAME})"
        )

        return@withContext if (compareVersions(latestVersion, BuildConfig.VERSION_NAME) > 0) {
            val downloadUrl = getDownloadUrl(latestRelease, Build.SUPPORTED_ABIS[0])
            CheckUpdateResult(
                hasUpdate = true,
                latestVersion = latestVersion,
                releaseNotes = latestRelease.body,
                downloadUrl = downloadUrl,
                isPreRelease = latestRelease.prerelease
            )
        } else {
            CheckUpdateResult(hasUpdate = false)
        }
    }

    private fun compareVersions(version1: String, version2: String): Int {
        // Compare all numeric segments so suffixed tags like "2.3.2-P1" work:
        // "2.3.2-P1" -> [2,3,2,1], "2.3.2" -> [2,3,2], "2.3.2-P2" > "2.3.2-P1" > "2.3.2"
        val numberRegex = Regex("\\d+")
        val v1 = numberRegex.findAll(version1).map { it.value.toInt() }.toList()
        val v2 = numberRegex.findAll(version2).map { it.value.toInt() }.toList()

        for (i in 0 until maxOf(v1.size, v2.size)) {
            val num1 = v1.getOrElse(i) { 0 }
            val num2 = v2.getOrElse(i) { 0 }
            if (num1 != num2) return num1 - num2
        }
        return 0
    }

    private fun getDownloadUrl(release: GitHubRelease, abi: String): String {
        val apkAssets = release.assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        val exactAbi = apkAssets.firstOrNull {
            it.name.contains(abi, ignoreCase = true) && !it.name.contains("fdroid", ignoreCase = true)
        }
        val universal = apkAssets.firstOrNull {
            it.name.contains("universal", ignoreCase = true)
        }
        val anyApk = apkAssets.firstOrNull()

        return (exactAbi ?: universal ?: anyApk)?.browserDownloadUrl
            ?: throw IllegalStateException("No compatible APK found")
    }

    internal fun isLocalProxyReachable(port: Int): Boolean {
        if (port <= 0) return false
        return try {
            java.net.Socket().use { sock ->
                sock.connect(java.net.InetSocketAddress(AppConfig.LOOPBACK, port), 150)
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
