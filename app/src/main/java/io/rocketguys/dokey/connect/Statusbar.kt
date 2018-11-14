package io.rocketguys.dokey.connect

import android.support.design.widget.BaseTransientBottomBar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.rocketguys.dokey.R
import kotlin.properties.Delegates


/**
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class Statusbar
    private constructor(parent: ViewGroup, content: View, contentViewCallback: android.support.design.snackbar.ContentViewCallback) :
        BaseTransientBottomBar<Statusbar>(parent, content, contentViewCallback) {

    companion object {
        private fun make(parent: ViewGroup): Statusbar {
            val content = LayoutInflater.from(parent.context).inflate(R.layout.snackbar_connecting, parent, false) as StatusbarContentLayout
            return Statusbar(parent, content, content)
        }

        fun connecting(parent: ViewGroup, action: (() -> Unit) ?= null): Statusbar{
            val statusBar = make(parent)
            statusBar.message = parent.context.getString(R.string.snack_connecting_msg)
            statusBar.actionText = parent.context.getString(R.string.snack_connecting_action)
            statusBar.action = action
            statusBar.duration = LENGTH_INDEFINITE

            return statusBar
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