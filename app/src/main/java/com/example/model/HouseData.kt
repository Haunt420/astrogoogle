package com.example.model

data class HouseData(
    /** House cusps: cusps[0] is unused, cusps[1]..cusps[12] are house 1 to 12 longitudes */
    val cusps: DoubleArray = DoubleArray(13),
    val ascendant: Double = 0.0,
    val mc: Double = 0.0,
    val ic: Double = 0.0,
    val descendant: Double = 0.0,
    val vertex: Double = 0.0,
    val houseSystem: HouseSystem = HouseSystem.PLACIDUS,
    val isValid: Boolean = false,      // false if calculated without birth data or location
    val polarFallbackUsed: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HouseData) return false
        return cusps.contentEquals(other.cusps) &&
               ascendant == other.ascendant &&
               mc == other.mc &&
               ic == other.ic &&
               descendant == other.descendant &&
               vertex == other.vertex &&
               houseSystem == other.houseSystem &&
               isValid == other.isValid &&
               polarFallbackUsed == other.polarFallbackUsed
    }

    override fun hashCode(): Int {
        var result = cusps.contentHashCode()
        result = 31 * result + ascendant.hashCode()
        result = 31 * result + mc.hashCode()
        result = 31 * result + ic.hashCode()
        result = 31 * result + descendant.hashCode()
        result = 31 * result + vertex.hashCode()
        result = 31 * result + houseSystem.hashCode()
        result = 31 * result + isValid.hashCode()
        result = 31 * result + polarFallbackUsed.hashCode()
        return result
    }
}

enum class HouseSystem(val displayName: String, val code: Char) {
    PLACIDUS("Placidus", 'P'),
    KOCH("Koch", 'K'),
    PORPHYRY("Porphyry", 'O'),
    WHOLE_SIGN("Whole Sign", 'W'),
    EQUAL("Equal", 'E'),
    REGIOMONTANUS("Regiomontanus", 'R'),
    CAMPANUS("Campanus", 'C')
}
