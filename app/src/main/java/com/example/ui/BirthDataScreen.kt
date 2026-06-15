package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
