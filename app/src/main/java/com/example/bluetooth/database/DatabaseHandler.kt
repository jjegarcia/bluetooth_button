package com.example.bluetooth.database

import android.content.ContentValues.TAG
import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.comms.LoggerWrapper
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.util.Timer
import kotlin.concurrent.timerTask

private const val DELAY_MS: Long = 1000
private const val REQUESTS_PATH = "requests"
private const val ALERTS_PATH = "new_alert"

class DatabaseHandler(
    private val bluetoothComponent: BluetoothComponent,
    firebaseDatabaseWrapper: FirebaseDatabaseWrapper,
    private val valueEventListenerWrapper: ValueEventListenerWrapper,
    private val loggerWrapper: LoggerWrapper

) : DatabaseHandlerI {
    private val database = firebaseDatabaseWrapper.getInstance()
    private val requestsRef = database.getReference(REQUESTS_PATH)
    private val newAlertRef = database.getReference(ALERTS_PATH)
    private var deviceList: java.util.HashMap<String, DeviceData>? = null
    private var valueEventListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            valueEventListenerWrapper.onDataChange(snapshot)
        }

        override fun onCancelled(error: DatabaseError) {
            valueEventListenerWrapper.onCancelled(error)
        }
    }

    override fun addDeviceData() {
        if (bluetoothComponent.myFirebaseMessagingService != null) {
            val token = bluetoothComponent.myFirebaseMessagingService?.getMyToken()
            if (fetchDeviceList()?.map { deviceData -> deviceData.value.token }
                    ?.contains(token) == true) return
            if (token != null && bluetoothComponent.locationHandler?.fetchCoordinates() != null) {
                addDeviceDataToRef(token, requestsRef)
                addTokenToRef(token, newAlertRef)
            }
        }
    }

    private fun addDeviceDataToRef(token: String, reference: DatabaseReference) {
        reference.push()
            .setValue(
                DeviceData(
                    token = token,
                    location = bluetoothComponent.locationHandler?.fetchCoordinates()
                )
            )
    }

    private fun addTokenToRef(token: String, reference: DatabaseReference) {
        loggerWrapper.log("Test", "Alerting Client")
        reference.push()
            .setValue(token)
        Timer().schedule(timerTask { reference.removeValue() }, DELAY_MS)
    }

    override fun getReference() = requestsRef
    override fun fetchDeviceList(): HashMap<String, DeviceData>? = deviceList

    override fun getValueEventListener(): ValueEventListener = valueEventListener
    override fun isTokenInList(): Boolean {
        if (deviceList == null) return false
        val token = bluetoothComponent.myFirebaseMessagingService?.getMyToken()
        return deviceList!!.map { deviceData -> deviceData.value.token }.contains(token)
    }

    override fun removeDevice() {
        val token = bluetoothComponent.myFirebaseMessagingService?.getMyToken()
        if (token != null) {
            val key = findKey(token)
            if (key != null) {
                requestsRef.child(key).removeValue()
            }
        }
    }

    private fun findKey(token: String): String? {
        val list = deviceList?.filter { entry ->
            entry.value.token == token
        }?.keys?.toList()
        if (list != null) {
            if (list.isEmpty()) return null
            return list[0]
        }
        return null
    }

    override fun updateCoordinates() {
        val key = bluetoothComponent.myFirebaseMessagingService?.getMyToken()?.let { getKey(it) }
        if (key != null) {
            val newCoordinates = bluetoothComponent.locationHandler?.fetchCoordinates()
            if (fetchCachedLocation(key) != newCoordinates) {
                requestsRef.child(key).child("location")
                    .setValue(newCoordinates)
                loggerWrapper.log("Test", "Updated DB Location:$newCoordinates ")
            }
        }
    }

    private fun fetchCachedLocation(key: String): Coordinates? {
        val sample: List<Coordinates?>? = deviceList?.filter { device ->
            device.key == key
        }?.map { deviceEntry ->
            deviceEntry.value.location
        }?.toList()
        return sample?.get(0) ?: bluetoothComponent.locationHandler?.fetchCoordinates()
    }

    override fun setDeviceList(inputDeviceList: HashMap<String, DeviceData>?) {
        deviceList = inputDeviceList
    }

    init {
        requestsRef.addValueEventListener(valueEventListener)
    }

    private fun getKey(token: String): String? {
        val list = deviceList?.filter { entry ->
            entry.value.token == token
        }?.keys?.toList()
        if (list != null) {
            if (list.isEmpty()) return null
            return list[0]
        }
        return null
    }
}

class ValueEventListenerWrapper constructor(
    val loggerWrapper: LoggerWrapper,
    val bluetoothComponent: BluetoothComponent
) {
    fun onDataChange(snapshot: DataSnapshot) {
        try {
            buildDeviceList(snapshot)
        } catch (error: Exception) {
            loggerWrapper.log(TAG, "Exception: $error")
            return
        }
        loggerWrapper.log(TAG, "snapshot: ${snapshot.value}")
    }

    fun onCancelled(error: DatabaseError) {
        loggerWrapper.log(TAG, "Failed to read database: ${error.toException()}")
    }

    private fun buildDeviceList(snapshot: DataSnapshot) {
        try {
            val newList =
                snapshot.getValue<HashMap<String, DeviceData>>()
            if (!isTokenInList(newList) && bluetoothComponent.databaseHandler?.isTokenInList() == true) {
                bluetoothComponent.viewModel?.requestReset()
            }
            setDeviceList(newList)
        } catch (e: Exception) {
            loggerWrapper.log(TAG, "error: $e")
        }
    }

    private fun isTokenInList(list: java.util.HashMap<String, DeviceData>?): Boolean {
        if (list == null) return false
        val token = bluetoothComponent.myFirebaseMessagingService?.getMyToken()
        val test = list.map { deviceData -> deviceData.value.token }
        return test.contains(token)
    }

    private fun setDeviceList(inputDeviceList: HashMap<String, DeviceData>?) {
        bluetoothComponent.databaseHandler?.setDeviceList(inputDeviceList)
    }
}

class FirebaseDatabaseWrapper {
    fun getInstance(): FirebaseDatabase = FirebaseDatabase.getInstance()
}

data class DeviceData constructor(
    @PropertyName("location") val location: Coordinates? = Coordinates(0.0, 0.0),
    @PropertyName("token") val token: String = ""
)

data class Coordinates constructor(
    @PropertyName("latitude") val latitude: Double? = 0.0,
    @PropertyName("longitude") val longitude: Double? = 0.0
)