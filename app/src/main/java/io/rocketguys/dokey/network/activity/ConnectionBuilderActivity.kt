package io.rocketguys.dokey.network.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.rocketguys.dokey.network.NetworkEvent
import json.JSONObject
import net.model.DeviceInfo

abstract class ConnectionBuilderActivity : NetworkActivity() {

    /**
     * Called when a connection with the dokey server has been created correctly.
     */
    abstract fun onConnectionEstablished(serverInfo: DeviceInfo)

    /*
    Error Events
     */

    /**
     * Called when the scanned server is not in the same network of the phone, and thus
     * cannot be connected.
     */
    abstract fun onServerNotInTheSameNetworkError()

    /**
     * Called when an error occurs while trying to connect to the requested server.
     */
    abstract fun onConnectionError()

    /**
     * Called when the key used to connect to a server is invalid and has been refused.
     */
    abstract fun onInvalidKeyError()

    /**
     * Called when the connected desktop server runs an outdated dokey server version,
     * and doesn't met the minimum requirements.
     */
    abstract fun onDesktopVersionTooLowError(serverInfo: DeviceInfo)

    /**
     * Called when the connected desktop server requires a more modern protocol version
     * than the one that the current version of the app can provide.
     * The mobile application must be updated.
     */
    abstract fun onMobileVersionTooLowError(serverInfo: DeviceInfo)

    /**
     * Called when the connection with the desktop server is interrupted.
     */
    abstract fun onConnectionClosed()

    override fun onResume() {
        super.onResume()

        // Register the broadcast listeners
        broadcastManager?.registerReceiver(NetworkEvent.NOT_IN_THE_SAME_NETWORK_ERROR_EVENT, notInTheSameNetworkReceiver)
        broadcastManager?.registerReceiver(NetworkEvent.CONNECTION_ESTABLISHED_EVENT, connectionEstablishedReceiver)
        broadcastManager?.registerReceiver(NetworkEvent.CONNECTION_ERROR_EVENT, connectionErrorReceiver)
        broadcastManager?.registerReceiver(NetworkEvent.INVALID_KEY_EVENT, invalidKeyReceiver)
        broadcastManager?.registerReceiver(NetworkEvent.DESKTOP_VERSION_TOO_LOW_EVENT, desktopVersionTooLowReceiver)
        broadcastManager?.registerReceiver(NetworkEvent.MOBILE_VERSION_TOO_LOW_EVENT, mobileVersionTooLowReceiver)
        broadcastManager?.registerReceiver(NetworkEvent.CONNECTION_CLOSED_EVENT, connectionClosedReceiver)
    }

    override fun onPause() {
        super.onPause()

        // Unregister all the listeners
        broadcastManager?.unregisterReceiver(notInTheSameNetworkReceiver)
        broadcastManager?.unregisterReceiver(connectionEstablishedReceiver)
        broadcastManager?.unregisterReceiver(connectionErrorReceiver)
        broadcastManager?.unregisterReceiver(invalidKeyReceiver)
        broadcastManager?.unregisterReceiver(desktopVersionTooLowReceiver)
        broadcastManager?.unregisterReceiver(mobileVersionTooLowReceiver)
        broadcastManager?.unregisterReceiver(connectionClosedReceiver)
    }

    private val notInTheSameNetworkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onServerNotInTheSameNetworkError()
        }
    }

    private val connectionEstablishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val deviceInfoPayload = intent?.getStringExtra("payload")
            val deviceInfo = DeviceInfo.fromJson(JSONObject(deviceInfoPayload))
            onConnectionEstablished(deviceInfo)
        }
    }

    private val connectionErrorReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onConnectionError()
        }
    }

    private val invalidKeyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onInvalidKeyError()
        }
    }

    private val desktopVersionTooLowReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val deviceInfoPayload = intent?.getStringExtra("payload")
            val deviceInfo = DeviceInfo.fromJson(JSONObject(deviceInfoPayload))
            onDesktopVersionTooLowError(deviceInfo)
        }
    }

    private val mobileVersionTooLowReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val deviceInfoPayload = intent?.getStringExtra("payload")
            val deviceInfo = DeviceInfo.fromJson(JSONObject(deviceInfoPayload))
            onMobileVersionTooLowError(deviceInfo)
        }
    }

    private val connectionClosedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onConnectionClosed()
        }
    }
}