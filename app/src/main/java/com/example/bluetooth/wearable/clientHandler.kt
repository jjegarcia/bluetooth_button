package com.example.bluetooth.wearable

import android.content.Context
import android.util.Log
import com.example.bluetooth.BluetoothComponent
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable.getNodeClient

class ClientHandler constructor(val bluetoothComponent: BluetoothComponent) : ClientHandlerI {

    override fun initialise(conext: Context) {


        val nodeListTask = getNodeClient(conext).connectedNodes
        try {
            val nodes =
                Tasks.await(
                    nodeListTask
                )
            Log.i("wearables", "Task fetched nodes")

            Log.i("wearables:", "wearables obtained")
        } catch (e: Exception) {
            Log.i("exception", "error $e")
        }
    }

}
