package com.rutasdeautobuses.transmileniositp.service

import android.content.Context
import com.huawei.wearengine.HiWear
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WatchService @Inject constructor(@ApplicationContext private val context: Context) {
    private val deviceClient = HiWear.getDeviceClient(context)
    private val authClient = HiWear.getAuthClient(context)
    private val p2pClient = HiWear.getP2pClient(context)

    private fun hasAvailableDevices() {
        deviceClient.hasAvailableDevices().addOnSuccessListener {
            hasAvailable ->

        }.addOnFailureListener {
        }
    }
}