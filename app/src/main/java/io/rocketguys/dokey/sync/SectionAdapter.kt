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
        @StringDef(LAUNCHPAD, SHORTCUT, SYSTEM)
        @Retention(AnnotationRetention.SOURCE)
        annotation class SectionType

        const val LAUNCHPAD = "launchpad"
        const val SHORTCUT = "shortcut"
        const val SYSTEM = "system"
    }

    fun notifySectionChanged(section: Section?)
}

fun Section?.isTypeOf(@SectionType type: String): Boolean{
    if (this == null) return false
    return if (id == type)
        true
    else type == SHORTCUT
}