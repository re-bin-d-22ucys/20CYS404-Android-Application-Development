package com.example.share_quote_app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.share_quote_app.viewModel.FavoritesViewModel
import com.example.share_quote_app.ui.theme.NeonBlue
import com.example.share_quote_app.ui.theme.NeonPink
import com.example.share_quote_app.R


import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onNavigateBack: () -> Unit, favoritesViewModel: FavoritesViewModel = viewModel()) {
    val favorites by favoritesViewModel.favorites.collectAsState()

    LaunchedEffect(Unit) {
        favoritesViewModel.loadFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        GlowingIcon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", glowColor = NeonBlue)
                    }
                }
            )
        },
        containerColor = Color.Black
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(favorites) { quote ->
                GlowingBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    glowColor = NeonPink,
                    cornerRadius = 16.dp,
                    spreadRadius = 4.dp,
                    blurRadius = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
Text(
                            text = quote.q,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "- ${quote.a}",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.stylish_font)),
                            textAlign = TextAlign.End,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                        IconButton(onClick = { favoritesViewModel.removeFavorite(quote) }, modifier = Modifier.align(Alignment.End)) {
                            GlowingIcon(Icons.Filled.Delete, contentDescription = "Remove from Favorites", glowColor = NeonPink)
                        }
                    }
                }
            }
        }
    }
}