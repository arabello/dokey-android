package io.rocketguys.dokey.connect

import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import io.rocketguys.dokey.R
import kotlin.properties.Delegates


/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class ConnectionStatusSnackBar
    private constructor(parent: ViewGroup, content: View, contentViewCallback: android.support.design.snackbar.ContentViewCallback) :
        BaseTransientBottomBar<ConnectionStatusSnackBar>(parent, content, contentViewCallback) {

    class CustomContentViewCallback(val content: View) : android.support.design.snackbar.ContentViewCallback {

        override fun animateContentOut(delay: Int, duration: Int) {
            // add custom *in animations for your views
            // e.g. original snackbar uses alpha animation, from 0 to 1
            ViewCompat.setScaleY(content, 0f)
            ViewCompat.animate(content)
                    .scaleY(1f).setDuration(duration.toLong()).startDelay = delay.toLong()
        }

        override fun animateContentIn(delay: Int, duration: Int) {
            // add custom *out animations for your views
            // e.g. original snackbar uses alpha animation, from 1 to 0
            ViewCompat.setScaleY(content, 1f)
            ViewCompat.animate(content)
                    .scaleY(0f)
                    .setDuration(duration.toLong()).startDelay = delay.toLong()
        }
    }

    companion object {
        fun make(parent: ViewGroup): ConnectionStatusSnackBar {
            // inflate custom layout
            val inflater = LayoutInflater.from(parent.context)
            val content = inflater.inflate(R.layout.snackbar_connecting, parent, false)

            // create snackbar with custom view
            val callback = CustomContentViewCallback(content)
            val snackBar = ConnectionStatusSnackBar(parent, content, callback)

            // Remove black background padding on left and right
            snackBar.getView().setPadding(0, 0, 0, 0)

            // set snackbar duration
            snackBar.duration = Snackbar.LENGTH_INDEFINITE
            return snackBar
        }
    }

    var message by Delegates.observable<String>(""){_, _, newValue ->
        getView().findViewById<TextView>(R.id.snack_bar_msg).text = newValue
    }

    var actionText by Delegates.observable<String>(""){_, _, newValue ->
        getView().findViewById<TextView>(R.id.snack_bar_action).text = newValue
    }

    var action by Delegates.observable<(() -> Unit)?>(null) { _, _, newValue ->
        getView().findViewById<TextView>(R.id.snack_bar_action).setOnClickListener{
            newValue?.invoke()
            dismiss()
        }
    }
}