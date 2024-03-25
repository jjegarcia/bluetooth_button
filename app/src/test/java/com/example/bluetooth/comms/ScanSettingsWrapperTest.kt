package com.example.bluetooth.comms

import android.bluetooth.le.ScanSettings
import io.mockk.*
import org.junit.Test

class ScanSettingsWrapperTest {
    val subject = ScanSettingsWrapper()

    @Test
    fun getBuilder() {
        mockkObject(ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY))
        every {ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)  } returns mockk(relaxed = true)
        mockkStatic(ScanSettings.Builder::class)
    every { ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build() } returns mockk()
      subject.getBuilder(ScanSettings.SCAN_MODE_LOW_LATENCY)

        verify {
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
        }
    }
}