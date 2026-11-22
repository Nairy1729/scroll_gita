package com.scrollthegita.core.design.background

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import com.scrollthegita.core.design.GitaColors
import com.scrollthegita.core.design.LocalGitaPalette
import com.scrollthegita.core.model.BackgroundStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Theme-driven ambient background. Motion runs only for the active page when animations are enabled,
 * and only inside this composable's Canvas draw phase, so shloka text never recomposes on animation ticks.
 *
 * Each style provides:
 * 1. An ultra-smooth, low-cost ambient animated variant (gentle motion).
 * 2. An identical, motion-safe static variant when "still" is selected or when system reduced motion is active.
 *
 * Performance target: < 2 ms frame cost on Pixel 7 emulator.
 * Reusable paths and pre-calculated static coordinate arrays eliminate garbage collection allocations.
 */
@Composable
fun SpiritualBackground(
    style: BackgroundStyle,
    animationsEnabled: Boolean,
    isPageActive: Boolean,
    motionPaused: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalGitaPalette.current
    val colors = remember(palette.parchment) { backgroundColors(palette.parchment) }
    val reducedMotion = rememberReducedMotion()
    val animate = shouldAnimateBackground(
        animationsEnabled = animationsEnabled,
        isPageActive = isPageActive,
        motionPaused = motionPaused,
        systemReducedMotion = reducedMotion,
    )
    if (animate) {
        MovingBackground(style = style, colors = colors, modifier = modifier)
    } else {
        StillBackground(style = style, colors = colors, modifier = modifier)
    }
}

@Composable
private fun MovingBackground(
    style: BackgroundStyle,
    colors: BgColors,
    modifier: Modifier,
) {
    val flowPaths = remember { List(3) { Path() } }
    val transition = rememberInfiniteTransition(label = "spiritual_bg")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleMillis(style), easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "bg_phase",
    )
    Canvas(modifier) {
        drawSpiritual(style, progress.value, colors, flowPaths)
    }
}

@Composable
private fun StillBackground(
    style: BackgroundStyle,
    colors: BgColors,
    modifier: Modifier,
) {
    val flowPaths = remember { List(3) { Path() } }
    Canvas(modifier) {
        drawSpiritual(style, progress = 0.35f, colors = colors, flowPaths = flowPaths)
    }
}

fun shouldAnimateBackground(
    animationsEnabled: Boolean,
    isPageActive: Boolean,
    motionPaused: Boolean,
    systemReducedMotion: Boolean,
): Boolean = animationsEnabled && isPageActive && !motionPaused && !systemReducedMotion

@Composable
private fun rememberReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        runCatching {
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

private fun cycleMillis(style: BackgroundStyle): Int = when (style) {
    BackgroundStyle.KARMA_SUNRISE -> 24_000
    BackgroundStyle.PEACE_LOTUS -> 28_000
    BackgroundStyle.WISDOM_LIGHT -> 22_000
    BackgroundStyle.DETACHMENT_FLOW -> 26_000
    BackgroundStyle.DEVOTION_AURA -> 20_000
    BackgroundStyle.MEDITATION_MANDALA -> 36_000
    BackgroundStyle.COURAGE_DAWN -> 24_000
    BackgroundStyle.DIVINE_COSMOS -> 22_000
    BackgroundStyle.PARCHMENT_GRAIN -> 30_000
}

/** 0..1 smooth sinusoidal wave, continuous across restarts. */
private fun breath(progress: Float, offset: Float = 0f): Float {
    val theta = ((progress + offset) * 2.0 * PI).toFloat()
    return (sin(theta) + 1f) / 2f
}

private fun DrawScope.drawSpiritual(
    style: BackgroundStyle,
    progress: Float,
    colors: BgColors,
    flowPaths: List<Path>,
) {
    drawRect(colors.base)
    when (style) {
        BackgroundStyle.KARMA_SUNRISE -> drawKarma(progress, colors)
        BackgroundStyle.PEACE_LOTUS -> drawLotus(progress, colors)
        BackgroundStyle.WISDOM_LIGHT -> drawWisdom(progress, colors)
        BackgroundStyle.DETACHMENT_FLOW -> drawFlow(progress, colors, flowPaths)
        BackgroundStyle.DEVOTION_AURA -> drawDevotion(progress, colors)
        BackgroundStyle.MEDITATION_MANDALA -> drawMandala(progress, colors)
        BackgroundStyle.COURAGE_DAWN -> drawDawn(progress, colors)
        BackgroundStyle.DIVINE_COSMOS -> drawCosmos(progress, colors)
        BackgroundStyle.PARCHMENT_GRAIN -> drawParchmentGrain(progress, colors)
    }
    // Keeps the middle of the screen close to base color so shloka text has guaranteed WCAG AA contrast
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(colors.base.copy(alpha = 0.72f), colors.base.copy(alpha = 0f)),
            center = Offset(size.width / 2f, size.height * 0.46f),
            radius = size.minDimension * 0.62f,
        ),
    )
}

/** Style 1: Slow Drifting Gradient */
private fun DrawScope.drawKarma(progress: Float, colors: BgColors) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(colors.deep, colors.base, colors.accent.copy(alpha = 0.25f)),
        ),
    )
    val drift = breath(progress)
    val centerOffset = Offset(size.width * (0.38f + 0.24f * drift), size.height * (0.76f + 0.04f * drift))
    val radius = size.minDimension * 0.45f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colors.gold.copy(alpha = 0.20f), Color.Transparent),
            center = centerOffset,
            radius = radius,
        ),
        radius = radius,
        center = centerOffset,
    )
}

/** Style 2: Sacred Lotus Bloom */
private fun DrawScope.drawLotus(progress: Float, colors: BgColors) {
    drawRect(colors.base)
    val alpha = 0.09f + 0.04f * breath(progress)
    val center = Offset(size.width * 0.5f, size.height * 0.72f)
    val petal = size.minDimension * 0.11f
    repeat(8) { index ->
        rotate(index * 45f, center) {
            drawOval(
                color = colors.stroke,
                topLeft = Offset(center.x - petal * 0.28f, center.y - petal * 1.35f),
                size = Size(petal * 0.56f, petal * 1.2f),
                alpha = alpha,
                style = Stroke(width = size.minDimension * 0.0025f),
            )
        }
    }
    drawCircle(
        color = colors.stroke,
        radius = petal * 0.22f,
        center = center,
        alpha = alpha,
        style = Stroke(width = size.minDimension * 0.0025f),
    )
}

/** Style 3: Wisdom Radiant Light */
private fun DrawScope.drawWisdom(progress: Float, colors: BgColors) {
    val radius = size.minDimension * (0.28f + 0.08f * breath(progress))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colors.gold.copy(alpha = 0.18f), Color.Transparent),
            center = Offset(size.width * 0.5f, size.height * 0.28f),
            radius = radius,
        ),
        radius = radius,
        center = Offset(size.width * 0.5f, size.height * 0.28f),
    )
}

/** Style 4: Soft Flowing Streams */
private fun DrawScope.drawFlow(progress: Float, colors: BgColors, paths: List<Path>) {
    val shift = (breath(progress) - 0.5f) * size.width * 0.08f
    val stroke = Stroke(width = size.minDimension * 0.004f)
    repeat(3) { index ->
        val path = paths[index]
        path.rewind()
        val y = size.height * (0.58f + index * 0.08f)
        path.moveTo(-40f + shift, y)
        path.quadraticTo(
            size.width * 0.35f + shift,
            y - size.height * 0.06f,
            size.width + 40f + shift,
            y + size.height * 0.02f,
        )
        drawPath(
            path = path,
            color = colors.mist,
            alpha = 0.16f,
            style = stroke,
        )
    }
}

/** Style 5: Devotion Aura */
private fun DrawScope.drawDevotion(progress: Float, colors: BgColors) {
    val amount = breath(progress)
    val radius = size.minDimension * (0.34f + 0.06f * amount)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.accent.copy(alpha = 0.14f + 0.06f * amount),
                colors.gold.copy(alpha = 0.04f),
                Color.Transparent,
            ),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

/** Style 6: Faint Mandala Rotation */
private fun DrawScope.drawMandala(progress: Float, colors: BgColors) {
    val scale = 0.98f + 0.04f * breath(progress)
    val c = center
    val base = size.minDimension * 0.22f * scale
    val stroke = Stroke(width = size.minDimension * 0.002f)
    drawCircle(color = colors.gold, radius = base, center = c, alpha = 0.14f, style = stroke)
    drawCircle(color = colors.gold, radius = base * 0.62f, center = c, alpha = 0.10f, style = stroke)
    drawCircle(color = colors.gold, radius = base * 1.38f, center = c, alpha = 0.08f, style = stroke)

    // Gentle micro-rotation across a full cycle (slow, meditative drift)
    val rotationAngle = (progress * 360f) % 360f
    rotate(rotationAngle * 0.05f, c) {
        repeat(6) { index ->
            val angle = index * 30f
            rotate(angle, c) {
                drawLine(
                    color = colors.gold,
                    start = Offset(c.x - base * 1.38f, c.y),
                    end = Offset(c.x + base * 1.38f, c.y),
                    strokeWidth = size.minDimension * 0.0016f,
                    alpha = 0.09f,
                )
            }
        }
    }
}

/** Style 7: Courage Dawn */
private fun DrawScope.drawDawn(progress: Float, colors: BgColors) {
    val rise = breath(progress)
    val glowCenter = Offset(size.width * 0.5f, size.height * (0.86f - 0.16f * rise))
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(colors.base, colors.deep, colors.accent.copy(alpha = 0.20f)),
        ),
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colors.gold.copy(alpha = 0.08f + 0.10f * rise), Color.Transparent),
            center = glowCenter,
            radius = size.minDimension * 0.48f,
        ),
        radius = size.minDimension * 0.48f,
        center = glowCenter,
    )
}

/** Style 8: Soft Particle Dust */
private fun DrawScope.drawCosmos(progress: Float, colors: BgColors) {
    var index = 0
    while (index < CosmosPoints.size) {
        val baseX = CosmosPoints[index] * size.width
        val baseY = CosmosPoints[index + 1] * size.height
        val phase = CosmosPoints[index + 2]
        val driftX = sin(((progress + phase) * 2.0 * PI).toFloat()) * size.width * 0.015f
        val driftY = cos(((progress + phase) * 2.0 * PI).toFloat()) * size.height * 0.012f
        val particleRadius = size.minDimension * (0.0035f + 0.0015f * breath(progress, phase))
        drawCircle(
            color = colors.gold,
            radius = particleRadius,
            center = Offset(baseX + driftX, baseY + driftY),
            alpha = 0.06f + 0.12f * breath(progress, phase),
        )
        index += 3
    }
}

/** Style 9: Parchment Grain (organic fibers and subtle warm texture) */
private fun DrawScope.drawParchmentGrain(progress: Float, colors: BgColors) {
    val strokeWidth = size.minDimension * 0.0015f
    var index = 0
    val breathFactor = 0.85f + 0.15f * breath(progress)
    while (index < GrainFibers.size) {
        val x = GrainFibers[index] * size.width
        val y = GrainFibers[index + 1] * size.height
        val length = GrainFibers[index + 2] * size.minDimension
        val angleDeg = GrainFibers[index + 3]
        val baseAlpha = GrainFibers[index + 4] * breathFactor

        val rad = angleDeg * PI.toFloat() / 180f
        val dx = cos(rad) * length
        val dy = sin(rad) * length

        drawLine(
            color = colors.fiber,
            start = Offset(x, y),
            end = Offset(x + dx, y + dy),
            strokeWidth = strokeWidth,
            alpha = baseAlpha,
        )
        index += 5
    }
}

private val CosmosPoints = floatArrayOf(
    0.18f, 0.16f, 0.05f,
    0.72f, 0.12f, 0.22f,
    0.84f, 0.28f, 0.41f,
    0.12f, 0.34f, 0.63f,
    0.63f, 0.22f, 0.80f,
    0.30f, 0.78f, 0.15f,
    0.78f, 0.70f, 0.33f,
    0.48f, 0.86f, 0.57f,
    0.22f, 0.58f, 0.71f,
    0.88f, 0.52f, 0.90f,
    0.15f, 0.82f, 0.44f,
    0.68f, 0.62f, 0.18f,
    0.40f, 0.18f, 0.75f,
    0.85f, 0.84f, 0.30f,
)

private val GrainFibers = floatArrayOf(
    0.12f, 0.14f, 0.018f, 25f, 0.10f,
    0.35f, 0.22f, 0.024f, 80f, 0.08f,
    0.78f, 0.18f, 0.015f, 140f, 0.11f,
    0.88f, 0.32f, 0.022f, 15f, 0.09f,
    0.20f, 0.42f, 0.019f, 65f, 0.10f,
    0.65f, 0.38f, 0.025f, 110f, 0.07f,
    0.15f, 0.62f, 0.020f, 45f, 0.11f,
    0.42f, 0.68f, 0.016f, 160f, 0.09f,
    0.82f, 0.60f, 0.022f, 30f, 0.10f,
    0.28f, 0.82f, 0.018f, 95f, 0.08f,
    0.58f, 0.84f, 0.024f, 20f, 0.12f,
    0.75f, 0.78f, 0.017f, 135f, 0.09f,
    0.90f, 0.88f, 0.021f, 50f, 0.10f,
    0.08f, 0.75f, 0.023f, 75f, 0.08f,
    0.48f, 0.12f, 0.016f, 170f, 0.11f,
)

@Immutable
data class BgColors(
    val base: Color,
    val deep: Color,
    val accent: Color,
    val gold: Color,
    val mist: Color,
    val stroke: Color,
    val fiber: Color,
)

fun backgroundColors(parchment: Boolean): BgColors = if (parchment) {
    BgColors(
        base = GitaColors.Parchment,
        deep = GitaColors.SoftCream,
        accent = GitaColors.MutedSaffron,
        gold = GitaColors.ParchmentGold,
        mist = GitaColors.CalmBlueGray,
        stroke = GitaColors.CalmBlueGray,
        fiber = Color(0xFFC7BBAE),
    )
} else {
    BgColors(
        base = GitaColors.Midnight,
        deep = GitaColors.DeepBlue,
        accent = GitaColors.MutedSaffron,
        gold = GitaColors.AntiqueGold,
        mist = GitaColors.CalmBlueGray,
        stroke = GitaColors.AntiqueGold,
        fiber = Color(0xFF2E3553),
    )
}
