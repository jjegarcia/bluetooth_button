package com.example.bluetooth.messaging

interface MyFirebaseMessagingServiceI {
    fun getMyToken(): String
    fun setMessage()
}