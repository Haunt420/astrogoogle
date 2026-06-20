package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HouseSystem
import com.example.viewmodel.TransitUiState
import com.example.viewmodel.TransitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitScreen(
    state: TransitUiState,
    viewModel: TransitViewModel,
    onNavigateToBirthData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickSettings by remember { mutableStateOf(false) }

    val celestialBackground = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF070414),
                Color(0xFF0F0A27),
                Color(0xFF020108)
            )
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ASTRO TRANSIT WHEEL",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (state.birthData.isSet) {
                                "Natal: ${state.birthData.name} (${state.birthData.locationName})"
                            } else {
                                "Tap 'Birth Data' to set natal positions"
                            },
                            fontSize = 10.sp,
                            color = if (state.birthData.isSet) Color(0xFFFFB300) else Color.White.copy(alpha = 0.4f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showQuickSettings = !showQuickSettings }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Preferences menu",
                            tint = if (showQuickSettings) Color(0xFF4DD0E1) else Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToBirthData) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Set birth data",
                            tint = Color(0xFFFFB300)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF070414),
                    titleContentColor = Color.White
                )
            )
        },
        modifier = modifier.background(celestialBackground)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .background(celestialBackground)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick preferences drawer toggle
            AnimatedVisibility(
                visible = showQuickSettings,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131124))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PREFERENCES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4DD0E1)
                        )

                        // Orb limits slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Major Aspect Orb Threshold",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    String.format("%.1f°", state.orbTolerance),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4DD0E1)
                                )
                            }
                            Slider(
                                value = state.orbTolerance.toFloat(),
                                onValueChange = { viewModel.setOrbTolerance(it.toDouble()) },
                                valueRange = 1f..10f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF4DD0E1),
                                    activeTrackColor = Color(0xFF4DD0E1)
                                )
                            )
                        }

                        // System of Houses selector (if birth location set)
                        if (state.birthData.isSet && state.birthData.hasLocation) {
                            Column {
                                Text(
                                    "Selected House System",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    HouseSystem.entries.forEach { h ->
                                        val active = h == state.birthData.houseSystem
                                        SuggestionChip(
                                            onClick = { viewModel.updateBirthHouseSystem(h) },
                                            label = {
                                                Text(
                                                    h.displayName,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (active) Color(0xFFFFB300) else Color(0x15FFFFFF),
                                                labelColor = if (active) Color(0xFF0F0C1B) else Color.White
                                            ),
                                            border = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Notice panel if birth data is NOT set
            if (!state.birthData.isSet) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBirthData() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x11FFB300))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Birth data required",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Natal houses & ASC/MC are locked",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB300)
                            )
                            Text(
                                text = "Tap here to save your birth time and coordinates for deep analytical bi-wheel aspects.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Navigate",
                            tint = Color(0xFFFFB300).copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Interactive Double Wheel Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                TransitChartCanvas(state = state.chartState, birthData = state.birthData, modifier = Modifier.fillMaxSize()
                )

                if (state.isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center),
                        color = Color(0xFF4DD0E1),
                        strokeWidth = 3.dp
                    )
                }
            }

            // Display metadata detail badge
            if (state.birthData.isSet && state.chartState.natalHouses.polarFallbackUsed) {
                Text(
                    text = "⚠️ Placidus is invalid at polar latitudes. Defaulted to Equal system.",
                    fontSize = 11.sp,
                    color = Color(0xFFFFB74D),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Scrubber timeline control
            TimeScrubber(
                currentInstant = state.transitInstant,
                selectedGranularity = state.selectedGranularity,
                isPlaying = state.isPlaying,
                onInstantChanged = { viewModel.setTransitInstant(it) },
                onGranularitySelected = { viewModel.setGranularity(it) },
                onNudge = { viewModel.nudgeTransit(it) },
                onTogglePlayback = { viewModel.togglePlayback() },
                onResetToNow = { viewModel.resetToNow() }
            )

            // Aspect matches ledger
            AspectLedger(
                aspects = state.chartState.aspects,
                showMinorAspects = state.showMinorAspects,
                birthData = state.birthData,
                onToggleMinorAspects = { viewModel.setShowMinorAspects(it) }
            )

            // Positions table
            PositionTable(
                natalPositions = state.chartState.natalPositions,
                transitPositions = state.chartState.transitPositions
            )

            // Small source indicator
            Text(
                text = "${state.chartState.source} Calculations",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.2f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
