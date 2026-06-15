package com.example.model

data class Aspect(
    val natalPosition: BodyPosition,
    val transitPosition: BodyPosition,
    val type: AspectType,
    val orb: Double,           // actual angular distance from exact, in degrees
    val strength: Float        // 1.0 = exact, approaches 0 at orb limit
)
