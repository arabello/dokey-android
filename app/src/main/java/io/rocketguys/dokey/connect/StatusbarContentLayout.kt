package io.rocketguys.dokey.connect

import android.content.Context
import android.support.design.snackbar.ContentViewCallback
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class StatusbarContentLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), ContentViewCallback {

    constructor(context: Context) : this(context, null)

    override fun animateContentIn(delay: Int, duration: Int) {
    }

    override fun animateContentOut(delay: Int, duration: Int) {
    }
}