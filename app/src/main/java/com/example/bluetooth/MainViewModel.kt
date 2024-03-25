package com.example.bluetooth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel constructor(private val bluetoothComponent: BluetoothComponent) : ViewModel() {

    private val _screenDataFlow = MutableStateFlow<ScreenData?>(null)
    val screenDataFlow = _screenDataFlow

    fun postValue(value: ScreenData?) {
        _screenDataFlow.tryEmit(value)
    }

    fun requestReset() {
        bluetoothComponent.gattComponent?.requestReset(bluetoothComponent.gattComponent?.getCharacteristic())
        bluetoothComponent.stateMachine?.setState(
            targetState = State.Initial,
            block = {
                postValue(
                    ScreenData(
                        indicatorColor = State.Initial.state.color
                    )
                )
                bluetoothComponent.databaseHandler?.removeDevice()
            }
        )
    }

    fun requestNotify() {
        bluetoothComponent.gattComponent?.requestNotify(bluetoothComponent.gattComponent?.getCharacteristic())
    }

    fun stopScanning() {
        bluetoothComponent.scanComponent?.stopScanning()
    }

    fun connectBle() {
        bluetoothComponent.scanComponent?.connectBle()
    }

    fun sendToForeground() {
//        bluetoothComponent.databaseHandler?.addDeviceData()
        bluetoothComponent.serviceFactory?.start()
    }
}
