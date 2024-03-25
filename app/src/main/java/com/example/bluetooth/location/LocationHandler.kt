package com.example.bluetooth.location

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.bluetooth.BluetoothComponent
import com.example.bluetooth.comms.LoggerWrapper
import com.example.bluetooth.database.Coordinates
import com.google.android.gms.location.*
import com.google.android.gms.location.Granularity.*
import com.google.android.gms.location.Priority.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task

private const val TIME_INTERVAL = 5000L
private const val MINIMAL_DISTANCE = 5f

class LocationHandler constructor(
    val context: Context,
    val bluetoothComponent: BluetoothComponent,
    val locationServicesWrapper: LocationServicesWrapper,
    val loggerWrapper: LoggerWrapper,
) : LocationHandlerI {
    private var fusedLocationProviderClient: FusedLocationProviderClient =
        locationServicesWrapper.getFusedLocationProviderClient(context)
    private var coordinates: Coordinates? = null
    private var locationRequest: LocationRequest =
        LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, TIME_INTERVAL).apply {
            setMinUpdateDistanceMeters(MINIMAL_DISTANCE)
            setGranularity(GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            setLocation()
            bluetoothComponent.databaseHandler?.updateCoordinates()
        }
    }


    @SuppressLint("MissingPermission")
    override fun setLocation() {
        with(locationServicesWrapper) {
            val locationTask = getLastLocation(fusedLocationProviderClient)
            addOnSuccessListener(
                locationTask = locationTask,
                block = { location: Location ->
                    if (!locationTask.isSuccessful) {
                        loggerWrapper.log(
                            ContentValues.TAG,
                            "Location Services Exception: ${locationTask.exception}"
                        )
                        return@addOnSuccessListener
                    }
                    val locationCandidate = Coordinates(location.latitude, location.longitude)
                    loggerWrapper.log("Test", "Candidate location $locationCandidate")
                    if (locationCandidate != fetchCoordinates()) {
                        coordinates = Coordinates(location.latitude, location.longitude)
                        loggerWrapper.log("Test", "Updated Location $coordinates")
                        bluetoothComponent.databaseHandler?.updateCoordinates()
                    }
                }
            )
        }
    }


    @SuppressLint("MissingPermission")
    override fun startLocationUpdate() {
        locationServicesWrapper.startLocationUpdate(
            fusedLocationProviderClient = fusedLocationProviderClient,
            locationRequest = locationRequest,
            locationCallback = locationCallback
        )
    }

    override fun getLocationCallback(): LocationCallback = locationCallback

    override fun fetchCoordinates(): Coordinates? = coordinates

    init {
        setLocation()
        LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    }
}

class LocationServicesWrapper(
    private val onSuccessListenerWrapper: OnSuccessListenerWrapper,
    private val bluetoothComponent: BluetoothComponent,
) {
    fun getFusedLocationProviderClient(context: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getLastLocation(fusedLocationProviderClient: FusedLocationProviderClient): Task<Location> =
        fusedLocationProviderClient.lastLocation

    fun addOnSuccessListener(
        locationTask: Task<Location>,
        block: (Location) -> Unit
    ): Task<Location> =
        locationTask.addOnSuccessListener(onSuccessListenerWrapper.onSuccessListener(block))

    @SuppressLint("MissingPermission")
    fun startLocationUpdate(
        fusedLocationProviderClient: FusedLocationProviderClient,
        locationRequest: LocationRequest,
        locationCallback: LocationCallback
    ) {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        bluetoothComponent.locationHandler?.setLocation()
    }

    fun requestDatabaseUpdate() {
        bluetoothComponent.databaseHandler?.updateCoordinates()
    }
}

class OnSuccessListenerWrapper {
    fun onSuccessListener(block: (Location) -> Unit): OnSuccessListener<Location> {
        return OnSuccessListener(block)
    }
}