package io.rocketguys.dokey.sync

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import io.matteopellegrino.pagedgrid.element.AbstractElement
import io.rocketguys.dokey.HomeActivity
import io.rocketguys.dokey.R
import io.rocketguys.dokey.network.NetworkManagerService
import io.rocketguys.dokey.network.isAppOpen
import io.rocketguys.dokey.preferences.ContextualVibrator
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_command.view.*
import model.component.Component

/**
 * Custom [AbstractElement] representin a Dokey command
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class CommandElement(val component: Component, val networkManagerService: NetworkManagerService?, val activity: HomeActivity) : AbstractElement() {

    override fun inflateView(parent: ViewGroup): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.item_command, parent, false)

        // Request each command
        networkManagerService?.requestCommand(component.commandId!!) { cmd ->
            //Log.d("COMMAND", cmd?.json().toString())

            if (cmd != null) {
                view.cmdTxt.text = cmd.title
                view.setOnClickListener {
                    ContextualVibrator.from(activity).oneShotVibration(ContextualVibrator.SHORT)
                    networkManagerService.executeCommand(cmd)

                    if (cmd.isAppOpen()
                            && activity.navigation.selectedItemId == R.id.navigation_launchpad
                            && activity.isPadlockOpen()) {
                        activity.navigation.selectedItemId = R.id.navigation_shortcut
                    }
                }
            }

            // Request the image
            if (cmd?.iconId != null)
                networkManagerService.requestImage(cmd.iconId!!) { imageId, imageFile ->
                    if (imageFile != null)
                        Picasso.get().load(imageFile).into(view.cmdImg)
                }
        }

        return view
    }
}