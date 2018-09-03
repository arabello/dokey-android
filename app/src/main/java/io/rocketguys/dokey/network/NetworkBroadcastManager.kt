package io.rocketguys.dokey.network

import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import android.content.Intent

// Broadcast events
enum class NetworkEvent {
    APPLICATION_SWITCH_EVENT,
    CONNECTION_ESTABLISHED_EVENT,
    CONNECTION_ERROR_EVENT,
    INVALID_KEY_EVENT,
    DESKTOP_VERSION_TOO_LOW_EVENT,
    MOBILE_VERSION_TOO_LOW_EVENT,
    CONNECTION_CLOSED_EVENT,
    SECTION_MODIFIED_EVENT,
}

class NetworkBroadcastManager(val context: Context) {
    fun sendBroadcast(type: NetworkEvent) {
        val intent = Intent(type.name)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}