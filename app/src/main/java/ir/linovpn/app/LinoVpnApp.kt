package ir.linovpn.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ir.linovpn.xray.XrayEngine

@HiltAndroidApp
class LinoVpnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        runCatching {
            XrayEngine.initCore(this)
        }
    }
}
