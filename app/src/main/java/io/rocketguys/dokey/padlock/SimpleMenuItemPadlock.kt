package io.rocketguys.dokey.padlock

import android.graphics.drawable.Drawable
import android.view.MenuItem
import io.rocketguys.dokey.padlock.Padlock.Companion.LockState
import kotlin.properties.Delegates

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
open class SimpleMenuItemPadlock(@LockState initialState: Int) : MenuItemPadlock{
    override var menuItem: MenuItem ?= null
    override val icons: HashMap<Int, Drawable> = hashMapOf()
    override var onStateChange: ((oldState: Int, newState: Int) -> Unit) ?= null

    override var state: Int by Delegates.observable(initialState) { _, oldValue, newValue ->
        updateIcon()
        onStateChange?.invoke(oldValue, newValue)
    }

    override fun toggle(): Int{
        state = if (state == Padlock.CLOSE) Padlock.OPEN else Padlock.CLOSE
        return state
    }

    override fun `is`(checkState: Int): Boolean = state == checkState

    override fun updateIcon() {
        if (icons.containsKey(state)) menuItem?.icon = icons[state]
    }
}