package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DateRange
import com.example.viewmodel.ScrubGranularity
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeScrubber(
    currentInstant: Instant,
    selectedGranularity: ScrubGranularity,
    isPlaying: Boolean,
    onInstantChanged: (Instant) -> Unit,
    onGranularitySelected: (ScrubGranularity) -> Unit,
    onNudge: (Long) -> Unit,
    onTogglePlayback: () -> Unit,
    onResetToNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy  HH:mm 'UTC'")
        .withZone(ZoneOffset.UTC)

    val formattedStr = formatter.format(currentInstant)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141224)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Text representation of date & time with Calendar Trigger
            var showDatePicker by remember { mutableStateOf(false) }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = currentInstant.toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
                                val currentTime = currentInstant.atOffset(ZoneOffset.UTC).toLocalTime()
                                val updatedInstant = selectedDate.atTime(currentTime).toInstant(ZoneOffset.UTC)
                                onInstantChanged(updatedInstant)
                            }
                            showDatePicker = false
                        }) {
                            Text("OK", color = Color(0xFFFFB300))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                        }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = Color(0xFF1C1A32)
                    )
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = formattedStr,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4DD0E1),
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Slider & Play controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause button
                IconButton(
                    onClick = onTogglePlayback,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isPlaying) Color(0x334DD0E1) else Color(0x1AFFFFFF)
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Playback Speed Controls",
                        tint = if (isPlaying) Color(0xFF4DD0E1) else Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Left nudge
                IconButton(
                    onClick = { onNudge(-selectedGranularity.stepMinutes) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x0AFFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Step Back",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Main Timeline Slider
                Slider(
                    value = DateRange.toFraction(currentInstant),
                    onValueChange = { fraction ->
                        onInstantChanged(DateRange.fromFraction(fraction))
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4DD0E1),
                        activeTrackColor = Color(0xFF4DD0E1),
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )

                // Right nudge
                IconButton(
                    onClick = { onNudge(selectedGranularity.stepMinutes) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x0AFFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Step Forward",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Year markers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .offset(y = (-6).dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1900",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.35f)
                )
                Text(
                    text = "2050",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.35f)
                )
            }

            // Granularity selection Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ScrubGranularity.entries.forEach { g ->
                        val selected = g == selectedGranularity
                        SuggestionChip(
                            onClick = { onGranularitySelected(g) },
                            label = {
                                Text(
                                    text = g.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) Color(0xFF4DD0E1) else Color(0x0AFFFFFF),
                                labelColor = if (selected) Color(0xFF0C091A) else Color.White.copy(alpha = 0.85f)
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Now button
                ElevatedButton(
                    onClick = onResetToNow,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0x22FFFFB3),
                        contentColor = Color(0xFFFFB300)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Now",
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "NOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
