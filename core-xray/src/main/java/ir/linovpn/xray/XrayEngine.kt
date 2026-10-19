package ir.linovpn.xray

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
import java.util.concurrent.atomic.AtomicBoolean

class XrayEngine : VpnEngine {

    companion object {
        @Volatile
        private var coreController: CoreController? = null
        private val isRunningFlag = AtomicBoolean(false)

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
        get() = runCatching { Libv2ray.checkVersionX() }.getOrDefault("Xray-core 25.x")

    private val logs = mutableListOf<String>()

    override fun isLibraryLoaded(): Boolean = true

    override suspend fun start(configJsonOrYaml: String, tunConfig: TunConfig?): EngineResult = withContext(Dispatchers.IO) {
        try {
            logs.add("در حال راه‌اندازی هسته رسمی Xray...")
            val fd = tunConfig?.fd ?: -1

            val handler = object : CoreCallbackHandler {
                override fun onEmitStatus(status: Long, msg: String?): Long {
                    msg?.let { logs.add("[Xray] $it") }
                    return 0
                }
                override fun startup(): Long {
                    logs.add("[Xray] Core started.")
                    return 0
                }
                override fun shutdown(): Long {
                    logs.add("[Xray] Core shutdown.")
                    return 0
                }
            }

            val controller = Libv2ray.newCoreController(handler)
            coreController = controller
            controller.startLoop(configJsonOrYaml, fd)
            isRunningFlag.set(true)
            logs.add("هسته Xray فعال شد و ترافیک شبکه به تونل امن متصل گردید.")
            EngineResult.Success
        } catch (e: Throwable) {
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
