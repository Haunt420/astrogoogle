package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.HouseSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Instant

class BirthDataRepository(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore

    val birthDataFlow: Flow<BirthData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val epochMilli = prefs[KEY_BIRTH_EPOCH_MILLI]
            val isSet = prefs[KEY_IS_SET] ?: (epochMilli != null)
            if (epochMilli != null) {
                BirthData(
                    name = prefs[KEY_NAME] ?: "",
                    birthInstant = Instant.ofEpochMilli(epochMilli),
                    latitude = prefs[KEY_LAT] ?: 0.0,
                    longitude = prefs[KEY_LON] ?: 0.0,
                    locationName = prefs[KEY_LOC_NAME] ?: "",
                    hasLocation = prefs[KEY_HAS_LOCATION] ?: false,
                    houseSystem = prefs[KEY_HOUSE_SYSTEM]?.let {
                        try { HouseSystem.valueOf(it) } catch (e: Exception) { HouseSystem.PLACIDUS }
                    } ?: HouseSystem.PLACIDUS,
                    isSet = isSet
                )
            } else {
                BirthData()
            }
        }

    suspend fun save(data: BirthData) {
        dataStore.edit { prefs ->
            prefs[KEY_NAME] = data.name
            prefs[KEY_BIRTH_EPOCH_MILLI] = data.birthInstant.toEpochMilli()
            prefs[KEY_LAT] = data.latitude
            prefs[KEY_LON] = data.longitude
            prefs[KEY_LOC_NAME] = data.locationName
            prefs[KEY_HAS_LOCATION] = data.hasLocation
            prefs[KEY_HOUSE_SYSTEM] = data.houseSystem.name
            prefs[KEY_IS_SET] = data.isSet
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private val KEY_NAME = stringPreferencesKey("birth_name")
        private val KEY_BIRTH_EPOCH_MILLI = longPreferencesKey("birth_epoch_milli")
        private val KEY_LAT = doublePreferencesKey("birth_lat")
        private val KEY_LON = doublePreferencesKey("birth_lon")
        private val KEY_LOC_NAME = stringPreferencesKey("birth_location_name")
        private val KEY_HAS_LOCATION = booleanPreferencesKey("birth_has_location")
        private val KEY_HOUSE_SYSTEM = stringPreferencesKey("house_system")
        private val KEY_IS_SET = booleanPreferencesKey("is_birth_data_set")
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "birth_data")
