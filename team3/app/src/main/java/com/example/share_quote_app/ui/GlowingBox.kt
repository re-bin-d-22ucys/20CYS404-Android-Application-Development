package com.example.share_quote_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlowingBox(
    modifier: Modifier = Modifier,
    glowColor: Color = Color.White,
    cornerRadius: Dp = 8.dp,
    spreadRadius: Dp = 4.dp,
    blurRadius: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .graphicsLayer {
                this.shadowElevation = blurRadius.toPx()
                this.ambientShadowColor = glowColor
                this.spotShadowColor = glowColor
                this.shape = shape
                this.clip = true
            }
            .background(Color.Black.copy(alpha = 0.6f), shape) // Dark background for contrast
            .padding(spreadRadius), // Padding to ensure glow is visible
        content = content
    )
}
