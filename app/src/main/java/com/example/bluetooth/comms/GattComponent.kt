package com.example.bluetooth.comms

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.ScreenData
import com.example.bluetooth.State
import com.example.bluetooth.comms.PACKET.*
import com.example.bluetooth.comms.RequestType.*
import com.example.bluetooth.messaging.ToastWrapper
import java.util.*

private const val TARGET_CHARACTERISTIC_UUID = "49535343-1e4d-4bd9-ba61-23c647249616"
private const val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

@Suppress("DEPRECATION")
class GattComponent constructor(
    val bluetoothComponent: BluetoothComponent,
    private val bluetoothGattDescriptor: BluetoothGattDescriptorWrapper,
    private val handlerWrapper: HandlerWrapper,
    private val loggerWrapper: LoggerWrapper,
    private val serviceWrapper: ServiceWrapper,
    private val characteristicWrapper: CharacteristicWrapper,
    private val toastWrapper: ToastWrapper
) : GattComponentI {
    private var myCharacteristic: BluetoothGattCharacteristic? = null

    override fun requestReset(characteristic: BluetoothGattCharacteristic?) {
        characteristic?.let {
            bluetoothComponent.scanComponent?.let { scanComponent ->
                writeCharacteristic(
                    characteristic = characteristic,
                    payload = RESET.command,
                    gatt = scanComponent.myConnection,
                )
            }
        }
    }

    override fun requestNotify(characteristic: BluetoothGattCharacteristic?) {
        try {
            characteristic?.let {
                bluetoothComponent.scanComponent?.let { scanComponent ->
                    enableNotifications(
                        characteristic = characteristic,
                        scanComponent.myConnection
                    )
                }
            }
        } catch (error: Exception) {
            toastWrapper.makeText("Bluetooth Connection Exception: $error", Toast.LENGTH_SHORT)
            return
        }
    }

    override fun getCharacteristic(): BluetoothGattCharacteristic? = myCharacteristic

    val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    loggerWrapper.log(
                        "BluetoothGattCallback",
                        "Successfully connected to $deviceAddress"
                    )
                    bluetoothComponent.stateMachine?.setState(
                        targetState = State.Connected,
                        block = {
                            bluetoothComponent.viewModel?.postValue(
                                ScreenData(
                                    indicatorColor = State.Connected.state.color
                                )
                            )
                        }
                    )
                    handlerWrapper.post {
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    loggerWrapper.log(
                        "BluetoothGattCallback",
                        "Successfully disconnected from $deviceAddress"
                    )
                    gatt.close()
                }
            } else {
                loggerWrapper.log(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                loggerWrapper.log(
                    "BluetoothGattCallback",
                    "Discovered ${services.size} services for ${device.address}"
                )
                printGattTable()
//                requestNotify()
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                loggerWrapper.log(
                    "BluetoothGattCallback",
                    "Characteristic $uuid changed | value: $value"
                )
                when (getRequestType(value)) {
                    is BuzzType -> {
                        bluetoothComponent.stateMachine?.setState(
                            targetState = State.Buzzing,
                            block = {
                                //                        bluetoothComponent.databaseHandler?.addToken()
                                bluetoothComponent.notificationHelper?.createAlertNotification()
                                bluetoothComponent.viewModel?.postValue(
                                    ScreenData(
                                        indicatorColor = State.Buzzing.state.color
                                    )
                                )
                            }
                        )
                    }

                    is AlarmType -> {
                        bluetoothComponent.stateMachine?.setState(
                            targetState = State.Alarm,
                            block = {
                                //                        bluetoothComponent.databaseHandler?.addToken()
                                bluetoothComponent.notificationHelper?.createAlertNotification()
                                bluetoothComponent.viewModel?.postValue(
                                    ScreenData(
                                        indicatorColor = State.Alarm.state.color
                                    )
                                )
                                bluetoothComponent.databaseHandler?.addDeviceData()

                            }
                        )
                    }
                    is PushType -> {
                        bluetoothComponent.notificationHelper?.createAlertNotification()
                    }
                    else -> {
                        loggerWrapper.log(
                            "Invalid:",
                            "Characteristic $uuid changed | value: $value"
                        )

                    }
                }
                writeCharacteristic(characteristic, value, gatt)
            }
        }
    }

    private fun getRequestType(value: ByteArray?): RequestType {
        if (value != null) {
            return when (value[4]) {
                ALARM.header -> AlarmType
                BUZZ.header -> BuzzType
                PUSH.header -> PushType
                else -> Undetermined
            }
        }
        return Undetermined
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            loggerWrapper.log(
                "printGattTable",
                "No service and characteristic available, call discoverServices() first?"
            )
            return
        }
        services.forEach { service ->
            serviceWrapper.getCharacteristics(service).forEach { characteristic ->
                if (characteristicWrapper.getUuid(characteristic)
                        .toString() == TARGET_CHARACTERISTIC_UUID
                ) {
                    myCharacteristic = characteristic
                }
            }
            val characteristicsTable = serviceWrapper.getCharacteristics(service).joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { characteristic ->
                characteristicWrapper.getUuid(characteristic).toString()
            }
            val uuid = serviceWrapper.getUuid(service)//service.uuid
            loggerWrapper.log(
                "printGattTable",
                "\nService ${uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray,
        gatt: BluetoothGatt
    ) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }

            else -> {
                loggerWrapper.log(
                    "BluetoothGattCallback",
                    "Characteristic ${characteristic.uuid} cannot be written to"
                )
                return
            }
        }
        characteristic.writeType = writeType
        characteristic.value = payload
        gatt.writeCharacteristic(characteristic)
    }

    private fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    private fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    private fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
        properties and property != 0

    @SuppressLint("MissingPermission")
    private fun enableNotifications(
        characteristic: BluetoothGattCharacteristic,
        gatt: BluetoothGatt
    ) {
        val cccUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isNotifiable() -> {
                bluetoothGattDescriptor.getEnableNotificationValue()
            }

            else -> {
                Log.e(
                    "ConnectionManager",
                    "${characteristic.uuid} doesn't support notifications/indications"
                )
                return
            }
        }

        characteristic.getDescriptor(cccUuid)?.let { cccDescriptor ->
            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                Log.e(
                    "ConnectionManager",
                    "setCharacteristicNotification failed for ${characteristic.uuid}"
                )
                return
            }
            if (payload != null) {
                writeDescriptor(cccDescriptor, payload)
                bluetoothComponent.stateMachine?.setState(
                    targetState = State.Paired,
                    block = {
                        bluetoothComponent.viewModel?.postValue(
                            ScreenData(
                                indicatorColor = State.Paired.state.color
                            )
                        )
                    }
                )
            }
        } ?: Log.e(
            "ConnectionManager",
            "${characteristic.uuid} doesn't contain the CCC descriptor!"
        )
    }

    @SuppressLint("MissingPermission")
    private fun writeDescriptor(
        descriptor: BluetoothGattDescriptor,
        payload: ByteArray,
    ) {
        descriptor.value = payload
        bluetoothComponent.scanComponent?.myConnection?.writeDescriptor(descriptor)
    }
}

class BluetoothGattDescriptorWrapper {
    fun getEnableNotificationValue(): ByteArray? {
        return BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
    }
}

class LooperWrapper {
    fun getMainLooper(): Looper = Looper.getMainLooper()
}

class HandlerWrapper(private val looperWrapper: LooperWrapper) {
    fun post(block: () -> Unit) {
        Handler(looperWrapper.getMainLooper()).post(block)
    }
}

class LoggerWrapper {
    fun log(tag: String, message: String) {
        Log.i(tag, message)
    }
}

class ServiceWrapper {
    fun getCharacteristics(service: BluetoothGattService): List<BluetoothGattCharacteristic> =
        service.characteristics

    fun getUuid(service: BluetoothGattService): UUID = service.uuid
}

class CharacteristicWrapper {
    fun getUuid(characteristic: BluetoothGattCharacteristic): UUID = characteristic.uuid
}
