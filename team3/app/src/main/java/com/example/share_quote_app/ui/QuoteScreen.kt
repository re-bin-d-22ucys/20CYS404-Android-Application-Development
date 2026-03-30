package com.example.share_quote_app.ui

import android.util.Log
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.share_quote_app.Quote
import com.example.share_quote_app.QuoteManager
import com.example.share_quote_app.viewModel.FavoritesViewModel
import com.example.share_quote_app.ui.theme.NeonBlue
import com.example.share_quote_app.ui.theme.NeonGreen
import com.example.share_quote_app.ui.theme.NeonPink
import androidx.compose.ui.res.fontResource
import com.example.share_quote_app.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import com.example.share_quote_app.viewModel.QuoteViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteScreen(onSignOut: () -> Unit, onNavigateToFavorites: () -> Unit) {
    val quoteViewModel: QuoteViewModel = viewModel()
    val quoteList by quoteViewModel.quoteList.collectAsState()
    val isLoading by quoteViewModel.isLoading.collectAsState()
    val currentQuoteIndex by quoteViewModel.currentQuoteIndex.collectAsState()
    val context = LocalContext.current
    val favoritesViewModel: FavoritesViewModel = viewModel()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current

    LaunchedEffect(quoteList) {
        if (quoteList.isNotEmpty() && currentQuoteIndex >= quoteList.size) {
            quoteViewModel.updateCurrentQuoteIndex(0)
        }
    }

    fun updateQuote(index: Int) {
        quoteViewModel.updateCurrentQuoteIndex(index)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Quotable", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = {
                        if (quoteList.isNotEmpty()) {
                            val quote = quoteList[currentQuoteIndex]
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("quote", "${quote.q} - ${quote.a}")
                            clipboard.setPrimaryClip(clip)
                        }
                    }) {
                        GlowingIcon(Icons.Filled.ContentCopy, contentDescription = "Copy Quote", glowColor = NeonBlue)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToFavorites) {
                        GlowingIcon(Icons.Filled.Favorite, contentDescription = "Favorites", glowColor = NeonPink)
                    }
                    IconButton(onClick = {
                        if (quoteList.isNotEmpty()) {
                            val quote = quoteList[currentQuoteIndex]
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.type = "text/plain"
                            intent.putExtra(Intent.EXTRA_TEXT, "${quote.q} - ${quote.a}")
                            context.startActivity(intent)
                        }
                    }) {
                        GlowingIcon(Icons.Filled.Share, contentDescription = "Share Quote", glowColor = NeonGreen)
                    }

                    IconButton(onClick = { showLogoutDialog = true }) {
                        GlowingIcon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", glowColor = NeonBlue)
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // CircularProgressIndicator removed temporarily due to persistent compilation error
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (quoteList.isNotEmpty()) {
                    val quote = quoteList[currentQuoteIndex]
                    var isFavorite by remember { mutableStateOf(false) }

                    LaunchedEffect(quote) { // Recalculate favorite status when quote changes
                        favoritesViewModel.isFavorite(quote) { isFav ->
                            isFavorite = isFav
                        }
                    }

                    GlowingBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        glowColor = NeonGreen,
                        cornerRadius = 16.dp,
                        spreadRadius = 8.dp,
                        blurRadius = 16.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "\"${quote.q}\"",                                fontSize = 24.sp,
                                fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.stylish_font)),
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "- ${quote.a}",
                                fontSize = 18.sp,
                                fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.stylish_font)),
                                textAlign = TextAlign.End,
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = {
                            if (quoteList.isNotEmpty()) {
                                val newIndex = if (currentQuoteIndex > 0) currentQuoteIndex - 1 else quoteList.size - 1
                                updateQuote(newIndex)
                            }
                        }) {
                            GlowingIcon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Quote", glowColor = NeonBlue, tint = NeonBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Previous", color = NeonBlue)
                        }
                        OutlinedButton(onClick = {
                            if (quoteList.isNotEmpty()) {
                                val newIndex = (currentQuoteIndex + 1) % quoteList.size
                                updateQuote(newIndex)
                            }
                        }) {
                            Text(text = "Next", color = NeonBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            GlowingIcon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Quote", glowColor = NeonBlue, tint = NeonBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(onClick = {
                        if (quoteList.isNotEmpty()) {
                            if (isFavorite) {
                                favoritesViewModel.removeFavorite(quote)
                            } else {
                                favoritesViewModel.addFavorite(quote)
                            }
                            isFavorite = !isFavorite
                        }
                    }) {
                        GlowingIcon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                            contentDescription = "Favorite",
                            glowColor = if (isFavorite) NeonPink else Color.White,
                            tint = if (isFavorite) NeonPink else Color.Gray
                        )
                    }
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(onClick = {
                        onSignOut()
                        showLogoutDialog = false
                    }) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}