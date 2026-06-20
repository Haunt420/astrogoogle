package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DateRange
import com.example.viewmodel.ScrubGranularity
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

enum class ActiveUnit {
    MONTH, DAY, YEAR, HOUR, MINUTE
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RolodexElement(
    text: String,
    value: Long,
    isActive: Boolean,
    onActivate: () -> Unit,
    onNudge: (Long) -> Unit
) {
    var accumulatedDelta by remember { mutableFloatStateOf(0f) }
    var slideUp by remember { mutableStateOf(true) }
    var lastValue by remember { mutableLongStateOf(value) }

    if (value != lastValue) {
        slideUp = value > lastValue
        lastValue = value
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = { onActivate() },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    accumulatedDelta += dragAmount
                    if (accumulatedDelta < -30f) {
                        onNudge(1)
                        accumulatedDelta = 0f
                    } else if (accumulatedDelta > 30f) {
                        onNudge(-1)
                        accumulatedDelta = 0f
                    }
                },
                onDragEnd = { accumulatedDelta = 0f },
                onDragCancel = { accumulatedDelta = 0f }
            )
        }
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                if (slideUp) {
                    (slideInVertically { height -> height } + fadeIn(tween(200))) togetherWith
                            (slideOutVertically { height -> -height } + fadeOut(tween(200)))
                } else {
                    (slideInVertically { height -> -height } + fadeIn(tween(200))) togetherWith
                            (slideOutVertically { height -> height } + fadeOut(tween(200)))
                }
            },
            label = "rolodex"
        ) { targetText ->
            Text(
                text = targetText,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color.White else Color(0xFF4DD0E1),
                style = TextStyle(
                    shadow = if (isActive) Shadow(color = Color.White, blurRadius = 12f) else null
                ),
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
fun StaticText(text: String) {
    Text(
        text = text,
        fontSize = 22.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF888888),
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

@Composable
fun TimeScrubber(
    currentInstant: Instant,
    selectedGranularity: ScrubGranularity, // Ignored now
    isPlaying: Boolean, // Ignored from VM
    onInstantChanged: (Instant) -> Unit,
    onGranularitySelected: (ScrubGranularity) -> Unit, // Ignored
    onNudge: (Long) -> Unit, // Ignored
    onTogglePlayback: () -> Unit, // Ignored
    onResetToNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeUnit by remember { mutableStateOf(ActiveUnit.DAY) }
    var isPlayingLocal by remember { mutableStateOf(false) }

    val zdt = currentInstant.atZone(ZoneOffset.UTC)
    val currentZdt by rememberUpdatedState(zdt)

    LaunchedEffect(isPlayingLocal, activeUnit) {
        if (isPlayingLocal) {
            while (true) {
                delay(1000)
                val nextZdt = when (activeUnit) {
                    ActiveUnit.MONTH -> currentZdt.plusMonths(1)
                    ActiveUnit.DAY -> currentZdt.plusDays(1)
                    ActiveUnit.YEAR -> currentZdt.plusYears(1)
                    ActiveUnit.HOUR -> currentZdt.plusHours(1)
                    ActiveUnit.MINUTE -> currentZdt.plusMinutes(1)
                }
                val newInst = nextZdt.toInstant().coerceIn(DateRange.MIN_INSTANT, DateRange.MAX_INSTANT)
                onInstantChanged(newInst)
            }
        }
    }

    fun applyNudge(unit: ActiveUnit, amount: Long) {
        isPlayingLocal = false
        val nextZdt = when (unit) {
            ActiveUnit.MONTH -> zdt.plusMonths(amount)
            ActiveUnit.DAY -> zdt.plusDays(amount)
            ActiveUnit.YEAR -> zdt.plusYears(amount)
            ActiveUnit.HOUR -> zdt.plusHours(amount)
            ActiveUnit.MINUTE -> zdt.plusMinutes(amount)
        }
        val newInst = nextZdt.toInstant().coerceIn(DateRange.MIN_INSTANT, DateRange.MAX_INSTANT)
        onInstantChanged(newInst)
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F0C1B), RoundedCornerShape(12.dp)).padding(vertical = 16.dp, horizontal = 8.dp)
            ) {
                val dayOfWeek = zdt.format(java.time.format.DateTimeFormatter.ofPattern("EEE"))
                StaticText("$dayOfWeek,")
                
                RolodexElement(
                    text = zdt.format(java.time.format.DateTimeFormatter.ofPattern("MMM")),
                    value = zdt.year * 12L + zdt.monthValue,
                    isActive = activeUnit == ActiveUnit.MONTH,
                    onActivate = { activeUnit = ActiveUnit.MONTH },
                    onNudge = { applyNudge(ActiveUnit.MONTH, it) }
                )
                
                RolodexElement(
                    text = zdt.format(java.time.format.DateTimeFormatter.ofPattern("dd")),
                    value = zdt.toEpochSecond() / 86400, // Approximate for direction
                    isActive = activeUnit == ActiveUnit.DAY,
                    onActivate = { activeUnit = ActiveUnit.DAY },
                    onNudge = { applyNudge(ActiveUnit.DAY, it) }
                )
                
                StaticText(",")
                
                RolodexElement(
                    text = zdt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy")),
                    value = zdt.year.toLong(),
                    isActive = activeUnit == ActiveUnit.YEAR,
                    onActivate = { activeUnit = ActiveUnit.YEAR },
                    onNudge = { applyNudge(ActiveUnit.YEAR, it) }
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F0C1B), RoundedCornerShape(12.dp)).padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                RolodexElement(
                    text = zdt.format(java.time.format.DateTimeFormatter.ofPattern("HH")),
                    value = zdt.toEpochSecond() / 3600,
                    isActive = activeUnit == ActiveUnit.HOUR,
                    onActivate = { activeUnit = ActiveUnit.HOUR },
                    onNudge = { applyNudge(ActiveUnit.HOUR, it) }
                )
                
                StaticText(":")
                
                RolodexElement(
                    text = zdt.format(java.time.format.DateTimeFormatter.ofPattern("mm")),
                    value = zdt.toEpochSecond() / 60,
                    isActive = activeUnit == ActiveUnit.MINUTE,
                    onActivate = { activeUnit = ActiveUnit.MINUTE },
                    onNudge = { applyNudge(ActiveUnit.MINUTE, it) }
                )
                
                StaticText(" UTC")
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { isPlayingLocal = !isPlayingLocal },
                    modifier = Modifier.size(56.dp).background(if (isPlayingLocal) Color(0x334DD0E1) else Color(0x1AFFFFFF), RoundedCornerShape(28.dp))
                ) {
                    Icon(
                        imageVector = if (isPlayingLocal) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = if (isPlayingLocal) Color(0xFF4DD0E1) else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { isPlayingLocal = false },
                    modifier = Modifier.size(56.dp).background(Color(0x1AFFFFFF), RoundedCornerShape(28.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                ElevatedButton(
                    onClick = {
                        isPlayingLocal = false
                        onResetToNow()
                    },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0x22FFFFB3),
                        contentColor = Color(0xFFFFB300)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Now",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "NOW",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
