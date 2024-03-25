package com.example.bluetooth.comms

import android.bluetooth.BluetoothGatt

interface ScanComponentI {
    var myConnection: BluetoothGatt
    fun connectBle()
    fun stopScanning()
    fun initialise()
    fun checkEnabled()
}