package com.example.bluetooth.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bluetooth.MainActivity
import com.example.bluetooth.R
import com.google.firebase.inappmessaging.display.R.*

private const val ALERT_NOTIFICATION_ID = 1001
private const val FOREGROUND_NOTIFICATION_ID = 1002

class NotificationHelper constructor(
    val buildWrapper: BuildWrapper,
    val notificationChannelWrapper: NotificationChannelWrapper,
    val notificationManagerWrapper: NotificationManagerWrapper,
    val intentWrapper: IntentWrapper,
    val pendingIntentWrapper: PendingIntentWrapper,
    val builderWrapper: BuilderWrapper,
    val notificationManagerCompatWrapper: NotificationManagerCompatWrapper
) : NotificationHelperI {

    private lateinit var mContext: Context

    @SuppressLint("NewApi")
    private fun createNotificationChannel(
        context: Context,
        importance: Int,
        showBadge: Boolean,
        name: String,
        description: String,
        channelId: String
    ) {
        if (buildWrapper.getVersion() >= Build.VERSION_CODES.O) {
            notificationChannelWrapper.setChannel(
                channelId = channelId,
                name = name,
                importance = importance
            )
            notificationChannelWrapper.setDescription(description = description)
            notificationChannelWrapper.setShowBadge(showBadge)

            notificationManagerWrapper.initialise(context)
            notificationManagerWrapper.createNotificationChannel(notificationChannelWrapper.getChannel())
        }
        builderWrapper.initialise(context, channelId)
    }

    @SuppressLint("MissingPermission")
    private fun createDataNotification(
        context: Context,
        title: String,
        message: String,
        bigText: CharSequence,
        autoCancel: Boolean,
        iconRes: Int,
        id: Int
    ) {
        builderWrapper.apply {
            setSmallIcon(iconRes)
            setContentTitle(title)
            setContentText(message)
            setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
            setAutoCancel(autoCancel)

            intentWrapper.initialise(context)
            intentWrapper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)


            setContentIntent(
                pendingIntentWrapper.getActivity(
                    context,
                    0,
                    intentWrapper.getIntent(),
                    FLAG_IMMUTABLE
                )
            )
        }
        notificationManagerCompatWrapper.notify(id, builderWrapper.fetchBuilder().build())
    }

    override fun initialise(context: Context) {
        mContext = context
        createNotificationChannel(
            context = context,
            importance = NotificationManagerCompat.IMPORTANCE_DEFAULT,
            showBadge = false,
            name = context.getString(R.string.app_name),
            description = "App notification channel.",
            channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
        )
    }

    override fun createAlertNotification() {
        createDataNotification(
            context = mContext,
            title = "Ford Assurance",
            message = "You can trust we are here to help",
            bigText = "Device Trigger",
            autoCancel = true,
            iconRes = drawable.abc_ic_star_black_36dp,
            id = ALERT_NOTIFICATION_ID
        )
    }

    override fun createForegroundNotification(): Notification {
        return builderWrapper.fetchBuilder().build()
    }
}

class BuildWrapper {
    fun getVersion(): Int = Build.VERSION.SDK_INT
}

class NotificationChannelWrapper {
    private lateinit var channel: NotificationChannel

    @SuppressLint("NewApi")
    fun setChannel(channelId: String, name: String, importance: Int) {
        channel = NotificationChannel(channelId, name, importance)
    }

    fun setDescription(description: String) {
        channel.description = description
    }

    @SuppressLint("NewApi")
    fun setShowBadge(showBadge: Boolean) {
        channel.setShowBadge(showBadge)
    }

    @JvmName("getChannel1")
    fun getChannel(): NotificationChannel = channel

}

class NotificationManagerWrapper {
    private lateinit var notificationManager: NotificationManager

    @SuppressLint("NewApi")
    fun createNotificationChannel(channel: NotificationChannel) {
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("NewApi")
    fun initialise(context: Context) {
        notificationManager = context.getSystemService(NotificationManager::class.java)
    }
}

class IntentWrapper {
    private lateinit var intent: Intent
    fun initialise(context: Context) {
        intent = Intent(context, MainActivity::class.java)
    }

    fun getIntent() = intent
    fun setFlags(flags: Int) {
        intent.flags = flags
    }
}

class PendingIntentWrapper {
    fun getActivity(context: Context, requestCode: Int, intent: Intent, flags: Int): PendingIntent =
        PendingIntent.getActivity(context, requestCode, intent, flags)
}

class BuilderWrapper {

    private lateinit var builder: NotificationCompat.Builder

    fun initialise(context: Context, channelId: String) {
        builder = NotificationCompat.Builder(context, channelId)
    }

    fun fetchBuilder(): NotificationCompat.Builder = builder
    fun setSmallIcon(iconRes: Int) = builder.setSmallIcon(iconRes)
    fun setContentTitle(title: String) = builder.setContentTitle(title)
    fun setContentText(message: String) = builder.setContentText(message)
    fun setStyle(style: NotificationCompat.BigTextStyle) = builder.setStyle(style)
    fun setPriority(priority: Int) {
        builder.priority = priority
    }

    fun setAutoCancel(autoCancel: Boolean) = builder.setAutoCancel(autoCancel)
    fun setContentIntent(activity: PendingIntent) = builder.setContentIntent(activity)
}

class NotificationManagerCompatWrapper(val context: Context) {
    @SuppressLint("MissingPermission")
    fun notify(id: Int, build: Notification) {
        NotificationManagerCompat.from(context).notify(id, build)
    }
}
