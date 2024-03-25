package com.example.bluetooth.database

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

interface DatabaseHandlerI {
    fun addDeviceData()
    fun getReference(): DatabaseReference
    fun updateCoordinates()
    fun setDeviceList(inputDeviceList: java.util.HashMap<String, DeviceData>?)
    fun fetchDeviceList(): java.util.HashMap<String, DeviceData>?
    fun getValueEventListener(): ValueEventListener
    fun isTokenInList():Boolean
    fun removeDevice()
}