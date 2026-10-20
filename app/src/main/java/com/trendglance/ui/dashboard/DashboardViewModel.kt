package com.trendglance.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trendglance.data.local.PreferencesRepository
import com.trendglance.data.local.UserPreferences
import com.trendglance.data.model.TrendingTopic
import com.trendglance.data.repository.TrendsRepositoryImpl
import com.trendglance.worker.WorkManagerScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing the In-App Configuration Dashboard state and real-time widget previews.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = PreferencesRepository(application)
    private val trendsRepo = TrendsRepositoryImpl()

    val userPreferences: StateFlow<UserPreferences> = prefsRepo.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _previewTopics = MutableStateFlow<List<TrendingTopic>>(emptyList())
    val previewTopics: StateFlow<List<TrendingTopic>> = _previewTopics.asStateFlow()

    private val _isLoadingFeed = MutableStateFlow(false)
    val isLoadingFeed: StateFlow<Boolean> = _isLoadingFeed.asStateFlow()

    init {
        loadPreviewTopics()
    }

    fun loadPreviewTopics() {
        viewModelScope.launch {
            _isLoadingFeed.value = true
            val currentPrefs = userPreferences.value
            val result = trendsRepo.getTrendingTopics(
                geoRegion = currentPrefs.geoRegion,
                limit = currentPrefs.itemCount
            )
            result.onSuccess { topics ->
                _previewTopics.value = topics
            }
            _isLoadingFeed.value = false
        }
    }

    fun updateGeoRegion(geoRegion: String) {
        viewModelScope.launch {
            prefsRepo.updateGeoRegion(geoRegion)
            loadPreviewTopics()
            WorkManagerScheduler.triggerImmediateUpdate(getApplication())
        }
    }

    fun updateTimePeriod(timePeriod: String) {
        viewModelScope.launch {
            prefsRepo.updateTimePeriod(timePeriod)
            loadPreviewTopics()
        }
    }

    fun updateCategory(category: String) {
        viewModelScope.launch {
            prefsRepo.updateCategory(category)
            loadPreviewTopics()
        }
    }

    fun updateItemCount(itemCount: Int) {
        viewModelScope.launch {
            prefsRepo.updateItemCount(itemCount)
            loadPreviewTopics()
            WorkManagerScheduler.triggerImmediateUpdate(getApplication())
        }
    }

    fun updateRefreshIntervalMinutes(minutes: Int) {
        viewModelScope.launch {
            prefsRepo.updateRefreshIntervalMinutes(minutes)
            WorkManagerScheduler.scheduleWidgetUpdate(getApplication(), intervalMinutes = minutes)
        }
    }

    fun updateWidgetStyling(
        backgroundColorHex: String,
        backgroundOpacity: Float,
        fontSizeSp: Int,
        textColorHex: String
    ) {
        viewModelScope.launch {
            prefsRepo.updateWidgetStyling(
                backgroundColorHex = backgroundColorHex,
                backgroundOpacity = backgroundOpacity,
                fontSizeSp = fontSizeSp,
                textColorHex = textColorHex
            )
            WorkManagerScheduler.triggerImmediateUpdate(getApplication())
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            loadPreviewTopics()
            WorkManagerScheduler.triggerImmediateUpdate(getApplication())
        }
    }
}
