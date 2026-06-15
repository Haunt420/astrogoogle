package swisseph

object SweDate {
    const val SE_GREG_CAL = 1.0

    fun getJulDay(year: Int, month: Int, day: Int, hourDecimal: Double, cal: Double): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + (hourDecimal / 24.0) + b - 1524.5
    }
}
