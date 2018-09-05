package io.rocketguys.dokey.util

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.preference.PreferenceManager
import io.rocketguys.dokey.R

/**
 * Helper class to make vibration.
 * It loads the default shared preferences,
 * all methods such as [ContextualVibrator.oneShotVibration]
 * won't work if vibration preference is set to disable
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class ContextualVibrator private constructor(private val vibrator: Vibrator, private val shouldVibrate: Boolean) {
    
    companion object {
        private var instance: ContextualVibrator ?= null
        fun from(context: Context): ContextualVibrator =
                if (instance != null)
                    instance as ContextualVibrator
                else
                    ContextualVibrator(context.applicationContext.getSystemService(VIBRATOR_SERVICE) as Vibrator,
                            PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_ux_vibration_key), false))
    }

    fun oneShotVibration(millis: Long): Boolean {
        if (!shouldVibrate)
            return false

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(millis)
        }
        
        return true
    }
}
