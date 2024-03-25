package com.example.bluetooth.comms

import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test

class LooperWrapperTest {

    val subject = LooperWrapper()

    @Test
    fun getMainLooper() {

        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        subject.getMainLooper()
        verify { Looper.getMainLooper() }
    }
}