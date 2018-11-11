package io.rocketguys.dokey.network.usb

import json.JSONObject
import net.model.DeviceInfo
import java.io.DataInputStream
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.SocketException
import java.nio.charset.Charset

const val USB_DISCOVERY_PORT = 34730  // Port used to detect a usb connection from the phone
const val USB_DISCOVERY_CHECK_INTERVAL = 500L // How often to check for a USB connection

/**
 * Used to detect if the phone is connected with an USB cable to a server
 */
class USBDetectionDaemon() : Thread() {
    // Called when a USB connection has been detected.
    var onUSBConnectionDetected : ((USBConnectionPayload) -> Unit)? = null

    @Volatile private var shouldStop = false

    override fun run() {
        val localhost = InetAddress.getByName("localhost")

        while (!shouldStop) {
            try {
                val socket = Socket(localhost, USB_DISCOVERY_PORT)
                socket.soTimeout = 2

                // Read the content
                val din = DataInputStream(socket.getInputStream())

                // Read the server port
                val serverPort = din.readInt()

                // Read the key
                val keyLength = din.readInt()
                val key = ByteArray(keyLength)
                din.readFully(key, 0, keyLength)

                // Read the device info
                val deviceInfoPayloadSize = din.readInt()
                val deviceInfoBuffer = ByteArray(deviceInfoPayloadSize)
                din.readFully(deviceInfoBuffer, 0, deviceInfoPayloadSize)
                val deviceInfoPayload = String(deviceInfoBuffer, Charset.forName("UTF-8"))
                val deviceInfo = DeviceInfo.fromJson(JSONObject(deviceInfoPayload))

                val usbPayload = USBConnectionPayload(localhost.hostAddress, serverPort, key, deviceInfo)

                // Notify the listener
                onUSBConnectionDetected?.invoke(usbPayload)

                // Exit the loop
                break
            }catch (e: SocketException) { e.printStackTrace()
            }catch (e: IOException) { e.printStackTrace() }

            Thread.sleep(USB_DISCOVERY_CHECK_INTERVAL)
        }
    }

    fun stopDiscovery() {
        shouldStop = true
    }
}