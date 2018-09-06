package io.rocketguys.dokey.network.util

import net.model.DeviceInfo

/**
 * Factory for the DeviceInfo associated with the device.
 */
object DeviceInfoBuilder {
    val deviceInfo : DeviceInfo
        get() {
            val info = DeviceInfo()
            info.os = DeviceInfo.OS.ANDROID
            info.name = android.os.Build.MODEL  // TODO: improve device name
            info.id = android.os.Build.MODEL
            return info
        }
}