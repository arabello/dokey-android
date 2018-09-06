package io.rocketguys.dokey.network.handler

import io.rocketguys.dokey.network.NetworkEvent
import io.rocketguys.dokey.network.NetworkManagerService
import json.JSONObject
import net.model.ServiceHandler

class SectionModifiedHandler(val service: NetworkManagerService) : ServiceHandler {
    override val targetType: String = "section_edit"

    override fun onServiceRequest(body: JSONObject?): JSONObject? {
        val receivedSection = service.sectionParser.fromJSON(body!!)

        // Update the section cache
        service.sectionCache?.saveSection(receivedSection)

        // Notify the section
        service.broadcastManager?.sendBroadcast(NetworkEvent.SECTION_MODIFIED_EVENT, receivedSection.id)

        return null
    }

}