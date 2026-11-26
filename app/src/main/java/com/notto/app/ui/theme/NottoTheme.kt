package com.notto.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class NottoColors(
    val bg: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val green: Color,
    val red: Color,
    val hairline: Color,
    val cardGradients: List<Brush>
)

val LightNottoColors = NottoColors(
    bg = Color(0xFFF4F3F8),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFBFBFE),
    text = Color(0xFF1C1C1E),
    muted = Color(0xFF75757D),
    accent = Color(0xFF5E5CE6),
    green = Color(0xFF34C759),
    red = Color(0xFFFF3B30),
    hairline = Color(0x1F3C3C43),
    cardGradients = listOf(
        Brush.linearGradient(listOf(Color(0xFFEBE8FF), Color(0xFFD3CEFF))),
        Brush.linearGradient(listOf(Color(0xFFE2F8EE), Color(0xFFC2EEDA))),
        Brush.linearGradient(listOf(Color(0xFFFFEDE5), Color(0xFFFFD5C7))),
        Brush.linearGradient(listOf(Color(0xFFE6F3FF), Color(0xFFC8E2FF)))
    )
)

val DarkNottoColors = NottoColors(
    bg = Color(0xFF131318),
    surface = Color(0xFF1E1E26),
    surfaceElevated = Color(0xFF282833),
    text = Color(0xFFF4F3F8),
    muted = Color(0xFFA0A0AC),
    accent = Color(0xFF7A78FF),
    green = Color(0xFF32D74B),
    red = Color(0xFFFF453A),
    hairline = Color(0x28FFFFFF),
    cardGradients = listOf(
        Brush.linearGradient(listOf(Color(0xFF2C2947), Color(0xFF23203B))),
        Brush.linearGradient(listOf(Color(0xFF1E382E), Color(0xFF172C24))),
        Brush.linearGradient(listOf(Color(0xFF3E2924), Color(0xFF311F1B))),
        Brush.linearGradient(listOf(Color(0xFF1F3144), Color(0xFF172636)))
    )
)

val LocalNottoColors = staticCompositionLocalOf { LightNottoColors }

object NottoMotion {
    val SpringEase = CubicBezierEasing(0.22f, 1.12f, 0.36f, 1f)
    val PopEase = CubicBezierEasing(0.34f, 1.45f, 0.64f, 1f)

    fun <T> softSpring() = spring<T>(
        dampingRatio = 0.80f,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> bouncySpring() = spring<T>(
        dampingRatio = 0.62f,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> standardTween(ms: Int = 240) = tween<T>(durationMillis = ms, easing = SpringEase)
}
