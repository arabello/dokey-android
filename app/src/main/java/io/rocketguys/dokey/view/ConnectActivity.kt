package io.rocketguys.dokey.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import io.rocketguys.dokey.network.NetworkEvent
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import io.rocketguys.dokey.network.activity.NetworkActivity
import net.model.DeviceInfo

class ConnectActivity : ConnectionBuilderActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.setBackgroundColor(Color.RED)
    }

    override fun onServiceConnected() {
        // To start a connection, call the "beginConnection" method with the QR code content.
        networkManagerService?.beginConnection("DOKEY;192.168.1.45/24:192.168.56.1/24:192.168.121.1/24:192.168.127.1/24;89,105,43,16,75,87,10,41,65,42")
    }

    override fun onConnectionEstablished(serverInfo: DeviceInfo) {
        Log.d("CONNECT", "Connection established")

        // Start the main activity
        val intent = Intent(this, TestNavigationDrawerActivity::class.java)
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