package com.example.bluetooth.messaging

import android.content.Context
import android.widget.Toast
import io.mockk.*
import org.junit.Test

class ToastWrapperTest {

    private val context: Context = mockk(relaxed = true)
    val subject = ToastWrapper(context)

    @Test
    fun makeText() {
        mockkStatic(Toast::class)
        every { Toast.makeText(context,"abc",20).show() } just Runs
         subject.makeText("abc", 20)
        verify { Toast.makeText(context, "abc", 20).show() }
    }
}