package ir.linovpn.xray

import android.content.Context
import android.provider.Settings
import android.util.Base64
import go.Seq
import ir.linovpn.common.EngineResult
import ir.linovpn.common.EngineStats
import ir.linovpn.common.TunConfig
import ir.linovpn.common.VpnEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

class XrayEngine : VpnEngine {

    companion object {
        @Volatile
        private var coreController: CoreController? = null
        private val isRunningFlag = AtomicBoolean(false)
        private val isEnvInitialized = AtomicBoolean(false)

        fun initCore(context: Context) {
            if (isEnvInitialized.compareAndSet(false, true)) {
                try {
                    Seq.setContext(context.applicationContext)
                    val assetDir = context.getDir("assets", Context.MODE_PRIVATE)
                    copyAssets(context, assetDir)

                    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                        ?: "default_linovpn_device_id"
                    val deviceIdBytes = androidId.toByteArray(Charsets.UTF_8).copyOf(32)
                    val deviceId = Base64.encodeToString(deviceIdBytes, Base64.NO_PADDING or Base64.URL_SAFE).trim()

                    Libv2ray.initCoreEnv(assetDir.absolutePath, deviceId)
                } catch (e: Throwable) {
                    isEnvInitialized.set(false)
                }
            }
        }

        private fun copyAssets(context: Context, targetDir: File) {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val assetNames = listOf("geoip.dat", "geosite.dat", "geoip-only-cn-private.dat")
            val existing = targetDir.list()?.toSet() ?: emptySet()
            for (name in assetNames) {
                val targetFile = File(targetDir, name)
                if (!existing.contains(name) || targetFile.length() == 0L) {
                    try {
                        context.assets.open(name).use { input ->
                            FileOutputStream(targetFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    } catch (_: Throwable) {}
                }
            }
        }

        fun measureDelay(configJson: String, testUrl: String = "https://www.google.com/generate_204"): Long {
            return try {
                Libv2ray.measureOutboundDelay(configJson, testUrl)
            } catch (e: Throwable) {
                -1L
            }
        }
    }

    override val name: String = "Xray-core"
    override val version: String
        get() = runCatching { Libv2ray.checkVersionX() }.getOrDefault("Xray-core v2.3.11")

    private val logs = mutableListOf<String>()

    override fun isLibraryLoaded(): Boolean = true

    override suspend fun start(configJsonOrYaml: String, tunConfig: TunConfig?): EngineResult = withContext(Dispatchers.IO) {
        try {
            val fd = tunConfig?.fd ?: -1
            android.util.Log.i("LinoVPN", "XrayEngine: start called with tun fd=$fd")
            android.util.Log.d("LinoVPN", "XrayEngine config JSON:\n$configJsonOrYaml")
            logs.add("در حال راه‌اندازی هسته رسمی Xray...")

            val handler = object : CoreCallbackHandler {
                override fun onEmitStatus(status: Long, msg: String?): Long {
                    android.util.Log.d("LinoVPN", "[Xray Status $status] $msg")
                    msg?.let { logs.add("[Xray] $it") }
                    return 0
                }
                override fun startup(): Long {
                    android.util.Log.i("LinoVPN", "[Xray] Core started.")
                    logs.add("[Xray] Core started.")
                    return 0
                }
                override fun shutdown(): Long {
                    android.util.Log.i("LinoVPN", "[Xray] Core shutdown.")
                    logs.add("[Xray] Core shutdown.")
                    return 0
                }
            }

            // Clean up any stale running controller
            runCatching {
                coreController?.stopLoop()
                coreController = null
            }

            val controller = Libv2ray.newCoreController(handler)
            coreController = controller
            controller.startLoop(configJsonOrYaml, fd)
            isRunningFlag.set(true)
            android.util.Log.i("LinoVPN", "XrayEngine: startLoop completed successfully, isRunning=true")
            logs.add("هسته Xray فعال شد و ترافیک شبکه به تونل امن متصل گردید.")
            EngineResult.Success
        } catch (e: Throwable) {
            android.util.Log.e("LinoVPN", "XrayEngine start failed: ${e.message}", e)
            logs.add("خطا در راه‌اندازی Xray: ${e.message}")
            isRunningFlag.set(false)
            EngineResult.Failure("Xray start failed: ${e.message}", e)
        }
    }

    override suspend fun stop(): EngineResult = withContext(Dispatchers.IO) {
        try {
            logs.add("در حال بستن تونل Xray...")
            coreController?.stopLoop()
            coreController = null
            isRunningFlag.set(false)
            logs.add("هسته Xray با موفقیت متوقف شد.")
            EngineResult.Success
        } catch (e: Throwable) {
            isRunningFlag.set(false)
            EngineResult.Failure("Xray stop failed: ${e.message}", e)
        }
    }

    override fun isRunning(): Boolean {
        return coreController?.isRunning ?: isRunningFlag.get()
    }

    override fun getStats(): EngineStats {
        return try {
            val statsJson = coreController?.queryAllOutboundTrafficStats() ?: ""
            if (statsJson.isNotEmpty()) {
                val json = JSONObject(statsJson)
                val up = json.optLong("uplink", 0L)
                val down = json.optLong("downlink", 0L)
                EngineStats(uploadBytes = up, downloadBytes = down)
            } else {
                EngineStats()
            }
        } catch (e: Throwable) {
            EngineStats()
        }
    }

    override fun getLogs(): List<String> = logs.takeLast(100)

    override fun clearLogs() {
        logs.clear()
    }
}
