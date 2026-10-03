package com.metron.app.haptics

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.metron.app.MetronApp

object HapticsManager {

    private val vibrator: Vibrator? by lazy {
        val context = MetronApp.instance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun isEnabled(): Boolean {
        return MetronApp.repository.isHapticsEnabled.value
    }

    /**
     * Subtle light tick for keypad button presses, chips, and tab transitions
     */
    fun tick() {
        if (!isEnabled()) return
        vibrator?.let { v ->
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(10)
            }
        }
    }

    /**
     * Standard tactile click
     */
    fun click() {
        if (!isEnabled()) return
        vibrator?.let { v ->
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(20)
            }
        }
    }

    /**
     * Satisfying double-pulse haptic for saving an expense or completing a transaction
     */
    fun success() {
        if (!isEnabled()) return
        vibrator?.let { v ->
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else {
                val timings = longArrayOf(0, 15, 60, 25)
                @Suppress("DEPRECATION")
                v.vibrate(timings, -1)
            }
        }
    }

    /**
     * Stronger feedback for destructive actions (deletions, warnings, budget limits reached)
     */
    fun warning() {
        if (!isEnabled()) return
        vibrator?.let { v ->
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                val timings = longArrayOf(0, 30, 80, 50)
                @Suppress("DEPRECATION")
                v.vibrate(timings, -1)
            }
        }
    }
}
