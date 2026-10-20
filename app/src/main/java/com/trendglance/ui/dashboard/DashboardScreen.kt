package com.trendglance.ui.dashboard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.trendglance.widget.TrendGlanceWidgetReceiver
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trendglance.data.local.UserPreferences
import com.trendglance.data.model.TrendingTopic

/**
 * Main Configuration Dashboard Screen allowing real-time widget customization, location/category pickers,
 * WorkManager refresh interval configuration, and interactive live widget preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSignOut: () -> Unit
) {
    val userPrefs by viewModel.userPreferences.collectAsState()
    val previewTopics by viewModel.previewTopics.collectAsState()
    val isLoading by viewModel.isLoadingFeed.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TrendGlance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = userPrefs.getGeoDisplayCountryName(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.triggerManualSync() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Feed"
                        )
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Account"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            WidgetLivePreviewCard(
                userPrefs = userPrefs,
                topics = previewTopics,
                isLoading = isLoading
            )

            FeedSettingsCard(
                userPrefs = userPrefs,
                onGeoSelected = { viewModel.updateGeoRegion(it) },
                onTimePeriodSelected = { viewModel.updateTimePeriod(it) },
                onCategorySelected = { viewModel.updateCategory(it) },
                onItemCountChanged = { viewModel.updateItemCount(it) },
                onRefreshIntervalChanged = { viewModel.updateRefreshIntervalMinutes(it) }
            )

            WidgetStylingCard(
                userPrefs = userPrefs,
                onStylingUpdated = { bgHex, opacity, fontSize, textHex ->
                    viewModel.updateWidgetStyling(bgHex, opacity, fontSize, textHex)
                }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { requestPinWidget(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Widget to Home Screen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { 
                        viewModel.triggerManualSync()
                        Toast.makeText(context, "Widget Updated Successfully", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apply & Sync Widget Now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun requestPinWidget(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val myProvider = ComponentName(context, TrendGlanceWidgetReceiver::class.java)

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val successCallback = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, TrendGlanceWidgetReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
            Toast.makeText(context, "Opening Home Screen Widget prompt...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Pinning widgets is not supported by your launcher.", Toast.LENGTH_LONG).show()
        }
    } else {
        Toast.makeText(context, "Long-press your Home Screen to add TrendGlance Widget.", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun WidgetLivePreviewCard(
    userPrefs: UserPreferences,
    topics: List<TrendingTopic>,
    isLoading: Boolean
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Live Widget Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (isLoading) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { requestPinWidget(context) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add to Home",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val bgArgb = userPrefs.getResolvedBackgroundColorArgb()
            val textArgb = userPrefs.getResolvedTextColorArgb()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(bgArgb))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "▲ ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF34A853)
                            )
                            Text(
                                text = "Google Trends",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(textArgb)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(textArgb).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = userPrefs.getGeoDisplayCountryName(),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = Color(textArgb).copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(Color(textArgb).copy(alpha = 0.15f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "↻", fontSize = 14.sp, color = Color(textArgb))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val displayTopics = if (topics.isNotEmpty()) topics.take(userPrefs.itemCount)
                    else listOf(
                        TrendingTopic("Android 16 Features", "500K+", "Google releases newest Android SDK updates with advanced API capabilities.", "Today"),
                        TrendingTopic("SpaceX Orbital Mission", "200K+", "Starship completes crucial flight test phase in low Earth orbit.", "Today"),
                        TrendingTopic("Generative AI Progress", "1M+", "Major advancements announced in on-device AI efficiency and speed.", "Today")
                    ).take(userPrefs.itemCount)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        displayTopics.forEachIndexed { index, topic ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(textArgb).copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "▲ #${index + 1} ",
                                            fontSize = 11.sp,
                                            color = Color(0xFF34A853),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = topic.title,
                                            fontSize = userPrefs.fontSizeSp.sp,
                                            color = Color(textArgb),
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF34A853).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = topic.approxTraffic,
                                            fontSize = 10.sp,
                                            color = Color(0xFF34A853),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (topic.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Breakdown: ${topic.description}",
                                        fontSize = 10.sp,
                                        color = Color(textArgb).copy(alpha = 0.70f),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedSettingsCard(
    userPrefs: UserPreferences,
    onGeoSelected: (String) -> Unit,
    onTimePeriodSelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onItemCountChanged: (Int) -> Unit,
    onRefreshIntervalChanged: (Int) -> Unit
) {
    val geoRegions = mapOf(
        "US" to "United States (US)",
        "GB" to "United Kingdom (GB)",
        "IN" to "India (IN)",
        "CA" to "Canada (CA)",
        "AU" to "Australia (AU)",
        "DE" to "Germany (DE)",
        "FR" to "France (FR)",
        "JP" to "Japan (JP)",
        "BR" to "Brazil (BR)"
    )

    val categories = mapOf(
        "all" to "All Categories",
        "b" to "Business",
        "t" to "Technology",
        "e" to "Entertainment",
        "s" to "Sports"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Feed Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            var geoExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = geoExpanded,
                onExpandedChange = { geoExpanded = !geoExpanded }
            ) {
                OutlinedTextField(
                    value = geoRegions[userPrefs.geoRegion] ?: userPrefs.geoRegion,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Country / Location") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = geoExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = geoExpanded,
                    onDismissRequest = { geoExpanded = false }
                ) {
                    geoRegions.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                onGeoSelected(code)
                                geoExpanded = false
                            }
                        )
                    }
                }
            }

            var catExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = catExpanded,
                onExpandedChange = { catExpanded = !catExpanded }
            ) {
                OutlinedTextField(
                    value = categories[userPrefs.category] ?: userPrefs.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Google Trends Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = catExpanded,
                    onDismissRequest = { catExpanded = false }
                ) {
                    categories.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                onCategorySelected(code)
                                catExpanded = false
                            }
                        )
                    }
                }
            }

            Column {
                Text(
                    text = "Widget Item Count",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 5).forEach { count ->
                        FilterChip(
                            selected = userPrefs.itemCount == count,
                            onClick = { onItemCountChanged(count) },
                            label = { Text("Top $count Items") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            val intervalLabels = mapOf(
                30 to "30 Minutes",
                60 to "1 Hour",
                180 to "3 Hours",
                360 to "6 Hours",
                720 to "12 Hours",
                1440 to "24 Hours"
            )
            var intervalExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = intervalExpanded,
                onExpandedChange = { intervalExpanded = !intervalExpanded }
            ) {
                OutlinedTextField(
                    value = intervalLabels[userPrefs.refreshIntervalMinutes] ?: "${userPrefs.refreshIntervalMinutes} Minutes",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Refresh Interval") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = intervalExpanded,
                    onDismissRequest = { intervalExpanded = false }
                ) {
                    intervalLabels.forEach { (minutes, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onRefreshIntervalChanged(minutes)
                                intervalExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetStylingCard(
    userPrefs: UserPreferences,
    onStylingUpdated: (String, Float, Int, String) -> Unit
) {
    val bgColors = listOf(
        "#1E1E2E" to "Dark Navy",
        "#121212" to "Charcoal",
        "#064E3B" to "Emerald",
        "#1E3A8A" to "Royal Blue",
        "#3B0764" to "Purple",
        "#881337" to "Crimson",
        "#FFFFFF" to "White"
    )

    val textColors = listOf(
        "#FFFFFF" to "White",
        "#000000" to "Black",
        "#FACC15" to "Gold",
        "#93C5FD" to "Soft Blue",
        "#A7F3D0" to "Mint"
    )

    var selectedBgHex by remember(userPrefs) { mutableStateOf(userPrefs.backgroundColorHex) }
    var selectedOpacity by remember(userPrefs) { mutableFloatStateOf(userPrefs.backgroundOpacity) }
    var selectedFontSize by remember(userPrefs) { mutableIntStateOf(userPrefs.fontSizeSp) }
    var selectedTextHex by remember(userPrefs) { mutableStateOf(userPrefs.textColorHex) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Widget UI Customization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = "Background Color",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bgColors.forEach { (hex, name) ->
                        val colorInt = android.graphics.Color.parseColor(hex)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .border(
                                    width = if (selectedBgHex.equals(
                                            hex,
                                            ignoreCase = true
                                        )
                                    ) 3.dp else 1.dp,
                                    color = if (selectedBgHex.equals(
                                            hex,
                                            ignoreCase = true
                                        )
                                    ) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedBgHex = hex
                                    onStylingUpdated(
                                        selectedBgHex,
                                        selectedOpacity,
                                        selectedFontSize,
                                        selectedTextHex
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedBgHex.equals(hex, ignoreCase = true)) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = name,
                                    tint = if (hex == "#FFFFFF") Color.Black else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Background Opacity",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(selectedOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = selectedOpacity,
                    onValueChange = {
                        selectedOpacity = it
                        onStylingUpdated(
                            selectedBgHex,
                            selectedOpacity,
                            selectedFontSize,
                            selectedTextHex
                        )
                    },
                    valueRange = 0.2f..1.0f
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Topic Title Font Size",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$selectedFontSize sp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = selectedFontSize.toFloat(),
                    onValueChange = {
                        selectedFontSize = it.toInt()
                        onStylingUpdated(
                            selectedBgHex,
                            selectedOpacity,
                            selectedFontSize,
                            selectedTextHex
                        )
                    },
                    valueRange = 12f..20f,
                    steps = 7
                )
            }

            Column {
                Text(
                    text = "Widget Text Color",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    textColors.forEach { (hex, name) ->
                        val colorInt = android.graphics.Color.parseColor(hex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .border(
                                    width = if (selectedTextHex.equals(
                                            hex,
                                            ignoreCase = true
                                        )
                                    ) 3.dp else 1.dp,
                                    color = if (selectedTextHex.equals(
                                            hex,
                                            ignoreCase = true
                                        )
                                    ) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedTextHex = hex
                                    onStylingUpdated(
                                        selectedBgHex,
                                        selectedOpacity,
                                        selectedFontSize,
                                        selectedTextHex
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedTextHex.equals(hex, ignoreCase = true)) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = name,
                                    tint = if (hex == "#FFFFFF" || hex == "#A7F3D0") Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
