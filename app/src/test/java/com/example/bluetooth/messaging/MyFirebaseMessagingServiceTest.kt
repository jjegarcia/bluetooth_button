package com.example.bluetooth.messaging

import android.content.ContentValues.TAG
import android.content.Context
import com.example.bluetooth.comms.LoggerWrapper
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Test
import java.lang.Exception

private const val EXPECTED_MESSAGE = "abc"

class MyFirebaseMessagingServiceTest {
    private val loggerWrapper: LoggerWrapper = mockk(relaxed = true)
    private val toastWrapper: ToastWrapper = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private val mockFirebaseMessagingWrapper: FirebaseMessagingWrapper = mockk()

    private val mockFirebaseMessaging: FirebaseMessaging = mockk()
    private val mockTask: Task<String> = mockk()

    private val subject = MyFirebaseMessagingService(
        loggerWrapper = loggerWrapper,
        firebaseMessagingWrapper = mockFirebaseMessagingWrapper,
        toastWrapper = toastWrapper,
        context = context
    )

    @Test
    fun `Given new token issued Then log its content`() {
        subject.onNewToken(EXPECTED_MESSAGE)
        verify { loggerWrapper.log(TAG, "Refreshed token: $EXPECTED_MESSAGE") }
    }

    @Test
    fun `Given token received When Successful Then saves its content`() {
        val taskSlot = slot<Task<String>>()
        val blockSlot = slot<(Task<String>) -> Unit>()
        every { mockTask.result } returns EXPECTED_MESSAGE
        every { mockTask.isSuccessful } returns true

        mockFirebaseMessagingWrapper.apply {
            every { getInstance() } returns mockFirebaseMessaging
            every { getTask(any()) } returns mockTask
            every {
                addOnCompleteListener(
                    task = capture(taskSlot),
                    block = capture(blockSlot)
                )
            } returns mockTask
        }

        every { loggerWrapper.log(any(), any()) } just Runs
        every { toastWrapper.makeText(any(), any()) } just Runs

        subject.setMessage()

        verify {
            subject.firebaseMessagingWrapper.addOnCompleteListener(
                task = taskSlot.captured,
                block = blockSlot.captured
            )
        }

        blockSlot.invoke(mockTask)

        assertEquals("abc", taskSlot.captured.result)

    }

    @Test
    fun `Given token received When Unsuccessful Then won't saves its content`() {
        val taskSlot = slot<Task<String>>()
        val blockSlot = slot<(Task<String>) -> Unit>()
        mockTask.apply {
            every { result } returns EXPECTED_MESSAGE
            every { isSuccessful } returns false
            every { exception } returns Exception()
        }
        mockFirebaseMessagingWrapper.apply {
            every { getInstance() } returns mockFirebaseMessaging
            every { getTask(any()) } returns mockTask
            every {
                addOnCompleteListener(
                    task = capture(taskSlot),
                    block = capture(blockSlot)
                )
            } returns mockTask
        }
        every { loggerWrapper.log(any(), any()) } just Runs
        every { toastWrapper.makeText(any(), any()) } just Runs

        subject.setMessage()

        verify {
            subject.firebaseMessagingWrapper.addOnCompleteListener(
                task = taskSlot.captured,
                block = blockSlot.captured
            )
        }

        blockSlot.invoke(mockTask)

        assertNotSame(EXPECTED_MESSAGE, subject.getMyToken())

    }
}