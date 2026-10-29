package com.example.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.graphics.Color

enum class HapticPatternType(
    val label: String,
    val description: String,
    val waveformTimings: LongArray,
    val waveformAmplitudes: IntArray
) {
    MESSAGE_DELIVERED(
        label = "Message Delivered Pulse",
        description = "Crisp double-tap confirmation for E2EE packet dispatch",
        waveformTimings = longArrayOf(0L, 18L, 35L, 28L),
        waveformAmplitudes = intArrayOf(0, 180, 0, 255)
    ),
    FILE_TRANSFER_COMPLETE(
        label = "Swarm File Complete Harmonic",
        description = "Tri-pulse ascending confirmation for 256KB BLAKE3 file completion",
        waveformTimings = longArrayOf(0L, 20L, 35L, 25L, 35L, 45L),
        waveformAmplitudes = intArrayOf(0, 140, 0, 200, 0, 255)
    ),
    DRAWER_OR_TELEMETRY_TOGGLE(
        label = "Telemetry & Drawer Micro-Tick",
        description = "Subtle 14ms physical detent for drawer, telemetry, and sheet toggles",
        waveformTimings = longArrayOf(0L, 14L),
        waveformAmplitudes = intArrayOf(0, 160)
    ),
    SWIPE_ACTION_SNAP(
        label = "Gesture Threshold Snap",
        description = "Tactile snap when swipe-to-pin or swipe-to-reply crosses activation threshold",
        waveformTimings = longArrayOf(0L, 22L),
        waveformAmplitudes = intArrayOf(0, 230)
    ),
    SWIPE_TRASH_WARNING(
        label = "Conversation Trash Pulse",
        description = "Heavy dual-thump confirmation when swiping left to trash a thread",
        waveformTimings = longArrayOf(0L, 32L, 30L, 38L),
        waveformAmplitudes = intArrayOf(0, 255, 0, 220)
    ),
    TYPOGRAPHY_CONTRAST_SHIFT(
        label = "Readability Mode Detent",
        description = "Instant tactile click when switching font scale or sunlight contrast mode",
        waveformTimings = longArrayOf(0L, 16L, 25L, 16L),
        waveformAmplitudes = intArrayOf(0, 150, 0, 200)
    )
}

enum class FontScalePreset(
    val label: String,
    val shortBadge: String,
    val scaleFactor: Float
) {
    COMPACT("Compact (0.9x)", "A-", 0.90f),
    STANDARD("Standard (1.0x)", "A", 1.00f),
    LARGE("Large Field (1.18x)", "A+", 1.18f),
    EXTRA_LARGE("Protest Max (1.35x)", "A++", 1.35f);

    fun next(): FontScalePreset {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }
}

enum class AdaptiveContrastMode(
    val label: String,
    val shortBadge: String,
    val description: String
) {
    AMOLED_STEALTH(
        label = "AMOLED Stealth (#000000)",
        shortBadge = "AMOLED",
        description = "Pure AMOLED Black (#000000) with Samsung Teal (#007A5E) & Dark Charcoal (#38383A)"
    ),
    HIGH_CONTRAST_PROTEST_NIGHT(
        label = "High-Contrast Night",
        shortBadge = "HI-CON",
        description = "Ultra-sharp neon borders & maximum white luminance for smoke/low-visibility night ops"
    ),
    SUNLIGHT_OUTDOOR_MAX(
        label = "Harsh Sunlight Daylight",
        shortBadge = "SUNLIGHT",
        description = "High-luminance daylight contrast palette for direct overhead sunlight readability"
    );

    fun next(): AdaptiveContrastMode {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }
}

data class AdaptiveContrastPalette(
    val backgroundColor: Color,
    val elevatedSurfaceColor: Color,
    val inputPillColor: Color,
    val outgoingBubbleColor: Color,
    val incomingBubbleColor: Color,
    val outgoingTextColor: Color,
    val incomingTextColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val timestampTextColor: Color,
    val accentCyanColor: Color,
    val accentGreenColor: Color,
    val bubbleBorderWidthDp: Float
)

object TacticalHapticEngine {

    @Volatile
    var isHapticsEnabled: Boolean = true

    fun triggerTacticalHaptic(
        context: Context,
        pattern: HapticPatternType
    ): String {
        if (!isHapticsEnabled) {
            return "Haptics Muted (${pattern.label})"
        }
        try {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(
                        pattern.waveformTimings,
                        pattern.waveformAmplitudes,
                        -1
                    )
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(25L)
                }
            }
        } catch (_: Exception) {
            // Graceful fallback in JVM / emulator environments without hardware vibrator
        }
        return "📳 ${pattern.label} (${pattern.waveformTimings.sum()}ms)"
    }

    fun resolvePalette(mode: AdaptiveContrastMode): AdaptiveContrastPalette {
        return when (mode) {
            AdaptiveContrastMode.AMOLED_STEALTH -> AdaptiveContrastPalette(
                backgroundColor = Color(0xFF000000),
                elevatedSurfaceColor = Color(0xFF141416),
                inputPillColor = Color(0xFF1C1C1E),
                outgoingBubbleColor = Color(0xFF007A5E),
                incomingBubbleColor = Color(0xFF38383A),
                outgoingTextColor = Color(0xFFFFFFFF),
                incomingTextColor = Color(0xFFFFFFFF),
                primaryTextColor = Color(0xFFFFFFFF),
                secondaryTextColor = Color(0xFFAEAEB2),
                timestampTextColor = Color(0xFF8E8E93),
                accentCyanColor = Color(0xFF36B5FF),
                accentGreenColor = Color(0xFF00E676),
                bubbleBorderWidthDp = 0f
            )

            AdaptiveContrastMode.HIGH_CONTRAST_PROTEST_NIGHT -> AdaptiveContrastPalette(
                backgroundColor = Color(0xFF000000),
                elevatedSurfaceColor = Color(0xFF1E1E24),
                inputPillColor = Color(0xFF25252C),
                outgoingBubbleColor = Color(0xFF005C46),
                incomingBubbleColor = Color(0xFF28282E),
                outgoingTextColor = Color(0xFFFFFFFF),
                incomingTextColor = Color(0xFFFFFFFF),
                primaryTextColor = Color(0xFFFFFFFF),
                secondaryTextColor = Color(0xFFE5E5EA),
                timestampTextColor = Color(0xFFD1D1D6),
                accentCyanColor = Color(0xFF00FFFF),
                accentGreenColor = Color(0xFF00FF88),
                bubbleBorderWidthDp = 1.5f
            )

            AdaptiveContrastMode.SUNLIGHT_OUTDOOR_MAX -> AdaptiveContrastPalette(
                backgroundColor = Color(0xFFF5F7FA),
                elevatedSurfaceColor = Color(0xFFFFFFFF),
                inputPillColor = Color(0xFFE4E9F0),
                outgoingBubbleColor = Color(0xFF005B44),
                incomingBubbleColor = Color(0xFFDCE2EC),
                outgoingTextColor = Color(0xFFFFFFFF),
                incomingTextColor = Color(0xFF0A0E14),
                primaryTextColor = Color(0xFF0A0E14),
                secondaryTextColor = Color(0xFF2C3545),
                timestampTextColor = Color(0xFF455266),
                accentCyanColor = Color(0xFF0055B3),
                accentGreenColor = Color(0xFF006E3A),
                bubbleBorderWidthDp = 1.2f
            )
        }
    }
}
