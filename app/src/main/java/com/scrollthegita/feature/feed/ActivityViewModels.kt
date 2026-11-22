package com.scrollthegita.feature.feed

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun activityReadingNavigator(): ReadingNavigator {
    val activity = LocalActivity.current as ComponentActivity
    return hiltViewModel(viewModelStoreOwner = activity)
}
