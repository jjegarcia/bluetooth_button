package com.example.bluetooth.comms

import android.bluetooth.BluetoothGattCharacteristic

interface GattComponentI {
    fun requestReset(characteristic: BluetoothGattCharacteristic?)

    fun requestNotify(characteristic: BluetoothGattCharacteristic?)
    fun getCharacteristic(): BluetoothGattCharacteristic?
}