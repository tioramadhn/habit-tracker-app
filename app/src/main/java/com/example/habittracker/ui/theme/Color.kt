package com.example.habittracker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/* Brand palette: a calm green/teal, generated from seed #1F6F4A. */
val GreenPrimaryLight = Color(0xFF146C44)
val GreenOnPrimaryLight = Color(0xFFFFFFFF)
val GreenContainerLight = Color(0xFFA2F5BF)
val GreenOnContainerLight = Color(0xFF002110)
val TealSecondaryLight = Color(0xFF4D6357)
val TealContainerLight = Color(0xFFCFE9D9)
val SandTertiaryLight = Color(0xFF3B6470)
val SandContainerLight = Color(0xFFBEEAF8)
val SurfaceLight = Color(0xFFF6FBF4)
val SurfaceVariantLight = Color(0xFFDCE5DB)
val OutlineLight = Color(0xFF707972)

val GreenPrimaryDark = Color(0xFF86D9A5)
val GreenOnPrimaryDark = Color(0xFF00391E)
val GreenContainerDark = Color(0xFF00522E)
val GreenOnContainerDark = Color(0xFFA2F5BF)
val TealSecondaryDark = Color(0xFFB4CCBD)
val TealContainerDark = Color(0xFF354B40)
val SandTertiaryDark = Color(0xFFA3CDDC)
val SandContainerDark = Color(0xFF224C57)
val SurfaceDark = Color(0xFF0F1511)
val SurfaceVariantDark = Color(0xFF414941)
val OutlineDark = Color(0xFF8A938B)

/** Per-habit accent colors, index is stored with the habit. */
private val HabitPaletteLight = listOf(
    Color(0xFF2E7D32),
    Color(0xFF00897B),
    Color(0xFF1E88E5),
    Color(0xFF5E35B1),
    Color(0xFFD81B60),
    Color(0xFFE64A19),
    Color(0xFFF9A825),
    Color(0xFF546E7A),
)

private val HabitPaletteDark = listOf(
    Color(0xFF81C784),
    Color(0xFF4DB6AC),
    Color(0xFF64B5F6),
    Color(0xFFB39DDB),
    Color(0xFFF06292),
    Color(0xFFFF8A65),
    Color(0xFFFFD54F),
    Color(0xFF90A4AE),
)

val HabitColorCount = HabitPaletteLight.size

/** Accent color for a habit, adapted to the active theme. */
@Composable
fun habitColor(colorIndex: Int): Color {
    val palette = if (LocalIsDarkTheme.current) HabitPaletteDark else HabitPaletteLight
    return palette[((colorIndex % palette.size) + palette.size) % palette.size]
}

/** Light-theme swatch, used for the color picker so swatches stay recognizable. */
fun habitSwatch(colorIndex: Int, dark: Boolean): Color {
    val palette = if (dark) HabitPaletteDark else HabitPaletteLight
    return palette[((colorIndex % palette.size) + palette.size) % palette.size]
}
