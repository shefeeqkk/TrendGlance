package com.trendglance.data.local

/**
 * Data model representing all configurable preferences for the TrendGlance app and widget.
 * Saved locally using Jetpack DataStore Preferences.
 */
data class UserPreferences(
    val geoRegion: String = "US",               // Country/Region code (e.g. US, GB, IN, CA, AU, DE, FR, JP)
    val timePeriod: String = "24h",             // Filter period: 24h, 48h, 7d
    val category: String = "all",               // Category: all, b (Business), t (Tech), e (Entertainment), s (Sports)
    val itemCount: Int = 5,                     // Display top 3 or top 5 trending topics
    val refreshIntervalMinutes: Int = 180,      // Background refresh interval in minutes (30, 60, 180, 360, 720, 1440)
    val backgroundColorHex: String = "#1E1E2E", // Widget background color in Hex format (includes #FFFFFF White)
    val backgroundOpacity: Float = 0.90f,      // Widget background opacity slider (0.2f to 1.0f)
    val fontSizeSp: Int = 14,                   // Topic text font size in SP (12 to 20)
    val textColorHex: String = "#FFFFFF",        // Widget text color in Hex format (includes #000000 Black)
    val isAuthenticated: Boolean = false,       // Auth state: true if signed in with Google, false if guest
    val userName: String? = null,               // Display name of authenticated user
    val userEmail: String? = null               // Email of authenticated user
) {
    /**
     * Maps country codes to full display country names.
     */
    fun getGeoDisplayCountryName(): String {
        return when (geoRegion.uppercase()) {
            "US" -> "United States"
            "GB" -> "United Kingdom"
            "IN" -> "India"
            "CA" -> "Canada"
            "AU" -> "Australia"
            "DE" -> "Germany"
            "FR" -> "France"
            "JP" -> "Japan"
            "BR" -> "Brazil"
            else -> geoRegion
        }
    }

    /**
     * Resolves the background color with the user-selected opacity into an ARGB Int.
     * Supports White (#FFFFFF), Navy (#1E1E2E), Charcoal (#121212), etc.
     */
    fun getResolvedBackgroundColorArgb(): Int {
        return try {
            val cleanHex = backgroundColorHex.removePrefix("#")
            val colorInt = android.graphics.Color.parseColor("#$cleanHex")
            val alpha = (backgroundOpacity * 255).toInt().coerceIn(0, 255)
            (colorInt and 0x00FFFFFF) or (alpha shl 24)
        } catch (e: Exception) {
            android.graphics.Color.argb((backgroundOpacity * 255).toInt(), 30, 30, 46)
        }
    }

    /**
     * Resolves the text color into an ARGB Int for Glance and Compose.
     * Supports Black (#000000), White (#FFFFFF), Gold (#FACC15), etc.
     */
    fun getResolvedTextColorArgb(): Int {
        return try {
            val cleanHex = textColorHex.removePrefix("#")
            android.graphics.Color.parseColor("#$cleanHex")
        } catch (e: Exception) {
            android.graphics.Color.WHITE
        }
    }
}
