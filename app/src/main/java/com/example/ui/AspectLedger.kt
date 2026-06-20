package com.example.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import com.example.model.Aspect
import com.example.model.AspectType

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

@Composable
fun AspectLedger(
    aspects: List<Aspect>,
    showMinorAspects: Boolean,
    birthData: com.example.data.BirthData?,
    onToggleMinorAspects: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var tappedAspect by remember { mutableStateOf<Aspect?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .background(Color(0xFF110E1D), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        tappedAspect?.let { aspect ->
            AspectInfoPopup(aspect = aspect, birthData = birthData, onDismiss = { tappedAspect = null })
        }
        
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
                    AspectLedgerRow(
                        aspect = aspect,
                        birthData = birthData,
                        onClick = { tappedAspect = aspect }
                    )
                }
            }
        }
    }
}

@Composable
fun AspectLedgerRow(
    aspect: Aspect,
    birthData: com.example.data.BirthData?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Formula column: [Transit Planet Glyph]  L  [Aspect GLYPH]  L  [Natal Planet Glyph]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1.3f)
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
            var rarityStr by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(aspect, birthData) {
                if (birthData != null) {
                    val occs = com.example.engine.calculateLifetimeAspects(
                        aspect.natalPosition,
                        aspect.transitPosition.body,
                        aspect.type,
                        birthData.birthInstant
                    )
                    val count = occs.size
                    rarityStr = when {
                        count < 3 -> "Very Rare (Once or twice a lifetime)"
                        count < 10 -> "Rare (A few times a lifetime)"
                        count < 30 -> "Uncommon (Every few years)"
                        count < 100 -> "Common (Yearly)"
                        else -> "Very Common (Monthly or more)"
                    }
                }
            }

            Column {
                Text(
                    text = if (rarityStr != null) "${aspect.type.title} - $rarityStr" else aspect.type.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "${aspect.transitPosition.body.displayName} - ${aspect.natalPosition.body.displayName}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // Orb progress and values
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(0.7f)
        ) {
            val totalMinutes = Math.round(aspect.orb * 60.0).toInt()
            val orbDeg = totalMinutes / 60
            val orbMin = totalMinutes % 60
            val exactSuffix = if (totalMinutes <= 2) " exact" else ""
            Text(
                text = "${orbDeg}°${orbMin.toString().padStart(2, '0')}$exactSuffix",
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
