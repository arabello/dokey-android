package io.rocketguys.dokey.adapter

import model.section.Section

/**
 * TODO: Add class description
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