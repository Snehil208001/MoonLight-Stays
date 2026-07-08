package com.snehil.moon_stays_androidapp.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

// Motion tokens — canonical source: design-system/tokens.json → motion
// Same durations/easings as the web app (Tailwind duration-* / Framer Motion).
object Motion {
    const val DurationFast = 150
    const val DurationBase = 250
    const val DurationSlow = 400
    const val DurationScreen = 500
    const val DurationSkeleton = 1500

    val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Decelerate = CubicBezierEasing(0f, 0f, 0f, 1f)
    val Accelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)

    // Interaction constants — see DESIGN_SYSTEM.md §7
    const val ButtonPressScale = 0.97f
    const val CardPressScale = 0.98f
}
