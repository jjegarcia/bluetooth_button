package com.example.bluetooth.comms

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.MainActivity
import com.example.bluetooth.messaging.ToastWrapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

private const val DEVICE_ADDRESS = "04:91:62:97:83:78"

class ScanComponentTest {
    private val bluetoothComponent: BluetoothComponent = mockk(relaxed = true)
    private val bluetoothAdapter: BluetoothAdapter = mockk(relaxed = true)
    private val mainActivity: MainActivity = mockk(relaxed = true)
    private val resultWrapper: ScanResultWrapper = mockk(relaxed = true)
    private val scanSettingsWrapper: ScanSettingsWrapper = mockk(relaxed = true)
    private val activityCompactWrapper: ActivityCompactWrapper = mockk(relaxed = true)
    private val loggerWrapper: LoggerWrapper = mockk(relaxed = true)
    private val bluetoothDevice: BluetoothDevice = mockk(relaxed = true)
    private val scanRecord: ScanRecord = mockk(relaxed = true)
    private val toastWrapper: ToastWrapper = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)


    private val subject = ScanComponent(
        bluetoothComponent = bluetoothComponent,
        bluetoothAdapter = bluetoothAdapter,
        resultWrapper = resultWrapper,
        scanSettingsWrapper = scanSettingsWrapper,
        activityCompatWrapper = activityCompactWrapper,
        loggerWrapper = loggerWrapper,
        toastWrapper = toastWrapper,
        context = context
    )

    @Test
    fun `Given callback with scan results When new device detected Then add to list`() {
        val callBackType = 3
        val result = ScanResult(bluetoothDevice, 1, 1, 1, 1, 1, 1, 1, scanRecord, 1)
        every { resultWrapper.getDevice(any()) } returns bluetoothDevice

        subject.scanCallback.onScanResult(callBackType, result)

        assertFalse(subject.getDevices().isEmpty())
        assertTrue(subject.getDevices().size == 1)
    }

    @Test
    fun `Given callback with scan results When not new device detected Then don't add to list`() {
        val callBackType = 3
        val result = ScanResult(bluetoothDevice, 1, 1, 1, 1, 1, 1, 1, scanRecord, 1)
        every { resultWrapper.getDevice(any()) } returns bluetoothDevice

        subject.scanCallback.onScanResult(callBackType, result)
        subject.scanCallback.onScanResult(callBackType, result)

        assertTrue(subject.getDevices().size == 1)

    }

    @Test
    fun `Given request to connect When target device is found and permitted Then connect`() {
        val callBackType = 3
        every { bluetoothDevice.address } returns DEVICE_ADDRESS
        val result = ScanResult(bluetoothDevice, 1, 1, 1, 1, 1, 1, 1, scanRecord, 1)
        every { resultWrapper.getDevice(any()) } returns bluetoothDevice

        subject.scanCallback.onScanResult(callBackType, result)

        subject.connectBle()

        verify { subject.getDevices()[0].connectGatt(any(), any(), any()) }
    }

    @Test
    fun `Given request to connect When target device is not found Then connect`() {
        val callBackType = 3
        every { bluetoothDevice.address } returns "123"
        val result = ScanResult(bluetoothDevice, 1, 1, 1, 1, 1, 1, 1, scanRecord, 1)
        every { resultWrapper.getDevice(any()) } returns bluetoothDevice

        subject.scanCallback.onScanResult(callBackType, result)

        subject.connectBle()

        verify(exactly = 0) { subject.getDevices()[0].connectGatt(any(), any(), any()) }
    }

    @Test
    fun `Given request to connect When target device is but not permissions found Then connect`() {
        val callBackType = 3
        every { bluetoothDevice.address } returns DEVICE_ADDRESS
        val result = ScanResult(bluetoothDevice, 1, 1, 1, 1, 1, 1, 1, scanRecord, 1)
        every { resultWrapper.getDevice(any()) } returns bluetoothDevice
        every {
            activityCompactWrapper.checkSelfPermission(
                any(),
                any()
            )
        } returns PackageManager.SIGNATURE_NO_MATCH
        subject.scanCallback.onScanResult(callBackType, result)

        subject.connectBle()

        verify(exactly = 0) { subject.getDevices()[0].connectGatt(any(), any(), any()) }
    }

    @Test
    fun `Given stop scanning requested Then stops scanning invoked`() {

        subject.stopScanning()

        verify { subject.bleScanner.stopScan(subject.scanCallback) }
    }

    @Test
    fun `Given initialise requested Then start scanning invoked`() {

        subject.initialise()

        verify {
            subject.bleScanner.startScan(
                null,
                scanSettingsWrapper.getBuilder(ScanSettings.SCAN_MODE_LOW_LATENCY),
                subject.scanCallback
            )
        }
    }

    @Test
    fun `Given checkEnabled requested When adapter is not enabled Then reports issue`() {
        every { bluetoothAdapter.isEnabled } returns false
        subject.checkEnabled()

        verify {
            loggerWrapper.log("Bluetooth App", "Missing Adapter")
        }
    }

    @Test
    fun `Given checkEnabled requested When adapter is  enabled Then won't report issue`() {
        every { bluetoothAdapter.isEnabled } returns true
        subject.checkEnabled()

        verify(exactly = 0) {
            loggerWrapper.log("Bluetooth App", "Missing Adapter")
        }
    }
}