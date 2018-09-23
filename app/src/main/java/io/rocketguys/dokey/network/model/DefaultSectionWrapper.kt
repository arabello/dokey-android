package io.rocketguys.dokey.network.model

import io.rocketguys.dokey.network.NetworkManagerService
import json.JSONObject
import model.page.Page
import model.parser.page.PageParser
import model.section.Section

class DefaultSectionWrapper(val networkManagerService: NetworkManagerService, val section: Section) : SectionWrapper {
    override val app: App? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        if (section.id!!.startsWith("app:")) {
            val appPath = section.id!!.split("app:").get(1)
            App(networkManagerService, section.name!!, appPath)
        }else{
            null
        }
    }

    override var id: String?
        get() = section.id
        set(value) {section.id = value}

    override var lastEdit: Long?
        get() = section.lastEdit
        set(value) {section.lastEdit = value}

    override var name: String?
        get() = section.name
        set(value) {section.name = value}

    override var pages: MutableList<Page>?
        get() = section.pages
        set(value) {section.pages = value}

    override val type: String?
        get() = section.type

    override fun json(): JSONObject {
        return section.json()
    }

    override fun populateFromJSON(json: JSONObject, pageParser: PageParser) {
        section.populateFromJSON(json, pageParser)
    }

}