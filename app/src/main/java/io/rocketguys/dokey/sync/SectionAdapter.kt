package io.rocketguys.dokey.sync

import android.support.annotation.StringDef
import io.rocketguys.dokey.network.model.App
import model.section.Section

/**
 * This adapter is responsible to adapt
 * the view for a given [Section]
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
interface SectionAdapter{
    companion object {
        @Target(AnnotationTarget.EXPRESSION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
        @StringDef(LAUNCHPAD, SHORTCUT, SYSTEM)
        @Retention(AnnotationRetention.SOURCE)
        annotation class SectionType

        const val LAUNCHPAD = "launchpad"
        const val SHORTCUT = "shortcut"
        const val SYSTEM = "system"
    }

    fun notifySectionChanged(section: Section?)
}

fun Section.isEmpty(): Boolean{
    if (this.pages?.size == 0)
        return true
    this.pages?.forEach { page ->
        if (page.components?.size != 0)
            return false
    }
    return true
}

fun App.relatedSectionId() = "app:$path" // For lack of API(section.id when section is null)