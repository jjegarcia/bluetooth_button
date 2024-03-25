package com.example.bluetooth.service

import com.example.bluetooth.comms.LoggerWrapper
import com.example.bluetooth.location.LocationHandler
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class MyServiceTest {
    private val loggerWrapper: LoggerWrapper = mockk(relaxed = true)
    private val mockLocationHandler: LocationHandler = mockk(relaxed = true)
    val subject = MyService()

    @Test
    fun `Given request to setup service Then opens location realtime service `() {

        subject.setUpService(mockLocationHandler, loggerWrapper)

        verify { loggerWrapper.log(any(),any()) }
        verify { mockLocationHandler.startLocationUpdate() }
    }
}