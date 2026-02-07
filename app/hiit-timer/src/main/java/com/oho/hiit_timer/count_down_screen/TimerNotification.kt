package com.oho.hiit_timer.count_down_screen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.oho.hiit_timer.HiitActivity
import com.oho.utils.R

object NotificationHelper {

    const val CHANNEL_ID = "hiit_timer"
    const val NOTIF_ID = 1001

    enum class Action { Open, Pause, Resume, Stop }

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "HIIT Timer",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Keeps HIIT timer running"
                    setShowBadge(false)
                }
            )
        }
    }

    fun build(
        ctx: Context,
        title: String,
        text: String,
        ongoing: Boolean,
        actions: List<Action>,
    ): Notification {
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // <-- replace with your icon
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(openUiPendingIntent(ctx))

        actions.forEach { a ->
            builder.addAction(toAction(ctx, a))
        }

        return builder.build()
    }

    fun notify(
        ctx: Context,
        title: String,
        text: String,
        ongoing: Boolean,
        actions: List<Action>,
    ) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, build(ctx, title, text, ongoing, actions))
    }

    private fun openUiPendingIntent(ctx: Context): PendingIntent {
        // Replace HiitRunActivity with your actual route/activity.
        val open = Intent(ctx, HiitActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getActivity(
            ctx,
            /*requestCode*/ 10,
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun toAction(ctx: Context, action: Action): NotificationCompat.Action {
        return when (action) {
            Action.Open -> NotificationCompat.Action(
                0,
                "Open",
                pendingServiceCmd(ctx, "open")
            )

            Action.Pause -> NotificationCompat.Action(
                0,
                "Pause",
                pendingServiceCmd(ctx, "pause_resume")
            )

            Action.Resume -> NotificationCompat.Action(
                0,
                "Resume",
                pendingServiceCmd(ctx, "pause_resume")
            )

            Action.Stop -> NotificationCompat.Action(
                0,
                "Stop",
                pendingServiceCmd(ctx, "stop")
            )
        }
    }

    private fun pendingServiceCmd(ctx: Context, cmd: String): PendingIntent {
        val i = Intent(ctx, HiitRunService::class.java).apply {
            putExtra("extra_cmd", cmd)
        }
        return PendingIntent.getService(
            ctx,
            /*requestCode*/ cmd.hashCode(),
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
