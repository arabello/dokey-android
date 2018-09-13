package io.rocketguys.dokey.sync

import model.section.Section

/**
 * This adapter is responsible to adapt
 * the view for a given [Section]
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
interface SectionAdapter{
    companion object {
          const val LAUNCHPAD_ID = "launchpad"
          const val SHORTCUT_ID = "shortcut"
          const val SYSTEM_ID = "system"
    }

    fun notifySectionChanged(section: Section?)
}