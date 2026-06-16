package com.example.engine

import android.util.Log
import com.example.data.BirthData
import com.example.model.*
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.Instant
import java.time.ZoneOffset
import kotlin.math.abs
import kotlin.math.floor

class SwissEphCalculator(private val ephePath: String) : ChartCalculator {

    private val TAG = "SwissEphCalculator"

    private val calcFlags: Int
        get() = if (ephePath.isNotEmpty()) {
            SweConst.SEFLG_SWIEPH or SweConst.SEFLG_SPEED
        } else {
            SweConst.SEFLG_MOSEPH or SweConst.SEFLG_SPEED
        }

    override fun calculateChart(
        birthData: BirthData,
        transitInstant: Instant,
        orbTolerance: Double,
        showMinorAspects: Boolean
    ): ChartState {
        val se = try {
            if (ephePath.isNotEmpty()) SwissEph(ephePath) else SwissEph()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SwissEph with path '$ephePath': ${e.message}, falling back to default constructor")
            SwissEph()
        }

        try {
            val natalJd = if (birthData.isSet) {
                birthData.birthInstant.toJulianDay()
            } else {
                transitInstant.toJulianDay()
            }
            val transitJd = transitInstant.toJulianDay()

            val natalPositions = calculatePositions(se, natalJd, ChartRing.NATAL)
            val transitPositions = calculatePositions(se, transitJd, ChartRing.TRANSIT)

            val natalHouses = if (birthData.isSet && birthData.hasLocation) {
                calculateHouses(se, natalJd, birthData.latitude, birthData.longitude, birthData.houseSystem)
            } else {
                HouseData(isValid = false)
            }

            val natalWithAngles = if (natalHouses.isValid) {
                natalPositions + calculateAngularPoints(natalHouses, ChartRing.NATAL)
            } else {
                natalPositions
            }

            val aspects = calculateAspects(
                natal = natalWithAngles,
                transit = transitPositions,
                orbTolerance = orbTolerance,
                showMinorAspects = showMinorAspects
            )

            return ChartState(
                natalInstant = if (birthData.isSet) birthData.birthInstant else transitInstant,
                transitInstant = transitInstant,
                natalPositions = natalWithAngles,
                transitPositions = transitPositions,
                natalHouses = natalHouses,
                transitHouses = HouseData(isValid = false),
                aspects = aspects,
                orbTolerance = orbTolerance,
                showMinorAspects = showMinorAspects,
                source = "Swiss Ephemeris " + (if (ephePath.isNotEmpty()) "(Ecliptic Files)" else "(Analytical Moshier)")
            )
        } finally {
            se.swe_close()
        }
    }

    // -------------------------------------------------------------------------
    // Position calculation
    // -------------------------------------------------------------------------

    private fun calculatePositions(se: SwissEph, jd: Double, ring: ChartRing): List<BodyPosition> {
        val list = mutableListOf<BodyPosition>()

        // 1. Calculate standard Swiss Ephemeris bodies
        for (body in ChartBody.calcBodies) {
            val pos = calculateBodyPosition(se, jd, body, ring)
            if (pos != null) {
                list.add(pos)
            }
        }

        // 2. Derive South Node from North Node (if North Node was calculated)
        val northNode = list.find { it.body == ChartBody.NORTH_NODE }
        if (northNode != null) {
            val southNode = BodyPosition(
                body = ChartBody.SOUTH_NODE,
                eclipticLongitude = (northNode.eclipticLongitude + 180.0).normalizedDegrees(),
                eclipticLatitude = -northNode.eclipticLatitude,
                speedDegreesPerDay = northNode.speedDegreesPerDay,
                isRetrograde = northNode.isRetrograde,
                ring = ring
            )
            list.add(southNode)
        }

        return list
    }

    private fun calculateBodyPosition(
        se: SwissEph,
        jd: Double,
        body: ChartBody,
        ring: ChartRing
    ): BodyPosition? {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        val result = se.swe_calc_ut(jd, body.sweId, calcFlags, xx, serr)
        if (result == SweConst.ERR) {
            Log.w(TAG, "SwissEph error for ${body.displayName}: $serr")
            // Try fallback to Moshier if using SWIEPH failed
            if ((calcFlags and SweConst.SEFLG_SWIEPH) != 0) {
                val fallbackFlags = SweConst.SEFLG_MOSEPH or SweConst.SEFLG_SPEED
                val fallbackResult = se.swe_calc_ut(jd, body.sweId, fallbackFlags, xx, serr)
                if (fallbackResult != SweConst.ERR) {
                    val longitude = xx[0].normalizedDegrees()
                    val speed = xx[3]
                    return BodyPosition(
                        body = body,
                        eclipticLongitude = longitude,
                        eclipticLatitude = xx[1],
                        speedDegreesPerDay = speed,
                        isRetrograde = speed < -0.0001,
                        ring = ring
                    )
                }
            }
            return null
        }

        val longitude = xx[0].normalizedDegrees()
        val speed = xx[3]

        return BodyPosition(
            body = body,
            eclipticLongitude = longitude,
            eclipticLatitude = xx[1],
            speedDegreesPerDay = speed,
            isRetrograde = speed < -0.0001,
            ring = ring
        )
    }

    // -------------------------------------------------------------------------
    // House calculation
    // -------------------------------------------------------------------------

    private fun calculateHouses(
        se: SwissEph,
        jd: Double,
        latitude: Double,
        longitude: Double,
        houseSystem: HouseSystem
    ): HouseData {
        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)

        // Try standard calculation
        var polarFallback = false
        var hSysCode = houseSystem.code.code.toInt()
        var result = se.swe_houses(jd, latitude, longitude, hSysCode, cusps, ascmc)

        if (result == SweConst.ERR && houseSystem == HouseSystem.PLACIDUS) {
            // Placidus fails at polar latitudes, fall back to Equal system
            polarFallback = true
            hSysCode = HouseSystem.EQUAL.code.code.toInt()
            result = se.swe_houses(jd, latitude, longitude, hSysCode, cusps, ascmc)
        }

        if (result == SweConst.ERR) {
            return HouseData(isValid = false)
        }

        val finalSys = if (polarFallback) HouseSystem.EQUAL else houseSystem

        return HouseData(
            cusps = cusps,
            ascendant = ascmc[0].normalizedDegrees(),
            mc = ascmc[1].normalizedDegrees(),
            ic = (ascmc[1] + 180.0).normalizedDegrees(),
            descendant = (ascmc[0] + 180.0).normalizedDegrees(),
            vertex = ascmc[3].normalizedDegrees(),
            houseSystem = finalSys,
            isValid = true,
            polarFallbackUsed = polarFallback
        )
    }

    private fun calculateAngularPoints(houses: HouseData, ring: ChartRing): List<BodyPosition> {
        if (!houses.isValid) return emptyList()
        return listOf(
            angularPoint(ChartBody.ASCENDANT, houses.ascendant, ring),
            angularPoint(ChartBody.MIDHEAVEN, houses.mc, ring),
            angularPoint(ChartBody.IC, houses.ic, ring),
            angularPoint(ChartBody.DESCENDANT, houses.descendant, ring)
        )
    }

    private fun angularPoint(body: ChartBody, longitude: Double, ring: ChartRing) = BodyPosition(
        body = body,
        eclipticLongitude = longitude,
        eclipticLatitude = 0.0,
        speedDegreesPerDay = 0.0,
        isRetrograde = false,
        ring = ring
    )

    // -------------------------------------------------------------------------
    // Aspect calculation
    // -------------------------------------------------------------------------

    private fun calculateAspects(
        natal: List<BodyPosition>,
        transit: List<BodyPosition>,
        orbTolerance: Double,
        showMinorAspects: Boolean
    ): List<Aspect> {
        val activeAspectTypes = AspectType.entries
            .filter { it.isMajor || showMinorAspects }

        val aspects = mutableListOf<Aspect>()

        for (natalPos in natal) {
            for (transitPos in transit) {
                // Eliminate self-aspects of the exact same body in transit vs natal if same time, 
                // but generally they are different rings so it's fine.
                val separation = angularSeparation(natalPos.eclipticLongitude, transitPos.eclipticLongitude)
                
                var bestMatch: Pair<AspectType, Double>? = null
                for (type in activeAspectTypes) {
                    val allowedOrb = if (type.isMajor) orbTolerance.coerceIn(1.0, 10.0) else type.defaultOrb
                    val diff = abs(separation - type.angle)
                    if (diff <= allowedOrb) {
                        if (bestMatch == null || diff < bestMatch.second) {
                            bestMatch = Pair(type, diff)
                        }
                    }
                }

                if (bestMatch != null) {
                    val (type, orb) = bestMatch
                    val allowedOrb = if (type.isMajor) orbTolerance.coerceIn(1.0, 10.0) else type.defaultOrb
                    val strength = (1.0 - (orb / allowedOrb)).coerceIn(0.05, 1.0).toFloat()
                    
                    aspects.add(
                        Aspect(
                            natalPosition = natalPos,
                            transitPosition = transitPos,
                            type = type,
                            orb = orb,
                            strength = strength
                        )
                    )
                }
            }
        }

        return aspects
            .sortedWith(compareByDescending<Aspect> { it.strength }.thenBy { it.orb })
            .take(50)
    }
}

// -------------------------------------------------------------------------
// Helper extension functions
// -------------------------------------------------------------------------

private fun Double.normalizedDegrees(): Double = ((this % 360.0) + 360.0) % 360.0

private fun angularSeparation(a: Double, b: Double): Double {
    val diff = abs(a.normalizedDegrees() - b.normalizedDegrees()) % 360.0
    return if (diff > 180.0) 360.0 - diff else diff
}

/**
 * Convert java.time.Instant to Julian Day number (UT).
 * Uses Gregorian calendar converter.
 */
fun Instant.toJulianDay(): Double {
    val zdt = atZone(ZoneOffset.UTC)
    val hourDecimal = zdt.hour + zdt.minute / 60.0 + zdt.second / 3600.0 +
                      zdt.nano / 3_600_000_000_000.0
    return SweDate.getJulDay(
        zdt.year, zdt.monthValue, zdt.dayOfMonth,
        hourDecimal, SweDate.SE_GREG_CAL.toDouble()
    )
}
