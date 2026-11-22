package com.sakuva.app.feature.common

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.sakuva.app.core.locale.LocalStrings

@Composable
fun FeedbackEffect(
    feedback: Feedback?,
    snackbarHostState: SnackbarHostState,
    onConsumed: () -> Unit,
) {
    val strings = LocalStrings.current
    LaunchedEffect(feedback) {
        if (feedback != null) {
            snackbarHostState.showSnackbar(strings.messageFor(feedback))
            onConsumed()
        }
    }
}
