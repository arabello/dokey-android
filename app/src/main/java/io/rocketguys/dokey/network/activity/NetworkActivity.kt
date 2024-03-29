package io.rocketguys.dokey.network.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import io.rocketguys.dokey.network.NetworkBroadcastManager
import io.rocketguys.dokey.network.NetworkManagerService

/**
 * This class provides the basic functions to automatically bind to
 * the network manager service.
 * An activity that must interact with the desktop application must
 * extend this class.
 */
abstract class NetworkActivity : AppCompatActivity() {
    // Network related variables
    var isBound = false
    var serviceStarted = false
    var networkManagerService : NetworkManagerService? = null

    // Used to manage network related events
    var broadcastManager : NetworkBroadcastManager? = null

    /**
     * Called when the NetworkManagerService has been bound to the activity.
     * This is a the place where the interaction with the service can begin.
     */
    abstract fun onServiceConnected()

    /**
     * Start the networking service.
     */
    fun startNetworkService() {
        // Start the Service
        val intent = Intent(this, NetworkManagerService::class.java)

        // Based on the SDK version, start the service as a foreground service or not.
        // Check out new background limits on oreo:
        // https://developer.android.com/about/versions/oreo/background.html
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        serviceStarted = true
    }

    /**
     * Stop the networking service
     */
    fun stopNetworkService() {
        // Stop the Service
        val intent = Intent(this, NetworkManagerService::class.java)
        networkManagerService?.closeConnection()
        serviceStarted = false
        stopService(intent)
    }

    private fun bindNetworkService() {
        if (!isBound) {
            val intent = Intent(this, NetworkManagerService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }

    /**
     * Used to bind/unbind the NetworkManagerService
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as NetworkManagerService.NetworkManagerBinder
            networkManagerService = binder.service

            onServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            networkManagerService = null
        }
    }

    override fun onStart() {
        // Request to bind the service
        bindNetworkService()

        super.onStart()
    }

    override fun onStop() {
        // Unbind the service
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }

        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the broadcast manager
        broadcastManager = NetworkBroadcastManager(this.applicationContext)
    }
}