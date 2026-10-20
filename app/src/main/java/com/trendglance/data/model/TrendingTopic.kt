package com.trendglance.data.model

/**
 * Clean Architecture Domain Model representing a single trending subject from Google Trends.
 */
data class TrendingTopic(
    val title: String,               // Name/keyword of the trending topic
    val approxTraffic: String,       // Search volume estimate (e.g. "500K+", "1M+")
    val description: String,          // Snippet or news summary
    val pubDate: String,             // Date string when topic started trending
    val newsTitle: String? = null,    // Primary news headline associated with trend
    val newsUrl: String? = null,      // Article URL for user tap navigation
    val pictureUrl: String? = null    // Thumbnail image URL
)
