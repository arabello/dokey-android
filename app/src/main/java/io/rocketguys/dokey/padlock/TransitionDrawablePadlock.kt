package io.rocketguys.dokey.padlock

import android.graphics.drawable.TransitionDrawable
import android.view.MenuItem
import io.rocketguys.dokey.padlock.Padlock.Companion.LockState
import kotlin.properties.Delegates

/**
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class TransitionDrawablePadlock(@LockState initialState: Int, private val transitionDuration: Int) : SimpleMenuItemPadlock(initialState) {

    override var state: Int by Delegates.observable(initialState) { _, oldValue, newValue ->
        updateIcon()
        onStateChange?.invoke(oldValue, newValue)
    }

    override fun updateIcon(){
        if (icons.containsKey(state)) menuItem?.updateIcon()
    }

    private fun MenuItem.updateIcon(){
        val trans = TransitionDrawable(arrayOf(icon, icons[state]))
        trans.isCrossFadeEnabled = true
        icon = trans
        trans.startTransition(transitionDuration)
    }
}