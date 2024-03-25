package com.example.bluetooth.comms

import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.core.app.NotificationManagerCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityCompactWrapperTest {
    val subject = ActivityCompactWrapper()
    private val mockContext: Context = mockk(relaxed = true)


    @Test
    fun checkSelfPermission() {
        mockkStatic(TextUtils::class)
        every { TextUtils.equals(any(), any()) } returns true

        mockkStatic(android.os.Process::class)
        every { android.os.Process.myPid() } returns mockk(relaxed = true)
        every { android.os.Process.myUid() } returns mockk(relaxed = true)

        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(mockContext).areNotificationsEnabled() } returns true


        assertEquals(
            PackageManager.PERMISSION_GRANTED,
            subject.checkSelfPermission(mockContext, android.Manifest.permission.POST_NOTIFICATIONS)
        )
    }
    @Test
    fun checkSelfPermissionDenied() {
        mockkStatic(TextUtils::class)
        every { TextUtils.equals(any(), any()) } returns true

        mockkStatic(android.os.Process::class)
        every { android.os.Process.myPid() } returns mockk(relaxed = true)
        every { android.os.Process.myUid() } returns mockk(relaxed = true)

        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(mockContext).areNotificationsEnabled() } returns false


        assertEquals(
            PackageManager.PERMISSION_DENIED,
            subject.checkSelfPermission(mockContext, android.Manifest.permission.POST_NOTIFICATIONS)
        )
    }
}