package com.example.model

enum class AspectType(
    val title: String,
    val angle: Double,
    val glyph: String,
    val defaultOrb: Double,
    val isMajor: Boolean
) {
    CONJUNCTION("Conjunction",  0.0,   "☌", 8.0, true),
    OPPOSITION( "Opposition",   180.0, "☍", 8.0, true),
    TRINE(      "Trine",        120.0, "△", 7.0, true),
    SQUARE(     "Square",       90.0,  "□", 7.0, true),
    SEXTILE(    "Sextile",      60.0,  "⚹", 5.0, true),
    QUINCUNX(   "Quincunx",     150.0, "⚻", 3.0, false),
    SEMISEXTILE("Semisextile",  30.0,  "⚺", 2.0, false),
    SEMISQUARE( "Semisquare",   45.0,  "∠", 2.0, false),
    SESQUISQUARE("Sesquisquare",135.0, "□/", 2.0, false)
}
