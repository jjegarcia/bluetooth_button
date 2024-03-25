package com.example.bluetooth.notifications

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat

interface NotificationHelperI {
    fun createAlertNotification()
    fun createForegroundNotification(): Notification
    fun initialise(context: Context)

}