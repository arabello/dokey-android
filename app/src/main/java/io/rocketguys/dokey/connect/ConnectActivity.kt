package io.rocketguys.dokey.connect

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
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
        setContentView(R.layout.activity_connect)

        // TODO Check user wifi connection (needed)

        // Set up new scan btn
        scanBtn.setOnClickListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        }

        // Try to connect using QR code cache
        qrPayload = ScanActivity.cache(this).qrCode
        if (qrPayload == null)
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        else{
            progressBar.smoothToShow()
            val deviceInfo = ScanActivity.cache(this).deviceInfo
            devInfoText.text = "${deviceInfo?.name} ${deviceInfo?.os}"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        clearErrorHandler()
        val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
        if(result != null) {
            Log.d(TAG, "QR scan result: ${result.contents}")

            if (result.contents == null){
                // User cancelled scanning
                commonErrorHandler(getString(R.string.acty_connect_scan_hint))
            }else if (!result.contents.startsWith(ScanActivity.QR_PAYLOAD_CHECK)){
                // User did not scanned a Dokey's QRCode
                commonErrorHandler(getString(R.string.acty_connect_scan_hint))

                val dialog = ConnectDialog.from(this).createDialogInvalidQRCode()
                dialog.setOnDismissListener {
                    startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
                }
                dialog.show()
            }else{
                qrPayload = result.contents
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

    private fun clearErrorHandler(){
        progressBar.indicator.color = ContextCompat.getColor(this, R.color.colorAccent)
        devInfoText.text = ""
        connectivityText.text = getString(R.string.acty_connect_msg)
        scanBtn.visibility = View.GONE
    }

    override fun onConnectionError() {
        Log.d(TAG, "Connection error")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        // TODO Handle UX
    }

    override fun onServerNotInTheSameNetworkError() {
        Log.d(TAG, "Not in the same server")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        val dialog = ConnectDialog.from(this).createDialogOnServerNotInTheSameNetworkError()
        dialog.setOnDismissListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        }
        dialog.show()
    }

    override fun onInvalidKeyError() {
        Log.d(TAG, "Invalid key")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
    }

    override fun onDesktopVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d(TAG, "Desktop version too low")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        val dialog = ConnectDialog.from(this).createDialogOnDesktopVersionTooLowError(serverInfo)
        dialog.setOnDismissListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        }
        dialog.show()
    }

    override fun onMobileVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d(TAG, "Mobile version too low")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        val dialog = ConnectDialog.from(this).createDialogOnMobileVersionTooLowError(serverInfo)
        dialog.setOnDismissListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        }
        dialog.show()
    }

    override fun onConnectionClosed() {
        Log.d(TAG, "Connection closed")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        // TODO Handle UX
    }
}