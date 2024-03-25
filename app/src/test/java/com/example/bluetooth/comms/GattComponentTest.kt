package com.example.bluetooth.comms

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import androidx.compose.ui.graphics.Color
import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.ScreenData
import com.example.bluetooth.State
import com.example.bluetooth.comms.PACKET.*
import com.example.bluetooth.messaging.ToastWrapper
import io.mockk.*
import org.junit.Test
import java.util.*

private const val TARGET_CHARACTERISTIC_UUID = "49535343-1e4d-4bd9-ba61-23c647249616"

@Suppress("DEPRECATION")
class GattComponentTest {
    private fun uuid() = UUID(2222, 3211)
    private val char: BluetoothGattCharacteristic = mockk(relaxed = true)
    private val bluetoothComponent: BluetoothComponent = mockk(relaxed = true)
    private val bluetoothGattDescriptor: BluetoothGattDescriptorWrapper = mockk(relaxed = true)
    private val handlerWrapper: HandlerWrapper = mockk(relaxed = true)
    private val loggerWrapper: LoggerWrapper = mockk(relaxed = true)
    private val serviceWrapper: ServiceWrapper = mockk(relaxed = true)
    private val characteristicWrapper: CharacteristicWrapper = mockk(relaxed = true)
    private val toastWrapper: ToastWrapper = mockk(relaxed = true)

    private val subject = GattComponent(
        bluetoothComponent = bluetoothComponent,
        bluetoothGattDescriptor = bluetoothGattDescriptor,
        handlerWrapper = handlerWrapper,
        loggerWrapper = loggerWrapper,
        serviceWrapper = serviceWrapper,
        characteristicWrapper = characteristicWrapper,
        toastWrapper = toastWrapper
    )

    private val gatt: BluetoothGatt = mockk(relaxed = true)

    @Test
    fun `Given reset requested When mCharacteristic is null Then  don't write to Characteristic`() {
        every { bluetoothComponent.scanComponent!!.myConnection } returns gatt

        subject.requestReset(characteristic = null)


        verify(exactly = 0) { gatt.writeCharacteristic(any()) }
    }

    @Test
    fun `Given reset requested When mCharacteristic PROPERTY_WRITE Then write to Characteristic`() {
        every { bluetoothComponent.scanComponent!!.myConnection } returns gatt
        every { char.uuid } returns uuid()
        every { char.properties } returns PROPERTY_WRITE
        subject.requestReset(characteristic = char)

        verify(exactly = 1) { gatt.writeCharacteristic(any()) }
    }

    @Test
    fun `Given reset requested When mCharacteristic  PROPERTY_WRITE_NO_RESPONSE Then  write to Characteristic`() {
        every { bluetoothComponent.scanComponent!!.myConnection } returns gatt
        every { char.uuid } returns uuid()
        every { char.properties } returns PROPERTY_WRITE_NO_RESPONSE
        subject.requestReset(characteristic = char)

        verify(exactly = 1) { gatt.writeCharacteristic(any()) }
    }

    @Test
    fun `Given reset requested  When mCharacteristic  Unknown PROPERTY Then  don't write to Characteristic`() {
        every { bluetoothComponent.scanComponent!!.myConnection } returns gatt
        every { char.uuid } returns UUID(2, 3)
        every { char.properties } returns PERMISSION_READ
        subject.requestReset(characteristic = char)

        verify(exactly = 0) { gatt.writeCharacteristic(any()) }
    }

    @Test
    fun `Given Notification Requested When mCharacteristic is null Then call nothing`() {
        every { bluetoothComponent.scanComponent!!.myConnection } returns gatt
        subject.requestNotify(characteristic = null)

        verify(exactly = 0) { gatt.writeDescriptor(any()) }
    }

    @Test
    fun `Given Notification Requested When mCharacteristic  Then write Descriptor`() {
        every { bluetoothGattDescriptor.getEnableNotificationValue() } returns byteArrayOf(0x22)
        every { bluetoothComponent.scanComponent!!.myConnection } returns gatt
        every { char.uuid } returns uuid()
        every { gatt.setCharacteristicNotification(any(), any()) } returns true
        every { char.properties } returns PROPERTY_NOTIFY

        subject.requestNotify(characteristic = char)

        verify(exactly = 1) { gatt.writeDescriptor(any()) }
    }

    @Test
    fun `Given changed Characteristic When Buzzing is requested Then issues callback `() {
        val blockSlot = slot<() -> Unit>()
        every { char.uuid } returns uuid()
        every { char.value } returns byteArrayOf(2, 2, BUZZ.header)
        every { char.properties } returns PROPERTY_WRITE
        every { gatt.writeCharacteristic(any()) } returns true
        every {
            bluetoothComponent.stateMachine?.setState(
                targetState = State.Buzzing,
                block = capture(blockSlot)
            )
        } returns mockk()

        subject.gattCallback.onCharacteristicChanged(gatt, char)

        verify {
            bluetoothComponent.stateMachine?.setState(
                targetState = State.Buzzing,
                block = blockSlot.captured
            )
        }

        blockSlot.invoke()

        verify { bluetoothComponent.notificationHelper?.createAlertNotification() }
        verify { bluetoothComponent.viewModel?.postValue(ScreenData(Color.Green)) }

    }

    @Test
    fun `Given changed Characteristic When Buzzing is requested Then issues Callback `() {
        val blockSlot = slot<() -> Unit>()
        every { char.uuid } returns uuid()
        every { char.value } returns byteArrayOf(2, 2, ALARM.header)
        every { char.properties } returns PROPERTY_WRITE
        every { gatt.writeCharacteristic(any()) } returns true
        every {
            bluetoothComponent.stateMachine?.setState(
                targetState = State.Alarm,
                block = capture(blockSlot)
            )
        } returns mockk()

        subject.gattCallback.onCharacteristicChanged(gatt, char)

        verify {
            bluetoothComponent.stateMachine?.setState(
                targetState = State.Alarm,
                block = blockSlot.captured
            )
        }

        blockSlot.invoke()

        verify { bluetoothComponent.notificationHelper?.createAlertNotification() }
        verify { bluetoothComponent.viewModel?.postValue(ScreenData(Color.Red)) }
    }

    @Test
    fun `Given changed Characteristic When write Property is valid Then loops back characteristic `() {
        every { char.uuid } returns uuid()
        every { char.value } returns byteArrayOf(2, 2, BUZZ.header)
        every { char.properties } returns PROPERTY_WRITE
        every { gatt.writeCharacteristic(any()) } returns true

        subject.gattCallback.onCharacteristicChanged(gatt, char)

        verify { gatt.writeCharacteristic(any()) }
    }

    @Test
    fun `Given changed Characteristic When write Property is not valid Then loops back characteristic `() {
        every { char.uuid } returns uuid()
        every { char.value } returns byteArrayOf(2, 2, BUZZ.header)
        every { char.properties } returns PROPERTY_NOTIFY
        every { gatt.writeCharacteristic(any()) } returns true

        subject.gattCallback.onCharacteristicChanged(gatt, char)

        verify(exactly = 0) { gatt.writeCharacteristic(any()) }
    }

    @Test
    fun `Given connection changed When connected Then issue available services`() {
        val status = BluetoothGatt.GATT_SUCCESS
        val newState = BluetoothProfile.STATE_CONNECTED

        every { gatt.discoverServices() } returns true

        subject.gattCallback.onConnectionStateChange(gatt, status, newState)

        verify { handlerWrapper.post(any()) }
    }

    @Test
    fun `Given connection changed When not connected Then won't available services`() {
        val status = BluetoothGatt.GATT_FAILURE
        val newState = BluetoothProfile.STATE_CONNECTED

        every { gatt.discoverServices() } returns true

        subject.gattCallback.onConnectionStateChange(gatt, status, newState)

        verify(exactly = 0) { handlerWrapper.post(any()) }
    }

    @Test
    fun `Given connection changed When not connected' Then won't available services`() {
        val status = BluetoothGatt.GATT_SUCCESS
        val newState = BluetoothProfile.STATE_DISCONNECTED
        every { gatt.discoverServices() } returns true

        subject.gattCallback.onConnectionStateChange(gatt, status, newState)

        verify(exactly = 0) { handlerWrapper.post(any()) }
    }

    @Test
    fun `Given on services enquired When empty list Then won't list available services`() {
        every { gatt.discoverServices() } returns true
        every { gatt.services } returns listOf()

        subject.gattCallback.onServicesDiscovered(gatt, BluetoothGatt.GATT_SUCCESS)

        verify {
            loggerWrapper.log(
                "printGattTable",
                "No service and characteristic available, call discoverServices() first?"
            )
        }
    }

    @Test
    fun `Given on services enquired When not empty list Then list available services`() {
        val service = BluetoothGattService(uuid(), 22)
        every { gatt.discoverServices() } returns true
        every { gatt.services } returns listOf(service)
        every { serviceWrapper.getCharacteristics(service = service) } returns listOf(
            BluetoothGattCharacteristic(UUID.fromString(TARGET_CHARACTERISTIC_UUID), 2, 2)
        )
        every { characteristicWrapper.getUuid(any()) } returns uuid()

        subject.gattCallback.onServicesDiscovered(gatt, BluetoothGatt.GATT_SUCCESS)

        verify(exactly = 0) {
            loggerWrapper.log(
                "printGattTable",
                "No service and characteristic available, call discoverServices() first?"
            )
        }
    }
}