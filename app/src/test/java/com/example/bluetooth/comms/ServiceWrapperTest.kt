package com.example.bluetooth.comms

import android.bluetooth.BluetoothGattService
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class ServiceWrapperTest {
    private val mockService: BluetoothGattService= mockk(relaxed= true)

    val subject=ServiceWrapper()
    @Test
    fun getCharacteristics() {

        subject.getCharacteristics(mockService)
        verify { mockService.characteristics }
    }

    @Test
    fun getUuid() {
        subject.getUuid(mockService)
        verify { mockService.uuid }
    }
}