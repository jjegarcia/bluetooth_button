package com.example.bluetooth.location

import android.content.Context
import android.location.Location
import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.comms.LoggerWrapper
import com.example.bluetooth.database.Coordinates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import io.mockk.*
import junit.framework.TestCase.assertNotSame
import org.junit.Before
import org.junit.Test

private const val LATITUDE = 40.5
private const val LONGITUDE = 7.9

class LocationHandlerTest {
    private val mockContext: Context = mockk(relaxed = true)
    private val mockLocationServicesWrapper: LocationServicesWrapper = mockk(relaxed = true)
    private val mockLocation: Location = mockk(relaxed = true)
    private val mockLocationResult: LocationResult = mockk(relaxed = true)
    private val mockTask: Task<Location> = mockk(relaxed = true)
    private val mockFusedLocationProviderClient: FusedLocationProviderClient = mockk()
    private val loggerWrapper: LoggerWrapper = mockk(relaxed = true)
    private val bluetoothComponent: BluetoothComponent = mockk(relaxed = true)
    val subject =
        LocationHandler(
            context = mockContext,
            bluetoothComponent = bluetoothComponent,
            locationServicesWrapper = mockLocationServicesWrapper,
            loggerWrapper = loggerWrapper
        )

    @Before
    fun setUp() {
        every { mockLocationServicesWrapper.getFusedLocationProviderClient(mockContext) } returns
                mockFusedLocationProviderClient
    }


    @Test
    fun `Given callback for getting last location requested When successful response Then update coordinates`() {
        val locationTaskSlot = slot<Task<Location>>()
        val blockSlot = slot<(Location) -> Unit>()
        mockTask.apply {
            every { isSuccessful } returns true
        }

        mockLocation.apply {
            every { latitude } returns LATITUDE
            every { longitude } returns LONGITUDE
        }

        mockLocationServicesWrapper.apply {
            every { getLastLocation(any()) } returns mockTask
            every {
                addOnSuccessListener(
                    locationTask = capture(locationTaskSlot),
                    block = capture(blockSlot)
                )
            } returns mockTask
        }

        subject.setLocation()

        verify {
            subject.locationServicesWrapper.addOnSuccessListener(
                locationTask = locationTaskSlot.captured,
                block = blockSlot.captured
            )
        }
    }

    @Test
    fun `Given callback for getting last location requested When unsuccessful response Then won't update coordinates`() {
        val locationTaskSlot = slot<Task<Location>>()
        val blockSlot = slot<(Location) -> Unit>()
        mockTask.apply {
            every { isSuccessful } returns false
            every { exception } returns Exception()
        }

        mockLocation.apply {
            every { latitude } returns LATITUDE
            every { longitude } returns LONGITUDE
        }

        mockLocationServicesWrapper.apply {
            every { getLastLocation(any()) } returns mockTask
            every {
                addOnSuccessListener(
                    locationTask = capture(locationTaskSlot),
                    block = capture(blockSlot)
                )
            } returns mockTask
        }

        subject.setLocation()

        verify {
            subject.locationServicesWrapper.addOnSuccessListener(
                locationTask = locationTaskSlot.captured,
                block = blockSlot.captured
            )
        }

        blockSlot.invoke(mockLocation)
        assertNotSame(Coordinates(LATITUDE, LONGITUDE), subject.fetchCoordinates())
    }

    @Test
    fun `Given callback for getting last location requested When successful response but same location Then won't update coordinates`() {
        val locationTaskSlot = slot<Task<Location>>()
        val blockSlot = slot<(Location) -> Unit>()
        mockTask.apply {
            every { isSuccessful } returns true
        }

        mockLocation.apply {
            every { latitude } returns LATITUDE
            every { longitude } returns LONGITUDE
        }

        mockLocationServicesWrapper.apply {
            every { getLastLocation(any()) } returns mockTask
            every {
                addOnSuccessListener(
                    locationTask = capture(locationTaskSlot),
                    block = capture(blockSlot)
                )
            } returns mockTask
        }

        subject.setLocation()

        verify {
            subject.locationServicesWrapper.addOnSuccessListener(
                locationTask = locationTaskSlot.captured,
                block = blockSlot.captured
            )
        }

        blockSlot.invoke(mockLocation)
        blockSlot.invoke(mockLocation)
        assertNotSame(Coordinates(LATITUDE, LONGITUDE), subject.fetchCoordinates())
        verify(exactly = 0) { mockLocationServicesWrapper.requestDatabaseUpdate() }
    }

    @Test
    fun `Given location updated Then delegates to wrapper`() {
        subject.startLocationUpdate()

        verify {
            mockLocationServicesWrapper.startLocationUpdate(
                fusedLocationProviderClient = any(),
                locationRequest = any(),
                locationCallback = any()
            )
        }
    }

    @Test
    fun `Given location set Then delegates to wrapper`() {

        subject.getLocationCallback().onLocationResult(mockLocationResult)

        verify {
            mockLocationServicesWrapper.getLastLocation(any())
            mockLocationServicesWrapper.addOnSuccessListener(any(), any())
            bluetoothComponent.databaseHandler?.updateCoordinates()
        }
    }
}