package io.rocketguys.dokey.network

import android.content.Context
import android.util.Log
import io.rocketguys.dokey.BuildConfig
import net.DEManager
import net.LinkManager
import net.model.DeviceInfo
import java.io.IOException
import java.net.Socket
import io.rocketguys.dokey.network.util.DeviceInfoBuilder
import net.DEDaemon

/**
 * This thread will handle the initial handshake and connection between the mobile
 * device and the computer.
 */
class NetworkThread(val context: Context, val socket : Socket, val key : ByteArray) : Thread() {

    // True if the mobile device is connected to the computer, false otherwise
    var isConnected = false

    // The broadcast manager will handle all the notifications to the application
    // about the network events that occur
    private val broadcastManager = NetworkBroadcastManager(context)

    // The link manager is the component that handle the real communication between
    // the computer and the mobile device
    private var linkManager : LinkManager? = null

    // LISTENERS

    // Called when the connection is closed
    var onConnectionClosed : (() -> Unit)? = null


    override fun run() {
        try {
            linkManager = LinkManager(socket, DeviceInfoBuilder.deviceInfo, BuildConfig.VERSION_CODE,
                    MINIMUM_DESKTOP_VERSION, true, key, false,
                    null, object : DEManager.OnConnectionListener {
                override fun onConnectionStarted(deviceInfo: DeviceInfo, versionNumber: Int) {
                    isConnected = true

                    Log.d(LOG_TAG,"Connected to ${deviceInfo.name}")

                    // Signal the connection event
                    broadcastManager.sendBroadcast(NetworkEvent.CONNECTION_ESTABLISHED_EVENT, deviceInfo.json().toString())
                }

                override fun onInvalidKey() {
                    Log.e(LOG_TAG, "Invalid key, desktop refused to connect")

                    // Signal the invalid key event
                    broadcastManager.sendBroadcast(NetworkEvent.INVALID_KEY_EVENT)

                    // Close the connection
                    socket.close()
                }

                override fun onReceiverVersionTooLow(deviceInfo: DeviceInfo, versionNumber: Int) {
                    Log.e(LOG_TAG, "Desktop version too low: ${deviceInfo.name} VER: $versionNumber")

                    // Signal the problem
                    broadcastManager.sendBroadcast(NetworkEvent.DESKTOP_VERSION_TOO_LOW_EVENT, deviceInfo.json().toString())

                    // Close the connection
                    socket.close()
                }

                override fun onConnectionNotAccepted(deviceInfo: DeviceInfo, versionNumber: Int) {
                    Log.e(LOG_TAG, "Mobile version too low: ${deviceInfo.name} VER: $versionNumber")

                    // Signal the problem
                    broadcastManager.sendBroadcast(NetworkEvent.MOBILE_VERSION_TOO_LOW_EVENT, deviceInfo.json().toString())

                    // Close the connection
                    socket.close()
                }
            })

            // Setup the listener to handle a closed connection
            linkManager?.onConnectionClosedListener = object : DEDaemon.OnConnectionClosedListener {
                override fun onConnectionClosed() {
                    closeConnection()
                }
            }

            // Start the link manager daemon
            linkManager?.startDaemon()
        }catch(e : IOException) {
            e.printStackTrace()
            Log.d(LOG_TAG, "Error opening socket!")

            broadcastManager.sendBroadcast(NetworkEvent.CONNECTION_ERROR_EVENT)
        }
    }

    /**
     * Close the connection
     */
    fun closeConnection() {
        // Stop the link manager
        linkManager?.stopDaemon()

        // Close the socket
        try {
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Reset variables
        linkManager = null
        isConnected = false

        // Send a broadcast
        broadcastManager.sendBroadcast(NetworkEvent.CONNECTION_CLOSED_EVENT)

        // Notify the listener
        onConnectionClosed?.invoke()
    }
}