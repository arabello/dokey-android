package io.rocketguys.dokey.connect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import com.journeyapps.barcodescanner.CaptureManager
import io.rocketguys.dokey.R
import json.JSONObject
import kotlinx.android.synthetic.main.activity_scan.*
import net.model.DeviceInfo

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class ScanActivity : AppCompatActivity(){

    companion object {
        private val TAG: String = ScanActivity::class.java.simpleName
        private const val CAMERA_PERM_ID = 1

        const val QR_PAYLOAD_CHECK = "DOKEY;"

        fun cache(context: Context): ScanCache = ScanCache(context)

        class ScanCache(context: Context){
            companion object {
                private const val QR_CODE_KEY = "qr_code"
                private const val DEVICE_INFO_KEY = "device_info"
            }
            private val pref = PreferenceManager.getDefaultSharedPreferences(context)!!

            var qrCode : String?
                get() {
                    val s = pref.getString(QR_CODE_KEY, null)
                    Log.d(TAG, "cache read qr code: $s")
                    return s
                }
                set(value) {
                    pref.edit().putString(QR_CODE_KEY, value).apply()
                    Log.d(TAG, "cache write qr code: $value")
                }

            var deviceInfo : DeviceInfo?
                get() {
                    val s = pref.getString(DEVICE_INFO_KEY, null)
                    Log.d(TAG, "cache read device info: $s")
                    return if (s == null) null else DeviceInfo.fromJson(JSONObject(s))
                }
                set(value) {
                    pref.edit().putString(DEVICE_INFO_KEY, value?.json().toString()).apply()
                    Log.d(TAG, "cache write device info: $value")
                }
        }
    }

    private lateinit var captureManager: CaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

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