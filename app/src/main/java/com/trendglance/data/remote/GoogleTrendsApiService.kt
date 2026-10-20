package com.trendglance.data.remote

import com.trendglance.data.model.TrendingTopic
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Data model for sending Google idToken to backend server.
 */
data class GoogleAuthRequest(
    val idToken: String,
    val email: String? = null,
    val name: String? = null
)

/**
 * Response model received from backend server after Google Token verification.
 */
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val userId: String?,
    val email: String?,
    val name: String?
)

/**
 * Retrofit service interface for Google Trends RSS feed and backend Auth endpoint.
 */
interface GoogleTrendsApiService {

    /**
     * Fetches top trending topics for the specified country/region code.
     * Strictly forces English language using URL parameter &hl=en.
     */
    @GET("trending/rss")
    suspend fun getTrendingTopics(
        @Query("geo") geoRegion: String = "US",
        @Query("hl") language: String = "en"
    ): List<TrendingTopic>

    /**
     * Sends Google ID token to backend for MongoDB Atlas verification & user creation.
     */
    @POST("api/auth/google")
    suspend fun verifyGoogleToken(
        @Body request: GoogleAuthRequest
    ): Response<AuthResponse>
}
