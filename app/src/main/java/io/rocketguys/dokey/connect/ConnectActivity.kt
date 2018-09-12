package io.rocketguys.dokey.connect

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.zxing.integration.android.IntentIntegrator
import io.rocketguys.dokey.HomeActivity
import io.rocketguys.dokey.R
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import kotlinx.android.synthetic.main.activity_connect.*
import net.model.DeviceInfo


class ConnectActivity : ConnectionBuilderActivity() {

    companion object {
        private val TAG: String = ConnectActivity::class.java.simpleName
    }

    private var qrPayload: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_connect)

        // TODO Check user wifi connection (needed)

        // Set up new scan btn
        scanBtn.setOnClickListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), IntentIntegrator.REQUEST_CODE)
        }

        // Try to connect using QR code cache
        qrPayload = ScanActivity.cache(this).qrCode
        if (qrPayload == null)
            startActivityForResult(Intent(this, ScanActivity::class.java), IntentIntegrator.REQUEST_CODE)
        else{
            progressBar.smoothToShow()
            val deviceInfo = ScanActivity.cache(this).deviceInfo
            devInfoText.text = "${deviceInfo?.name} ${deviceInfo?.os}"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onCreate")
        val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
        if(result != null) {
            if(result.contents == null) {
                // User cancelled scanning
                // TODO Handle UX case
            } else {
                qrPayload = if (result.contents.startsWith(ScanActivity.QR_PAYLOAD_CHECK)) result.contents else null
                Log.d(TAG, "QR scan result: $qrPayload")
            }
        }
    }

    override fun onServiceConnected() {
        if (qrPayload != null) {
            networkManagerService?.beginConnection(qrPayload!!)
            Log.d(TAG, "Begin connection")
        }
    }

    // Start HomeActivity, connection is stable
    override fun onConnectionEstablished(serverInfo: DeviceInfo) {
        Log.d(TAG, "Connection established to $serverInfo")

        // Update cache
        ScanActivity.cache(this).qrCode = qrPayload
        ScanActivity.cache(this).deviceInfo = serverInfo

        progressBar.smoothToHide()

        // Start the main activity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun commonErrorHandler(msg: String){
        progressBar.indicator.color = Color.RED
        devInfoText.text = msg
        connectivityText.text = getString(R.string.acty_connect_error)
        scanBtn.visibility = View.VISIBLE
    }

    override fun onServerNotInTheSameNetworkError() {
        Log.d(TAG, "Not in the same server")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        // TODO Handle error
    }

    override fun onConnectionError() {
        Log.d(TAG, "Connection error")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        // TODO Handle UX

        // Request scan
        //startActivityForResult(Intent(this, ScanActivity::class.java), IntentIntegrator.REQUEST_CODE)
    }

    override fun onInvalidKeyError() {
        Log.d(TAG, "Invalid key")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        // TODO Handle error
    }

    override fun onDesktopVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d(TAG, "Desktop version too low")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        // TODO Handle error
    }

    override fun onMobileVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d(TAG, "Mobile version too low")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        // TODO Handle error
    }

    override fun onConnectionClosed() {
        Log.d(TAG, "Connection closed")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        // TODO Handle error
    }
}