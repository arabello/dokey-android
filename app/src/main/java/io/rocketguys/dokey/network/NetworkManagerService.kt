package io.rocketguys.dokey.network

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

// This is the minimum desktop version number that
// this version of the app can support
const val MINIMUM_DESKTOP_VERSION = 3

const val LOG_TAG = "NET_MAN_SERVICE"

/**
 * This service will manage the connection to the desktop computer
 * and all the data exchange.
 */
class NetManagerService : Service() {
    private val mBinder : IBinder = NetManagerBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = mBinder

    inner class NetManagerBinder : Binder() {
        val service : NetManagerService
            get() = this@NetManagerService
    }

    fun startConnection() {

    }
}