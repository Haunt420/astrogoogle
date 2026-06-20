package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DateRange
import com.example.viewmodel.ScrubGranularity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset

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
    var slideUp by remember { mutableStateOf(true) }
    var lastValue by remember { mutableLongStateOf(value) }

    if (value != lastValue) {
        slideUp = value > lastValue
        lastValue = value
    }

    val coroutineScope = rememberCoroutineScope()
    var accumulatedDelta by remember { mutableFloatStateOf(0f) }
    
    val density = androidx.compose.ui.platform.LocalDensity.current
    val thresholdPx = with(density) { 50.dp.toPx() } // Require 50dp drag per step

    val draggableState = rememberDraggableState { delta ->
        accumulatedDelta -= delta
        if (accumulatedDelta > thresholdPx) {
            val steps = (accumulatedDelta / thresholdPx).toInt()
            onNudge(steps.toLong())
            accumulatedDelta -= steps * thresholdPx
        } else if (accumulatedDelta < -thresholdPx) {
            val steps = (accumulatedDelta / thresholdPx).toInt()
            onNudge(steps.toLong())
            accumulatedDelta -= steps * thresholdPx
        }
    }

    // A subtle glowing background when active
    val activeBgBrush = if (isActive) {
        androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(Color(0xFF4DD0E1).copy(alpha = 0.35f), Color.Transparent),
            radius = 120f
        )
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .draggable(
                orientation = Orientation.Vertical,
                state = draggableState,
                onDragStarted = { 
                    onActivate()
                    accumulatedDelta = 0f 
                },
                onDragStopped = { velocity ->
                    val v = -velocity
                    if (Math.abs(v) > 500f) { // Velocity threshold to trigger momentum
                        coroutineScope.launch {
                            var lastAnimValue = 0f
                            try {
                                Animatable(0f).animateDecay(
                                    initialVelocity = v,
                                    animationSpec = exponentialDecay(frictionMultiplier = 2.5f) // Higher friction
                                ) {
                                    val diff = this.value - lastAnimValue
                                    lastAnimValue = this.value
                                    accumulatedDelta += diff
                                    if (accumulatedDelta > thresholdPx) {
                                        val steps = (accumulatedDelta / thresholdPx).toInt()
                                        onNudge(steps.toLong())
                                        accumulatedDelta -= steps * thresholdPx
                                    } else if (accumulatedDelta < -thresholdPx) {
                                        val steps = (accumulatedDelta / thresholdPx).toInt()
                                        onNudge(steps.toLong())
                                        accumulatedDelta -= steps * thresholdPx
                                    }
                                }
                            } finally {
                                accumulatedDelta = 0f
                            }
                        }
                    } else {
                        accumulatedDelta = 0f
                    }
                }
            )
            .background(activeBgBrush, RoundedCornerShape(12.dp))
            .border(
                if (isActive) 1.5.dp else 1.dp,
                if (isActive) Color(0xFF4DD0E1).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .height(44.dp)
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                if (slideUp) {
                    (slideInVertically { height -> height } + fadeIn(tween(150))) togetherWith
                            (slideOutVertically { height -> -height } + fadeOut(tween(150)))
                } else {
                    (slideInVertically { height -> -height } + fadeIn(tween(150))) togetherWith
                            (slideOutVertically { height -> height } + fadeOut(tween(150)))
                }
            },
            label = "rolodex"
        ) { targetText ->
            Text(
                text = targetText,
                fontSize = 28.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isActive) Color(0xFFFFFFFF) else Color(0xFF4DD0E1).copy(alpha = 0.8f),
                style = TextStyle(
                    shadow = if (isActive) Shadow(color = Color(0xFF4DD0E1), blurRadius = 24f) else null
                )
            )
        }
    }
}

@Composable
fun StaticText(text: String) {
    Text(
        text = text,
        fontSize = 24.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Light,
        color = Color(0xFF6C6C85),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
fun TimeScrubber(
    currentInstant: Instant,
    selectedGranularity: ScrubGranularity, // Ignored
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
    var localZdt by remember(currentInstant) { mutableStateOf(zdt) }

    LaunchedEffect(isPlayingLocal, activeUnit) {
        if (isPlayingLocal) {
            while (true) {
                delay(1000)
                val nextZdt = when (activeUnit) {
                    ActiveUnit.MONTH -> localZdt.plusMonths(1)
                    ActiveUnit.DAY -> localZdt.plusDays(1)
                    ActiveUnit.YEAR -> localZdt.plusYears(1)
                    ActiveUnit.HOUR -> localZdt.plusHours(1)
                    ActiveUnit.MINUTE -> localZdt.plusMinutes(1)
                }
                localZdt = nextZdt
                val newInst = nextZdt.toInstant().coerceIn(DateRange.MIN_INSTANT, DateRange.MAX_INSTANT)
                onInstantChanged(newInst)
            }
        }
    }

    fun applyNudge(unit: ActiveUnit, amount: Long) {
        isPlayingLocal = false
        val nextZdt = when (unit) {
            ActiveUnit.MONTH -> localZdt.plusMonths(amount)
            ActiveUnit.DAY -> localZdt.plusDays(amount)
            ActiveUnit.YEAR -> localZdt.plusYears(amount)
            ActiveUnit.HOUR -> localZdt.plusHours(amount)
            ActiveUnit.MINUTE -> localZdt.plusMinutes(amount)
        }
        localZdt = nextZdt
        val newInst = nextZdt.toInstant().coerceIn(DateRange.MIN_INSTANT, DateRange.MAX_INSTANT)
        onInstantChanged(newInst)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141224)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0C1B), RoundedCornerShape(16.dp))
                    .padding(vertical = 18.dp, horizontal = 12.dp)
            ) {
                val dayOfWeek = localZdt.format(java.time.format.DateTimeFormatter.ofPattern("EEE"))
                StaticText("$dayOfWeek,")
                
                RolodexElement(
                    text = localZdt.format(java.time.format.DateTimeFormatter.ofPattern("MMM")),
                    value = localZdt.year * 12L + localZdt.monthValue,
                    isActive = activeUnit == ActiveUnit.MONTH,
                    onActivate = { activeUnit = ActiveUnit.MONTH },
                    onNudge = { applyNudge(ActiveUnit.MONTH, it) }
                )
                
                RolodexElement(
                    text = localZdt.format(java.time.format.DateTimeFormatter.ofPattern("dd")),
                    value = localZdt.toEpochSecond() / 86400,
                    isActive = activeUnit == ActiveUnit.DAY,
                    onActivate = { activeUnit = ActiveUnit.DAY },
                    onNudge = { applyNudge(ActiveUnit.DAY, it) }
                )
                
                StaticText(",")
                
                RolodexElement(
                    text = localZdt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy")),
                    value = localZdt.year.toLong(),
                    isActive = activeUnit == ActiveUnit.YEAR,
                    onActivate = { activeUnit = ActiveUnit.YEAR },
                    onNudge = { applyNudge(ActiveUnit.YEAR, it) }
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0C1B), RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp, horizontal = 12.dp)
            ) {
                RolodexElement(
                    text = localZdt.format(java.time.format.DateTimeFormatter.ofPattern("HH")),
                    value = localZdt.toEpochSecond() / 3600,
                    isActive = activeUnit == ActiveUnit.HOUR,
                    onActivate = { activeUnit = ActiveUnit.HOUR },
                    onNudge = { applyNudge(ActiveUnit.HOUR, it) }
                )
                
                StaticText(":")
                
                RolodexElement(
                    text = localZdt.format(java.time.format.DateTimeFormatter.ofPattern("mm")),
                    value = localZdt.toEpochSecond() / 60,
                    isActive = activeUnit == ActiveUnit.MINUTE,
                    onActivate = { activeUnit = ActiveUnit.MINUTE },
                    onNudge = { applyNudge(ActiveUnit.MINUTE, it) }
                )
                
                StaticText(" UTC")
            }

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { isPlayingLocal = !isPlayingLocal },
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            if (isPlayingLocal) Color(0x334DD0E1) else Color(0x1AFFFFFF),
                            RoundedCornerShape(32.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isPlayingLocal) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = if (isPlayingLocal) Color(0xFF4DD0E1) else Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                IconButton(
                    onClick = { isPlayingLocal = false },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0x1AFFFFFF), RoundedCornerShape(32.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(40.dp))
                
                ElevatedButton(
                    onClick = {
                        isPlayingLocal = false
                        onResetToNow()
                    },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0x22FFFFB3),
                        contentColor = Color(0xFFFFB300)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Now",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "NOW",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
