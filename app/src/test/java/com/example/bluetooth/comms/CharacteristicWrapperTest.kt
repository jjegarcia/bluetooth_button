package com.example.bluetooth.comms

import android.bluetooth.BluetoothGattCharacteristic
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class CharacteristicWrapperTest {
    private val mockCharacteristic: BluetoothGattCharacteristic = mockk(relaxed = true)
    val subject = CharacteristicWrapper()

    @Test
    fun getUuid() {
        subject.getUuid(characteristic = mockCharacteristic)
        verify { mockCharacteristic.uuid }
    }
}