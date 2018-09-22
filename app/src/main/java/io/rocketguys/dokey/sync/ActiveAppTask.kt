package io.rocketguys.dokey.sync

import io.rocketguys.dokey.network.NetworkManagerService
import java.util.*

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class ActiveAppTask(val networkManagerService: NetworkManagerService?, val adapter: ActiveAppAdapter) : TimerTask() {
    override fun run() {
        networkManagerService?.requestActiveApps { activeApps ->
            if (adapter.activeApps != activeApps) {
                adapter.activeApps = activeApps
                adapter.notifyDataSetChanged()
            }
        }
    }
}