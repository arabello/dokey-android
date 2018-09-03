package io.rocketguys.dokey.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.rocketguys.dokey.network.NetworkActivity
import io.rocketguys.dokey.network.NetworkPayloadResolver

class ConnectActivity : NetworkActivity() {
    override fun onServiceConnected() {
//        networkManagerService?.startConnection("192.168.1.45", 60642,
//                byteArrayOf(77,4,35,55,125,101,25,106,12,43))
        NetworkPayloadResolver(this.applicationContext).parse("123")
    }
}