package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BirthData
import com.example.data.BirthDataRepository
import com.example.data.EphemerisAssetExtractor
import com.example.engine.SwissEphCalculator
import com.example.model.ChartState
import com.example.model.HouseSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

enum class ScrubGranularity(val label: String, val stepMinutes: Long) {
    MINUTE("MIN",   1L),
    HOUR(  "HR",    60L),
    DAY(   "DAY",   1440L),
    MONTH( "MONTH", 43200L),  // ~30 days
    YEAR(  "YEAR",  525600L)  // ~365 days
}

object DateRange {
    val MIN_INSTANT: Instant = Instant.parse("1900-01-01T00:00:00Z")
    val MAX_INSTANT: Instant = Instant.parse("2050-12-31T23:59:00Z")
    val TOTAL_MINUTES: Long = ChronoUnit.MINUTES.between(MIN_INSTANT, MAX_INSTANT)

    fun fromFraction(f: Float): Instant {
        val clampedF = f.coerceIn(0f, 1f)
        val minuteOffset = (clampedF * TOTAL_MINUTES).toLong()
        return MIN_INSTANT.plus(minuteOffset, ChronoUnit.MINUTES)
    }

    fun toFraction(instant: Instant): Float {
        val clamped = instant.coerceIn(MIN_INSTANT, MAX_INSTANT)
        val elapsed = ChronoUnit.MINUTES.between(MIN_INSTANT, clamped)
        return (elapsed.toFloat() / TOTAL_MINUTES.toFloat()).coerceIn(0f, 1f)
    }
}

data class TransitUiState(
    val chartState: ChartState = ChartState.Empty,
    val transitInstant: Instant = Instant.now().coerceIn(DateRange.MIN_INSTANT, DateRange.MAX_INSTANT),
    val selectedGranularity: ScrubGranularity = ScrubGranularity.DAY,
    val orbTolerance: Double = 8.0,
    val showMinorAspects: Boolean = false,
    val isCalculating: Boolean = false,
    val isPlaying: Boolean = false,
    val birthData: BirthData = BirthData()
)

class TransitViewModel(application: Application) : AndroidViewModel(application) {

    private val birthDataRepo = BirthDataRepository(application)
    private val ephePath = EphemerisAssetExtractor.ensureExtracted(application)
    private val calculator = SwissEphCalculator(ephePath)

    private val transitInstant = MutableStateFlow(
        Instant.now().coerceIn(DateRange.MIN_INSTANT, DateRange.MAX_INSTANT)
    )
    private val selectedGranularity = MutableStateFlow(ScrubGranularity.DAY)
    private val orbTolerance = MutableStateFlow(8.0)
    private val showMinorAspects = MutableStateFlow(false)
    private val isPlaying = MutableStateFlow(false)
    private var playbackJob: Job? = null

    // Track a flow of state transitions
    val uiState: StateFlow<TransitUiState> = combine(
        transitInstant,
        selectedGranularity,
        orbTolerance,
        showMinorAspects,
        isPlaying,
        birthDataRepo.birthDataFlow
    ) { flows ->
        CalculationTrigger(
            instant = flows[0] as Instant,
            granularity = flows[1] as ScrubGranularity,
            orb = flows[2] as Double,
            minor = flows[3] as Boolean,
            playing = flows[4] as Boolean,
            birthData = flows[5] as BirthData
        )
    }
    .distinctUntilChanged()
    .flatMapLatest { trigger ->
        flow {
            val chart = withContext(Dispatchers.Default) {
                calculator.calculateChart(
                    birthData = trigger.birthData,
                    transitInstant = trigger.instant,
                    orbTolerance = trigger.orb,
                    showMinorAspects = trigger.minor
                )
            }

            emit(
                TransitUiState(
                    chartState = chart,
                    transitInstant = trigger.instant,
                    selectedGranularity = trigger.granularity,
                    orbTolerance = trigger.orb,
                    showMinorAspects = trigger.minor,
                    isCalculating = false,
                    isPlaying = trigger.playing,
                    birthData = trigger.birthData
                )
            )
        }
    }
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TransitUiState()
    )

    private data class CalculationTrigger(
        val instant: Instant,
        val granularity: ScrubGranularity,
        val orb: Double,
        val minor: Boolean,
        val playing: Boolean,
        val birthData: BirthData
    )

    fun setTransitInstant(instant: Instant) {
        transitInstant.value = instant.coerceIn(DateRange.MIN_INSTANT, DateRange.MAX_INSTANT)
    }

    fun setGranularity(granularity: ScrubGranularity) {
        selectedGranularity.value = granularity
    }

    fun setOrbTolerance(orb: Double) {
        orbTolerance.value = orb.coerceIn(1.0, 10.0)
    }

    fun setShowMinorAspects(show: Boolean) {
        showMinorAspects.value = show
    }

    fun nudgeTransit(minutes: Long) {
        val current = transitInstant.value
        val next = current.plus(minutes, ChronoUnit.MINUTES)
        if (next.isBefore(DateRange.MIN_INSTANT)) {
            // Wraps to MAX
            transitInstant.value = DateRange.MAX_INSTANT
        } else if (next.isAfter(DateRange.MAX_INSTANT)) {
            // Wraps to MIN
            transitInstant.value = DateRange.MIN_INSTANT
        } else {
            transitInstant.value = next
        }
    }

    fun resetToNow() {
        transitInstant.value = Instant.now().coerceIn(DateRange.MIN_INSTANT, DateRange.MAX_INSTANT)
    }

    fun togglePlayback() {
        if (isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isPlaying.value) {
                delay(250)
                nudgeTransit(selectedGranularity.value.stepMinutes)
            }
        }
    }

    private fun stopPlayback() {
        isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun updateBirthHouseSystem(system: HouseSystem) {
        viewModelScope.launch {
            val currentData = birthDataRepo.birthDataFlow.firstOrNull() ?: BirthData()
            if (currentData.isSet) {
                birthDataRepo.save(currentData.copy(houseSystem = system))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}
