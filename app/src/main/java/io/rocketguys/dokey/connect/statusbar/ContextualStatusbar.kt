package io.rocketguys.dokey.connect.statusbar

import android.view.View
import io.rocketguys.dokey.R
import io.rocketguys.dokey.connect.ScanActivity
import io.rocketguys.dokey.connect.statusbar.Statusbar.Companion.LENGTH_INDEFINITE
import io.rocketguys.dokey.network.activity.ConnectedActivity

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class ContextualStatusbar(val activity: ConnectedActivity) {

    fun connecting(view: View): Statusbar {

        val statusBar = Statusbar.make(view, R.string.snack_connecting_msg, LENGTH_INDEFINITE)

        statusBar.setAction(R.string.snack_connecting_action){
            ScanActivity.cache(activity).clear()
            activity.stopNetworkService()
        }

        return statusBar
    }
}