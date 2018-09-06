package io.rocketguys.dokey.network.handler

import io.rocketguys.dokey.network.NetworkEvent
import io.rocketguys.dokey.network.NetworkManagerService
import json.JSONObject
import net.model.ServiceHandler

class CommandModifiedHandler(val service: NetworkManagerService) : ServiceHandler {
    override val targetType: String = "command_edit"

    override fun onServiceRequest(body: JSONObject?): JSONObject? {
        val receivedCommand = service.commandParser.fromJSON(body!!)

        // Update the command cache
        service.commandCache?.saveCommand(receivedCommand)

        // Notify the command
        service.broadcastManager?.sendBroadcast(NetworkEvent.COMMAND_MODIFIED_EVENT, receivedCommand.id.toString())

        return null
    }

}