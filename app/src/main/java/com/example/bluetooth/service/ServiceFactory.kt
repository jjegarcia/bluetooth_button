package com.example.bluetooth.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat.startForeground
import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.comms.LoggerWrapper
import com.example.bluetooth.location.LocationHandler

private const val ONGOING_NOTIFICATION_ID = 33

class ServiceFactory constructor(
    val context: Context,
    val locationHandler: LocationHandler,
    val bluetoothComponent: BluetoothComponent,
    val loggerWrapper: LoggerWrapper
) : ServiceFactoryI {
    var myService: MyService? = null
    override fun start() {
        val callService = Intent(context, MyService::class.java)
        context.bindService(callService, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun stop() {
        context.stopService(Intent(context, MyService::class.java))
    }

    val mConnection: ServiceConnection = object : ServiceConnection {
        @RequiresApi(34)
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            myService = (service as MyService.LocalBinder).getService()

            bluetoothComponent.notificationHelper?.createForegroundNotification()?.let {
                startForeground(
                    myService!!,
                    ONGOING_NOTIFICATION_ID,
                    it,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            }
            myService?.setUpService(locationHandler,loggerWrapper)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            myService = null
        }
    }
}