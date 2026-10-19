package ir.linovpn.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import libv2ray.Libv2ray

@HiltAndroidApp
class LinoVpnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        runCatching {
            Libv2ray.initCoreEnv(filesDir.absolutePath, filesDir.absolutePath)
        }
    }
}
