package com.example.bluetooth.service

import android.content.Context
import android.content.Intent
import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.comms.LoggerWrapper
import com.example.bluetooth.location.LocationHandler
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ServiceFactoryTest {
    private val mockContext: Context = mockk(relaxed = true)
    private val mockLocationHandler: LocationHandler = mockk(relaxed = true)
    private val mockBluetoothComponent: BluetoothComponent = mockk(relaxed = true)
    private val mockLoggerWrapper: LoggerWrapper = mockk(relaxed = true)

    val subject = ServiceFactory(
        mockContext,
        mockLocationHandler,
        mockBluetoothComponent,
        mockLoggerWrapper
    )

    @Test
    fun `Given  service start request Then binds  service`() {
        subject.start()
        mockContext.bindService(
            Intent(mockContext, MyService::class.java),
            subject.mConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    @Test
    fun `Given  service stopped request Then stops service `() {
        subject.stop()
        mockContext.stopService(Intent(mockContext, MyService::class.java))
    }

    @Test
    fun `Given  service disconnected request Then unbinds service `() {
        subject.mConnection.onServiceDisconnected(mockk())
        assertEquals(null, subject.myService)
    }
}