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
class NetworkManagerService : Service() {
    private var networkThread : NetworkThread? = null

    /**
     * Attempt to start a connection with the given address.
     *
     * @return false if the connection has already been started before, true otherwise.
     */
    fun startConnection(address: String, port: Int, key: ByteArray) : Boolean {
        // Make sure the network thread has not been started yet
        if (networkThread == null) {
            // Create the network thread
            networkThread = NetworkThread(this.applicationContext, address, port, key)

            // Setup all the needed network thread listeners
            networkThread!!.onConnectionClosed = {
                // Reset the network thread
                networkThread = null
            }

            // Start the network thread
            networkThread!!.start()
            return true
        }

        return false
    }


    /*
    SERVICE BLOATWARE, needed to make everything work. Not very useful though.
     */

    private val mBinder : IBinder = NetworkManagerBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = mBinder

    inner class NetworkManagerBinder : Binder() {
        val service : NetworkManagerService
            get() = this@NetworkManagerService
    }
}