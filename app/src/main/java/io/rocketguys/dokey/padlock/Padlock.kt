package io.rocketguys.dokey.padlock

import android.support.annotation.IntDef

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
interface Padlock {
    companion object {
        @IntDef(CLOSE, OPEN)
        @Retention(AnnotationRetention.SOURCE)
        annotation class LockState

        const val CLOSE = 0
        const val OPEN = 1
    }

    @LockState
    var state: Int

    @LockState
    fun toggle(): Int

    fun `is`(@LockState checkState: Int): Boolean

    var onStateChange: ((oldState: Int, newState: Int) -> Unit)?
}