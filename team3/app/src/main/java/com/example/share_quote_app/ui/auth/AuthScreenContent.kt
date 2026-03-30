package com.example.share_quote_app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.share_quote_app.R
import androidx.compose.ui.graphics.Color

@Composable
fun AuthScreenContent(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quotable",
            fontSize = 48.sp,
            fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.stylish_font)),
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        content()
    }
}