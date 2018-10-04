package io.rocketguys.dokey.sync

import android.support.annotation.StringDef
import io.rocketguys.dokey.sync.SectionAdapter.Companion.SHORTCUT
import io.rocketguys.dokey.sync.SectionAdapter.Companion.SectionType
import model.section.Section

/**
 * This adapter is responsible to adapt
 * the view for a given [Section]
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
interface SectionAdapter{
    companion object {
        @Target(AnnotationTarget.EXPRESSION, AnnotationTarget.VALUE_PARAMETER)
        @StringDef(LAUNCHPAD, SHORTCUT, SYSTEM)
        @Retention(AnnotationRetention.SOURCE)
        annotation class SectionType

        const val LAUNCHPAD = "launchpad"
        const val SHORTCUT = "shortcut"
        const val SYSTEM = "system"
    }

    fun notifySectionChanged(section: Section?)

    var currentSection: Section?
}

fun Section?.isTypeOf(@SectionType type: String): Boolean{
    if (this == null) return false
    return if (id == type)
        true
    else type == SHORTCUT
}

fun Section.exist(): Boolean{
    if (this.pages?.size == 0)
        return false
    this.pages?.forEach { page ->
        if (page.components?.size != 0)
            return true
    }
    return false
}