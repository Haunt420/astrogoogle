package com.example.model

import java.time.Instant

data class ChartState(
    val natalInstant: Instant,
    val transitInstant: Instant,
    val natalPositions: List<BodyPosition>,
    val transitPositions: List<BodyPosition>,
    val natalHouses: HouseData,
    val transitHouses: HouseData,     // transit houses (typically unused unless transit location is given)
    val aspects: List<Aspect>,
    val orbTolerance: Double,
    val showMinorAspects: Boolean,
    val source: String = "Swiss Ephemeris"
) {
    companion object {
        val Empty = ChartState(
            natalInstant = Instant.EPOCH,
            transitInstant = Instant.EPOCH,
            natalPositions = emptyList(),
            transitPositions = emptyList(),
            natalHouses = HouseData(isValid = false),
            transitHouses = HouseData(isValid = false),
            aspects = emptyList(),
            orbTolerance = 8.0,
            showMinorAspects = false
        )
    }
}
