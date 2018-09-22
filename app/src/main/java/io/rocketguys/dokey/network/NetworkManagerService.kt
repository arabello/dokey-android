package io.rocketguys.dokey.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import io.rocketguys.dokey.HomeActivity
import io.rocketguys.dokey.R
import io.rocketguys.dokey.network.cache.CommandCache
import io.rocketguys.dokey.network.cache.ImageCache
import io.rocketguys.dokey.network.cache.SectionCache
import json.JSONObject
import model.command.Command
import model.parser.command.TypeCommandParser
import model.parser.component.CachingComponentParser
import model.parser.page.DefaultPageParser
import model.parser.section.DefaultSectionParser
import model.section.Section
import net.LinkManager
import net.model.DeviceInfo
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors

// This is the minimum desktop version number that
// this version of the app can support
const val MINIMUM_DESKTOP_VERSION = 3

const val LOG_TAG = "NET_MAN_SERVICE"

// Dokey server port ranges
const val MIN_PORT = 60642
const val MAX_PORT = 60644

const val SCANNING_PORT_TIMEOUT = 5000

// Notification constants
const val NOTIFICATION_CHANNEL_ID = "dokey_notification_channel_1"
const val SERVICE_NOTIFICATION_ID = 101

// Pending intent constants
const val PENDINT_INTENT_REQUEST_CODE_CONTENT_CLICK = 0
const val PENDINT_INTENT_REQUEST_CODE_DISCONNECT = 1

const val PENDING_INTENT_DISCONNECT_SERVICE = "PENDING_INTENT_DISCONNECT_SERVICE"


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

    var isConnected = false

    // The broadcast manager will handle all the notifications to the application
    // about the network events that occur
    var broadcastManager : NetworkBroadcastManager? = null

    // When connected to a server, this variable will hold the computer details
    private var serverInfo : DeviceInfo? = null

    private var handler : Handler? = null  // Used in the runOnUiThread method

    // Initialize the thread pool
    private val executorService = Executors.newFixedThreadPool(4)

    private var notificationBuilder : NotificationCompat.Builder? = null

    /*
    Parsers
     */
    val commandParser = TypeCommandParser()
    val componentParser = CachingComponentParser()
    val pageParser = DefaultPageParser(componentParser)
    val sectionParser = DefaultSectionParser(pageParser)

    /*
    Caches
     */
    var commandCache : CommandCache? = null
    var sectionCache : SectionCache? = null
    var imageCache : ImageCache? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create the notification channel for ANDROID >= OREO
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Dokey",
                    NotificationManager.IMPORTANCE_LOW)
            notificationChannel.description = "Dokey channel"
            notificationManager.createNotificationChannel(notificationChannel)
        }

        if (notificationBuilder == null) {
            notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_connected)
                    .setContentTitle(getString(R.string.ntf_service_title))
                    .setOngoing(true)
                    .setChannelId(NOTIFICATION_CHANNEL_ID)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentText(getString(R.string.ntf_service_desc))

            val notificationClickIntent = Intent(this, HomeActivity::class.java)
            // Because clicking the notification opens a new ("special") activity, there's
            // no need to create an artificial back stack.
            val notificationClickPendingIntent = PendingIntent.getActivity(this,
                    PENDINT_INTENT_REQUEST_CODE_CONTENT_CLICK, notificationClickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            notificationBuilder?.setContentIntent(notificationClickPendingIntent)

            val disconnectIntent = Intent(this, HomeActivity::class.java)
            disconnectIntent.putExtra(PENDING_INTENT_DISCONNECT_SERVICE, true)
            val disconnectPendingIntent = PendingIntent.getActivity(this,
                    PENDINT_INTENT_REQUEST_CODE_DISCONNECT,
                    disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            notificationBuilder?.addAction(R.drawable.ic_arrow_down_24dp, getString(R.string.ntf_service_action_disconnect), disconnectPendingIntent)
        }

        startForeground(SERVICE_NOTIFICATION_ID, notificationBuilder?.build())

        return Service.START_NOT_STICKY
    }

    /*
    INITIAL CONNECTION METHODS, needed to establish a connection with a dokey server
     */

    /**
     * Begin the connection procedure with a dokey server from the given QR code payload.
     */
    fun beginConnection(payload : String) {
        // If the connection as not already been started
        if (connectionBuilderThread == null && !isConnected) {
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
                scanPorts(parsingResult.address, suggestedPort = parsingResult.suggestedPort, onFound = { // Server was found
                    // Attempt to establish the connection with the dokey server
                    startConnection(it, parsingResult.key)
                }, onNotFound = {  // Server was not found
                    broadcastManager?.sendBroadcast(NetworkEvent.CONNECTION_ERROR_EVENT)
                })

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
            networkThread = NetworkThread(this, socket, key)

            // Setup all the needed network thread listeners
            networkThread!!.onConnectionEstablished = {deviceInfo ->
                isConnected = true

                serverInfo = deviceInfo

                // Reset the caches
                commandCache = CommandCache(this@NetworkManagerService, commandParser, deviceInfo.id)
                sectionCache = SectionCache(this@NetworkManagerService, sectionParser, deviceInfo.id)
                imageCache = ImageCache(this@NetworkManagerService, deviceInfo.id)

                updateNotificationMessage(getString(R.string.ntf_service_desc_connected, deviceInfo.name))

                Log.d("CACHE", "Setup")
            }
            networkThread!!.onConnectionClosed = {
                // Reset the network thread
                networkThread = null
                isConnected = false

                stopSelf()
            }

            // Start the network thread
            networkThread!!.start()
            return true
        }

        return false
    }

    // Used in the "scanPorts" method as a counter of the results
    private var currentPortScanCount = 0
    private var hasServerBeenFound = false

    /**
     * Check all known dokey ports in the default range to find a valid dokey server.
     * The function is asynchronous and creates a new thread for each port to scan.
     * The result of the analysis is delivering by calling one of the callbacks.
     * onFound() is called if a dokey server is found.
     * onNotFound() is called if no server is found after analyzing all the ports
     */
    private fun scanPorts(address: String, suggestedPort: Int, onFound : (Socket) -> Unit, onNotFound : () -> Unit) {
        // Create the set of possible dokey ports
        val ports = mutableSetOf<Int>()
        ports.addAll(MIN_PORT..MAX_PORT)
        ports.add(suggestedPort)

        // Reset state variables
        currentPortScanCount = 0
        hasServerBeenFound = false

        // Cycle through all ports
        for (port in ports) {
            Thread {
                val socket = scanPort(address, port)
                if (socket != null) {
                    var alreadyFound = false

                    synchronized(this@NetworkManagerService) {
                        alreadyFound = hasServerBeenFound
                    }

                    if (!alreadyFound) {
                        onFound(socket)
                    }
                }else{
                    synchronized(this@NetworkManagerService) {
                        currentPortScanCount++
                        if (currentPortScanCount > (MAX_PORT - MIN_PORT)) {
                            onNotFound()
                        }
                    }
                }
            }.start()
        }
    }

    /**
     * Scan the given combination of address/port to find if a Dokey server is available.
     *
     * @return an open socket to the dokey server if found, null otherwise.
     */
    private fun scanPort(address: String, port: Int): Socket? {
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
            Log.d(LOG_TAG, e.message)
        }

        return null
    }

    /**
     * Close the current connection and reset the service.
     */
    fun closeConnection() {
        networkThread?.closeConnection()
        networkThread = null
    }

    /*
    APP LEVEL METHODS, needed to interact with the dokey server
     */

    /**
     * Request the command with the given id. The function will run asynchronously and
     * when the command is available the "callback" function will be called.
     * If the command cannot be found, the callback function will be called with
     * a null argument.
     */
    fun requestCommand(id: Int, forceCache : Boolean = false, callback: (Command?) -> Unit) {
        // Make the request
        executorService.execute {
            // At first, check if the command is available in the cache
            val cachedCommand = commandCache?.getCommand(id)

            val requestBody = JSONObject()
            requestBody.put("id", id)

            // If there is a cached command, send also the last edit to check if it is up to date
            if (cachedCommand != null) {
                requestBody.put("lastEdit", cachedCommand.lastEdit)
            }

            if (forceCache) { // Use cached version without requesting it
                runOnUiThread(Runnable {
                    callback(cachedCommand)
                })
                return@execute
            }

            networkThread?.linkManager?.requestService("get_command", requestBody, object : ServiceResponseAdapter() {
                override fun onServiceResponse(responseBody: JSONObject?) {
                    // Decode the received command
                    val found = responseBody!!.getBoolean("found")
                    if (!found) {  // Command not found, empty callback
                        runOnUiThread(Runnable {
                            callback(null)
                        })
                    }else{  // Command found
                        val upToDate = responseBody.getBoolean("up")
                        if (upToDate) {  // Cached command is up to date, return that one
                            runOnUiThread(Runnable {
                                callback(cachedCommand)
                            })
                        }else{
                            // Cached command is not up to date, decode the set one and update the cache
                            val receivedCommandJson = responseBody.getJSONObject("command")
                            val receivedCommand = commandParser.fromJSON(receivedCommandJson)

                            // Update the cache
                            commandCache?.saveCommand(receivedCommand)

                            // Notify the listener
                            runOnUiThread(Runnable {
                                callback(receivedCommand)
                            })
                        }
                    }
                }
            })
        }
    }

    /**
     * Request the section with the given id. The function will run asynchronously and
     * when the section is available the "callback" function will be called.
     * If the section cannot be found, the callback function will be called with
     * a null argument.
     *
     * @param forceCache if true, only use the cached version of the section without
     *                   requesting it from the server.
     *
     * @param remoteLastEdit if set, represents the remote section last edit. This is used
     *                       to determine if the section can be taken directly from the cache
     *                       without requesting it to the server.
     */
    fun requestSection(id: String, forceCache : Boolean = false, remoteLastEdit: Long = Long.MAX_VALUE,
                       callback: (Section?) -> Unit) {
        // Make the request
        executorService.execute {
            // At first, check if the section is available in the cache
            val cachedSection = sectionCache?.getSection(id)

            val requestBody = JSONObject()
            requestBody.put("id", id)

            // If there is a cached section, send also the last edit to check if it is up to date
            if (cachedSection != null) {
                requestBody.put("lastEdit", cachedSection.lastEdit)
            }

            // Use cached version without requesting it
            if (forceCache || remoteLastEdit <= cachedSection?.lastEdit ?: 0) {
                runOnUiThread(Runnable {
                    callback(cachedSection)
                })
                return@execute
            }

            // Request the section to the server
            networkThread?.linkManager?.requestService("get_section", requestBody, object : ServiceResponseAdapter() {
                override fun onServiceResponse(responseBody: JSONObject?) {
                    // Decode the received section
                    val found = responseBody!!.getBoolean("found")
                    if (!found) {  // Command not found, empty callback
                        runOnUiThread(Runnable {
                            callback(null)
                        })
                    }else{  // Command found
                        val upToDate = responseBody.getBoolean("up")
                        if (upToDate) {  // Cached section is up to date, return that one
                            runOnUiThread(Runnable {
                                callback(cachedSection)
                            })
                        }else{
                            // Cached section is not up to date, decode the set one and update the cache
                            val receivedSectionJson = responseBody.getJSONObject("section")
                            val receivedSection = sectionParser.fromJSON(receivedSectionJson)

                            // Update the cache
                            sectionCache?.saveSection(receivedSection)

                            // Notify the listener
                            runOnUiThread(Runnable {
                                callback(receivedSection)
                            })
                        }
                    }
                }
            })
        }
    }

    /**
     * Request the image with the given id. The function will run asynchronously and
     * when the image is available the "callback" function will be called.
     * If the image cannot be found, the callback function will be called with
     * a null argument.
     */
    fun requestImage(id: String, callback: (imageId: String, imageFile: File?) -> Unit) {
        // Make the request
        executorService.execute {
            // At first, check if the image is available in the cache
            val cachedImage = imageCache?.getImageFile(id)

            // If there is a cached image, return immediately
            if (cachedImage != null) {
                runOnUiThread(Runnable {
                    callback(id, cachedImage)
                })
            }else{  // No cached image, request it from the server
                networkThread?.linkManager?.requestImage(id, object : LinkManager.OnImageResponseListener {
                    override fun onImageReceived(imageIdentifier: String, iconFile: File) {
                        // Image found, save it in the cache
                        val cachedImageFile = imageCache?.saveImage(imageIdentifier, iconFile)

                        // Return the image
                        runOnUiThread(Runnable {
                            callback(imageIdentifier, cachedImageFile)
                        })
                    }

                    override fun onImageNotFound(imageIdentifier: String) {
                        // Image not found, return an empty callback
                        runOnUiThread(Runnable {
                            callback(imageIdentifier, null)
                        })
                    }
                })
            }
        }
    }

    /**
     * Execute the given command in the connected dokey desktop.
     */
    fun executeCommand(command: Command) {
        executorService.execute {
            networkThread?.linkManager?.sendCommand(command, null)
        }
    }

    /**
     * Convenience class to work with service responses.
     */
    open class ServiceResponseAdapter : LinkManager.OnServiceResponseListener {
        override fun onServiceError() {}
        override fun onServiceResponse(responseBody: JSONObject?) {}
    }

    /**
     * Request the list of currently active applications.
     */
    fun requestActiveApps(callback: (List<App>) -> Unit) {
        // Make the request
        executorService.execute {
            // Request the list to the server
            networkThread?.linkManager?.requestService("active_app_list", null, object : ServiceResponseAdapter() {
                override fun onServiceResponse(responseBody: JSONObject?) {
                    val apps = responseBody!!.getJSONArray("apps")

                    val outputApps = mutableListOf<App>()

                    for (jsonApp in apps) {
                        jsonApp as JSONObject
                        val app = App(jsonApp.getString("name"), jsonApp.getString("path"))
                        outputApps.add(app)
                    }

                    runOnUiThread(Runnable {
                        callback(outputApps)
                    })
                }
            })
        }
    }

    /**
     * Request to give focus to the given app.
     */
    fun requestAppFocus(app: App) {
        // Make the request
        executorService.execute {
            val requestBody = JSONObject()
            requestBody.put("app", app.path)

            networkThread?.linkManager?.requestService("focus_app", requestBody, null)
        }
    }

    /**
     * This class represents an application and it is used in the active app request.
     */
    inner class App(val name: String, val path: String) {
        /**
         * Request the icon of the current image, using the "requestImage" method of
         * the NetworkManagerService
         */
        fun requestIcon(callback: (imageId: String, imageFile: File?) -> Unit) {
            requestImage("app:$path", callback)
        }

        /**
         * Request focus to the current app.
         */
        fun requestFocus() {
            requestAppFocus(this)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as App

            if (name != other.name) return false
            if (path != other.path) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + path.hashCode()
            return result
        }

        override fun toString(): String {
            return "App(name='$name', path='$path')"
        }
    }

    /*
    NOTIFICATION RELATED
     */

    private fun updateNotificationMessage(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder?.setContentText(text)
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notificationBuilder?.build())
    }

    /*
    SERVICE BLOATWARE, needed to make everything work. Not very useful though.
     */

    private val mBinder : IBinder = NetworkManagerBinder()

    override fun onCreate() {
        handler = Handler()

        super.onCreate()

        broadcastManager = NetworkBroadcastManager(this.applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder = mBinder

    inner class NetworkManagerBinder : Binder() {
        val service : NetworkManagerService
            get() = this@NetworkManagerService
    }

    private fun runOnUiThread(runnable: Runnable) {
        handler?.post(runnable)
    }
}