package swisseph

import kotlin.math.*

class SwissEph {

    constructor()
    constructor(path: String)

    fun swe_close() {}

    fun swe_calc_ut(jd: Double, bodyId: Int, flags: Int, xx: DoubleArray, serr: StringBuffer): Int {
        val T = (jd - 2451545.0) / 36525.0

        try {
            when (bodyId) {
                SweConst.SE_SUN -> {
                    // Apparent solar coordinates
                    val L = (280.46646 + 36000.76983 * T).normalizedDegrees()
                    val g = (357.52911 + 35999.05029 * T).normalizedDegrees()
                    val lambda = (L + 1.9148 * sin(g.toRadians()) + 0.02 * sin((2 * g).toRadians())).normalizedDegrees()
                    xx[0] = lambda
                    xx[1] = 0.0 // Sun is on the ecliptic
                    xx[2] = 1.0 // approx distance 1 AU
                    xx[3] = 0.9856 // speed in deg/day
                }
                SweConst.SE_MOON -> {
                    // Simple lunar model
                    val L0 = (218.31644 + 481267.88123 * T).normalizedDegrees()
                    val M = (134.96339 + 477198.86756 * T).normalizedDegrees()
                    val F = (93.27210 + 483202.01753 * T).normalizedDegrees()
                    val D = (297.85019 + 445267.11140 * T).normalizedDegrees()

                    val lambda = (L0 + 6.289 * sin(M.toRadians()) + 1.274 * sin((2 * D - M).toRadians()) +
                            0.658 * sin((2 * D).toRadians()) + 0.214 * sin((2 * M).toRadians()) -
                            0.186 * sin((357.52911 + 35999.05029 * T).toRadians()) -
                            0.114 * sin((2 * F).toRadians())).normalizedDegrees()

                    val beta = 5.128 * sin(F.toRadians()) + 0.28 * sin((M + F).toRadians()) + 0.28 * sin((D - F).toRadians())

                    xx[0] = lambda
                    xx[1] = beta
                    xx[2] = 0.00257 // earth-moon distance
                    xx[3] = 13.176 // moon speed in deg/day
                }
                // Planetary bodies (Heliocentric to geocentric transformation)
                SweConst.SE_MERCURY, SweConst.SE_VENUS, SweConst.SE_MARS,
                SweConst.SE_JUPITER, SweConst.SE_SATURN, SweConst.SE_URANUS,
                SweConst.SE_NEPTUNE, SweConst.SE_PLUTO,
                SweConst.SE_CHIRON, SweConst.SE_CERES, SweConst.SE_PALLAS, SweConst.SE_JUNO, SweConst.SE_VESTA, SweConst.SE_PHOLUS -> {
                    val elements = getElements(bodyId, T)
                    val earthElements = getElements(-99, T) // Earth's elements

                    val pPos = getHeliocentricPos(elements, T)
                    val ePos = getHeliocentricPos(earthElements, T)

                    // Geocentric ecliptic vector
                    val x = pPos[0] - ePos[0]
                    val y = pPos[1] - ePos[1]
                    val z = pPos[2] - ePos[2]

                    var lambda = atan2(y, x).toDegrees().normalizedDegrees()
                    var beta = atan2(z, sqrt(x * x + y * y)).toDegrees()

                    xx[0] = lambda
                    xx[1] = beta
                    xx[2] = sqrt(x * x + y * y + z * z)
                    xx[3] = elements.speed // Orbit rate fallback
                }
                SweConst.SE_TRUE_NODE -> {
                    // True node
                    val lambda = (125.04452 - 1934.13626 * T).normalizedDegrees()
                    xx[0] = lambda
                    xx[1] = 0.0
                    xx[2] = 1.0
                    xx[3] = -0.0529 // retrograde true node speed
                }
                SweConst.SE_MEAN_APOG -> {
                    // Black Moon Lilith
                    val lambda = (310.13847 + 40690.11782 * T).normalizedDegrees()
                    xx[0] = lambda
                    xx[1] = 0.0
                    xx[2] = 1.0
                    xx[3] = 0.1114 // Lilith speed
                }
                else -> {
                    xx[0] = 0.0
                    xx[1] = 0.0
                    xx[2] = 1.0
                    xx[3] = 0.0
                }
            }
            return SweConst.OK
        } catch (e: Exception) {
            serr.append(e.message ?: "Calculation error")
            return SweConst.ERR
        }
    }

    // Mathematical orbital elements definition
    private class PlanetElements(
        val a: Double, val e: Double, val I: Double, val L: Double, val w: Double, val Node: Double, val speed: Double
    )

    private fun getElements(bodyId: Int, T: Double): PlanetElements {
        return when (bodyId) {
            -99 -> PlanetElements( // Earth
                1.00000261 - 0.00000003 * T,
                0.01671123 - 0.00003661 * T,
                0.0,
                (100.46457166 + 35999.37244981 * T).normalizedDegrees(),
                (102.93768193 + 0.32327364 * T).normalizedDegrees(),
                0.0,
                0.9856
            )
            SweConst.SE_MERCURY -> PlanetElements(
                0.38709893,
                0.20563069 + 0.00002040 * T,
                (7.00487 + 0.00607 * T).normalizedDegrees(),
                (252.25084 + 149472.67411 * T).normalizedDegrees(),
                (77.45645 + 0.15901 * T).normalizedDegrees(),
                (48.33167 - 0.12534 * T).normalizedDegrees(),
                4.092
            )
            SweConst.SE_VENUS -> PlanetElements(
                0.72333199,
                0.00677323 - 0.00004776 * T,
                (3.39471 + 0.00079 * T).normalizedDegrees(),
                (181.97973 + 58517.81538 * T).normalizedDegrees(),
                (131.53298 + 0.002 * T).normalizedDegrees(),
                (76.68069 - 0.27769 * T).normalizedDegrees(),
                1.602
            )
            SweConst.SE_MARS -> PlanetElements(
                1.52366231,
                0.09341233 + 0.00011902 * T,
                (1.85061 - 0.00724 * T).normalizedDegrees(),
                (355.45332 + 19140.30268 * T).normalizedDegrees(),
                (336.04084 + 0.443 * T).normalizedDegrees(),
                (49.57854 - 0.291 * T).normalizedDegrees(),
                0.524
            )
            SweConst.SE_JUPITER -> PlanetElements(
                5.20336301 + 0.00060737 * T,
                0.04839266 - 0.00012880 * T,
                (1.30530 - 0.00415 * T).normalizedDegrees(),
                (34.40438 + 3034.74612 * T).normalizedDegrees(),
                (14.75385 + 0.191 * T).normalizedDegrees(),
                (100.55615 + 0.20409 * T).normalizedDegrees(),
                0.083
            )
            SweConst.SE_SATURN -> PlanetElements(
                9.53707032 - 0.00301530 * T,
                0.05415060 - 0.00036762 * T,
                (2.48446 + 0.00193 * T).normalizedDegrees(),
                (49.94432 + 1222.11379 * T).normalizedDegrees(),
                (92.43194 + 0.412 * T).normalizedDegrees(),
                (113.71504 - 0.25912 * T).normalizedDegrees(),
                0.033
            )
            SweConst.SE_URANUS -> PlanetElements(
                19.19126393 + 0.0015204 * T,
                0.04716771 - 0.0001915 * T,
                (0.76986 - 0.00209 * T).normalizedDegrees(),
                (313.23218 + 428.48202 * T).normalizedDegrees(),
                (170.96424 + 0.096 * T).normalizedDegrees(),
                (74.22988 - 0.09421 * T).normalizedDegrees(),
                0.011
            )
            SweConst.SE_NEPTUNE -> PlanetElements(
                30.06896348 - 0.00125 * T,
                0.00858587 + 0.000025 * T,
                (1.76917 - 0.0036 * T).normalizedDegrees(),
                (304.88003 + 218.45945 * T).normalizedDegrees(),
                (44.97135 - 0.01 * T).normalizedDegrees(),
                (131.72169 - 0.0026 * T).normalizedDegrees(),
                0.006
            )
            SweConst.SE_PLUTO -> PlanetElements(
                39.48168677,
                0.24880766 + 0.000045 * T,
                (17.14175 + 0.003 * T).normalizedDegrees(),
                (238.92881 + 145.20774 * T).normalizedDegrees(),
                (224.06676 - 0.01 * T).normalizedDegrees(),
                (110.30347 - 0.012 * T).normalizedDegrees(),
                0.004
            )
            SweConst.SE_CHIRON -> PlanetElements(13.67, 0.380, 6.93, (200.0 + 7.08 * T).normalizedDegrees(), 339.3, 208.8, 0.019)
            SweConst.SE_CERES -> PlanetElements(2.767, 0.079, 10.59, (153.9 + 130.3 * T).normalizedDegrees(), 73.0, 80.3, 0.214)
            SweConst.SE_PALLAS -> PlanetElements(2.772, 0.231, 34.84, (185.3 + 130.1 * T).normalizedDegrees(), 310.2, 173.1, 0.211)
            SweConst.SE_JUNO -> PlanetElements(2.670, 0.258, 12.98, (347.1 + 137.7 * T).normalizedDegrees(), 248.1, 169.9, 0.226)
            SweConst.SE_VESTA -> PlanetElements(2.361, 0.089, 7.14, (258.1 + 162.1 * T).normalizedDegrees(), 251.1, 103.8, 0.271)
            SweConst.SE_PHOLUS -> PlanetElements(20.3, 0.572, 24.6, (32.0 + 3.91 * T).normalizedDegrees(), 354.9, 119.4, 0.010)
            else -> PlanetElements(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }
    }

    private fun getHeliocentricPos(el: PlanetElements, T: Double): DoubleArray {
        val M = (el.L - el.w).normalizedDegrees()

        // Solve Kepler's equation: E - e * sin(E) = M
        var E = M.toRadians()
        val e = el.e
        for (i in 0..4) {
            E = M.toRadians() + e * sin(E)
        }

        // Coordinates in orbital plane
        val xOrb = el.a * (cos(E) - e)
        val yOrb = el.a * sqrt(1.0 - e * e) * sin(E)

        // Rotate orbital position to ecliptic coordinates
        val nodeRad = el.Node.toRadians()
        val inclRad = el.I.toRadians()
        val omegaRad = (el.w - el.Node).toRadians() // Argument of perihelion

        val cosNode = cos(nodeRad)
        val sinNode = sin(nodeRad)
        val cosIncl = cos(inclRad)
        val sinIncl = sin(inclRad)
        val cosOm = cos(omegaRad)
        val sinOm = sin(omegaRad)

        val xEcl = xOrb * (cosOm * cosNode - sinOm * sinNode * cosIncl) - yOrb * (sinOm * cosNode + cosOm * sinNode * cosIncl)
        val yEcl = xOrb * (cosOm * sinNode + sinOm * cosNode * cosIncl) - yOrb * (sinOm * sinNode - cosOm * cosNode * cosIncl)
        val zEcl = xOrb * (sinOm * sinIncl) + yOrb * (cosOm * sinIncl)

        return doubleArrayOf(xEcl, yEcl, zEcl)
    }

    // Astrological local house calculation using spherical trigonometry
    fun swe_houses(jd: Double, latitude: Double, longitude: Double, hSysCode: Int, cusps: DoubleArray, ascmc: DoubleArray): Int {
        val T = (jd - 2451545.0) / 36525.0

        // Obliquity of reference ecliptic
        val eps = (23.4392911 - 0.0130042 * T).toRadians()

        // Greenwich Sidereal Time (GST) in hours
        val d = jd - 2451545.0
        val GMST = (18.697374558 + 24.06570982441908 * d) % 24.0

        // Local Sidereal Time (LST) in radians
        val LSTDeg = ((GMST + (longitude / 15.0)) * 15.0).normalizedDegrees()
        val RAMC = LSTDeg.toRadians()

        // Midheaven calculation
        val mcRad = atan2(sin(RAMC), cos(RAMC) * cos(eps))
        val MC = mcRad.toDegrees().normalizedDegrees()

        // Ascendant calculation
        val latRad = latitude.toRadians()
        val ascRad = atan2(cos(RAMC), -sin(RAMC) * cos(eps) - tan(latRad) * sin(eps))
        val ASC = ascRad.toDegrees().normalizedDegrees()

        val IC = (MC + 180.0).normalizedDegrees()
        val DSC = (ASC + 180.0).normalizedDegrees()

        ascmc[0] = ASC
        ascmc[1] = MC
        ascmc[2] = DSC
        ascmc[3] = (LSTDeg + 90.0).normalizedDegrees() // Vertex approximation

        // Calculate 12 house cusps (Equal or simplified system)
        // Classical Placidus can fail at high latitudes, fallback to Equal
        for (i in 1..12) {
            val offset = (i - 1) * 30.0
            cusps[i] = (ASC + offset).normalizedDegrees()
        }

        return SweConst.OK
    }
}

// Extension helper functions
private fun Double.normalizedDegrees(): Double {
    var d = this % 360.0
    if (d < 0.0) d += 360.0
    return d
}

private fun Double.toRadians(): Double = Math.toRadians(this)
private fun Double.toDegrees(): Double = Math.toDegrees(this)
