package com.example.bluetooth.messaging

import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test

class FirebaseMessagingWrapperTest {
    private val onCompleteListenerWrapper:OnCompleteListenerWrapper= mockk(relaxed = true)

    val subject= FirebaseMessagingWrapper(onCompleteListenerWrapper = onCompleteListenerWrapper)
    @Test
    fun getInstance() {
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns mockk(relaxed = true)
        subject.getInstance()
        verify { FirebaseMessaging.getInstance() }
    }

    @Test
    fun getTask() {
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns mockk(relaxed = true)
        every { FirebaseMessaging.getInstance().token } returns mockk(relaxed = true)

        subject.getTask(FirebaseMessaging.getInstance())
        verify { FirebaseMessaging.getInstance().token}
    }

    @Test
    fun addOnCompleteListener() {
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns mockk(relaxed = true)
        every { FirebaseMessaging.getInstance().token } returns mockk(relaxed = true)
        every { FirebaseMessaging.getInstance().token.addOnCompleteListener(any()) } returns mockk(relaxed = true)

        subject.addOnCompleteListener(task = FirebaseMessaging.getInstance().token, block = {})
        verify { FirebaseMessaging.getInstance().token.addOnCompleteListener(any())}
    }
}