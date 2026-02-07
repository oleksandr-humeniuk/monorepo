package com.oho.hiit_timer.count_down_screen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    const val CHANNEL_ID = "hiit_timer"
    const val NOTIF_ID = 1001

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "HIIT Timer",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Keeps HIIT timer running in background"
                    setShowBadge(false)
                }
            )
        }
    }

    fun build(ctx: Context, title: String, text: String, ongoing: Boolean): Notification {
        return NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play) // replace with your icon
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }

    fun notify(ctx: Context, title: String, text: String, ongoing: Boolean) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, build(ctx, title, text, ongoing))
    }
}
