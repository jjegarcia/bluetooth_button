package com.example.bluetooth.comms


import android.bluetooth.le.ScanResult
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class ScanResultWrapperTest {

    val subject = ScanResultWrapper()
    val mockScanResult: ScanResult = mockk(relaxed = true)

    @Test
    fun getDevice() {

        subject.getDevice(mockScanResult)
        verify { mockScanResult.device }
    }
}