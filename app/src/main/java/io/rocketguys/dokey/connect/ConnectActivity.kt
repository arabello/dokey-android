package io.rocketguys.dokey.connect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.usb.UsbManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.zxing.integration.android.IntentIntegrator
import io.rocketguys.dokey.HomeActivity
import io.rocketguys.dokey.R
import io.rocketguys.dokey.connect.usb.USBInstructionActivity
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import io.rocketguys.dokey.network.usb.USBDetectionDaemon
import kotlinx.android.synthetic.main.activity_connect.*
import net.model.DeviceInfo
import kotlin.properties.Delegates


class ConnectActivity : ConnectionBuilderActivity() {

    companion object {
        private val TAG: String = ConnectActivity::class.java.simpleName
        private val DISABLE_USB_DAEMON_FOR_DEBUG = false // Set TRUE for debug purpose ONLY. Set FALSE in production
        const val EXTRA_FORCE_SCAN = "extra_force_scan"
        const val EXTRA_FIRST_LAUNCH = "first_launch"
        const val EXTRA_DISABLE_USB_DAEMON = "disable_usb_daemon"
        const val USB_STATE_CHANGE_ACTION = "android.hardware.usb.action.USB_STATE"

        @IntDef(CLOSED, ERROR, CONNECTING_USB, CONNECTING_CACHE_QR, CONNECTING_QR, ESTABLISHED_USB, ESTABLISHED_QR)
        @Retention(AnnotationRetention.SOURCE)
        private annotation class ConnectivityStatus

        const val CLOSED = -2
        const val ERROR = -1
        const val CONNECTING_USB = 0
        const val CONNECTING_CACHE_QR = 1
        const val CONNECTING_QR = 2
        const val ESTABLISHED_USB = 3
        const val ESTABLISHED_QR = 4

        @IntDef(QR_CODE, USB)
        @Retention(AnnotationRetention.SOURCE)
        private annotation class ConnectionType

        const val QR_CODE = 10
        const val USB = 11

        @ConnectionType var LAST_CONNECTION_TYPE: Int ?= null
    }

    private var disableDaemon = false
    private var usbDetectionDaemon: USBDetectionDaemon ?= null
    private var scannedQRPayload: String ?= null

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

    private var connectivityStatus by Delegates.observable<@io.rocketguys.dokey.connect.ConnectActivity.Companion.ConnectivityStatus Int>(CONNECTING_USB){_, _, newValue ->
        when(newValue){
            CLOSED -> {
                connectProgressBar.indicator.color = Color.RED
                connectivityInfo.text = getString(R.string.acty_connect_closed)
                showConnectActions = true
            }
            ERROR -> {
                connectProgressBar.indicator.color = Color.RED
                connectivityInfo.text = getString(R.string.acty_connect_error)
                showConnectActions = true
            }
            ESTABLISHED_QR -> {
                connectProgressBar.indicator.color = ContextCompat.getColor(this, R.color.colorAccent)
                connectivityInfo.text = getString(R.string.acty_connect_msg)
                showConnectActions = false
                LAST_CONNECTION_TYPE = QR_CODE
            }
            ESTABLISHED_USB -> {
                connectProgressBar.indicator.color = ContextCompat.getColor(this, R.color.colorAccent)
                connectivityInfo.text = getString(R.string.acty_connect_msg)
                showConnectActions = false
                LAST_CONNECTION_TYPE = USB
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        isAdbEnabled = if (Build.VERSION.SDK_INT >= 17)
            Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        else
            Settings.Secure.getInt(contentResolver, Settings.Secure.ADB_ENABLED, 0) == 1

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

        disableDaemon = intent.getBooleanExtra(EXTRA_DISABLE_USB_DAEMON, false)
        val forceScan = intent.getBooleanExtra(EXTRA_FORCE_SCAN, false)
        val firstLaunch = intent.getBooleanExtra(EXTRA_FIRST_LAUNCH, false)

        when {
            forceScan -> startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
            firstLaunch -> {
                connectivityInfo.text = getString(R.string.acty_connect_msg)
                deviceInfo.text = getString(R.string.acty_connect_first_launch)
            }
            else -> {
                // Try to connect using payload cache

                if (ScanActivity.cache(this).qrCode != null) {
                    connectivityStatus = CONNECTING_CACHE_QR
                    connectivityInfo.text = getString(R.string.acty_connect_msg)
                    val info = ScanActivity.cache(this).deviceInfo
                    if (info != null)
                        deviceInfo.text = getString(R.string.acty_connect_device_info, info.name, info.os)
                }else{
                    connectivityStatus = ERROR
                    deviceInfo.setText(R.string.acty_connect_no_device_found)
                }
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

    override fun onStop() {
        super.onStop()

        // If the service is not connected to the dokey server, close it when going into background
        if (networkManagerService?.isConnected == false) {
            stopNetworkService()
        }

        usbDetectionDaemon?.stopDiscovery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
        if(result != null) {
            Log.d(TAG, "QR scan result: ${result.contents}")

            if (result.contents == null){
                // User cancelled scanning
                connectivityStatus = ERROR
                deviceInfo.setText(R.string.acty_connect_scan_hint)
            }else if (!result.contents.startsWith(ScanActivity.QR_PAYLOAD_CHECK)){
                // User did not scanned a Dokey's QRCode
                connectivityStatus = ERROR
                deviceInfo.setText(R.string.acty_connect_scan_hint)

                val dialog = ConnectDialog.from(this).createDialogInvalidQRCode()
                dialog.setOnDismissListener {
                    startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
                }
                dialog.show()
            }else{
                scannedQRPayload = result.contents
            }
        }
    }

    private fun startUSBDaemon(){
        usbDetectionDaemon =  USBDetectionDaemon()

        usbDetectionDaemon?.onUSBConnectionDetected = { usbPayload ->
            connectivityStatus = CONNECTING_USB
            connectivityInfo.text = getString(R.string.acty_connect_msg)
            networkManagerService?.beginConnection(usbPayload)
            Log.d(TAG, ":onServiceConnected: USB: Begin connection")
        }

        usbDetectionDaemon?.start()
    }

    override fun onServiceConnected() {
        // If the service is already connected to the dokey server, go to the home
        // activity instantly
        if (networkManagerService?.isConnected == true) {
            startHomeActivity()
            return
        }

        isAdbEnabled = if (Build.VERSION.SDK_INT >= 17)
            Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        else
            Settings.Secure.getInt(contentResolver, Settings.Secure.ADB_ENABLED, 0) == 1

        Log.d(TAG, ":onServiceConnected\n isAdbEnabled? $isAdbEnabled\n disableDaemon? $disableDaemon\n DISABLE_USB_DAEMON_FOR_DEBUG? $DISABLE_USB_DAEMON_FOR_DEBUG")

        // USB Mode
        if (isAdbEnabled && !disableDaemon && !DISABLE_USB_DAEMON_FOR_DEBUG) {
            startUSBDaemon()
        }

        // Try QR Code cache
        if (ScanActivity.cache(this).qrCode != null) {
            connectivityStatus = CONNECTING_CACHE_QR
            connectivityInfo.text = getString(R.string.acty_connect_msg)
            networkManagerService?.beginConnection(ScanActivity.cache(this).qrCode!!)
            Log.d(TAG, ":onServiceConnected: [CACHE] QR Code: Begin connection")
        }

        // Use scanned QR Payload
        if (scannedQRPayload != null) { // Wifi mode
            connectivityStatus = CONNECTING_QR
            connectivityInfo.text = getString(R.string.acty_connect_msg)
            networkManagerService?.beginConnection(scannedQRPayload!!)
            Log.d(TAG, ":onServiceConnected: QR Code: Begin connection")
        }
    }

    // Start HomeActivity, connection is stable
    override fun onConnectionEstablished(serverInfo: DeviceInfo) {
        Log.d(TAG, "Connection established to $serverInfo")
        deviceInfo.text = getString(R.string.acty_connect_device_info, serverInfo.name, serverInfo.os)

        when(connectivityStatus){
            CONNECTING_USB -> connectivityStatus = ESTABLISHED_USB
            CONNECTING_CACHE_QR -> connectivityStatus = ESTABLISHED_QR
            CONNECTING_QR -> {
                ScanActivity.cache(this).qrCode = scannedQRPayload
                ScanActivity.cache(this).deviceInfo = serverInfo
                connectivityStatus = ESTABLISHED_QR
            }
        }

        startHomeActivity()
    }

    private fun startHomeActivity() {
        // Start the main activity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onConnectionError() {
        Log.d(TAG, "Connection error")
        connectivityStatus = ERROR
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
        connectivityStatus = ERROR

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
        connectivityStatus = ERROR

        ScanActivity.cache(this).clear()
    }

    override fun onDesktopVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d(TAG, "Desktop version too low")
        connectivityStatus = ERROR

        val dialog = ConnectDialog.from(this).createDialogOnDesktopVersionTooLowError(serverInfo)
        dialog.setOnDismissListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        }
        dialog.show()
    }

    override fun onMobileVersionTooLowError(serverInfo: DeviceInfo) {
        Log.d(TAG, "Mobile version too low")
        connectivityStatus = ERROR

        val dialog = ConnectDialog.from(this).createDialogOnMobileVersionTooLowError(serverInfo)
        dialog.setOnDismissListener {
            startActivityForResult(Intent(this, ScanActivity::class.java), ScanActivity.REQUEST_CODE)
        }
        dialog.show()
    }

    override fun onConnectionClosed() {
        Log.d(TAG, "Connection closed")
        connectivityStatus = CLOSED
    }
}