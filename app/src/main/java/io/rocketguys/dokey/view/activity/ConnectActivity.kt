package io.rocketguys.dokey.view.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import net.model.DeviceInfo

class ConnectActivity : ConnectionBuilderActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.setBackgroundColor(Color.RED)
    }

    override fun onServiceConnected() {
        // To start a connection, call the "beginConnection" method with the QR code content.
        // TODO Verify string id "DOKEY;" otherwise error shown
        networkManagerService?.beginConnection("DOKEY;192.168.1.157/24;106,79,10,77,65,104,12,100,66,56")
    }

    // Start HomeActivity, connection is stable
    override fun onConnectionEstablished(serverInfo: DeviceInfo) {
        Log.d("CONNECT", "Connection established")

        // Start the main activity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
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