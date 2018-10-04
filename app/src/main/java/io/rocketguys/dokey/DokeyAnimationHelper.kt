package io.rocketguys.dokey

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.support.v7.widget.RecyclerView
import android.view.MenuItem

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */


// Transition animation to change action icons in the mToolbar
fun MenuItem.transIconTo(newIcon: Drawable, duration: Int) {
    val crossfader = TransitionDrawable(arrayOf(this.icon, newIcon))
    crossfader.isCrossFadeEnabled = true
    this.icon = crossfader
    crossfader.startTransition(duration)
}

// Transition animation to change Active Apps RecyclerView background
fun RecyclerView.transBackgroundTo(newBackground: Drawable, duration: Int){
    val start = if (this.background == null) ColorDrawable(Color.TRANSPARENT) else this.background
    val crossfader = TransitionDrawable(arrayOf(start, newBackground))
    crossfader.isCrossFadeEnabled = true
    this.background = crossfader
    crossfader.startTransition(duration)
}

// Transition animation to change