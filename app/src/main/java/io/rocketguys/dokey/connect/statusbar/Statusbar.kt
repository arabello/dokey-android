package io.rocketguys.dokey.connect.statusbar

import android.support.annotation.IntDef
import android.support.annotation.IntRange
import android.support.annotation.StringRes
import android.support.design.widget.BaseTransientBottomBar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.rocketguys.dokey.R
import kotlin.properties.Delegates
import android.widget.FrameLayout
import android.support.design.widget.CoordinatorLayout




/**
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class Statusbar
    private constructor(parent: ViewGroup, content: View, contentViewCallback: android.support.design.snackbar.ContentViewCallback) :
        BaseTransientBottomBar<Statusbar>(parent, content, contentViewCallback) {

    var onShow: (() -> Unit) ?= null
    var onDismiss: (() -> Unit) ?= null

    init {
        getView().findViewById<Button>(R.id.statusbar_action).visibility = View.GONE
        getView().setPadding(0,0,0,0)
    }

    companion object {
        @IntDef(BaseTransientBottomBar.LENGTH_INDEFINITE, BaseTransientBottomBar.LENGTH_SHORT, BaseTransientBottomBar.LENGTH_LONG)
        @IntRange(from = 1)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Duration

        const val LENGTH_INDEFINITE = BaseTransientBottomBar.LENGTH_INDEFINITE
        const val LENGTH_SHORT = BaseTransientBottomBar.LENGTH_SHORT
        const val LENGTH_LONG = BaseTransientBottomBar.LENGTH_LONG

        fun make(view: View, @StringRes textId: Int, @Duration duration: Int)
                = make(view, view.context.resources.getString(textId), duration)

        fun make(view: View, message: String, @Duration duration: Int): Statusbar {
            val parent = findSuitableParent(view) ?:
                throw IllegalArgumentException("No suitable parent found from the given view. Please provide a valid view.")

            val content = LayoutInflater.from(parent.context).inflate(R.layout.statusbar, parent, false)
            val statusbar = Statusbar(parent, content, StatusbarContentLayout())
            statusbar.message = message
            statusbar.duration = duration
            return statusbar
        }

        private fun findSuitableParent(targetView: View?): ViewGroup? {
            var view = targetView
            var fallback: ViewGroup? = null
            do {
                if (view is CoordinatorLayout) {
                    // We've found a CoordinatorLayout, use it
                    return view
                } else if (view is FrameLayout) {
                    if (view.getId() == android.R.id.content) {
                        // If we've hit the decor content view, then we didn't find a CoL in the
                        // hierarchy, so use it.
                        return view
                    } else {
                        // It's not the content view but we'll use it as our fallback
                        fallback = view
                    }
                }

                if (view != null) {
                    // Else, we will loop and crawl up the view hierarchy and try to find a parent
                    val parent = view.parent
                    view = if (parent is View) parent else null
                }
            } while (view != null)

            // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
            return fallback
        }
    }

    var message by Delegates.observable<String>(""){_, _, newValue ->
        getView().findViewById<TextView>(R.id.statusbar_msg).text = newValue
    }

    fun setDismissText(@StringRes textRes: Int) = setAction(textRes, null)

    fun setDismissText(actionText: String) = setAction(actionText, null)

    fun setAction(@StringRes textRes: Int, action: (() -> Unit) ?= null) = setAction(getView().context.resources.getString(textRes), action)

    fun setAction(actionText: String, action: (() -> Unit) ?= null){
        getView().findViewById<Button>(R.id.statusbar_action).apply {
            visibility = View.VISIBLE
            text = actionText
            setOnClickListener {
                action?.invoke()
                dismiss()
            }
        }
    }

    override fun show() {
        super.show()
        onShow?.invoke()
    }

    override fun dismiss() {
        super.dismiss()
        onDismiss?.invoke()
    }
}