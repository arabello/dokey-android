package io.rocketguys.dokey.connect

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.ViewGroup
import com.google.zxing.integration.android.IntentIntegrator
import io.rocketguys.dokey.HomeActivity
import io.rocketguys.dokey.R
import io.rocketguys.dokey.R.color.grad_1
import io.rocketguys.dokey.R.color.grad_2
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import kotlinx.android.synthetic.main.activity_connect.*
import kotlinx.android.synthetic.main.activity_home.*
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

        // Try to connect using QR code cache
        qrPayload = ScanActivity.cache(this).qrCode
        if (qrPayload == null)
            startActivityForResult(Intent(this, ScanActivity::class.java), IntentIntegrator.REQUEST_CODE)
        else{
            progressBar.smoothToShow()
            val deviceInfo = ScanActivity.cache(this).deviceInfo
            devInfoTextView.text = "${deviceInfo?.name} ${deviceInfo?.os}"
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

    override fun onServerNotInTheSameNetworkError() {
        Log.d(TAG, "Not in the same server")

        // TODO Handle error
    }

    override fun onConnectionError() {
        Log.d(TAG, "Connection error")

        // TODO Handle error
    }

    override fun onInvalidKeyError() {
        Log.d(TAG, "Invalid key")

        // TODO Handle error
    }

    override fun onDesktopVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d(TAG, "Desktop version too low")

        // TODO Handle error
    }

    override fun onMobileVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d(TAG, "Mobile version too low")

        // TODO Handle error
    }

    override fun onConnectionClosed() {
        Log.d(TAG, "Connection closed")

        // TODO Handle error
    }
}