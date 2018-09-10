package io.rocketguys.dokey.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

// Broadcast events
enum class NetworkEvent {
    // Initial connection events
    NOT_IN_THE_SAME_NETWORK_ERROR_EVENT,
    CONNECTION_ESTABLISHED_EVENT,
    CONNECTION_ERROR_EVENT,
    INVALID_KEY_EVENT,
    DESKTOP_VERSION_TOO_LOW_EVENT,
    MOBILE_VERSION_TOO_LOW_EVENT,
    CONNECTION_CLOSED_EVENT,

    // Application level events
    SECTION_MODIFIED_EVENT,
    COMMAND_MODIFIED_EVENT,
    APPLICATION_SWITCH_EVENT
}

/**
 * Used to send and receive network-related local broadcasts. Mainly used to communicate
 * between the NetworkManagerService and other activities.
 */
class NetworkBroadcastManager(val context: Context) {
    /**
     * Send a local broadcast with the given network event.
     */
    fun sendBroadcast(type: NetworkEvent, payload: String? = null) {
        Log.d("BROAD_MANAGER", type.name)

        val intent = Intent(type.name)
        if (payload != null) {
            intent.putExtra("payload", payload)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    /**
     * Register the given broadcast receiver for the given network event.
     */
    fun registerReceiver(type: NetworkEvent, receiver: BroadcastReceiver) {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
                IntentFilter(type.name))
    }

    /**
     * Unregister the given broadcast receiver.
     */
    fun unregisterReceiver(receiver: BroadcastReceiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
    }

}