package io.rocketguys.dokey.view

import android.util.Log
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import io.rocketguys.dokey.network.activity.NetworkActivity
import net.model.DeviceInfo

class ConnectActivity : ConnectionBuilderActivity() {
    override fun onServiceConnected() {
        // To start a connection, call the "beginConnection" method with the QR code content.
        networkManagerService?.beginConnection("DOKEY;192.168.1.45/24:192.168.56.1/24:192.168.121.1/24:192.168.127.1/24;77,4,35,55,125,101,25,106,12,43")
    }

    override fun onConnectionEstablished(serverInfo: DeviceInfo) {
        Log.d("CONNECT", "Connection established")

        // Request the section
        networkManagerService?.requestSection("launchpad") {
            Log.d("SECTION", it?.json().toString())

            it?.pages?.forEach { page ->
                page.components?.forEach { component ->
                    // Request each command
                    networkManagerService?.requestCommand(component.commandId!!) {
                        Log.d("COMMAND", it?.json().toString())

                        // Request the image
                        networkManagerService?.requestImage(it?.iconId!!) { imageId, imageFile ->
                            Log.d("IMAGE", imageFile?.absolutePath)
                        }
                    }
                }
            }
        }
    }

    override fun onServerNotInTheSameNetworkError() {
        Log.d("CONNECT", "Not in the same server")
    }

    override fun onConnectionError() {
        Log.d("CONNECT", "Connection error")
    }

    override fun onInvalidKeyError() {
        Log.d("CONNECT", "Invalid key")
    }

    override fun onDesktopVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d("CONNECT", "Desktop version too low")
    }

    override fun onMobileVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d("CONNECT", "Mobile version too low")
    }

    override fun onConnectionClosed() {
        Log.d("CONNECT", "Connection closed")
    }
}