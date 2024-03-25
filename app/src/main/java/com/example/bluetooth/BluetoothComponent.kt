package com.example.bluetooth

import com.example.bluetooth.comms.GattComponent
import com.example.bluetooth.comms.ScanComponentI
import com.example.bluetooth.database.DatabaseHandlerI
import com.example.bluetooth.location.LocationHandlerI
import com.example.bluetooth.messaging.MyFirebaseMessagingServiceI
import com.example.bluetooth.notifications.NotificationHelperI
import com.example.bluetooth.service.ServiceFactoryI
import com.example.bluetooth.wearable.ClientHandlerI

class BluetoothComponent {
    var notificationHelper: NotificationHelperI? = null
    var clientHandler: ClientHandlerI? = null
    var scanComponent: ScanComponentI? = null
    var gattComponent: GattComponent? = null
    var databaseHandler: DatabaseHandlerI? = null
    var myFirebaseMessagingService: MyFirebaseMessagingServiceI? = null
    var viewModel: MainViewModel? = null
    var stateMachine: StateMachineI? = null
    var locationHandler: LocationHandlerI? = null
    var serviceFactory: ServiceFactoryI? = null
}