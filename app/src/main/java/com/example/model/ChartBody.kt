package com.example.model

import swisseph.SweConst

enum class ChartBody(
    val displayName: String,
    val glyph: String,
    val shortCode: String,
    val sweId: Int,
    val category: BodyCategory,
    val defaultVisible: Boolean = true
) {
    // Luminaries
    SUN("Sun",          "☉", "SUN",  SweConst.SE_SUN,        BodyCategory.LUMINARY),
    MOON("Moon",        "☽", "MON",  SweConst.SE_MOON,       BodyCategory.LUMINARY),

    // Inner planets
    MERCURY("Mercury",  "☿", "MER",  SweConst.SE_MERCURY,    BodyCategory.PLANET),
    VENUS("Venus",      "♀", "VEN",  SweConst.SE_VENUS,      BodyCategory.PLANET),
    MARS("Mars",        "♂", "MAR",  SweConst.SE_MARS,       BodyCategory.PLANET),

    // Outer planets
    JUPITER("Jupiter",  "♃", "JUP",  SweConst.SE_JUPITER,    BodyCategory.PLANET),
    SATURN("Saturn",    "♄", "SAT",  SweConst.SE_SATURN,     BodyCategory.PLANET),
    URANUS("Uranus",    "♅", "URA",  SweConst.SE_URANUS,     BodyCategory.PLANET),
    NEPTUNE("Neptune",  "♆", "NEP",  SweConst.SE_NEPTUNE,    BodyCategory.PLANET),
    PLUTO("Pluto",      "♇", "PLU",  SweConst.SE_PLUTO,      BodyCategory.PLANET),

    // Lunar points
    NORTH_NODE("North Node",     "☊", "NND",  SweConst.SE_TRUE_NODE, BodyCategory.LUNAR_POINT),
    SOUTH_NODE("South Node",     "☋", "SND",  -1,                    BodyCategory.LUNAR_POINT), // derived: NN + 180
    LILITH("Black Moon Lilith",  "⚸", "LIL",  SweConst.SE_MEAN_APOG, BodyCategory.LUNAR_POINT),

    // Asteroids & centaurs
    CHIRON("Chiron",             "⚷", "CHI",  SweConst.SE_CHIRON,    BodyCategory.ASTEROID),
    CERES("Ceres",               "⚳", "CER",  SweConst.SE_CERES,     BodyCategory.ASTEROID),
    PALLAS("Pallas Athena",      "⚴", "PAL",  SweConst.SE_PALLAS,    BodyCategory.ASTEROID),
    JUNO("Juno",                 "⚵", "JUN",  SweConst.SE_JUNO,      BodyCategory.ASTEROID),
    VESTA("Vesta",               "⚶", "VES",  SweConst.SE_VESTA,     BodyCategory.ASTEROID),

    // Angular points
    ASCENDANT("Ascendant",       "AC", "ASC",  -2,  BodyCategory.ANGLE), // derived from swe_houses ascmc[0]
    MIDHEAVEN("Midheaven",       "MC", "MC",   -3,  BodyCategory.ANGLE),  // derived from swe_houses ascmc[1]
    IC("Imum Coeli",             "IC", "IC",   -4,  BodyCategory.ANGLE),         // derived: MC + 180 (opposite MC)
    DESCENDANT("Descendant",     "DC", "DSC",  -5,  BodyCategory.ANGLE);   // derived: ASC + 180 (opposite ASC)

    companion object {
        /** Bodies that can be calculated without birth location */
        val ephemerisOnly: List<ChartBody> = entries.filter { it.sweId >= 0 }

        /** Bodies that always need swe_calc() (sweId >= 0 and not SOUTH_NODE) */
        val calcBodies: List<ChartBody> = entries.filter { it.sweId >= 0 }

        /** Bodies derived geometrically, no swe_calc needed */
        val derivedBodies: List<ChartBody> = entries.filter { it.sweId == -1 }

        /** Angular points from swe_houses() */
        val angleBodies: List<ChartBody> = entries.filter { it.sweId <= -2 }
    }
}

enum class BodyCategory { LUMINARY, PLANET, LUNAR_POINT, ASTEROID, ANGLE }
