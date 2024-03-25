package com.example.bluetooth

import androidx.compose.ui.graphics.Color
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

internal class MainViewModelTest {
    private val bluetoothComponent: BluetoothComponent = mockk(relaxed = true)
    private val subject = MainViewModel(bluetoothComponent = bluetoothComponent)

    @Test
    fun `Given reset requested Then calls expected function`() {
        val blockSlot = slot<() -> Unit>()
        every {
            bluetoothComponent.stateMachine?.setState(
                targetState = State.Buzzing,
                block = capture(blockSlot)
            )
        } returns Unit

        every { bluetoothComponent.run { gattComponent!!.requestReset(any()) } } just Runs

        subject.requestReset()

        verify { bluetoothComponent.gattComponent!!.requestReset(any()) }
    }

    @Test
    fun `Given Notification requested Then calls expected function`() {
        every { bluetoothComponent.run { gattComponent!!.requestNotify(any()) } } just Runs

        subject.requestNotify()
        verify { bluetoothComponent.gattComponent!!.requestNotify(any()) }
    }

    @Test
    fun `Given Stop scanning requested Then calls expected function`() {
        every { bluetoothComponent.run { scanComponent!!.stopScanning() } } just Runs

        subject.stopScanning()
        verify { bluetoothComponent.scanComponent!!.stopScanning() }
    }

    @Test
    fun `Given connection requested Then calls expected function`() {
        every { bluetoothComponent.run { scanComponent!!.connectBle() } } just Runs
        subject.connectBle()

        verify { bluetoothComponent.scanComponent!!.connectBle() }
    }

    @Test
    fun `Given Test requested Then calls expected function`() {
        every { bluetoothComponent.run { notificationHelper!!.createAlertNotification() } } just Runs
        subject.sendToForeground()

        verify { bluetoothComponent.serviceFactory!!.start() }
    }

    @Test
    fun `Given Request Posting Value Then emits value`() {

        val screenData = ScreenData(indicatorColor = Color.Black)
        val mocKMutableStateFlow: MutableStateFlow<ScreenData> = mockk(relaxed = true)
        every { mocKMutableStateFlow.tryEmit(screenData) } returns true
        subject.postValue(screenData)
        assertEquals(screenData, subject.screenDataFlow.value)
    }
}