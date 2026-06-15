package com.example.model

import androidx.compose.ui.graphics.Color

enum class ZodiacSign(
    val displayName: String,
    val glyph: String,
    val element: Element,
    val color: Color
) {
    ARIES("Aries", "♈", Element.FIRE, Color(0xFFFF5252)),
    TAURUS("Taurus", "♉", Element.EARTH, Color(0xFF4CAF50)),
    GEMINI("Gemini", "♊", Element.AIR, Color(0xFF00B0FF)),
    CANCER("Cancer", "♋", Element.WATER, Color(0xFF00E5FF)),
    LEO("Leo", "♌", Element.FIRE, Color(0xFFFFB300)),
    VIRGO("Virgo", "♍", Element.EARTH, Color(0xFF8D6E63)),
    LIBRA("Libra", "♎", Element.AIR, Color(0xFFE040FB)),
    SCORPIO("Scorpio", "♏", Element.WATER, Color(0xFF29B6F6)),
    SAGITTARIUS("Sagittarius", "♐", Element.FIRE, Color(0xFFFF9100)),
    CAPRICORN("Capricorn", "♑", Element.EARTH, Color(0xFF66BB6A)),
    AQUARIUS("Aquarius", "♒", Element.AIR, Color(0xFF7C4DFF)),
    PISCES("Pisces", "♓", Element.WATER, Color(0xFF26A69A));

    enum class Element {
        FIRE, EARTH, AIR, WATER
    }
}
