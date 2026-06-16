package com.example.data

import com.example.model.HouseSystem
import java.time.Instant

data class BirthData(
    val name: String = "Grace Hopper",
    val birthInstant: Instant = Instant.ofEpochMilli(812462400000L), // Oct 1, 1995 12:00 UTC
    val latitude: Double = 40.7128,          // decimal degrees, N+ S- (New York)
    val longitude: Double = -74.0060,        // decimal degrees, E+ W-
    val locationName: String = "New York City, NY",
    val hasLocation: Boolean = true,
    val houseSystem: HouseSystem = HouseSystem.PLACIDUS,
    val isSet: Boolean = true                // Always active and gorgeous on first launch
)
