package com.example.bluetooth.comms

import android.os.Handler
import io.mockk.*

import org.junit.Test

class HandlerWrapperTest {
    private val looperWrapper: LooperWrapper = mockk(relaxed = true)
    val subject = HandlerWrapper(looperWrapper)

    @Test
    fun post() {
        val mockBlock: () -> Unit= mockk(relaxed = true)
        every { looperWrapper.getMainLooper() } returns mockk()


        mockkStatic(android.os.Handler::class)

        every { Handler(looperWrapper.getMainLooper()).post (mockBlock) } returns true

        subject.post (block = mockBlock)

//        verify { Handler(looperWrapper.getMainLooper()).post(mockBlock) }
    }
}