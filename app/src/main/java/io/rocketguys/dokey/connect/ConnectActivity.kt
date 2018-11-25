package io.rocketguys.dokey.connect

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.zxing.integration.android.IntentIntegrator
import io.rocketguys.dokey.HomeActivity
import io.rocketguys.dokey.R
import io.rocketguys.dokey.connect.usb.USBInstructionActivity
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import kotlinx.android.synthetic.main.activity_connect.*
import net.model.DeviceInfo
import kotlin.properties.Delegates


class ConnectActivity : ConnectionBuilderActivity() {

    companion object {
        private val TAG: String = ConnectActivity::class.java.simpleName
        const val EXTRA_FORCE_SCAN = "extra_force_scan"
        const val EXTRA_FIRST_LAUNCH = "first_launch"
    }

    private var qrPayload: String? = null

    private var isAdbEnabled by Delegates.observable<Boolean>(false) { _, _, newValue ->
        if (newValue){
            usbEnableBtn.visibility = View.INVISIBLE
            usbText.visibility = View.VISIBLE
            usbIcon.visibility = View.VISIBLE
        }else{
            usbEnableBtn.visibility = View.VISIBLE
            usbText.visibility = View.INVISIBLE
            usbIcon.visibility = View.INVISIBLE
        }
    }

    private var showConnectActions by Delegates.observable<Boolean>(false) { _, _, newValue ->
        connectActions.visibility = if (newValue) View.VISIBLE else View.INVISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        // Start the service
        startNetworkService()

        // Set up new scan btn
        scanBtn.setOnClickListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        }

        // Set up enable usb btn
        usbEnableBtn.setOnClickListener {
            startActivity(Intent(this, USBInstructionActivity::class.java))
        }

        val forceScan = intent.getBooleanExtra(EXTRA_FORCE_SCAN, false)
        val firstLaunch = intent.getBooleanExtra(EXTRA_FIRST_LAUNCH, false)

        if (forceScan)
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        else if (firstLaunch){
            deviceInfo.text = getString(R.string.acty_connect_first_launch)
            showConnectActions = true
        }else{
            // Try to connect using payload cache
            qrPayload = ScanActivity.cache(this).qrCode

            if (qrPayload != null) {
                showConnectActions = false
                val info = ScanActivity.cache(this).deviceInfo
                if (info != null)
                    deviceInfo.text = getString(R.string.acty_connect_device_info, info.name, info.os)
            }else{
                showError(getString(R.string.acty_connect_no_device_found))
            }
        }
    }


    override fun onStart() {
        super.onStart()

        // If the server is not started yet, start it
        if (!serviceStarted) {
            startNetworkService()
        }
    }

    override fun onResume() {
        super.onResume()
        isAdbEnabled = if (Build.VERSION.SDK_INT >= 17)
            Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        else
            Settings.Secure.getInt(contentResolver, Settings.Secure.ADB_ENABLED, 0) == 1

    }

    override fun onStop() {
        super.onStop()

        // If the service is not connected to the dokey server, close it when going into background
        if (networkManagerService?.isConnected == false) {
            stopNetworkService()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        hideError()
        val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
        if(result != null) {
            Log.d(TAG, "QR scan result: ${result.contents}")

            if (result.contents == null){
                // User cancelled scanning
                showError(getString(R.string.acty_connect_scan_hint))
            }else if (!result.contents.startsWith(ScanActivity.QR_PAYLOAD_CHECK)){
                // User did not scanned a Dokey's QRCode
                showError(getString(R.string.acty_connect_scan_hint))

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

        // If a QR payload was cached, try to connect to the server using it.
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

        startHomeActivity()
    }

    private fun startHomeActivity() {
        // Start the main activity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun showError(msg: String){
        connectProgressBar.indicator.color = Color.RED
        deviceInfo.text = msg
        connectivityInfo.text = getString(R.string.acty_connect_error)
        showConnectActions = true
    }

    private fun hideError(){
        connectProgressBar.indicator.color = ContextCompat.getColor(this, R.color.colorAccent)
        deviceInfo.text = ""
        connectivityInfo.text = getString(R.string.acty_connect_msg)
        showConnectActions = false
    }

    override fun onConnectionError() {
        Log.d(TAG, "Connection error")
        showError(getString(R.string.acty_connect_scan_hint))
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
        showError(getString(R.string.acty_connect_scan_hint))

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
        showError(getString(R.string.acty_connect_scan_hint))

        startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
    }

    override fun onDesktopVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d(TAG, "Desktop version too low")
        showError(getString(R.string.acty_connect_scan_hint))

        val dialog = ConnectDialog.from(this).createDialogOnDesktopVersionTooLowError(serverInfo)
        dialog.setOnDismissListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        }
        dialog.show()
    }

    override fun onMobileVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d(TAG, "Mobile version too low")
        showError(getString(R.string.acty_connect_scan_hint))

        val dialog = ConnectDialog.from(this).createDialogOnMobileVersionTooLowError(serverInfo)
        dialog.setOnDismissListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        }
        dialog.show()
    }

    override fun onConnectionClosed() {
        Log.d(TAG, "Connection closed")
        showError(getString(R.string.acty_connect_scan_hint))

        // TODO Handle UX
    }
}