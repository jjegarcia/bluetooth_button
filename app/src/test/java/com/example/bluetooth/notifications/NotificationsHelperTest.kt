package com.example.bluetooth.notifications

import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import io.mockk.*
import org.junit.Test

class NotificationsHelperTest {
    private val mockBuildWrapper: BuildWrapper = mockk()
    private val mockNotificationChannelWrapper: NotificationChannelWrapper = mockk()
    private val mockNotificationManagerWrapper: NotificationManagerWrapper = mockk()
    private val mockIntentWrapper: IntentWrapper = mockk(relaxed = true)
    private val mockPendingIntentWrapper: PendingIntentWrapper = mockk(relaxed = true)
    private val mockBuilderWrapper: BuilderWrapper = mockk(relaxed = true)
    private val mockNotificationManagerCompatWrapper: NotificationManagerCompatWrapper = mockk(relaxed = true)

    private val mockChannel: NotificationChannel = mockk(relaxed = true)

    val subject = NotificationHelper(
        buildWrapper = mockBuildWrapper,
        notificationChannelWrapper = mockNotificationChannelWrapper,
        notificationManagerWrapper = mockNotificationManagerWrapper,
        intentWrapper = mockIntentWrapper,
        pendingIntentWrapper = mockPendingIntentWrapper,
        builderWrapper = mockBuilderWrapper,
        notificationManagerCompatWrapper = mockNotificationManagerCompatWrapper
    )
    private val mockContext: Context = mockk(relaxed = true)


    @Test
    fun `Given notification initialised When version greater than 22 Then creates notification channel `() {
        every { mockBuildWrapper.getVersion() } returns Build.VERSION_CODES.O
        every { mockNotificationChannelWrapper.setChannel(any(), any(), any()) } just Runs
        every { mockNotificationChannelWrapper.setDescription(any()) } just Runs
        every { mockNotificationChannelWrapper.setShowBadge(any()) } just Runs
        every { mockNotificationChannelWrapper.getChannel() } returns mockChannel

        every { mockNotificationManagerWrapper.initialise(any()) } just Runs
        every { mockNotificationManagerWrapper.createNotificationChannel(mockChannel) } just Runs

        subject.initialise(mockContext)

        verify { mockNotificationManagerWrapper.createNotificationChannel(mockChannel) }
    }

    @Test
    fun `Given notification initialised When version not greater than 22 Then creates notification channel `() {
        every { mockBuildWrapper.getVersion() } returns Build.VERSION_CODES.O - 1
        every { mockNotificationChannelWrapper.setChannel(any(), any(), any()) } just Runs
        every { mockNotificationChannelWrapper.setDescription(any()) } just Runs
        every { mockNotificationChannelWrapper.setShowBadge(any()) } just Runs
        every { mockNotificationChannelWrapper.getChannel() } returns mockChannel

        every { mockNotificationManagerWrapper.initialise(any()) } just Runs
        every { mockNotificationManagerWrapper.createNotificationChannel(mockChannel) } just Runs

        subject.initialise(mockContext)

        verify(exactly = 0) { mockNotificationManagerWrapper.createNotificationChannel(mockChannel) }
    }

    @Test
    fun `Given notification created Then delegates creation`() {
        every { mockBuildWrapper.getVersion() } returns Build.VERSION_CODES.O
        every { mockNotificationChannelWrapper.setChannel(any(), any(), any()) } just Runs
        every { mockNotificationChannelWrapper.setDescription(any()) } just Runs
        every { mockNotificationChannelWrapper.setShowBadge(any()) } just Runs
        every { mockNotificationChannelWrapper.getChannel() } returns mockChannel

        every { mockNotificationManagerWrapper.initialise(any()) } just Runs
        every { mockNotificationManagerWrapper.createNotificationChannel(mockChannel) } just Runs

        every { mockBuilderWrapper.initialise(mockContext,any())  } just Runs
        every { mockBuilderWrapper.setPriority(any())  } just Runs

        subject.initialise(mockContext)
        subject.createAlertNotification()

        verify { mockNotificationManagerCompatWrapper.notify(any(),any()) }
    }

}