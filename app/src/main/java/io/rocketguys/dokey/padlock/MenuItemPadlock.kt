package io.rocketguys.dokey.padlock

import android.graphics.drawable.Drawable
import android.view.MenuItem
import io.rocketguys.dokey.padlock.Padlock.Companion.LockState

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
interface MenuItemPadlock : Padlock {
    var menuItem: MenuItem?

    @LockState
    val icons: HashMap<Int, Drawable>

    fun updateIcon()
}