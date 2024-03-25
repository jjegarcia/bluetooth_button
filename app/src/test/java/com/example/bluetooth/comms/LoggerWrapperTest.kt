package com.example.bluetooth.comms

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test

class LoggerWrapperTest {

    val subject = LoggerWrapper()

    @Test
    fun log() {
        mockkStatic(Log::class)
        every { Log.i("tag","abc") } returns 2
        subject.log("tag", "abc")
        verify { Log.i("tag", "abc") }
    }
}