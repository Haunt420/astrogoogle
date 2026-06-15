package com.example.engine

import com.example.data.BirthData
import com.example.model.ChartState
import java.time.Instant

interface ChartCalculator {
    /**
     * Calculate a full bi-wheel chart state.
     *
     * @param birthData The natal birth data. If [BirthData.isSet] is false, natal
     *   positions are computed for [transitInstant] at 0°N 0°E with no house data.
     * @param transitInstant The transit moment to compute the outer ring for.
     * @param orbTolerance The orb in degrees for aspect detection.
     * @param showMinorAspects Whether to include minor aspects in calculation.
     */
    fun calculateChart(
        birthData: BirthData,
        transitInstant: Instant,
        orbTolerance: Double,
        showMinorAspects: Boolean
    ): ChartState
}
