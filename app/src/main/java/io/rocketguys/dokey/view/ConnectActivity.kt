package io.rocketguys.dokey.view

import io.rocketguys.dokey.network.NetworkActivity

class ConnectActivity : NetworkActivity() {
    override fun onServiceConnected() {
//        networkManagerService?.startConnection("192.168.1.45", 60642,
//                byteArrayOf(77,4,35,55,125,101,25,106,12,43))
        //NetworkPayloadResolver(this.applicationContext).parse("DOKEY;192.168.1.45/24:192.168.56.1/24:192.168.121.1/24:192.168.127.1/24;77,4,35,55,125,101,25,106,12,43")
        networkManagerService?.beginConnection("DOKEY;192.168.1.45/24:192.168.56.1/24:192.168.121.1/24:192.168.127.1/24;77,4,35,55,125,101,25,106,12,43")
    }
}