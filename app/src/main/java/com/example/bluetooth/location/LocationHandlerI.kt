package com.example.bluetooth.location

import com.example.bluetooth.database.Coordinates
import com.google.android.gms.location.LocationCallback

interface LocationHandlerI {
    fun setLocation()
    fun fetchCoordinates(): Coordinates?
    fun startLocationUpdate()
    fun getLocationCallback(): LocationCallback
}