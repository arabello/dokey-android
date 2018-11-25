package io.rocketguys.dokey.network.usb

import net.model.DeviceInfo
import java.util.*

data class USBConnectionPayload(val serverAddress: String, val serverPort: Int, val key: ByteArray,
                                val deviceInfo: DeviceInfo) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as USBConnectionPayload

        if (serverAddress != other.serverAddress) return false
        if (serverPort != other.serverPort) return false
        if (!Arrays.equals(key, other.key)) return false
        if (deviceInfo != other.deviceInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serverAddress.hashCode()
        result = 31 * result + serverPort
        result = 31 * result + Arrays.hashCode(key)
        result = 31 * result + deviceInfo.hashCode()
        return result
    }
}