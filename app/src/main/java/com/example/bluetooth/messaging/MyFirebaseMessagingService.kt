package com.example.bluetooth.messaging

import android.content.ContentValues.TAG
import android.content.Context
import android.widget.Toast
import com.example.bluetooth.comms.LoggerWrapper
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService constructor(
    private val loggerWrapper: LoggerWrapper,
    val firebaseMessagingWrapper: FirebaseMessagingWrapper,
    val toastWrapper: ToastWrapper,
    val context: Context
) : FirebaseMessagingService(), MyFirebaseMessagingServiceI {
    private var token: String? = null
    override fun onNewToken(token: String) {
        loggerWrapper.log(TAG, "Refreshed token: $token")
    }

    override fun setMessage() {
        with(firebaseMessagingWrapper) {
            val test = getTask(getInstance())
            addOnCompleteListener(
                task = test,
                block = { task ->
                    if (!task.isSuccessful) {
                        loggerWrapper.log(
                            TAG,
                            "Fetching FCM registration token failed: ${task.exception}"
                        )
                        return@addOnCompleteListener
                    }

                    token = task.result

                    loggerWrapper.log(TAG, token ?: return@addOnCompleteListener)
                    toastWrapper.makeText(text = token, duration = Toast.LENGTH_SHORT)
                }
            )
        }
    }

    override fun getMyToken(): String = token ?: ""
}

class FirebaseMessagingWrapper constructor(val onCompleteListenerWrapper: OnCompleteListenerWrapper) {
    fun getInstance(): FirebaseMessaging =
        FirebaseMessaging.getInstance()

    fun getTask(firebaseMessaging: FirebaseMessaging): Task<String> =
        firebaseMessaging.token

    fun addOnCompleteListener(task: Task<String>, block: (Task<String>) -> Unit): Task<String> {
        return task.addOnCompleteListener(onCompleteListenerWrapper.onCompleteListener(block = block))
    }
}

class ToastWrapper constructor(val context: Context) {
    fun makeText(text: String?, duration: Int) {
        Toast.makeText(context, text, duration).show()
    }
}

class OnCompleteListenerWrapper {
    fun onCompleteListener(block: (Task<String>) -> Unit): OnCompleteListener<String> {
        return OnCompleteListener(block)
    }
}