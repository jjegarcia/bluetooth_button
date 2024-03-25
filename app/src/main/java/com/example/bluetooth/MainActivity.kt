package com.example.bluetooth

import android.Manifest.permission
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.example.bluetooth.compose.MainScreen

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothComponent: BluetoothComponent
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                permission.ACCESS_COARSE_LOCATION,
                permission.ACCESS_FINE_LOCATION
            ),
            PackageManager.PERMISSION_GRANTED
        )
        showScreen()
        initialiseComponents()
    }

    override fun onResume() {
        super.onResume()
        with(bluetoothComponent.scanComponent) {
            this?.checkEnabled()
        }
    }

    private fun initialiseComponents() {
        bluetoothComponent = BluetoothComponent()
        val bluetoothComponentFactory =
            BluetoothComponentFactory(
                bluetoothComponent = bluetoothComponent,
                context = this@MainActivity,
                bluetoothAdapter = bluetoothAdapter
            )
        bluetoothComponent.viewModel = MainViewModel(bluetoothComponent)
        bluetoothComponent.stateMachine = StateMachine()
        bluetoothComponent.locationHandler = bluetoothComponentFactory.locationHandler
        bluetoothComponent.notificationHelper = bluetoothComponentFactory.notificationHelper
        bluetoothComponent.clientHandler = bluetoothComponentFactory.clientHandler
        bluetoothComponent.scanComponent = bluetoothComponentFactory.scanComponent
        bluetoothComponent.gattComponent = bluetoothComponentFactory.gattComponent
        bluetoothComponent.databaseHandler = bluetoothComponentFactory.databaseHandler
        bluetoothComponent.myFirebaseMessagingService =
            bluetoothComponentFactory.myFirebaseMessagingService
        bluetoothComponent.myFirebaseMessagingService?.setMessage()
        with(bluetoothComponent) {
            scanComponent?.initialise()
            clientHandler?.initialise(this@MainActivity)
            notificationHelper?.initialise(this@MainActivity)
        }
        bluetoothComponent.serviceFactory = bluetoothComponentFactory.serviceFactory
    }

    private fun showScreen() {
        setContent {
            bluetoothComponent.viewModel?.let { MainScreen(viewModel = it) }
        }
    }
}