package io.rocketguys.dokey.sync

import android.support.design.widget.Snackbar
import android.support.v4.view.GestureDetectorCompat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import io.matteopellegrino.pagedgrid.element.AbstractElement
import io.rocketguys.dokey.HomeActivity
import io.rocketguys.dokey.R
import io.rocketguys.dokey.network.NetworkManagerService
import io.rocketguys.dokey.network.isAppOpen
import io.rocketguys.dokey.preferences.ContextualVibrator
import io.rocketguys.dokey.slider.SliderGesture
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_command.view.*
import model.component.Component
import android.util.DisplayMetrics
import android.widget.Toast
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
                //TODO if (it is a slider) setOnClickListener
                view.setOnLongClickListener {

                    val toast = Snackbar.make(activity.rootView, "value ", Snackbar.LENGTH_INDEFINITE)
                    toast.show()

                    val gesture = VerticalSliderGesture(0.0f, -50..50, gestureDomainSize){ old, new ->
                        toast.setText("value $new")
                    }

                    gesture.sensibility = 1.5f
                    val detector = GestureDetectorCompat(activity, gesture)
                    view.setOnTouchListener{ v, event ->
                        detector.onTouchEvent(event)
                        v.onTouchEvent(event)
                    }
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