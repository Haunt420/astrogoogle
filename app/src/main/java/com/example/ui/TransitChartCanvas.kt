package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.border
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.model.*
import kotlin.math.*

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.example.R

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
    
    val glyphMap = mapOf(
        ChartBody.SUN to ImageBitmap.imageResource(id = R.drawable.ic_sun),
        ChartBody.MOON to ImageBitmap.imageResource(id = R.drawable.ic_moon),
        ChartBody.MERCURY to ImageBitmap.imageResource(id = R.drawable.ic_mercury),
        ChartBody.VENUS to ImageBitmap.imageResource(id = R.drawable.ic_venus),
        ChartBody.MARS to ImageBitmap.imageResource(id = R.drawable.ic_mars),
        ChartBody.JUPITER to ImageBitmap.imageResource(id = R.drawable.ic_jupiter),
        ChartBody.SATURN to ImageBitmap.imageResource(id = R.drawable.ic_saturn),
        ChartBody.URANUS to ImageBitmap.imageResource(id = R.drawable.ic_uranus),
        ChartBody.NEPTUNE to ImageBitmap.imageResource(id = R.drawable.ic_neptune),
        ChartBody.PLUTO to ImageBitmap.imageResource(id = R.drawable.ic_pluto),
        ChartBody.NORTH_NODE to ImageBitmap.imageResource(id = R.drawable.ic_node),
        ChartBody.SOUTH_NODE to ImageBitmap.imageResource(id = R.drawable.ic_node),
        ChartBody.CHIRON to ImageBitmap.imageResource(id = R.drawable.ic_chiron),
        ChartBody.LILITH to ImageBitmap.imageResource(id = R.drawable.ic_lilith_bm),
        ChartBody.CERES to ImageBitmap.imageResource(id = R.drawable.ic_ceres),
        ChartBody.PALLAS to ImageBitmap.imageResource(id = R.drawable.ic_pallas_athena),
        ChartBody.JUNO to ImageBitmap.imageResource(id = R.drawable.ic_juno),
        ChartBody.VESTA to ImageBitmap.imageResource(id = R.drawable.ic_vesta),
        ChartBody.PHOLUS to ImageBitmap.imageResource(id = R.drawable.ic_pholus)
    )

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var tappedAspect by remember { mutableStateOf<Aspect?>(null) }

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

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val oldScale = scale
                        val newScale = (scale * zoom).coerceIn(1.0f, 6.0f)
                        
                        scale = newScale
                        if (newScale == 1.0f) {
                            offset = Offset.Zero
                        } else {
                            val scaleFactor = newScale / oldScale
                            offset = (offset + pan) * scaleFactor + centroid * (1f - scaleFactor)
                            
                            // Prevent dragging off boundaries
                            val maxPanX = (size.width * (newScale - 1f)) / 2f
                            val maxPanY = (size.height * (newScale - 1f)) / 2f
                            offset = Offset(
                                x = offset.x.coerceIn(-maxPanX * 1.5f, maxPanX * 1.5f),
                                y = offset.y.coerceIn(-maxPanY * 1.5f, maxPanY * 1.5f)
                            )
                        }
                    }
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Scroll) {
                                val scrollAmount = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                                if (scrollAmount != 0f) {
                                    val oldScale = scale
                                    val zoomFactor = if (scrollAmount > 0) 0.9f else 1.1f
                                    val newScale = (scale * zoomFactor).coerceIn(1.0f, 6.0f)
                                    
                                    scale = newScale
                                    if (newScale == 1.0f) {
                                        offset = Offset.Zero
                                    } else {
                                        val scaleFactor = newScale / oldScale
                                        offset = offset * scaleFactor
                                    }
                                }
                            }
                        }
                    }
                }
                .pointerInput(state.aspects, scale, offset) {
                    detectTapGestures(
                        onPress = { _: Offset -> tappedAspect = null },
                        onTap = { tapScreenOffset: Offset ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()
                            val center = Offset(width / 2f, height / 2f)
                            
                            val logicalTapX = (tapScreenOffset.x - center.x - offset.x) / scale + center.x
                            val logicalTapY = (tapScreenOffset.y - center.y - offset.y) / scale + center.y
                            
                            val outerRadius = (min(width, height) / 2f) * 0.95f
                            
                            val rotationLong = if (state.natalHouses.isValid) state.natalHouses.ascendant else 0.0
                            
                            fun localLongToOffset(long: Double, radiusFraction: Float): Offset {
                                val adjusted = (long - rotationLong + 360.0) % 360.0
                                val angleRad = Math.toRadians((180.0 - adjusted + 360.0) % 360.0)
                                val r = outerRadius * radiusFraction
                                return Offset(
                                    (center.x + r * cos(angleRad)).toFloat(),
                                    (center.y + r * sin(angleRad)).toFloat()
                                )
                            }
                            
                            var closest: Aspect? = null
                            var minDistance = Float.MAX_VALUE
                            for (aspect in state.aspects) {
                                val start = localLongToOffset(aspect.natalPosition.eclipticLongitude, 0.40f)
                                val end = localLongToOffset(aspect.transitPosition.eclipticLongitude, 0.40f)
                                
                                val dx = end.x - start.x
                                val dy = end.y - start.y
                                val l2 = dx * dx + dy * dy
                                val t = if (l2 == 0f) 0f else {
                                    ((logicalTapX - start.x) * dx + (logicalTapY - start.y) * dy) / l2
                                }.coerceIn(0f, 1f)
                                
                                val projX = start.x + t * dx
                                val projY = start.y + t * dy
                                val distDx = logicalTapX - projX
                                val distDy = logicalTapY - projY
                                val dist = sqrt((distDx * distDx + distDy * distDy).toDouble()).toFloat()
                                if (dist < minDistance) {
                                    minDistance = dist
                                    closest = aspect
                                }
                            }
                            
                            // Hitbox: 30f px, scaled
                            if (minDistance < (30f / scale) && closest != null) {
                                tappedAspect = closest
                            } else {
                                tappedAspect = null
                            }
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height / 2f)
            val outerRadius = (min(width, height) / 2f) * 0.95f

            if (outerRadius <= 0) return@Canvas

            // Apply zoom transform scale & pan translation to drawContext
            drawContext.transform.translate(offset.x, offset.y)
            drawContext.transform.scale(scale, scale, pivot = center)

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

        // Draw Zodiac signs segments - Ring 1: outer fraction 1.00f, inner fraction 0.80f
        val zodiacRingInner = 0.80f
        val zodiacRingOuter = 1.00f
        drawZodiacRing(center, outerRadius, zodiacRingInner, zodiacRingOuter, rotationLong, textMeasurer)

        // Setup house division cusps (use Equal House fallback if not set to ensure there is ALWAYS a gorgeous structural representation)
        val cusps = if (state.natalHouses.isValid) {
            state.natalHouses.cusps
        } else {
            DoubleArray(13) { idx -> ((idx - 1) * 30.0 % 360.0) }
        }

        val rInnerHouses = outerRadius * 0.40f
        val rOuterHouses = outerRadius * 0.80f

        // Draw filled background sectors of houses with subtle alternating shades
        for (i in 1..12) {
            val cuspLong = cusps[i]
            val nextCuspLong = cusps[if (i == 12) 1 else i + 1]

            val screenStartAngle = longToScreenAngle(cuspLong)
            val diff = (nextCuspLong - cuspLong + 360.0) % 360.0
            val sweepAngle = -diff // sweep counter-clockwise

            val opacity = if (i % 2 == 0) 0.05f else 0.02f
            drawArc(
                color = Color(0xFF4DD0E1).copy(alpha = opacity),
                startAngle = screenStartAngle.toFloat(),
                sweepAngle = sweepAngle.toFloat(),
                useCenter = true,
                topLeft = Offset(center.x - rOuterHouses, center.y - rOuterHouses),
                size = Size(rOuterHouses * 2f, rOuterHouses * 2f)
            )
        }

        // Mask the central hollow void space inside Ring 3 (from radius fraction 0f to 0.40f)
        drawCircle(
            color = Color(0xFF0F0C1B), // Matches chart background
            radius = rInnerHouses,
            center = center,
            style = Fill
        )

        // Draw concentric circular outline boundaries
        drawCircle(
            color = Color.White.copy(alpha = 0.20f),
            radius = outerRadius * 0.80f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.20f),
            radius = outerRadius * 0.60f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.20f),
            radius = outerRadius * 0.40f,
            center = center,
            style = Stroke(width = 1.2f.dp.toPx())
        )

        // Draw house dividers (spokes) extending from 0.40f (inner Ring 3) to 0.80f (outer Ring 2)
        for (i in 1..12) {
            val cuspLong = cusps[i]
            val cuspOffsetInner = longToOffset(cuspLong, 0.40f)
            val cuspOffsetOuter = longToOffset(cuspLong, 0.80f)

            drawLine(
                color = Color.White.copy(alpha = if (i == 1 || i == 10) 0.5f else 0.15f),
                start = cuspOffsetInner,
                end = cuspOffsetOuter,
                strokeWidth = if (i == 1 || i == 10) 2.2f.dp.toPx() else 0.9f.dp.toPx()
            )

            // Draw House labels in middle/low parts of sectors
            val midCuspLong = if (i < 12) {
                val nextCusp = cusps[i + 1]
                val diff = (nextCusp - cuspLong + 360.0) % 360.0
                (cuspLong + diff / 2.0) % 360.0
            } else {
                val nextCusp = cusps[1]
                val diff = (nextCusp - cuspLong + 360.0) % 360.0
                (cuspLong + diff / 2.0) % 360.0
            }

            // Ring 2 label (Transit ring, center radius fraction 0.64f)
            val labelOffsetR2 = longToOffset(midCuspLong, 0.64f)
            val labelLayoutR2 = textMeasurer.measure(
                text = i.toString(),
                style = TextStyle(fontSize = 10.sp, color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Black)
            )
            drawText(
                textLayoutResult = labelLayoutR2,
                topLeft = Offset(
                    labelOffsetR2.x - labelLayoutR2.size.width / 2f,
                    labelOffsetR2.y - labelLayoutR2.size.height / 2f
                )
            )

            // Ring 3 label (Natal ring, center radius fraction 0.44f)
            val labelOffsetR3 = longToOffset(midCuspLong, 0.44f)
            val labelLayoutR3 = textMeasurer.measure(
                text = i.toString(),
                style = TextStyle(fontSize = 10.sp, color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Black)
            )
            drawText(
                textLayoutResult = labelLayoutR3,
                topLeft = Offset(
                    labelOffsetR3.x - labelLayoutR3.size.width / 2f,
                    labelOffsetR3.y - labelLayoutR3.size.height / 2f
                )
            )
        }

        // Draw aspect web lines (terminating exactly at the inner boundary of Ring 3: 0.40f)
        for (aspect in state.aspects) {
            val nLong = aspect.natalPosition.eclipticLongitude
            val tLong = aspect.transitPosition.eclipticLongitude

            val startOffset = longToOffset(nLong, 0.40f)
            val endOffset = longToOffset(tLong, 0.40f)

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

        // Draw central beautiful organic Earth globe in the center empty space
        val globeRadius = outerRadius * 0.18f

        // Deep backing shadow
        drawCircle(
            color = Color(0xFF03010B),
            radius = globeRadius,
            center = center,
            style = Fill
        )

        // Globe base ocean (Gradient from deep indigo to rich blue at edges)
        val globeBrush = Brush.radialGradient(
            colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B4965)),
            center = center,
            radius = globeRadius
        )
        drawCircle(
            brush = globeBrush,
            radius = globeRadius,
            center = center,
            style = Fill
        )

        // Stylized continent organic shapes representing land mass in standard AstroFuture style
        val continentsColor = Color(0xFF708090).copy(alpha = 0.5f) // Silver / Slate grey landmass
        val continent1 = Path().apply {
            moveTo(center.x - globeRadius * 0.5f, center.y - globeRadius * 0.3f)
            quadraticTo(center.x - globeRadius * 0.2f, center.y - globeRadius * 0.6f, center.x + globeRadius * 0.1f, center.y - globeRadius * 0.4f)
            quadraticTo(center.x + globeRadius * 0.4f, center.y - globeRadius * 0.5f, center.x + globeRadius * 0.5f, center.y - globeRadius * 0.1f)
            quadraticTo(center.x + globeRadius * 0.2f, center.y + globeRadius * 0.2f, center.x + globeRadius * 0.3f, center.y + globeRadius * 0.5f)
            quadraticTo(center.x, center.y + globeRadius * 0.6f, center.x - globeRadius * 0.3f, center.y + globeRadius * 0.2f)
            quadraticTo(center.x - globeRadius * 0.6f, center.y + globeRadius * 0.1f, center.x - globeRadius * 0.5f, center.y - globeRadius * 0.3f)
            close()
        }
        drawPath(
            path = continent1,
            color = continentsColor,
            style = Fill
        )

        val continent2 = Path().apply {
            moveTo(center.x - globeRadius * 0.7f, center.y + globeRadius * 0.1f)
            quadraticTo(center.x - globeRadius * 0.4f, center.y + globeRadius * 0.3f, center.x - globeRadius * 0.5f, center.y + globeRadius * 0.6f)
            quadraticTo(center.x - globeRadius * 0.8f, center.y + globeRadius * 0.5f, center.x - globeRadius * 0.7f, center.y + globeRadius * 0.1f)
            close()
        }
        drawPath(
            path = continent2,
            color = continentsColor,
            style = Fill
        )

        // Atmosphere glow ring
        drawCircle(
            color = Color(0xFF4DD0E1).copy(alpha = 0.35f),
            radius = globeRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw sensitive Angular Nodes "AC" and "MC" in Ring 3 if natal houses are valid
        if (state.natalHouses.isValid) {
            val ascLong = state.natalHouses.ascendant
            val mcLong = state.natalHouses.mc

            // AC label in Ring 3 (Inner, near cusp line)
            val acOffset = longToOffset(ascLong, 0.46f)
            val acLayout = textMeasurer.measure(
                text = "AC",
                style = TextStyle(fontSize = 11.sp, color = Color(0xFF4DD0E1), fontWeight = FontWeight.Bold)
            )
            drawText(
                textLayoutResult = acLayout,
                topLeft = Offset(acOffset.x - acLayout.size.width / 2f, acOffset.y + 4.dp.toPx())
            )

            // MC label in Ring 3 (Inner, near cusp line)
            val mcOffset = longToOffset(mcLong, 0.46f)
            val mcLayout = textMeasurer.measure(
                text = "MC",
                style = TextStyle(fontSize = 11.sp, color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
            )
            drawText(
                textLayoutResult = mcLayout,
                topLeft = Offset(mcOffset.x + 4.dp.toPx(), mcOffset.y - mcLayout.size.height / 2f)
            )
        }

        // Layout standard positions on rings (Natal: Ring 3 [0.40f - 0.60f], Transit: Ring 2 [0.60f - 0.80f])
        val filteredNatal = state.natalPositions.filter { it.body.sweId >= 0 || it.body == ChartBody.SOUTH_NODE }
        val filteredTransit = state.transitPositions.filter { it.body.sweId >= 0 || it.body == ChartBody.SOUTH_NODE }

        val natalPlacements = fanningLayout(filteredNatal)
        val transitPlacements = fanningLayout(filteredTransit)

        // Draw Natal glyphs (Ring 3, centered at 0.50f fraction)
        for (p in natalPlacements) {
            val glyphOffset = longToOffset(p.finalLongitude, 0.50f + p.radialOffset * 0.035f)

            // Draw line connecting back to exact coordinate on inner boundary
            if (abs(p.finalLongitude - p.eclipticLongitude) > 0.1) {
                val exactOffset = longToOffset(p.eclipticLongitude, 0.60f)
                drawLine(
                    color = Color(0xFFFFB300).copy(alpha = 0.25f),
                    start = exactOffset,
                    end = glyphOffset,
                    strokeWidth = 0.8f.dp.toPx()
                )
            }

            drawGlyph(center, glyphOffset, p.body, Color(0xFFFFB300), textMeasurer, glyphMap[p.body])
        }

        // Draw Transit glyphs (Ring 2, centered at 0.70f fraction)
        for (p in transitPlacements) {
            val glyphOffset = longToOffset(p.finalLongitude, 0.70f + p.radialOffset * 0.035f)

            if (abs(p.finalLongitude - p.eclipticLongitude) > 0.1) {
                val exactOffset = longToOffset(p.eclipticLongitude, 0.80f)
                drawLine(
                    color = Color(0xFF4DD0E1).copy(alpha = 0.25f),
                    start = exactOffset,
                    end = glyphOffset,
                    strokeWidth = 0.8f.dp.toPx()
                )
            }

            drawGlyph(center, glyphOffset, p.body, Color(0xFF4DD0E1), textMeasurer, glyphMap[p.body])
        }
    }

    if (scale > 1.05f) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    scale = 1f
                    offset = Offset.Zero
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1B33).copy(alpha = 0.9f),
                    contentColor = Color(0xFFFFB300)
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Zoom",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFB300)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Reset Zoom",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    tappedAspect?.let { aspect ->
        AspectInfoPopup(aspect = aspect, onDismiss = { tappedAspect = null })
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AspectInfoPopup(
    aspect: Aspect,
    onDismiss: () -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1B33),
        contentColor = Color.White
    ) {
        val title = "Transit ${aspect.transitPosition.body.displayName} ${aspect.type.name.lowercase().replaceFirstChar { it.uppercase() }} Natal ${aspect.natalPosition.body.displayName}"
        val strengthP = (aspect.strength * 100).toInt()
        val orb = String.format("%.1f°", aspect.orb)

        val nature = when (aspect.type) {
            AspectType.CONJUNCTION -> "A powerful merging of energies. The transit planet is fusing its traits directly onto your natal placement, bringing a major focus and intensity to this area of life."
            AspectType.OPPOSITION -> "A challenging polarization. You may experience push-pull dynamics, conflicts, or external events acting as a mirror to your internal state. Finding balance is key."
            AspectType.SQUARE -> "Friction and dynamic tension. This aspect forces action and creates internal pressure that demands a resolution, leading to rapid growth."
            AspectType.TRINE -> "Harmonious flow and ease. Opportunities arise naturally and talents are readily available. Beware of taking this smooth energy for granted."
            AspectType.SEXTILE -> "A stimulating intellectual or social connection. It brings opportunities that require a bit of conscious effort to fully actualize."
            else -> "A subtle minor aspect, adding flavor and nuance to the background of your current transits."
        }
        
        val specific = getSpecificAspectText(aspect.transitPosition.body, aspect.natalPosition.body)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(title, color = Color(0xFFFFB300), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Orb: $orb  (Strength: $strengthP%)", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Text(nature, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
            Spacer(Modifier.height(12.dp))
            Text(specific, color = Color(0xFF81C784), fontSize = 15.sp, lineHeight = 22.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(Modifier.height(32.dp))
        }
    }
}

fun getSpecificAspectText(tBody: ChartBody, nBody: ChartBody): String {
    return "When Transiting ${tBody.displayName} interacts with Natal ${nBody.displayName}, ${tBody.displayName}'s themes of " +
        getBodyTheme(tBody) + " strongly flavor how you experience your foundational " + getBodyTheme(nBody) + ". " +
        "Pay attention to events or internal shifts that highlight these intertwined themes in your daily life right now."
}

fun getBodyTheme(body: ChartBody): String = when (body) {
    ChartBody.SUN -> "core identity, vitality, and ego expression"
    ChartBody.MOON -> "emotional needs, intuition, and inner world"
    ChartBody.MERCURY -> "communication, perception, and reasoning"
    ChartBody.VENUS -> "love, aesthetics, and relational harmony"
    ChartBody.MARS -> "drive, assertion, and primal energy"
    ChartBody.JUPITER -> "expansion, luck, and philosophical growth"
    ChartBody.SATURN -> "discipline, restriction, and maturity"
    ChartBody.URANUS -> "rebellion, sudden insights, and awakening"
    ChartBody.NEPTUNE -> "dreams, illusions, and spiritual dissolution"
    ChartBody.PLUTO -> "power, transformation, and rebirth"
    ChartBody.NORTH_NODE -> "karmic destiny and future growth"
    ChartBody.SOUTH_NODE -> "past karma and ingrained habits"
    ChartBody.CHIRON -> "deep wounds and holistic healing"
    ChartBody.LILITH -> "repressed desires and raw authenticity"
    ChartBody.CERES -> "nurturing and unconditional care"
    ChartBody.PALLAS -> "wisdom, strategy, and justice"
    ChartBody.JUNO -> "commitment, loyalty, and partnerships"
    ChartBody.VESTA -> "devotion, sacred focus, and independence"
    ChartBody.PHOLUS -> "catalytic thresholds and sudden shifts"
    else -> "mysterious cosmic forces"
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
    body: ChartBody,
    rimColor: Color,
    textMeasurer: TextMeasurer,
    glyphImage: androidx.compose.ui.graphics.ImageBitmap?
) {
    val radiusPx = 14.dp.toPx()

    // Draw the glow backing circle
    drawCircle(
        color = Color(0xFF0C0A14),
        radius = radiusPx,
        center = offset
    )

    // Draw a subtle border around the icon to make it stand out beautifully like a physical medal / token
    drawCircle(
        color = rimColor.copy(alpha = 0.45f),
        radius = radiusPx,
        center = offset,
        style = Stroke(width = 1.dp.toPx())
    )

    if (glyphImage != null) {
        val dimen = (radiusPx * 1.5f).toInt()
        val dstOffset = androidx.compose.ui.unit.IntOffset(
             (offset.x - dimen / 2f).toInt(),
             (offset.y - dimen / 2f).toInt()
        )
        drawImage(
             image = glyphImage,
             dstOffset = dstOffset,
             dstSize = androidx.compose.ui.unit.IntSize(dimen, dimen)
        )
    } else {
        val textLayout = textMeasurer.measure(
            text = body.glyph,
            style = TextStyle(fontSize = 12.sp, color = rimColor, fontWeight = FontWeight.Bold)
        )
        val w = textLayout.size.width
        val h = textLayout.size.height
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(offset.x - w / 2f, offset.y - h / 2f)
        )
    }
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

    val signs = ZodiacSign.entries

    // 1. Draw filled background slices for all zodiac signs (Element colors)
    for (i in 0..11) {
        val sign = signs[i]
        val startLongitude = i * 30.0
        val screenAngleDegrees = (180.0 - (startLongitude - rotationLong + 360.0) % 360.0 + 360.0) % 360.0

        // Sector wedge
        drawArc(
            color = sign.color.copy(alpha = 0.08f),
            startAngle = screenAngleDegrees.toFloat(),
            sweepAngle = -30f,
            useCenter = true,
            topLeft = Offset(center.x - outerR, center.y - outerR),
            size = Size(outerR * 2f, outerR * 2f)
        )
    }

    // Immediately draw a filled circle over the center to mask everything inside innerR
    drawCircle(
        color = Color(0xFF0F0C1B), // Matches chart background
        radius = innerR,
        center = center,
        style = Fill
    )

    // Draw outer enclosing boundary circle
    drawCircle(
        color = Color.White.copy(alpha = 0.20f),
        radius = outerR,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.20f),
        radius = innerR,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

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
            color = Color.White.copy(alpha = 0.25f),
            start = innerPt,
            end = outerPt,
            strokeWidth = 1.5f.dp.toPx()
        )

        // Draw Decan Dividing lines (at 10 degrees and 20 degrees within each sign)
        for (j in 1..2) {
            val decanLong = startLongitude + j * 10.0
            val decanAngle = (180.0 - (decanLong - rotationLong + 360.0) % 360.0 + 360.0) % 360.0
            val decanRad = Math.toRadians(decanAngle)

            val innerDecanPt = Offset(
                (center.x + innerR * cos(decanRad)).toFloat(),
                (center.y + innerR * sin(decanRad)).toFloat()
            )
            val outerDecanPt = Offset(
                (center.x + (innerR + (outerR - innerR) * 0.35f) * cos(decanRad)).toFloat(), // Ticks extending 35% across the zodiac band
                (center.y + (innerR + (outerR - innerR) * 0.35f) * sin(decanRad)).toFloat()
            )

            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = innerDecanPt,
                end = outerDecanPt,
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw fine 1-degree tick marks on the inner edge of the zodiac ring
        for (deg in 1 until 30) {
            if (deg % 10 == 0) continue // Skip decans already covered
            val tickLong = startLongitude + deg
            val tickAngle = (180.0 - (tickLong - rotationLong + 360.0) % 360.0 + 360.0) % 360.0
            val tickRad = Math.toRadians(tickAngle)
            val tickLengthFraction = if (deg % 5 == 0) 0.18f else 0.10f
            val tickLength = (outerR - innerR) * tickLengthFraction

            val tickInnerPt = Offset(
                (center.x + innerR * cos(tickRad)).toFloat(),
                (center.y + innerR * sin(tickRad)).toFloat()
            )
            val tickOuterPt = Offset(
                (center.x + (innerR + tickLength) * cos(tickRad)).toFloat(),
                (center.y + (innerR + tickLength) * sin(tickRad)).toFloat()
            )

            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = tickInnerPt,
                end = tickOuterPt,
                strokeWidth = 0.5f.dp.toPx()
            )
        }

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
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = sign.color
            )
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

    var converged = false
    var iterations = 0
    val minSep = 9.0 // 9 degrees separation ensures zero text or bubble overlapping
    while (!converged && iterations < 35) {
        converged = true
        iterations++

        for (i in placements.indices) {
            val current = placements[i]
            val nextIndex = (i + 1) % placements.size
            val next = placements[nextIndex]

            val rawDiff = next.finalLongitude - current.finalLongitude
            val diff = if (rawDiff < 0) rawDiff + 360.0 else rawDiff

            if (diff < minSep) {
                val pushAmount = (minSep - diff) / 2.0
                
                // Let's alternate custom staggered radial layers so they don't group at same height
                val currOffset = (current.radialOffset + 0.18f).coerceAtMost(1.2f)
                val nextOffset = (next.radialOffset + 0.18f).coerceAtMost(1.2f)

                placements[i] = current.copy(
                    finalLongitude = (current.finalLongitude - pushAmount + 360.0) % 360.0,
                    radialOffset = currOffset
                )
                placements[nextIndex] = next.copy(
                    finalLongitude = (next.finalLongitude + pushAmount) % 360.0,
                    radialOffset = nextOffset
                )
                converged = false
            }
        }
    }

    // Post-pass: ensure close adjacent objects are offset radially to different levels
    // (creating concentric tiered orbits like the iOS app shown in the uploaded picture)
    for (i in 0 until placements.size) {
        val current = placements[i]
        val prev = placements[(i - 1 + placements.size) % placements.size]
        val rawDiff = current.finalLongitude - prev.finalLongitude
        val diff = if (rawDiff < 0) rawDiff + 360.0 else rawDiff
        if (diff < minSep * 1.5) {
            // If they are angularly very close, assign them alternating heights: 0f, 0.4f, 0.8f
            if (abs(current.radialOffset - prev.radialOffset) < 0.12f) {
                placements[i] = current.copy(radialOffset = (prev.radialOffset + 0.40f) % 1.2f)
            }
        }
    }

    return placements
}
