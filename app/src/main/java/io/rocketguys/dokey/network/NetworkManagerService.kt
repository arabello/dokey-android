package io.rocketguys.dokey.network

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.DataInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

// This is the minimum desktop version number that
// this version of the app can support
const val MINIMUM_DESKTOP_VERSION = 3

const val LOG_TAG = "NET_MAN_SERVICE"

// Dokey server port ranges
const val MIN_PORT = 60642
const val MAX_PORT = 60652

const val SCANNING_PORT_TIMEOUT = 3000

/**
 * This service will manage the connection to the desktop computer
 * and all the data exchange.
 */
class NetworkManagerService : Service() {
    /**
     * These numbers are sent directly when a new connection is created and makes possible for the receiver to
     * check if the connection is from a dokey server.
     */
    val DOKEY_NUMBERS = byteArrayOf(123, 11, 78, 23)

    private var networkThread : NetworkThread? = null
    private var connectionBuilderThread : Thread? = null

    // The broadcast manager will handle all the notifications to the application
    // about the network events that occur
    private var broadcastManager : NetworkBroadcastManager? = null

    /**
     * Begin the connection procedure with a dokey server from the given QR code payload.
     */
    fun beginConnection(payload : String) {
        // If the connection as not already been started
        if (connectionBuilderThread == null) {
            connectionBuilderThread = Thread {
                // Parse the payload to detect the correct ip address and key
                val networkPayloadResolver = NetworkPayloadResolver()
                val parsingResult = networkPayloadResolver.parse(payload)

                // Make sure a valid ip was found. If not, signal the error
                if (parsingResult == null) {
                    broadcastManager?.sendBroadcast(NetworkEvent.NOT_IN_THE_SAME_NETWORK_ERROR_EVENT)
                    return@Thread
                }

                // Scan the given address to find the correct port and create a valid socket
                val socket = scanPorts(parsingResult.address)

                // Make sure a valid server has been found
                if (socket == null) {
                    broadcastManager?.sendBroadcast(NetworkEvent.CONNECTION_ERROR_EVENT)
                    return@Thread
                }

                // Attempt to establish the connection with the dokey server
                startConnection(socket, parsingResult.key)

                // Reset the thread
                connectionBuilderThread = null
            }
            connectionBuilderThread?.start()
        }
    }

    /**
     * Attempt to start a connection with the given socket.
     *
     * @return false if the connection has already been started before, true otherwise.
     */
    private fun startConnection(socket: Socket, key: ByteArray) : Boolean {
        // Make sure the network thread has not been started yet
        if (networkThread == null) {
            // Create the network thread
            networkThread = NetworkThread(this.applicationContext, socket, key)

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

    /**
     * Check all known dokey ports in the default range until a valid dokey server is found
     * or all the ports are invalidated.
     *
     * @return an open socket to the dokey server if found, null otherwise.
     */
    private fun scanPorts(address: String): Socket? {
        // Cycle through all ports
        for (port in MIN_PORT..MAX_PORT) {
            Log.d(LOG_TAG, "Scanning address: $address with port: $port")
            try {
                // Attempt to open a socket with the given address
                val socket = Socket()
                socket.connect(InetSocketAddress(address, port), SCANNING_PORT_TIMEOUT)

                // Read the magic numbers to make sure there is a dokey server in the other side.
                var initialTime = System.currentTimeMillis()
                var isValid = true
                var currentDokeyNumberIndex = 0

                while((System.currentTimeMillis() - initialTime) < SCANNING_PORT_TIMEOUT) {
                    if (socket.getInputStream().available() > 0) {
                        val currentByte = socket.getInputStream().read()
                        if (currentByte != -1) {
                            if (DOKEY_NUMBERS[currentDokeyNumberIndex] == currentByte.toByte()) {
                                currentDokeyNumberIndex++

                                if (currentDokeyNumberIndex == 4) {
                                    break
                                }
                            }else{
                                isValid = false
                                break
                            }
                        }
                    }

                    Thread.sleep(100)
                }

                if (isValid){
                    return socket
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return null
    }


    /*
    SERVICE BLOATWARE, needed to make everything work. Not very useful though.
     */

    private val mBinder : IBinder = NetworkManagerBinder()

    override fun onCreate() {
        super.onCreate()

        broadcastManager = NetworkBroadcastManager(this.applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = mBinder

    inner class NetworkManagerBinder : Binder() {
        val service : NetworkManagerService
            get() = this@NetworkManagerService
    }
}