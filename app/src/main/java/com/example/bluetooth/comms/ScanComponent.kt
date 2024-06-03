package com.example.bluetooth.comms

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.messaging.ToastWrapper
import java.util.*

//private const val DEVICE_ADDRESS = "04:91:62:97:83:78" //proto-board
//private const val DEVICE_ADDRESS = "40:84:32:66:F8:6B" //new board
private const val DEVICE_ADDRESS = "04:91:62:9B:4B:C6" // intact board


class ScanComponent constructor(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothComponent: BluetoothComponent,
    private val resultWrapper: ScanResultWrapper,
    private val scanSettingsWrapper: ScanSettingsWrapper,
    private val activityCompatWrapper: ActivityCompactWrapper,
    private val loggerWrapper: LoggerWrapper,
    private val toastWrapper: ToastWrapper
) : ScanComponentI {
    override lateinit var myConnection: BluetoothGatt
    private val devices: MutableList<BluetoothDevice> = mutableListOf()
    val bleScanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    fun getDevices() = devices

    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(resultWrapper.getDevice(result)) {
                if (!devices.contains(this)) {
                    devices.add(this)
//                    if (this.address == DEVICE_ADDRESS) {
//                        stopScanning()
//                        connectBle()
//                    }
                }
            }
        }
    }

    override fun connectBle() {
        try {
            val myDevices = devices.filter { it.address == DEVICE_ADDRESS }

            if (myDevices.isNotEmpty()) {
                val myDevice = myDevices[0]
                if (activityCompatWrapper.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    myConnection =
                        myDevice.connectGatt(
                            context,
                            false,
                            bluetoothComponent.gattComponent?.gattCallback
                        )
                }
            }
        } catch (error: Exception) {
            toastWrapper.makeText("Bluetooth Connection Exception: $error", Toast.LENGTH_SHORT)
            return
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopScanning() {
        try {
            bleScanner.stopScan(scanCallback)
        } catch (error: Exception) {
            toastWrapper.makeText(
                text = "Bluetooth Connection Exception: $error",
                duration = Toast.LENGTH_SHORT
            )
            return
        }
    }

    @SuppressLint("MissingPermission")
    override fun initialise() {
        try {
            bleScanner.startScan(
                null,
                scanSettingsWrapper.getBuilder(ScanSettings.SCAN_MODE_LOW_LATENCY),
                scanCallback
            )
        } catch (error: Exception) {
            toastWrapper.makeText(
                text = "Bluetooth Connection Exception: $error",
                duration = Toast.LENGTH_SHORT
            )
            return
        }
    }

    override fun checkEnabled() {
        if (!bluetoothAdapter.isEnabled) {
            loggerWrapper.log("Bluetooth App", "Missing Adapter")
        }
    }
}

class ScanResultWrapper {
    fun getDevice(result: ScanResult): BluetoothDevice = result.device
}

class ScanSettingsWrapper {
    fun getBuilder(scanSetting: Int): ScanSettings =
        ScanSettings.Builder()
            .setScanMode(scanSetting)
            .build()
}

class ActivityCompactWrapper {
    fun checkSelfPermission(context: Context, permission: String) =
        ActivityCompat.checkSelfPermission(context, permission)
}
