package io.rocketguys.dokey.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.KeyEvent
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureManager
import io.rocketguys.dokey.R
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import kotlinx.android.synthetic.main.activity_connect.*
import kotlinx.android.synthetic.main.activity_home.*
import net.model.DeviceInfo


class ConnectActivity : ConnectionBuilderActivity() {

    /*
    *                                                          user cancelled
    *                                                        /
    *                                                       /
    *   onCreate -> permis -> QR Scan -> onActivityResult -> onScanComplete -> onConnectionEstablished -> HomeActivity
    *               request                                                        \
    *                                                                        \
    *                                                                          QR code invalid
    *                                                                          \
    *                                                                           \
    *                                                                              Service not connected
    *                                                                               - onServerNotInTheSameNetworkError
    *                                                                               - onConnectionError
    *                                                                               - onInvalidKeyError
    *                                                                               - onDesktopVersionTooLowError
    *                                                                               - onMobileVersionTooLowError
    *                                                                               - onConnectionClosed
    *
    *
     */

    companion object {
        const val QR_PAYLOAD_CHECK = "DOKEY;"
        const val CAMERA_PERM_ID = 1
    }

    private lateinit var captureManager: CaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = getString(R.string.title_activity_connect)

        captureManager = CaptureManager(this, barcodeView)
        captureManager.initializeFromIntent(intent, savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Camera permission is not granted

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // TODO Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERM_ID)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERM_ID)
            }

        }else{
            // Camera permission was granted
            captureManager.decode()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERM_ID -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Camera permission was granted
                    captureManager.decode()
                } else {
                    // Camera permission was denied
                    // TODO Handle UX here
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // User cancelled the scanning
                // TODO Handle UX here
            } else {
                // Scan went well, go ahead
                Log.d("QRCODE", result.contents)
                onScanComplete(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun onScanComplete(payload: String){
        if (!payload.startsWith(QR_PAYLOAD_CHECK)){
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

    override fun onResume() {
        super.onResume()
        captureManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        captureManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        captureManager.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}