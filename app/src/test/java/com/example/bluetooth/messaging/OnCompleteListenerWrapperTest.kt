package com.example.bluetooth.messaging

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test

class OnCompleteListenerWrapperTest {

    private val subject = OnCompleteListenerWrapper()

    @Test
    fun onCompleteListener() {
        mockkStatic(OnCompleteListener::class)

        val mockBlock: (Task<String>) -> Unit = mockk(relaxed = true)
        subject.onCompleteListener((mockBlock))

        verify { OnCompleteListener(function = mockBlock) }

    }
}