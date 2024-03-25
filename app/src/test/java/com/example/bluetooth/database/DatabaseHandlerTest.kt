package com.example.bluetooth.database

import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.comms.LoggerWrapper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

private const val targetKey = "123fg"
private const val targetToken = "abc"
private const val failingToken = "efg"

class DatabaseHandlerTest {
    private val bluetoothComponent: BluetoothComponent = mockk(relaxed = true)
    private val firebaseDatabaseWrapper: FirebaseDatabaseWrapper = mockk(relaxed = true)
    private val database: FirebaseDatabase = mockk(relaxed = true)
    private val valueEventListenerWrapper: ValueEventListenerWrapper = mockk(relaxed = true)
    private val loggerWrapper:LoggerWrapper=mockk(relaxed = true)
    private val snapshot: DataSnapshot = mockk(relaxed = true)
    private val error: DatabaseError = mockk(relaxed = true)


    private val coordinates = Coordinates(
        2.2,
        30.1
    )

    private val deviceData = DeviceData(coordinates, targetToken)

    private val newCoordinates = Coordinates(
        3.0,
        5.9
    )
    private val failingDeviceData = DeviceData(coordinates, failingToken)

    val subject = DatabaseHandler(
        bluetoothComponent = bluetoothComponent,
        firebaseDatabaseWrapper = firebaseDatabaseWrapper,
        valueEventListenerWrapper = valueEventListenerWrapper,
        loggerWrapper = loggerWrapper
    )

    @Test
    fun `Given requested When token is not null and userList Empty Then  add it to database`() {
        every { firebaseDatabaseWrapper.getInstance() } returns database
        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns targetToken
        every { bluetoothComponent.locationHandler?.fetchCoordinates() } returns coordinates

        subject.addDeviceData()

        verify {
            subject.getReference().push()
                .setValue(deviceData)
        }
    }

    @Test
    fun `Given requested When token is not null and userList has  this token Then don't add it to database`() {
        every { firebaseDatabaseWrapper.getInstance() } returns database
        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns targetToken
        every { bluetoothComponent.locationHandler?.fetchCoordinates() } returns coordinates

        subject.setDeviceList(hashMapOf(targetKey to deviceData))

        subject.addDeviceData()
        subject.addDeviceData()
        subject.addDeviceData()


        verify(exactly = 0) {
            subject.getReference().push()
                .setValue(deviceData)
        }
    }

    @Test
    fun `Given requested When token  is null  Then don't add to database`() {
        every { firebaseDatabaseWrapper.getInstance() } returns database
        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns null

        subject.addDeviceData()

        verify(exactly = 0) {
            subject.getReference().child("token").child("tokenId").push()
                .setValue(targetToken)
        }
    }

    @Test
    fun `Given updating location When token  is not null  Then  update record`() {
        every { firebaseDatabaseWrapper.getInstance() } returns database
        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns targetToken
        every { bluetoothComponent.locationHandler?.fetchCoordinates() } returns newCoordinates

        subject.setDeviceList(hashMapOf(targetKey to deviceData))
        subject.updateCoordinates()

        verify {
            subject.getReference().child(targetKey).child("location")
                .setValue(
                    newCoordinates
                )
        }
    }

    @Test
    fun `Given updating location When token  is  null  Then  don't update record`() {
        every { firebaseDatabaseWrapper.getInstance() } returns database
        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns null
        every { bluetoothComponent.locationHandler?.fetchCoordinates() } returns newCoordinates

        subject.setDeviceList(hashMapOf(targetKey to deviceData))
        subject.updateCoordinates()

        verify(exactly = 0) {
            subject.getReference().child(targetKey).child("location")
                .setValue(
                    newCoordinates
                )
        }
    }

    @Test
    fun `Given updating location When request hashmap  is  null  Then  don't update record`() {
        every { firebaseDatabaseWrapper.getInstance() } returns database
        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns targetToken
        every { bluetoothComponent.locationHandler?.fetchCoordinates() } returns newCoordinates

        subject.updateCoordinates()

        verify(exactly = 0) {
            subject.getReference().child(targetKey).child("location")
                .setValue(
                    newCoordinates
                )
        }
    }

    @Test
    fun `Given updating location When key not found  Then  don't update record`() {
        every { firebaseDatabaseWrapper.getInstance() } returns database
        every { bluetoothComponent.myFirebaseMessagingService?.getMyToken() } returns targetToken
        every { bluetoothComponent.locationHandler?.fetchCoordinates() } returns newCoordinates

        subject.setDeviceList(hashMapOf(targetKey to failingDeviceData))
        subject.updateCoordinates()

        verify(exactly = 0) {
            subject.getReference().child(targetKey).child("location")
                .setValue(
                    newCoordinates
                )
        }
    }

    @Test
    fun `Given initialised When event listener on change Then delegates`() {
        subject.getValueEventListener().onDataChange(snapshot)
        verify { valueEventListenerWrapper.onDataChange(snapshot) }
    }

    @Test
    fun `Given initialised When event listener on cancel Then delegates`() {
        subject.getValueEventListener().onCancelled(error)
        verify { valueEventListenerWrapper.onCancelled(error) }
    }
}