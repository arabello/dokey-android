package io.rocketguys.dokey.network.handler

import io.rocketguys.dokey.network.NetworkEvent
import io.rocketguys.dokey.network.NetworkManagerService
import json.JSONObject
import net.model.ServiceHandler

class ApplicationSwitchHandler(val service: NetworkManagerService) : ServiceHandler {
    override val targetType: String = "app_switch"

    override fun onServiceRequest(body: JSONObject?): JSONObject? {
        // Notify the command
        service.broadcastManager?.sendBroadcast(NetworkEvent.APPLICATION_SWITCH_EVENT, body.toString())

        return null
    }

}