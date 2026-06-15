package com.example.model

import kotlin.math.floor

data class BodyPosition(
    val body: ChartBody,
    val eclipticLongitude: Double,       // 0–360°, tropical geocentric
    val eclipticLatitude: Double,         // ecliptic latitude (usually small)
    val speedDegreesPerDay: Double,       // negative speed indicates retrograde motion
    val isRetrograde: Boolean,            // speed is negative
    val ring: ChartRing
) {
    val zodiacSign: ZodiacSign
        get() = ZodiacSign.entries[floor(eclipticLongitude / 30.0).toInt().coerceIn(0, 11)]

    val degreeInSign: Double
        get() = eclipticLongitude - zodiacSign.ordinal * 30.0

    val formattedDegree: String
        get() {
            val d = degreeInSign.toInt()
            val m = ((degreeInSign - d) * 60.0).toInt()
            return "$d°${m.toString().padStart(2, '0')}' ${zodiacSign.glyph}"
        }
}

enum class ChartRing { NATAL, TRANSIT }
