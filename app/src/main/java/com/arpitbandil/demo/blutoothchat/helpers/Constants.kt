package com.arpitbandil.demo.blutoothchat.helpers

import com.arpitbandil.demo.blutoothchat.BuildConfig
import java.util.UUID

object Constants {
    // values have to be globally unique
    const val INTENT_ACTION_DISCONNECT = "${BuildConfig.APPLICATION_ID}.Disconnect"
    const val NOTIFICATION_CHANNEL = "${BuildConfig.APPLICATION_ID}.Channel"


    // values have to be unique within each app
    const val NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001

    val BLUETOOTH_SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

}