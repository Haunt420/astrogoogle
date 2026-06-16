package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BodyPosition
import com.example.model.ChartRing
import com.example.model.BodyCategory

@Composable
fun PositionTable(
    natalPositions: List<BodyPosition>,
    transitPositions: List<BodyPosition>,
    modifier: Modifier = Modifier
) {
    var selectedRingTab by remember { mutableStateOf(ChartRing.TRANSIT) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .background(Color(0xFF110E1D), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        // Tab Headers
        TabRow(
            selectedTabIndex = selectedRingTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedRingTab.ordinal]),
                    color = if (selectedRingTab == ChartRing.TRANSIT) Color(0xFF4DD0E1) else Color(0xFFFFB300)
                )
            },
            divider = {},
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Tab(
                selected = selectedRingTab == ChartRing.TRANSIT,
                onClick = { selectedRingTab = ChartRing.TRANSIT },
                text = {
                    Text(
                        "TRANSIT POSITIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (selectedRingTab == ChartRing.TRANSIT) Color(0xFF4DD0E1) else Color.White.copy(alpha = 0.5f)
                    )
                }
            )
            Tab(
                selected = selectedRingTab == ChartRing.NATAL,
                onClick = { selectedRingTab = ChartRing.NATAL },
                text = {
                    Text(
                        "NATAL POSITIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (selectedRingTab == ChartRing.NATAL) Color(0xFFFFB300) else Color.White.copy(alpha = 0.5f)
                    )
                }
            )
        }

        // Selected table body
        val positions = if (selectedRingTab == ChartRing.TRANSIT) transitPositions else natalPositions

        if (positions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No planetary positions calculated.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        } else {
            // Table Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Body".uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.width(130.dp)
                )
                Text(
                    "Tropical Longitude".uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Speed".uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.width(60.dp)
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                positions.forEach { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Body Name & Glyph
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(130.dp)
                        ) {
                            Text(
                                text = p.body.glyph,
                                fontSize = 16.sp,
                                color = if (selectedRingTab == ChartRing.TRANSIT) Color(0xFF4DD0E1) else Color(0xFFFFB300),
                                modifier = Modifier.width(28.dp)
                            )
                            Text(
                                text = p.body.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        // Coordinates
                        Text(
                            text = p.formattedDegree,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.weight(1f)
                        )

                        // Speed + Retrograde icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.width(60.dp)
                        ) {
                            if (p.body.category == BodyCategory.ANGLE) {
                                Text(
                                    text = "—",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.3f)
                                )
                            } else {
                                if (p.isRetrograde) {
                                    Text(
                                        "℞",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF5350),
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                                Text(
                                    text = String.format("%.2f", p.speedDegreesPerDay),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (p.speedDegreesPerDay < 0) Color(0xFFEF5350) else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
