package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import kotlin.math.*

private const val MIN_ANGULAR_SEP = 6.0 // min degree separation to fan glyphs

data class GlyphPlacement24(
    val position: BodyPosition,
    val finalLongitude: Double,
    val radialOffset: Float
) {
    val body: ChartBody get() = position.body
    val eclipticLongitude: Double get() = position.eclipticLongitude
}

@Composable
fun TransitChartCanvas(
    state: ChartState,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val backgroundBrush = remember {
        Brush.radialGradient(
            colors = listOf(Color(0xFF0F0C1B), Color(0xFF05020A)),
            radius = 1200f
        )
    }

    // Colors of aspects
    val conjunctionColor = Color(0xFFFFB300)   // Gold
    val oppositionColor = Color(0xFFE57373)    // Red
    val trineColor = Color(0xFF81C784)         // Green
    val squareColor = Color(0xFF64B5F6)        // Blue
    val sextileColor = Color(0xFFBA68C8)       // Indigo/Violet
    val minorColor = Color(0x66B0BEC5)         // Soft gray-blue

    val zodiacColors = listOf(
        Color(0xFFFF5252), Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFF4DD0E1), // Aries, Taurus, Gemini, Cancer
        Color(0xFFFFD54F), Color(0xFFA1887F), Color(0xFFF06292), Color(0xFF4FC3F7), // Leo, Virgo, Libra, Scorpio
        Color(0xFFFFB74D), Color(0xFFE0E0E0), Color(0xFF9575CD), Color(0xFF4DB6AC)  // Sag, Cap, Aqu, Pis
    )

    Canvas(
        modifier = modifier
            .background(backgroundBrush)
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val outerRadius = (min(width, height) / 2f) * 0.95f

        if (outerRadius <= 0) return@Canvas

        // Rotation base: if Natal Ascendant is valid, rotate so ASC is at 180° (flat left).
        // Otherwise, baseline Aries 0° to the absolute left (180°).
        val rotationLong = if (state.natalHouses.isValid) {
            state.natalHouses.ascendant
        } else {
            0.0
        }

        // Mathematical conversion from Astrological Longitude counter-clockwise,
        // with alignment rotation to place Ascendant/0° at the flat left (180°).
        fun longToScreenAngle(long: Double): Double {
            val adjusted = (long - rotationLong + 360.0) % 360.0
            return (180.0 - adjusted + 360.0) % 360.0
        }

        fun longToOffset(long: Double, radiusFraction: Float): Offset {
            val angleRad = Math.toRadians(longToScreenAngle(long))
            val r = outerRadius * radiusFraction
            return Offset(
                x = (center.x + r * cos(angleRad)).toFloat(),
                y = (center.y + r * sin(angleRad)).toFloat()
            )
        }

        // Draw decorative starfield
        drawStarfield(center, outerRadius)

        // Draw Zodiac signs segments - radiusFraction: 0.88f to 1.00f
        val zodiacRingInner = 0.86f
        val zodiacRingOuter = 1.00f
        drawZodiacRing(center, outerRadius, zodiacRingInner, zodiacRingOuter, rotationLong, textMeasurer)

        // Draw house cusps if valid
        if (state.natalHouses.isValid) {
            val cusps = state.natalHouses.cusps
            for (i in 1..12) {
                val cuspLong = cusps[i]
                val cuspOffsetInner = longToOffset(cuspLong, 0.30f)
                val cuspOffsetOuter = longToOffset(cuspLong, 0.48f)
                
                // Draw line
                drawLine(
                    color = Color.White.copy(alpha = if (i == 1 || i == 10) 0.5f else 0.2f),
                    start = cuspOffsetInner,
                    end = cuspOffsetOuter,
                    strokeWidth = if (i == 1 || i == 10) 2.dp.toPx() else 1.dp.toPx()
                )

                // Draw house labels
                val midCuspLong = if (i < 12) {
                    val nextCusp = cusps[i + 1]
                    val diff = (nextCusp - cuspLong + 360.0) % 360.0
                    (cuspLong + diff / 2.0) % 360.0
                } else {
                    val nextCusp = cusps[1]
                    val diff = (nextCusp - cuspLong + 360.0) % 360.0
                    (cuspLong + diff / 2.0) % 360.0
                }

                val houseLabelOffset = longToOffset(midCuspLong, 0.36f)
                val labelLayout = textMeasurer.measure(
                    text = i.toString(),
                    style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.35f))
                )
                drawText(
                    textLayoutResult = labelLayout,
                    topLeft = Offset(
                        houseLabelOffset.x - labelLayout.size.width / 2f,
                        houseLabelOffset.y - labelLayout.size.height / 2f
                    )
                )
            }
        }

        // Draw aspect web lines (transit ring: 0.72f, natal ring: 0.48f)
        for (aspect in state.aspects) {
            val nLong = aspect.natalPosition.eclipticLongitude
            val tLong = aspect.transitPosition.eclipticLongitude

            val startOffset = longToOffset(nLong, 0.48f)
            val endOffset = longToOffset(tLong, 0.72f)

            // Select color based on aspect type
            val color = when (aspect.type) {
                AspectType.CONJUNCTION -> conjunctionColor
                AspectType.OPPOSITION -> oppositionColor
                AspectType.TRINE -> trineColor
                AspectType.SQUARE -> squareColor
                AspectType.SEXTILE -> sextileColor
                else -> minorColor
            }

            drawLine(
                color = color.copy(alpha = 0.4f * aspect.strength),
                start = startOffset,
                end = endOffset,
                strokeWidth = (0.5f + 1.5f * aspect.strength).dp.toPx(),
                pathEffect = if (aspect.type.isMajor) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Draw central dynamic hub to hold current datetime/source info
        drawCircle(
            color = Color(0xFF141224),
            radius = outerRadius * 0.28f,
            center = center
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = outerRadius * 0.28f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Draw sensitive Angular Nodes (ASC, MC, IC, DC) if valid
        if (state.natalHouses.isValid) {
            val angles = listOf(
                Pair("ASC", state.natalHouses.ascendant),
                Pair("MC", state.natalHouses.mc),
                Pair("IC", state.natalHouses.ic),
                Pair("DSC", state.natalHouses.descendant)
            )

            for ((label, lValue) in angles) {
                val markInner = longToOffset(lValue, 0.83f)
                val markOuter = longToOffset(lValue, 0.87f)
                val angleLabelOffset = longToOffset(lValue, 0.78f)

                drawLine(
                    color = Color(0xFFE8D0FF),
                    start = markInner,
                    end = markOuter,
                    strokeWidth = 2.dp.toPx()
                )

                val labelLayout = textMeasurer.measure(
                    text = label,
                    style = TextStyle(fontSize = 11.sp, color = Color(0xFFE8D0FF))
                )
                drawText(
                    textLayoutResult = labelLayout,
                    topLeft = Offset(
                        angleLabelOffset.x - labelLayout.size.width / 2f,
                        angleLabelOffset.y - labelLayout.size.height / 2f
                    )
                )
            }
        }

        // Layout standard positions on rings (Natal: 0.48f, Transit: 0.72f)
        val filteredNatal = state.natalPositions.filter { it.body.sweId >= 0 || it.body == ChartBody.SOUTH_NODE }
        val filteredTransit = state.transitPositions.filter { it.body.sweId >= 0 || it.body == ChartBody.SOUTH_NODE }

        val natalPlacements = fanningLayout(filteredNatal)
        val transitPlacements = fanningLayout(filteredTransit)

        // Draw Natal glyphs
        for (p in natalPlacements) {
            val glyphOffset = longToOffset(p.finalLongitude, 0.48f + p.radialOffset * 0.05f)

            // Draw tiny line pointing from adjusted position back to exact ecliptic long coordinate
            if (abs(p.finalLongitude - p.eclipticLongitude) > 0.1) {
                val exactOffset = longToOffset(p.eclipticLongitude, 0.48f)
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = exactOffset,
                    end = glyphOffset,
                    strokeWidth = 1.dp.toPx()
                )
            }

            drawGlyph(center, glyphOffset, p.body.glyph, Color(0xFFFFB300), textMeasurer)
        }

        // Draw Transit glyphs
        for (p in transitPlacements) {
            val glyphOffset = longToOffset(p.finalLongitude, 0.72f + p.radialOffset * 0.05f)

            if (abs(p.finalLongitude - p.eclipticLongitude) > 0.1) {
                val exactOffset = longToOffset(p.eclipticLongitude, 0.72f)
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = exactOffset,
                    end = glyphOffset,
                    strokeWidth = 1.dp.toPx()
                )
            }

            drawGlyph(center, glyphOffset, p.body.glyph, Color(0xFF4DD0E1), textMeasurer)
        }
    }
}

private fun DrawScope.drawStarfield(center: Offset, outerRadius: Float) {
    val stars = listOf(
        Offset(-0.2f, -0.6f), Offset(0.4f, -0.8f), Offset(-0.7f, 0.3f), Offset(0.6f, 0.2f),
        Offset(-0.4f, 0.7f), Offset(0.3f, 0.5f), Offset(-0.8f, -0.4f), Offset(0.8f, -0.6f),
        Offset(0.1f, -0.3f), Offset(-0.3f, -0.1f), Offset(0.5f, 0.7f), Offset(-0.5f, -0.8f)
    )

    for (star in stars) {
        val o = Offset(center.x + star.x * outerRadius, center.y + star.y * outerRadius)
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = 2f,
            center = o
        )
    }
}

private fun DrawScope.drawGlyph(
    center: Offset,
    offset: Offset,
    text: String,
    color: Color,
    textMeasurer: TextMeasurer
) {
    // Draw small background blur backing for the glyph
    drawCircle(
        color = Color(0xFF110E1D),
        radius = 12.dp.toPx(),
        center = offset
    )

    val layoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(fontSize = 15.sp, color = color)
    )
    val textWidth = layoutResult.size.width
    val textHeight = layoutResult.size.height

    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(offset.x - textWidth / 2f, offset.y - textHeight / 2f)
    )
}

private fun DrawScope.drawZodiacRing(
    center: Offset,
    outerRadius: Float,
    innerFraction: Float,
    outerFraction: Float,
    rotationLong: Double,
    textMeasurer: TextMeasurer
) {
    val innerR = outerRadius * innerFraction
    val outerR = outerRadius * outerFraction

    // Draw outer enclosing boundary circle
    drawCircle(
        color = Color.White.copy(alpha = 0.15f),
        radius = outerR,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.15f),
        radius = innerR,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    val signs = ZodiacSign.entries
    for (i in 0..11) {
        val sign = signs[i]
        val startLongitude = i * 30.0

        // Draw radial dividing line for sign boundaries
        val screenAngleDegrees = (180.0 - (startLongitude - rotationLong + 360.0) % 360.0 + 360.0) % 360.0
        val angleRad = Math.toRadians(screenAngleDegrees)

        val innerPt = Offset(
            (center.x + innerR * cos(angleRad)).toFloat(),
            (center.y + innerR * sin(angleRad)).toFloat()
        )
        val outerPt = Offset(
            (center.x + outerR * cos(angleRad)).toFloat(),
            (center.y + outerR * sin(angleRad)).toFloat()
        )

        drawLine(
            color = Color.White.copy(alpha = 0.10f),
            start = innerPt,
            end = outerPt,
            strokeWidth = 1.dp.toPx()
        )

        // Draw glyph in the middle of sign arc (startLong + 15 degrees)
        val midLong = startLongitude + 15.0
        val midScreenAngle = (180.0 - (midLong - rotationLong + 360.0) % 360.0 + 360.0) % 360.0
        val midAngleRad = Math.toRadians(midScreenAngle)

        val textRadius = (innerR + outerR) / 2f
        val glyphOffset = Offset(
            (center.x + textRadius * cos(midAngleRad)).toFloat(),
            (center.y + textRadius * sin(midAngleRad)).toFloat()
        )

        val textLayout = textMeasurer.measure(
            text = sign.glyph,
            style = TextStyle(fontSize = 13.sp, color = sign.color.copy(alpha = 0.85f))
        )
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                glyphOffset.x - textLayout.size.width / 2f,
                glyphOffset.y - textLayout.size.height / 2f
            )
        )
    }
}

/**
 * High quality layout algorithm for dense 24-body glyph fanning placement.
 * Loops through sorted degrees, identifies clusters, and extends/pushes them outwards.
 */
private fun fanningLayout(positions: List<BodyPosition>): List<GlyphPlacement24> {
    if (positions.isEmpty()) return emptyList()

    val sorted = positions.sortedBy { it.eclipticLongitude }
    val placements = sorted.map { GlyphPlacement24(it, it.eclipticLongitude, 0f) }.toMutableList()

    // Run custom relaxation loop to force apart close points
    var converged = false
    var iterations = 0
    while (!converged && iterations < 15) {
        converged = true
        iterations++

        for (i in placements.indices) {
            val current = placements[i]
            val nextIndex = (i + 1) % placements.size
            val next = placements[nextIndex]

            val currentLong = current.finalLongitude
            // Next longitude might wrap across 0/360 degrees boundary
            val rawDiff = next.finalLongitude - current.finalLongitude
            val diff = if (rawDiff < 0) rawDiff + 360.0 else rawDiff

            if (diff < MIN_ANGULAR_SEP) {
                // Too close! Push them apart
                val pushAmount = (MIN_ANGULAR_SEP - diff) / 2.0
                
                placements[i] = current.copy(
                    finalLongitude = (current.finalLongitude - pushAmount + 360.0) % 360.0,
                    radialOffset = (current.radialOffset + 0.12f).coerceAtMost(1.0f)
                )
                placements[nextIndex] = next.copy(
                    finalLongitude = (next.finalLongitude + pushAmount) % 360.0,
                    radialOffset = (next.radialOffset + 0.12f).coerceAtMost(1.0f)
                )
                converged = false
            } else {
                // Slowly relax radial offsets if safe
                if (current.radialOffset > 0f) {
                    placements[i] = current.copy(radialOffset = (current.radialOffset - 0.02f).coerceAtLeast(0f))
                }
            }
        }
    }

    return placements
}
