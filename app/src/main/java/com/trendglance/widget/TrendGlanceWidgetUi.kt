package com.trendglance.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.trendglance.data.local.UserPreferences

data class WidgetTopicItem(
    val title: String,
    val approxTraffic: String,
    val url: String?,
    val description: String
)

fun parseFormattedTopics(formatted: String?): List<WidgetTopicItem> {
    if (formatted.isNullOrBlank()) {
        return listOf(
            WidgetTopicItem("Android 16 Launch", "500K+", "https://developer.android.com", "Google releases newest Android SDK updates with advanced API capabilities."),
            WidgetTopicItem("SpaceX Starship Flight", "200K+", "https://spacex.com", "Starship completes crucial flight test phase in low Earth orbit."),
            WidgetTopicItem("Next-Gen AI Models", "1M+", "https://news.google.com", "Major advancements announced in on-device AI efficiency and speed."),
            WidgetTopicItem("Global Tech Summit 2025", "100K+", "https://trends.google.com", "Leaders convene to discuss future of cloud architecture and mobile apps."),
            WidgetTopicItem("Quantum Computing Record", "50K+", "https://trends.google.com", "Researchers achieve record qubit coherence times in room temperature tests.")
        )
    }
    return formatted.lines().mapNotNull { line ->
        val parts = line.split("|")
        if (parts.isNotEmpty() && parts[0].isNotBlank()) {
            val title = parts[0]
            val approxTraffic = parts.getOrNull(1) ?: "Trending"
            val url = parts.getOrNull(2)
            val desc = parts.getOrNull(3)
            val description = if (!desc.isNullOrBlank()) desc else "Real-time search volume and news updates."
            WidgetTopicItem(
                title = title,
                approxTraffic = approxTraffic,
                url = url,
                description = description
            )
        } else null
    }
}

/**
 * Responsive Google Widget UI layout adapting to 2x2, 2x3, 2x4, and 4x4 sizes.
 * Shows exact user-configured item count (Top 3 or Top 5) with Google Trends up-arrows and trend breakdown snippets.
 */
@Composable
fun TrendGlanceWidgetContent(
    userPrefs: UserPreferences,
    topics: List<WidgetTopicItem>,
    lastUpdated: String?
) {
    val size = LocalSize.current
    val context = LocalContext.current

    val backgroundColor = Color(userPrefs.getResolvedBackgroundColorArgb())
    val textColor = Color(userPrefs.getResolvedTextColorArgb())
    val fontSizeSp = userPrefs.fontSizeSp.sp

    val isSmallSquare = size.width < 180.dp && size.height < 180.dp
    val isTallNarrow = size.width < 180.dp && size.height >= 180.dp

    val maxCount = userPrefs.itemCount.coerceAtLeast(1)
    val sourceTopics = if (topics.isNotEmpty()) topics else parseFormattedTopics(null)
    val displayTopics = sourceTopics.take(maxCount)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ColorProvider(day = backgroundColor, night = backgroundColor))
            .cornerRadius(18.dp)
            .padding(10.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            GoogleWidgetHeader(
                userPrefs = userPrefs,
                lastUpdated = lastUpdated,
                textColor = textColor,
                isCompact = isSmallSquare || isTallNarrow
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (displayTopics.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No trending topics found",
                        style = TextStyle(
                            color = ColorProvider(day = textColor, night = textColor),
                            fontSize = fontSizeSp
                        )
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    itemsIndexed(displayTopics) { index, topic ->
                        GoogleWidgetTopicRow(
                            index = index,
                            topic = topic,
                            textColor = textColor,
                            fontSizeSp = fontSizeSp,
                            showBreakdown = !isSmallSquare,
                            context = context
                        )
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GoogleWidgetHeader(
    userPrefs: UserPreferences,
    lastUpdated: String?,
    textColor: Color,
    isCompact: Boolean
) {
    val googleGreen = Color(0xFF34A853)
    val primaryColorProvider = ColorProvider(day = textColor, night = textColor)
    val dimColorProvider = ColorProvider(day = textColor.copy(alpha = 0.7f), night = textColor.copy(alpha = 0.7f))
    val pillBgProvider = ColorProvider(day = textColor.copy(alpha = 0.12f), night = textColor.copy(alpha = 0.12f))

    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = "▲ ",
                style = TextStyle(
                    color = ColorProvider(day = googleGreen, night = googleGreen),
                    fontSize = if (isCompact) 13.sp else 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Google Trends",
                style = TextStyle(
                    color = primaryColorProvider,
                    fontSize = if (isCompact) 13.sp else 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Box(
                modifier = GlanceModifier
                    .background(pillBgProvider)
                    .cornerRadius(6.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = userPrefs.getGeoDisplayCountryName(),
                    style = TextStyle(
                        color = dimColorProvider,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            if (lastUpdated != null && !isCompact) {
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = "• $lastUpdated",
                    style = TextStyle(
                        color = dimColorProvider,
                        fontSize = 10.sp
                    )
                )
            }
        }

        Box(
            modifier = GlanceModifier
                .size(28.dp)
                .background(pillBgProvider)
                .cornerRadius(14.dp)
                .clickable(actionRunCallback<WidgetRefreshCallback>()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "↻",
                style = TextStyle(
                    color = primaryColorProvider,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun GoogleWidgetTopicRow(
    index: Int,
    topic: WidgetTopicItem,
    textColor: Color,
    fontSizeSp: androidx.compose.ui.unit.TextUnit,
    showBreakdown: Boolean,
    context: Context
) {
    val googleGreen = Color(0xFF34A853)
    val clickIntent = if (!topic.url.isNullOrBlank()) {
        Intent(Intent.ACTION_VIEW, Uri.parse(topic.url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    } else {
        context.packageManager.getLaunchIntentForPackage(context.packageName)
    }

    val rowBgProvider = ColorProvider(day = textColor.copy(alpha = 0.08f), night = textColor.copy(alpha = 0.08f))
    val textProvider = ColorProvider(day = textColor, night = textColor)
    val mutedProvider = ColorProvider(day = textColor.copy(alpha = 0.70f), night = textColor.copy(alpha = 0.70f))
    val trafficBgProvider = ColorProvider(day = googleGreen.copy(alpha = 0.15f), night = googleGreen.copy(alpha = 0.15f))
    val trafficTextProvider = ColorProvider(day = googleGreen, night = googleGreen)

    val modifier = if (clickIntent != null) {
        GlanceModifier
            .fillMaxWidth()
            .background(rowBgProvider)
            .cornerRadius(10.dp)
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .clickable(actionStartActivity(clickIntent))
    } else {
        GlanceModifier
            .fillMaxWidth()
            .background(rowBgProvider)
            .cornerRadius(10.dp)
            .padding(vertical = 6.dp, horizontal = 8.dp)
    }

    Column(modifier = modifier) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "▲ #${index + 1} ",
                style = TextStyle(
                    color = ColorProvider(day = googleGreen, night = googleGreen),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = topic.title,
                maxLines = 1,
                style = TextStyle(
                    color = textProvider,
                    fontSize = fontSizeSp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            Spacer(modifier = GlanceModifier.width(6.dp))

            Box(
                modifier = GlanceModifier
                    .background(trafficBgProvider)
                    .cornerRadius(6.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = topic.approxTraffic,
                    style = TextStyle(
                        color = trafficTextProvider,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        if (showBreakdown && topic.description.isNotBlank()) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Breakdown: ${topic.description}",
                maxLines = 2,
                style = TextStyle(
                    color = mutedProvider,
                    fontSize = 10.sp
                )
            )
        }
    }
}
