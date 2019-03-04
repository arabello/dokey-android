package io.rocketguys.dokey.sync

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
import android.util.DisplayMetrics
import io.rocketguys.dokey.slider.VerticalSliderDialogFragment
import io.rocketguys.dokey.slider.VerticalSliderGesture


/**
 * Implementation of [AbstractElement] for a Dokey command
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class CommandElement(val component: Component, val networkManagerService: NetworkManagerService?, val activity: HomeActivity) : AbstractElement() {


    override fun inflateView(parent: ViewGroup): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.item_command, parent, false)
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val gestureDomainSize =  displayMetrics.heightPixels * 3 / 4

        // Request each command
        networkManagerService?.requestCommand(component.commandId!!) { cmd ->
            //Log.d("COMMAND", cmd?.json().toString())

            if (cmd != null) {
                view.cmdTxt.text = cmd.title

                // TEST START //
                //TODO if (it is a slider_vertical) setOnClickListener
                view.setOnLongClickListener {

                    val slider = VerticalSliderDialogFragment.newInstance("Slider", VerticalSliderDialogFragment.GRAVITY_END)
                    slider.show(activity.supportFragmentManager, "slider_vertical")

                    true
                }
                // TEST END //

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