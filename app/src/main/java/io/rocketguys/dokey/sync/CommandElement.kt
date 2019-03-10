package io.rocketguys.dokey.sync

import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import com.squareup.picasso.Picasso
import io.matteopellegrino.pagedgrid.element.AbstractElement
import io.rocketguys.dokey.HomeActivity
import io.rocketguys.dokey.R
import io.rocketguys.dokey.network.NetworkManagerService
import io.rocketguys.dokey.network.isAppOpen
import io.rocketguys.dokey.preferences.ContextualVibrator
import io.rocketguys.dokey.slider.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_command.view.*
import model.command.AnalogCommand
import model.command.SimpleCommand
import model.component.Component


/**
 * Implementation of [AbstractElement] for a Dokey command
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class CommandElement(val component: Component, val networkManagerService: NetworkManagerService?, val activity: HomeActivity) : AbstractElement() {
    companion object {
        const val ANALOG_UX_POLICY_SHORT = 0
        const val ANALOG_UX_POLICY_LONG = 1
    }

    val analogUXPolicy = ANALOG_UX_POLICY_LONG

    override fun inflateView(parent: ViewGroup): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.item_command, parent, false)
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val gestureDomainSize =  displayMetrics.heightPixels * 3 / 4 // TODO (For Horizontal slider case use width)

        // Request each command
        networkManagerService?.requestCommand(component.commandId!!) { cmd ->
            //Log.d("COMMAND", cmd?.json().toString())

            if (cmd != null) {
                view.cmdTxt.text = cmd.title

                when (cmd){
                    /*
                    *  Simple
                    */
                    is SimpleCommand -> {
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

                    /*
                     *  Analog
                     */
                    is AnalogCommand -> {

                        val sliderView = VerticalSliderDialogFragment.newInstance("Slider", VerticalSliderDialogFragment.GRAVITY_END)
                        val sliderPresenter = SliderPresenter(sliderView)
                        val sliderInteractor = SliderUseCase(networkManagerService, sliderPresenter)
                        val sliderController = SliderController(sliderInteractor, cmd, 0f, gestureDomainSize.toFloat())
                        val sliderGesture = VerticalSliderGesture(sliderController)
                        val sliderGestureDetector = GestureDetector(activity, sliderGesture)

                        when(analogUXPolicy) {
                            ANALOG_UX_POLICY_SHORT -> view.setOnTouchListener { _, event ->
                                when (event.actionMasked) {
                                    MotionEvent.ACTION_DOWN -> {
                                        sliderView.show(activity.supportFragmentManager, "slider_vertical")
                                        true
                                    }

                                    MotionEvent.ACTION_MOVE -> {
                                        sliderGestureDetector.onTouchEvent(event)
                                    }

                                    MotionEvent.ACTION_UP -> {
                                        sliderView.dismiss()
                                        true
                                    }

                                    else -> false
                                }
                            }

                            ANALOG_UX_POLICY_LONG -> view.setOnClickListener{
                                sliderView.onDialogCreation = {
                                    it.window!!.decorView.setOnTouchListener { _, event ->
                                        when (event.actionMasked) {
                                            MotionEvent.ACTION_MOVE -> {
                                                sliderGestureDetector.onTouchEvent(event)
                                            }

                                            MotionEvent.ACTION_UP -> {
                                                sliderView.dismiss()

                                            }

                                            else -> {}
                                        }
                                        true
                                    }
                                }

                                sliderView.show(activity.supportFragmentManager, "slider_vertical")
                            }
                        }
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