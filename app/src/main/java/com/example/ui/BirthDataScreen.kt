package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HouseSystem
import com.example.viewmodel.BirthDataViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDataScreen(
    viewModel: BirthDataViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name by viewModel.name.collectAsState()
    val dateTimeVal by viewModel.birthDateTimeVal.collectAsState()
    val latVal by viewModel.latitudeVal.collectAsState()
    val lonVal by viewModel.longitudeVal.collectAsState()
    val locNameVal by viewModel.locationNameVal.collectAsState()
    val hasLocationVal by viewModel.hasLocationVal.collectAsState()
    val houseSystemVal by viewModel.houseSystemVal.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()

    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F0C1B),
                Color(0xFF05020A)
            )
        )
    }

    // Handlers for date segment fields
    var yearText by remember(dateTimeVal) { mutableStateOf(dateTimeVal.year.toString()) }
    var monthText by remember(dateTimeVal) { mutableStateOf(dateTimeVal.monthValue.toString()) }
    var dayText by remember(dateTimeVal) { mutableStateOf(dateTimeVal.dayOfMonth.toString()) }
    var hourText by remember(dateTimeVal) { mutableStateOf(dateTimeVal.hour.toString()) }
    var minuteText by remember(dateTimeVal) { mutableStateOf(dateTimeVal.minute.toString()) }

    var expandedHouseMenu by remember { mutableStateOf(false) }

    val suggestions = remember(locNameVal) {
        if (locNameVal.trim().length >= 2) {
            PRESET_LOCATIONS.filter {
                it.name.contains(locNameVal, ignoreCase = true) && 
                !it.name.equals(locNameVal.trim(), ignoreCase = true)
            }.take(5)
        } else {
            emptyList()
        }
    }

    // Observe save success triggers
    LaunchedEffect(viewModel.saveSuccess) {
        viewModel.saveSuccess.collectLatest { success ->
            if (success) {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "NATAL DATA SETUP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Return to chart",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0E0B1A),
                    titleContentColor = Color.White
                )
            )
        },
        modifier = modifier.background(backgroundBrush)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .background(backgroundBrush)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header explanation
            Text(
                text = "Set up your birth profile details. Exact UTC coordinates and time are required to compute house cusp positions, Ascendants, and natal aspects.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 18.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // General Profile Information
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141224)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "1. PERSONAL DATA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.setName(it) },
                        label = { Text("Your Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB300),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = Color(0xFFFFB300),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Birth Date & Time Inputs
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141224)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "2. DATE & UTC TIME OF BIRTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300)
                    )

                    // Triple row inputs [Year] [Month] [Day]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = yearText,
                            onValueChange = {
                                yearText = it
                                val y = it.toIntOrNull() ?: dateTimeVal.year
                                viewModel.setBirthDateTime(dateTimeVal.withYear(y.coerceIn(1500, 2100)))
                            },
                            label = { Text("Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB300),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1.2f)
                        )

                        OutlinedTextField(
                            value = monthText,
                            onValueChange = {
                                monthText = it
                                val m = it.toIntOrNull() ?: dateTimeVal.monthValue
                                viewModel.setBirthDateTime(dateTimeVal.withMonth(m.coerceIn(1, 12)))
                            },
                            label = { Text("Month") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB300),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = dayText,
                            onValueChange = {
                                dayText = it
                                val d = it.toIntOrNull() ?: dateTimeVal.dayOfMonth
                                viewModel.setBirthDateTime(dateTimeVal.withDayOfMonth(d.coerceIn(1, 31)))
                            },
                            label = { Text("Day") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB300),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Double row inputs [Hour] [Minute]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = hourText,
                            onValueChange = {
                                hourText = it
                                val h = it.toIntOrNull() ?: dateTimeVal.hour
                                viewModel.setBirthDateTime(dateTimeVal.withHour(h.coerceIn(0, 23)))
                            },
                            label = { Text("Hour (0-23)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB300),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = minuteText,
                            onValueChange = {
                                minuteText = it
                                val m = it.toIntOrNull() ?: dateTimeVal.minute
                                viewModel.setBirthDateTime(dateTimeVal.withMinute(m.coerceIn(0, 59)))
                            },
                            label = { Text("Minute (0-59)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB300),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Birth Coordinates & Location (Optional)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141224)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "3. GEOGRAPHIC COORDINATES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Include Location",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Checkbox(
                                checked = hasLocationVal,
                                onCheckedChange = { viewModel.setHasLocation(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFFFB300),
                                    uncheckedColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = hasLocationVal,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = locNameVal,
                                onValueChange = { viewModel.setLocationName(it) },
                                label = { Text("Location Name") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFB300),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (suggestions.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF231F3A), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                        .padding(vertical = 4.dp)
                                ) {
                                    suggestions.forEach { preset ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setLocationName(preset.name)
                                                    viewModel.setLatitude(preset.lat.toString())
                                                    viewModel.setLongitude(preset.lon.toString())
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = preset.name,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = latVal,
                                    onValueChange = { viewModel.setLatitude(it) },
                                    label = { Text("Latitude (e.g. 51.5)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFFB300),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = lonVal,
                                    onValueChange = { viewModel.setLongitude(it) },
                                    label = { Text("Longitude (e.g. -0.1)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFFB300),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Selected House System Dropdown card
            AnimatedVisibility(visible = hasLocationVal) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141224)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "4. DEFAULT HOUSE SYSTEM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedHouseMenu = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.12f))
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = houseSystemVal.displayName,
                                        fontSize = 14.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = "Expand menu",
                                        tint = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expandedHouseMenu,
                                onDismissRequest = { expandedHouseMenu = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(Color(0xFF1C1A32))
                            ) {
                                HouseSystem.entries.forEach { h ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                h.displayName,
                                                color = if (h == houseSystemVal) Color(0xFFFFB300) else Color.White
                                            )
                                        },
                                        onClick = {
                                            viewModel.setHouseSystem(h)
                                            expandedHouseMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error Message notice
            AnimatedVisibility(visible = errorMsg != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x22EF5350)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error message icon",
                            tint = Color(0xFFEF5350)
                        )
                        Text(
                            text = errorMsg ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFEF5350)
                        )
                    }
                }
            }

            // Buttons: Save & Clear (Side-by-side)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clear button
                OutlinedButton(
                    onClick = { viewModel.clearBirthData() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFEF5350).copy(alpha = 0.3f), Color(0xFFEF5350).copy(alpha = 0.3f))
                        )
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear fields", modifier = Modifier.size(16.dp))
                        Text("CLEAR PROFILE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Save button
                Button(
                    onClick = { viewModel.saveBirthData() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB300),
                        contentColor = Color(0xFF0C0A18)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Confirm", modifier = Modifier.size(16.dp))
                        Text("SAVE BIRTH RECORD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class PresetLocation(val name: String, val lat: Double, val lon: Double)

val PRESET_LOCATIONS = listOf(
    PresetLocation("New York City, USA", 40.7128, -74.0060),
    PresetLocation("London, United Kingdom", 51.5074, -0.1278),
    PresetLocation("Paris, France", 48.8566, 2.3522),
    PresetLocation("Tokyo, Japan", 35.6762, 139.6503),
    PresetLocation("Los Angeles, USA", 34.0522, -118.2437),
    PresetLocation("Chicago, USA", 41.8781, -87.6298),
    PresetLocation("Houston, USA", 29.7604, -95.3698),
    PresetLocation("Phoenix, USA", 33.4484, -112.0740),
    PresetLocation("Philadelphia, USA", 39.9526, -75.1652),
    PresetLocation("San Antonio, USA", 29.4241, -98.4936),
    PresetLocation("San Diego, USA", 32.7157, -117.1611),
    PresetLocation("Dallas, USA", 32.7767, -96.7970),
    PresetLocation("San Jose, USA", 37.3382, -121.8863),
    PresetLocation("Austin, USA", 30.2672, -97.7431),
    PresetLocation("Jacksonville, USA", 30.3322, -81.6557),
    PresetLocation("San Francisco, USA", 37.7749, -122.4194),
    PresetLocation("Seattle, USA", 47.6062, -122.3321),
    PresetLocation("Denver, USA", 39.7392, -104.9903),
    PresetLocation("Boston, USA", 42.3601, -71.0589),
    PresetLocation("Miami, USA", 25.7617, -80.1918),
    PresetLocation("Atlanta, USA", 33.7490, -84.3880),
    PresetLocation("Sydney, Australia", -33.8688, 151.2093),
    PresetLocation("Melbourne, Australia", -37.8136, 144.9631),
    PresetLocation("Berlin, Germany", 52.5200, 13.4050),
    PresetLocation("Munich, Germany", 48.1351, 11.5820),
    PresetLocation("Rome, Italy", 41.9028, 12.4964),
    PresetLocation("Milan, Italy", 45.4642, 9.1900),
    PresetLocation("Madrid, Spain", 40.4168, -3.7038),
    PresetLocation("Barcelona, Spain", 41.3851, 2.1734),
    PresetLocation("Vienna, Austria", 48.2082, 16.3738),
    PresetLocation("Zurich, Switzerland", 47.3769, 8.5417),
    PresetLocation("Geneva, Switzerland", 46.2044, 6.1432),
    PresetLocation("Brussels, Belgium", 50.8503, 4.3517),
    PresetLocation("Amsterdam, Netherlands", 52.3676, 4.9041),
    PresetLocation("Dublin, Ireland", 53.3498, -6.2603),
    PresetLocation("Copenhagen, Denmark", 55.6761, 12.5683),
    PresetLocation("Stockholm, Sweden", 59.3293, 18.0686),
    PresetLocation("Oslo, Norway", 59.9139, 10.7522),
    PresetLocation("Athens, Greece", 37.9838, 23.7275),
    PresetLocation("Cairo, Egypt", 30.0444, 31.2357),
    PresetLocation("Johannesburg, South Africa", -26.2041, 28.0473),
    PresetLocation("Cape Town, South Africa", -33.9249, 18.4241),
    PresetLocation("Dubai, United Arab Emirates", 25.2048, 55.2708),
    PresetLocation("Mumbai, India", 19.0760, 72.8777),
    PresetLocation("New Delhi, India", 28.6139, 77.2090),
    PresetLocation("Singapore", 1.3521, 103.8198),
    PresetLocation("Hong Kong", 22.3193, 114.1694),
    PresetLocation("Beijing, China", 39.9042, 116.4074),
    PresetLocation("Shanghai, China", 31.2304, 121.4737),
    PresetLocation("Moscow, Russia", 55.7558, 37.6173),
    PresetLocation("Rio de Janeiro, Brazil", -22.9068, -43.1729),
    PresetLocation("Sao Paulo, Brazil", -23.5505, -46.6333),
    PresetLocation("Buenos Aires, Argentina", -34.6037, -58.3816),
    PresetLocation("Toronto, Canada", 43.6532, -79.3832),
    PresetLocation("Vancouver, Canada", 49.2827, -123.1207),
    PresetLocation("Montreal, Canada", 45.5017, -73.5673),
    PresetLocation("Mexico City, Mexico", 19.4326, -99.1332),
    PresetLocation("Istanbul, Turkey", 41.0082, 28.9784),
    PresetLocation("Seoul, South Korea", 37.5665, 126.9780),
    PresetLocation("Bangkok, Thailand", 13.7563, 100.5018),
    PresetLocation("Manila, Philippines", 14.5995, 120.9842),
    PresetLocation("Jakarta, Indonesia", -6.2088, 106.8456),
    PresetLocation("Reykjavik, Iceland", 64.1466, -21.9426),
    PresetLocation("Honolulu, Hawaii, USA", 21.3069, -157.8583),
    PresetLocation("Anchorage, Alaska, USA", 61.2181, -149.9003)
)
