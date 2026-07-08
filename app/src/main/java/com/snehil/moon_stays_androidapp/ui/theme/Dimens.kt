package com.snehil.moon_stays_androidapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Spacing tokens — canonical source: design-system/tokens.json → spacing (4dp base unit)
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp

    val screenMargin = 16.dp
    val cardPadding = 16.dp
    val sectionGap = 32.dp
}

// Radius tokens — design-system/tokens.json → radius
// chips = pill, buttons/text fields = md, cards/images = lg, modals/sheets = xl
object Radii {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val pill = 999.dp
}

val MoonShapes = Shapes(
    extraSmall = RoundedCornerShape(Radii.sm),
    small = RoundedCornerShape(Radii.md),
    medium = RoundedCornerShape(Radii.lg),
    large = RoundedCornerShape(Radii.xl),
    extraLarge = RoundedCornerShape(Radii.xl)
)
