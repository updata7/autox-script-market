package org.autojs.autojs.ui.compose.util

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefreshState


@Composable
fun InvisibleSwipeRefreshIndicator(
    state: SwipeRefreshState,
    refreshTriggerDistance: Dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (state.isRefreshing) 1f else 0f,
        animationSpec = TweenSpec(durationMillis = 300), label = ""
    )

    Box(
        modifier = Modifier
            .padding(0.dp)
            .size(0.dp) // Set the size to 0 to hide the indicator
    ) {
        // Optionally add a dummy Composable if you need a placeholder
        // This is empty to ensure no visual content
    }
}