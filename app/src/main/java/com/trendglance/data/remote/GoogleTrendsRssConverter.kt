package com.trendglance.data.remote

import android.util.Xml
import com.trendglance.data.model.TrendingTopic
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.InputStream
import java.lang.reflect.Type

/**
 * Custom Retrofit Converter Factory for robust XML parsing of Google Trends RSS feeds.
 * Parses standard RSS 2.0 XML and handles Google-specific namespaces like ht:approx_traffic, ht:picture, etc.
 */
class GoogleTrendsRssConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, List<TrendingTopic>> {
        return GoogleTrendsRssResponseBodyConverter()
    }

    companion object {
        fun create(): GoogleTrendsRssConverterFactory = GoogleTrendsRssConverterFactory()
    }
}

class GoogleTrendsRssResponseBodyConverter : Converter<ResponseBody, List<TrendingTopic>> {

    override fun convert(value: ResponseBody): List<TrendingTopic> {
        return value.use { body ->
            parseRssFeed(body.byteStream())
        }
    }

    private fun parseRssFeed(inputStream: InputStream): List<TrendingTopic> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, "UTF-8")

        val topics = mutableListOf<TrendingTopic>()
        var eventType = parser.eventType

        var currentTitle = ""
        var currentApproxTraffic = ""
        var currentDescription = ""
        var currentPubDate = ""
        var currentNewsTitle: String? = null
        var currentNewsSnippet: String? = null
        var currentNewsUrl: String? = null
        var currentPictureUrl: String? = null

        var insideItem = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name
                    if (tagName.equals("item", ignoreCase = true)) {
                        insideItem = true
                        currentTitle = ""
                        currentApproxTraffic = ""
                        currentDescription = ""
                        currentPubDate = ""
                        currentNewsTitle = null
                        currentNewsSnippet = null
                        currentNewsUrl = null
                        currentPictureUrl = null
                    } else if (insideItem) {
                        when (tagName.lowercase()) {
                            "title" -> currentTitle = safeReadText(parser)
                            "ht:approx_traffic", "approx_traffic" -> currentApproxTraffic = safeReadText(parser)
                            "description" -> currentDescription = safeReadText(parser)
                            "pubdate" -> currentPubDate = safeReadText(parser)
                            "ht:picture", "picture" -> currentPictureUrl = safeReadText(parser)
                            "ht:news_item_title", "news_item_title" -> if (currentNewsTitle == null) currentNewsTitle = safeReadText(parser)
                            "ht:news_item_snippet", "news_item_snippet" -> if (currentNewsSnippet == null) currentNewsSnippet = safeReadText(parser)
                            "ht:news_item_url", "news_item_url" -> if (currentNewsUrl == null) currentNewsUrl = safeReadText(parser)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tagName = parser.name
                    if (tagName.equals("item", ignoreCase = true) && insideItem) {
                        insideItem = false
                        if (currentTitle.isNotBlank()) {
                            val resolvedDesc = when {
                                !currentNewsSnippet.isNullOrBlank() -> cleanHtml(currentNewsSnippet)
                                cleanHtml(currentDescription).isNotBlank() -> cleanHtml(currentDescription)
                                !currentNewsTitle.isNullOrBlank() -> cleanHtml(currentNewsTitle)
                                else -> "Real-time Google search trend and news updates."
                            }
                            topics.add(
                                TrendingTopic(
                                    title = cleanHtml(currentTitle),
                                    approxTraffic = if (currentApproxTraffic.isNotBlank()) currentApproxTraffic else "100K+",
                                    description = resolvedDesc,
                                    pubDate = currentPubDate,
                                    newsTitle = currentNewsTitle?.let { cleanHtml(it) },
                                    newsUrl = currentNewsUrl,
                                    pictureUrl = currentPictureUrl
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return topics
    }

    private fun safeReadText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.next()
        }
        return result
    }

    private fun cleanHtml(input: String): String {
        return input
            .replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }
}
