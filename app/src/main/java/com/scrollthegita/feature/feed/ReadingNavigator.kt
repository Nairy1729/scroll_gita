package com.scrollthegita.feature.feed

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Activity-scoped request so Chapters and Saved can return to a shloka without recreating the feed. */
@HiltViewModel
class ReadingNavigator @Inject constructor() : ViewModel() {
    private val _request = MutableStateFlow<String?>(null)
    val request: StateFlow<String?> = _request.asStateFlow()

    fun open(shlokaId: String) {
        _request.value = shlokaId
    }

    fun consume() {
        _request.value = null
    }
}
