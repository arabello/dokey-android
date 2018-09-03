package io.rocketguys.dokey.network

import net.model.DeviceInfo

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