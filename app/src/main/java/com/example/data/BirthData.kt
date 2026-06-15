package com.example.data

import com.example.model.HouseSystem
import java.time.Instant

data class BirthData(
    val name: String = "",
    val birthInstant: Instant = Instant.ofEpochMilli(474724800000L), // Fallback e.g. Mar 15 1985 12:00
    val latitude: Double = 0.0,         // decimal degrees, N+ S-
    val longitude: Double = 0.0,        // decimal degrees, E+ W-
    val locationName: String = "",
    val hasLocation: Boolean = false,
    val houseSystem: HouseSystem = HouseSystem.PLACIDUS,
    val isSet: Boolean = false           // false = no birth data entered yet
)
