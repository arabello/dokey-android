package io.rocketguys.dokey.connect

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.zxing.integration.android.IntentIntegrator
import io.rocketguys.dokey.HomeActivity
import io.rocketguys.dokey.R
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import io.rocketguys.dokey.network.usb.USBDetectionDaemon
import kotlinx.android.synthetic.main.activity_connect.*
import net.model.DeviceInfo


class ConnectActivity : ConnectionBuilderActivity() {

    companion object {
        private val TAG: String = ConnectActivity::class.java.simpleName
        const val EXTRA_FORCE_SCAN = "extra_force_scan"
    }

    private var qrPayload: String? = null

    private var usbDetectionDaemon : USBDetectionDaemon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        // Start the service
        startNetworkService()

        // Set up new scan btn
        introFragScanBtn.setOnClickListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        }

        val forceScan = intent.getBooleanExtra(EXTRA_FORCE_SCAN, false)

        // Try to connect using QR code cache
        qrPayload = ScanActivity.cache(this).qrCode
        if (forceScan || qrPayload == null)
            // TODO: pelle pensa ad una logica per mostrarlo solo nei casi in cui non sia partito il demone usb
            //startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        else{
            progressBar.smoothToShow()
            val deviceInfo = ScanActivity.cache(this).deviceInfo
            if (deviceInfo != null)
                devInfoText.text = getString(R.string.acty_connect_device_info, deviceInfo.name, deviceInfo.os)
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
        // If the service is already connected to the dokey server, go to the home
        // activity instantly
        if (networkManagerService?.isConnected == true) {
            startHomeActivity()
            return
        }

        /*
        TODO: pelle pensa ad una logica

        // If a QR payload was cached, try to connect to the server using it.
        if (qrPayload != null) {
            networkManagerService?.beginConnection(qrPayload!!)
            Log.d(TAG, "Begin connection")
        }
         */

        usbDetectionDaemon = USBDetectionDaemon()
        usbDetectionDaemon?.onUSBConnectionDetected = {usbPayload ->
            Log.d(TAG, "Usb payload detected: $usbPayload")
            networkManagerService?.beginConnection(usbPayload)
        }
        usbDetectionDaemon?.start()
    }

    // Start HomeActivity, connection is stable
    override fun onConnectionEstablished(serverInfo: DeviceInfo) {
        Log.d(TAG, "Connection established to $serverInfo")

        // Update cache
        ScanActivity.cache(this).qrCode = qrPayload
        ScanActivity.cache(this).deviceInfo = serverInfo

        startHomeActivity()
    }

    private fun startHomeActivity() {
        // Start the main activity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun commonErrorHandler(msg: String){
        progressBar.indicator.color = Color.RED
        devInfoText.text = msg
        connectivityText.text = getString(R.string.acty_connect_error)
        introFragScanBtn.visibility = View.VISIBLE
    }

    private fun clearErrorHandler(){
        progressBar.indicator.color = ContextCompat.getColor(this, R.color.colorAccent)
        devInfoText.text = ""
        connectivityText.text = getString(R.string.acty_connect_msg)
        introFragScanBtn.visibility = View.GONE
    }

    override fun onConnectionError() {
        Log.d(TAG, "Connection error")
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))
    }

    private fun isWifiConnected(): Boolean {
        val wifiMgr = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        return if (wifiMgr.isWifiEnabled) { // Wi-Fi adapter is ON

            val wifiInfo = wifiMgr.connectionInfo

            wifiInfo.networkId != -1
        } else {
            false // Wi-Fi adapter is OFF
        }
    }

    override fun onServerNotInTheSameNetworkError() {
        commonErrorHandler(getString(R.string.acty_connect_scan_hint))

        lateinit var dialog: AlertDialog

        // Check user wifi connection (needed)
        if (!isWifiConnected()) {
            Log.d(TAG, "Wifi is not connected")

            dialog = ConnectDialog.from(this).createDialogWifiNotConnected()
            dialog.setOnDismissListener {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }else{
            Log.d(TAG, "Server not in the same network")

            dialog = ConnectDialog.from(this).createDialogOnServerNotInTheSameNetworkError()
            dialog.setOnDismissListener {
                startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
            }
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

    override fun onStart() {
        super.onStart()

        // If the server is not started yet, start it
        if (!serviceStarted) {
            startNetworkService()
        }
    }

    override fun onStop() {
        super.onStop()

        usbDetectionDaemon?.stopDiscovery()
        usbDetectionDaemon = null

        // If the service is not connected to the dokey server, close it when going into background
        if (networkManagerService?.isConnected == false) {
            stopNetworkService()
        }
    }
}