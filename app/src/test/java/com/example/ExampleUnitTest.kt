package com.example

import com.example.model.ChartBody
import com.example.data.BirthData
import com.example.engine.SwissEphCalculator
import org.junit.Test
import java.io.File
import java.time.Instant

class ExampleUnitTest {
  @Test
  fun testCalcChart() {
    val calculator = SwissEphCalculator("")
    val birthData = BirthData(
      name = "Tim",
      birthInstant = Instant.parse("1985-03-15T12:00:00Z"),
      latitude = 47.1292,
      longitude = -88.5477,
      isSet = true
    )
    val transitInstant = Instant.parse("2026-06-15T12:00:00Z")
    val chart = calculator.calculateChart(birthData, transitInstant, 8.0, false)
    val sb = StringBuilder()
    sb.append("COMPUTED ASPECTS: count=${chart.aspects.size}\n")
    for (a in chart.aspects) {
      sb.append("ASPECT: transit=${a.transitPosition.body.name}(${a.transitPosition.eclipticLongitude}) natal=${a.natalPosition.body.name}(${a.natalPosition.eclipticLongitude}) type=${a.type.title} orb=${a.orb} strength=${a.strength}\n")
    }
    
    File("results.txt").writeText(sb.toString())
  }
}
