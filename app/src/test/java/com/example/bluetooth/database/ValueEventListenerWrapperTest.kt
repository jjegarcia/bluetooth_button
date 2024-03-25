package com.example.bluetooth.database

import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.comms.LoggerWrapper
import com.google.firebase.database.DataSnapshot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class ValueEventListenerWrapperTest {
    private val loggerWrapper: LoggerWrapper = mockk(relaxed = true)
    private val bluetoothComponent: BluetoothComponent = mockk(relaxed = true)
    private val mockSnapshot: DataSnapshot = mockk()

    val subject = ValueEventListenerWrapper(
        loggerWrapper,
        bluetoothComponent
    )

    private val oldDeviceList = hashMapOf(
        "ss" to DeviceData(
            Coordinates(0.0, 0.0),
            "pp"
        )
    )

    @Test
    fun `Given database changed Then invokes list of devices creation`() {
        every { mockSnapshot.value } returns oldDeviceList
        subject.onDataChange(mockSnapshot)

        verify { bluetoothComponent.databaseHandler?.setDeviceList(any()) }
    }

    @Test
    fun `Given removed token from client Then request reset board`() {
        every { mockSnapshot.value } returns oldDeviceList
        every { bluetoothComponent.databaseHandler?.fetchDeviceList() } returns hashMapOf(
            "ss" to DeviceData(
                Coordinates(0.0, 0.0),
                "xx"
            )
        )

        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns "xx"
        every { bluetoothComponent.viewModel!!.requestReset() } just Runs
        every { bluetoothComponent.databaseHandler?.isTokenInList() } returns true

        subject.onDataChange(mockSnapshot)

        verify { bluetoothComponent.viewModel?.requestReset() }
    }
   @Test
    fun `Given updated device list and token wasn't registered  from client Then won't request reset board`() {
        every { mockSnapshot.value } returns oldDeviceList
        every { bluetoothComponent.databaseHandler?.fetchDeviceList() } returns hashMapOf(
            "ss" to DeviceData(
                Coordinates(0.0, 0.0),
                "xx"
            )
        )

        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns "xx"
        every { bluetoothComponent.viewModel!!.requestReset() } just Runs
        every { bluetoothComponent.databaseHandler?.isTokenInList() } returns false

        subject.onDataChange(mockSnapshot)

        verify(exactly = 0) { bluetoothComponent.viewModel?.requestReset() }
    }
    @Test
    fun `Given null list of devices and token was registered from client Then reset board`() {
        every { mockSnapshot.value } returns null
        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns "pp"
        every { bluetoothComponent.viewModel!!.requestReset() } just Runs
        every { bluetoothComponent.databaseHandler?.isTokenInList() } returns true

        subject.onDataChange(mockSnapshot)

        verify { bluetoothComponent.viewModel?.requestReset() }
    }
   @Test
    fun `Given null list of devices and token wasn't registered from client Then won't reset board`() {
        every { mockSnapshot.value } returns null
        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns "pp"
        every { bluetoothComponent.viewModel!!.requestReset() } just Runs
        every { bluetoothComponent.databaseHandler?.isTokenInList() } returns false

        subject.onDataChange(mockSnapshot)

        verify(exactly = 0) { bluetoothComponent.viewModel?.requestReset() }
    }
    @Test
    fun `Given not removed token from client Then won't request reset board`() {
        every { mockSnapshot.value } returns oldDeviceList
        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns "pp"
        every { bluetoothComponent.viewModel!!.requestReset() } just Runs

        subject.onDataChange(mockSnapshot)

        verify(exactly = 0) { bluetoothComponent.viewModel?.requestReset() }
    }
}