package com.sakuva.app.core.designsystem

import androidx.compose.ui.graphics.Color

fun colorFromHex(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color.Gray)
