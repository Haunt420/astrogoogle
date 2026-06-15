package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import com.example.model.Aspect
import com.example.model.AspectType

@Composable
fun AspectLedger(
    aspects: List<Aspect>,
    showMinorAspects: Boolean,
    onToggleMinorAspects: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Colors of aspects
    val conjunctionColor = Color(0xFFFFB300)
    val oppositionColor = Color(0xFFE57373)
    val trineColor = Color(0xFF81C784)
    val squareColor = Color(0xFF64B5F6)
    val sextileColor = Color(0xFFBA68C8)
    val minorColor = Color(0xFFB0BEC5)

    fun getAspectColor(type: AspectType): Color = when (type) {
        AspectType.CONJUNCTION -> conjunctionColor
        AspectType.OPPOSITION -> oppositionColor
        AspectType.TRINE -> trineColor
        AspectType.SQUARE -> squareColor
        AspectType.SEXTILE -> sextileColor
        else -> minorColor
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .background(Color(0xFF110E1D), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transit-Natal Aspects".uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f)
            )

            // Major / Minor aspect filter toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Minor Aspects",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Switch(
                    checked = showMinorAspects,
                    onCheckedChange = onToggleMinorAspects,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4DD0E1),
                        checkedTrackColor = Color(0x334DD0E1),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.scale(0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (aspects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active aspects in orb.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aspects.take(30).forEach { aspect ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Formula column: [Transit Planet Glyph]  L  [Aspect GLYPH]  L  [Natal Planet Glyph]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.width(170.dp)
                        ) {
                            // Transit Body (Green-Blue)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = aspect.transitPosition.body.glyph,
                                    fontSize = 17.sp,
                                    color = Color(0xFF4DD0E1)
                                )
                                Text(
                                    text = "trans",
                                    fontSize = 8.sp,
                                    color = Color(0xFF4DD0E1).copy(alpha = 0.5f)
                                )
                            }

                            // Aspect Operator
                            Text(
                                text = aspect.type.glyph,
                                fontSize = 18.sp,
                                color = getAspectColor(aspect.type),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )

                            // Natal Body (Gold)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = aspect.natalPosition.body.glyph,
                                    fontSize = 17.sp,
                                    color = Color(0xFFFFB300)
                                )
                                Text(
                                    text = "natal",
                                    fontSize = 8.sp,
                                    color = Color(0xFFFFB300).copy(alpha = 0.5f)
                                )
                            }

                            // Text details
                            Column {
                                Text(
                                    text = aspect.type.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "${aspect.transitPosition.body.shortCode} - ${aspect.natalPosition.body.shortCode}",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                        }

                        // Orb progress and values
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            val orbDeg = aspect.orb.toInt()
                            val orbMin = ((aspect.orb - orbDeg) * 60.0).toInt()
                            Text(
                                text = "${orbDeg}°${orbMin.toString().padStart(2, '0')}' exact",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Thin status bar of aspect exactness strength
                            LinearProgressIndicator(
                                progress = { aspect.strength },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(3.dp),
                                color = getAspectColor(aspect.type),
                                trackColor = Color.White.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
    }
}
