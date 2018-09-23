package io.rocketguys.dokey.network.model

import model.section.Section

interface SectionWrapper : Section {
    val app : App?
}