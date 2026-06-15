package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BirthData
import com.example.data.BirthDataRepository
import com.example.model.HouseSystem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class BirthDataViewModel(application: Application) : AndroidViewModel(application) {

    private val birthDataRepo = BirthDataRepository(application)

    val birthData: StateFlow<BirthData> = birthDataRepo.birthDataFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BirthData())

    // Form states
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _birthDateTimeVal = MutableStateFlow<LocalDateTime>(LocalDateTime.of(1985, 3, 15, 12, 0))
    val birthDateTimeVal = _birthDateTimeVal.asStateFlow()

    private val _latitudeVal = MutableStateFlow("40.7128") // NYC default as user-friendly hint
    val latitudeVal = _latitudeVal.asStateFlow()

    private val _longitudeVal = MutableStateFlow("-74.0060")
    val longitudeVal = _longitudeVal.asStateFlow()

    private val _locationNameVal = MutableStateFlow("New York City, NY")
    val locationNameVal = _locationNameVal.asStateFlow()

    private val _hasLocationVal = MutableStateFlow(true)
    val hasLocationVal = _hasLocationVal.asStateFlow()

    private val _houseSystemVal = MutableStateFlow(HouseSystem.PLACIDUS)
    val houseSystemVal = _houseSystemVal.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg = _errorMsg.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    init {
        // Hydrate from repository values once ready
        viewModelScope.launch {
            birthData.collect { data ->
                if (data.isSet) {
                    _name.value = data.name
                    val ldt = LocalDateTime.ofInstant(data.birthInstant, ZoneOffset.UTC)
                    _birthDateTimeVal.value = ldt
                    _latitudeVal.value = data.latitude.toString()
                    _longitudeVal.value = data.longitude.toString()
                    _locationNameVal.value = data.locationName
                    _hasLocationVal.value = data.hasLocation
                    _houseSystemVal.value = data.houseSystem
                }
            }
        }
    }

    fun setName(value: String) {
        _name.value = value
    }

    fun setBirthDateTime(value: LocalDateTime) {
        _birthDateTimeVal.value = value
    }

    fun setLatitude(value: String) {
        _latitudeVal.value = value
    }

    fun setLongitude(value: String) {
        _longitudeVal.value = value
    }

    fun setLocationName(value: String) {
        _locationNameVal.value = value
    }

    fun setHasLocation(value: Boolean) {
        _hasLocationVal.value = value
    }

    fun setHouseSystem(value: HouseSystem) {
        _houseSystemVal.value = value
    }

    fun saveBirthData() {
        val lat = if (_hasLocationVal.value) _latitudeVal.value.toDoubleOrNull() else 0.0
        val lon = if (_hasLocationVal.value) _longitudeVal.value.toDoubleOrNull() else 0.0

        if (_hasLocationVal.value) {
            if (lat == null || lat < -90.0 || lat > 90.0) {
                _errorMsg.value = "Latitude must be a valid number between -90 and 90."
                return
            }
            if (lon == null || lon < -180.0 || lon > 180.0) {
                _errorMsg.value = "Longitude must be a valid number between -180 and 180."
                return
            }
        }

        val instant = _birthDateTimeVal.value.toInstant(ZoneOffset.UTC)
        if (instant.isBefore(Instant.parse("1500-01-01T00:00:00Z")) || 
            instant.isAfter(Instant.parse("2100-12-31T23:59:59Z"))) {
            _errorMsg.value = "Birth date must be between 1500 AD and 2100 AD."
            return
        }

        _errorMsg.value = null

        viewModelScope.launch {
            val newData = BirthData(
                name = _name.value.trim(),
                birthInstant = instant,
                latitude = lat ?: 0.0,
                longitude = lon ?: 0.0,
                locationName = _locationNameVal.value.trim(),
                hasLocation = _hasLocationVal.value,
                houseSystem = _houseSystemVal.value,
                isSet = true
            )
            birthDataRepo.save(newData)
            _saveSuccess.emit(true)
        }
    }

    fun clearBirthData() {
        viewModelScope.launch {
            birthDataRepo.clear()
            _name.value = ""
            _birthDateTimeVal.value = LocalDateTime.of(1985, 3, 15, 12, 0)
            _latitudeVal.value = "40.7128"
            _longitudeVal.value = "-74.0060"
            _locationNameVal.value = "New York City, NY"
            _hasLocationVal.value = true
            _houseSystemVal.value = HouseSystem.PLACIDUS
            _errorMsg.value = null
        }
    }

    fun clearError() {
        _errorMsg.value = null
    }
}
