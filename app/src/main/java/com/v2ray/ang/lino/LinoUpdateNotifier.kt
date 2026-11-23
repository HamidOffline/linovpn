package com.v2ray.ang.lino

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.v2ray.ang.R
import com.v2ray.ang.handler.AppLocaleManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.ui.checkupdate.CheckUpdateActivity

object LinoUpdateNotifier {

    private const val CHANNEL_ID = "linovpn_updates"
    private const val NOTIFICATION_ID = 30401
    private const val PREF_LAST_NOTIFIED_VERSION = "lino_last_notified_update_version"

    fun notifyIfNew(context: Context, latestVersion: String) {
        if (latestVersion.isBlank()) return
        val last = MmkvManager.decodeSettingsString(PREF_LAST_NOTIFIED_VERSION, "").orEmpty()
        if (last == latestVersion) return
        MmkvManager.encodeSettings(PREF_LAST_NOTIFIED_VERSION, latestVersion)
        showUpdateNotification(context, latestVersion)
    }

    fun showUpdateNotification(context: Context, latestVersion: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val loc = AppLocaleManager.localizedContext(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                loc.getString(R.string.lino_update_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(context, CheckUpdateActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, flags)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(loc.getString(R.string.lino_update_notif_title, latestVersion))
            .setContentText(loc.getString(R.string.lino_update_notif_body))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(loc.getString(R.string.lino_update_notif_body))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching { nm.notify(NOTIFICATION_ID, notification) }
    }
}
