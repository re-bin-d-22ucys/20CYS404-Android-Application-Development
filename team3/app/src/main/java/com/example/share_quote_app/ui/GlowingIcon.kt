package com.example.share_quote_app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape

@Composable
fun GlowingIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    glowColor: Color = Color.White,
    spreadRadius: Dp = 2.dp,
    blurRadius: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                this.shadowElevation = blurRadius.toPx()
                this.ambientShadowColor = glowColor
                this.spotShadowColor = glowColor
                this.shape = androidx.compose.foundation.shape.CircleShape // Assuming icon is circular
                this.clip = true
            }
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp) // Default icon size
        )
    }
}
