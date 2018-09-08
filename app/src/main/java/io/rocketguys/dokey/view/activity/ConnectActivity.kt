package io.rocketguys.dokey.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.zxing.integration.android.IntentIntegrator
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import net.model.DeviceInfo


class ConnectActivity : ConnectionBuilderActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivityForResult(Intent(this, ScanActivity::class.java), IntentIntegrator.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
        if(result != null) {
            if(result.contents == null) {
                // User cancelled scanning
                // TODO Handle UX case
            } else {
                onScanComplete(result.contents)
            }
        }
    }

    fun onScanComplete(payload: String){
        if (!payload.startsWith(ScanActivity.QR_PAYLOAD_CHECK)){
            // The QR code scanned is not for Dokey, alert user
            // TODO Handle error  here
            return
        }

        networkManagerService?.beginConnection(payload)
    }

    override fun onServiceConnected() {
        Log.d("CONNECT", "Service connected")
    }

    // Start HomeActivity, connection is stable
    override fun onConnectionEstablished(serverInfo: DeviceInfo) {
        Log.d("CONNECT", "Connection established")

        // Start the main activity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
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