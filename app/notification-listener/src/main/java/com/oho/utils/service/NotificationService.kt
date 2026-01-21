package com.oho.utils.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // log notification
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // optional
    }
}
