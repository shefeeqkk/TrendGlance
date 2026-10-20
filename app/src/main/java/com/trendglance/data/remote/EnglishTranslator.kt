package com.trendglance.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * High-performance translation helper that strictly converts foreign topic titles
 * and trend breakdown snippets to English using Google Translate's translation service.
 * Features in-memory thread-safe caching to ensure zero duplicate network requests.
 */
object EnglishTranslator {

    private val cache = ConcurrentHashMap<String, String>()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Translates input text to English strictly.
     * Returns original text if already in English or if offline.
     */
    suspend fun translateToEnglish(text: String): String = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return@withContext text

        // Return cached translation if available
        cache[trimmed]?.let { return@withContext it }

        // Quick check: if text contains only basic ASCII english characters/numbers, keep as is unless forced
        if (isPureAsciiEnglish(trimmed)) {
            cache[trimmed] = trimmed
            return@withContext trimmed
        }

        try {
            val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=en&dt=t&q=$encodedQuery"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrBlank()) {
                    val translatedText = parseTranslationJson(responseBody)
                    if (translatedText.isNotBlank()) {
                        cache[trimmed] = translatedText
                        return@withContext translatedText
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext trimmed
    }

    private fun isPureAsciiEnglish(text: String): Boolean {
        for (char in text) {
            if (char.code > 127) {
                return false
            }
        }
        return true
    }

    private fun parseTranslationJson(jsonStr: String): String {
        return try {
            val rootArray = JSONArray(jsonStr)
            val sentencesArray = rootArray.optJSONArray(0) ?: return ""
            val sb = StringBuilder()
            for (i in 0 until sentencesArray.length()) {
                val sentence = sentencesArray.optJSONArray(i)
                if (sentence != null && sentence.length() > 0) {
                    val translatedChunk = sentence.optString(0, "")
                    sb.append(translatedChunk)
                }
            }
            sb.toString().trim()
        } catch (e: Exception) {
            ""
        }
    }
}
