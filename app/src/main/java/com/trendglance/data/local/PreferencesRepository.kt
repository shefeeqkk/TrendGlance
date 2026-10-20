package com.trendglance.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "trend_glance_prefs")

/**
 * Repository implementation for storing and reading user configuration from Jetpack DataStore Preferences.
 */
class PreferencesRepository(private val context: Context) {

    private object Keys {
        val GEO_REGION = stringPreferencesKey("geo_region")
        val TIME_PERIOD = stringPreferencesKey("time_period")
        val CATEGORY = stringPreferencesKey("category")
        val ITEM_COUNT = intPreferencesKey("item_count")
        val REFRESH_INTERVAL_MINUTES = intPreferencesKey("refresh_interval_minutes")
        val BACKGROUND_COLOR_HEX = stringPreferencesKey("background_color_hex")
        val BACKGROUND_OPACITY = floatPreferencesKey("background_opacity")
        val FONT_SIZE_SP = intPreferencesKey("font_size_sp")
        val TEXT_COLOR_HEX = stringPreferencesKey("text_color_hex")
        val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    /**
     * Flow emitting updated user preferences whenever any configuration value changes in DataStore.
     */
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            geoRegion = prefs[Keys.GEO_REGION] ?: "US",
            timePeriod = prefs[Keys.TIME_PERIOD] ?: "24h",
            category = prefs[Keys.CATEGORY] ?: "all",
            itemCount = prefs[Keys.ITEM_COUNT] ?: 5,
            refreshIntervalMinutes = prefs[Keys.REFRESH_INTERVAL_MINUTES] ?: 180,
            backgroundColorHex = prefs[Keys.BACKGROUND_COLOR_HEX] ?: "#1E1E2E",
            backgroundOpacity = prefs[Keys.BACKGROUND_OPACITY] ?: 0.90f,
            fontSizeSp = prefs[Keys.FONT_SIZE_SP] ?: 14,
            textColorHex = prefs[Keys.TEXT_COLOR_HEX] ?: "#FFFFFF",
            isAuthenticated = prefs[Keys.IS_AUTHENTICATED] ?: false,
            userName = prefs[Keys.USER_NAME],
            userEmail = prefs[Keys.USER_EMAIL]
        )
    }

    suspend fun updateGeoRegion(geoRegion: String) {
        context.dataStore.edit { prefs -> prefs[Keys.GEO_REGION] = geoRegion }
    }

    suspend fun updateTimePeriod(timePeriod: String) {
        context.dataStore.edit { prefs -> prefs[Keys.TIME_PERIOD] = timePeriod }
    }

    suspend fun updateCategory(category: String) {
        context.dataStore.edit { prefs -> prefs[Keys.CATEGORY] = category }
    }

    suspend fun updateItemCount(itemCount: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.ITEM_COUNT] = itemCount }
    }

    suspend fun updateRefreshIntervalMinutes(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.REFRESH_INTERVAL_MINUTES] = minutes }
    }

    suspend fun updateWidgetStyling(
        backgroundColorHex: String,
        backgroundOpacity: Float,
        fontSizeSp: Int,
        textColorHex: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BACKGROUND_COLOR_HEX] = backgroundColorHex
            prefs[Keys.BACKGROUND_OPACITY] = backgroundOpacity
            prefs[Keys.FONT_SIZE_SP] = fontSizeSp
            prefs[Keys.TEXT_COLOR_HEX] = textColorHex
        }
    }

    suspend fun updateAuthState(isAuthenticated: Boolean, userName: String? = null, userEmail: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_AUTHENTICATED] = isAuthenticated
            if (userName != null) prefs[Keys.USER_NAME] = userName else prefs.remove(Keys.USER_NAME)
            if (userEmail != null) prefs[Keys.USER_EMAIL] = userEmail else prefs.remove(Keys.USER_EMAIL)
        }
    }
}
