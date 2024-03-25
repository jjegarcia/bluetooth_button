package com.example.bluetooth.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.bluetooth.comms.LoggerWrapper
import com.example.bluetooth.location.LocationHandler

class MyService : Service() {
    private var locationHandler: LocationHandler? = null
    private var loggerWrapper:LoggerWrapper?= null

    fun setUpService(myLocationHandler: LocationHandler,myLoggerWrapper: LoggerWrapper) {
        loggerWrapper= myLoggerWrapper
        loggerWrapper?.log("MyService", "initialised")
        locationHandler = myLocationHandler
        locationHandler?.startLocationUpdate()
    }

    override fun onDestroy() {
        Log.d("MyService", "destroyed")

    }
     inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): MyService = this@MyService
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }
}