package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RadioDashboard(viewModel: RadioViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showUrlDialog by remember { mutableStateOf(false) }
    var inputUrl by remember { mutableStateOf(uiState.currentStreamUrl) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = "https://mixerfm.com/img/cover.png",
                            placeholder = painterResource(R.drawable.og),
                            error = painterResource(R.drawable.og),
                            contentDescription = "Mixer FM Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "Mixer FM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                BackgroundDark,
                                Color(0xFF0F0B13),
                                BackgroundDark
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. LEFT PANE - NOW PLAYING & ALBUM ART
                Column(
                    modifier = Modifier
                        .weight(0.48f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.04f),
                                        Color.White.copy(alpha = 0.01f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            // Cover Art scaled to fit the vertical space perfectly on TV
                            CoverArtSection(
                                isPlaying = uiState.isPlaying,
                                isBuffering = uiState.isBuffering,
                                artworkUrl = uiState.artworkUrl,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .fillMaxHeight(0.7f)
                                    .aspectRatio(1f),
                                onClick = { viewModel.togglePlayPause() }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Metadata display: trackTitle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = uiState.trackTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .basicMarquee()
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (viewModel.isFavorite(uiState.trackTitle, uiState.trackArtist)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Add song to bookmarks",
                                    tint = if (viewModel.isFavorite(uiState.trackTitle, uiState.trackArtist)) RadioCyan else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            viewModel.toggleFavorites(uiState.trackTitle, uiState.trackArtist)
                                        }
                                        .testTag("bookmark_favorite_btn")
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Artist name
                            Text(
                                text = uiState.trackArtist,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = RadioLightCyan,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .basicMarquee()
                            )
                        }
                    }
                }

                // 2. RIGHT PANE - BOOKMARKS & RECENT TRACKS (SIDE BY SIDE ON WIDE TV)
                Row(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // BOOKMARKS COLUMN
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.02f))
                            .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "BOOKMARKS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        if (uiState.favorites.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No bookmarks yet.\nTap heart on the left\nto save songs.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.3f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                uiState.favorites.toList().forEach { f ->
                                    val parts = f.split("|||", limit = 2)
                                    val fTitle = parts.getOrNull(0) ?: "Unknown Track"
                                    val fArtist = parts.getOrNull(1) ?: "Live Broadcast"

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = fArtist,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 10.sp,
                                                color = RadioLightCyan,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.toggleFavorites(fTitle, fArtist) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Remove bookmark",
                                                tint = Color.White.copy(alpha = 0.3f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // RECENT TRACKS COLUMN
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.02f))
                            .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "RECENT TRACKS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        if (uiState.songHistory.size <= 1) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Waiting for history...",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.3f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                uiState.songHistory.drop(1).take(5).forEach { historyItem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.2f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = historyItem.title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.8f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = historyItem.artist,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.4f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                BackgroundDark,
                                Color(0xFF0F0B13),
                                BackgroundDark
                            )
                        )
                    ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. DUAL ARTWORK & NOW PLAYING INTEGRATION WRAPPER
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(28.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.04f),
                                        Color.White.copy(alpha = 0.01f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = 24.dp, bottom = 20.dp)
                        ) {
                            // Interactive Vinyl Artwork Section (tapping here triggers play/pause)
                            CoverArtSection(
                                isPlaying = uiState.isPlaying,
                                isBuffering = uiState.isBuffering,
                                artworkUrl = uiState.artworkUrl,
                                onClick = { viewModel.togglePlayPause() }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Metadata display: trackTitle in <h2> format (fontSize = 24.sp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                            ) {
                                Text(
                                    text = uiState.trackTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .basicMarquee()
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (viewModel.isFavorite(uiState.trackTitle, uiState.trackArtist)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Add song to bookmarks",
                                    tint = if (viewModel.isFavorite(uiState.trackTitle, uiState.trackArtist)) RadioCyan else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            viewModel.toggleFavorites(uiState.trackTitle, uiState.trackArtist)
                                        }
                                        .testTag("bookmark_favorite_btn")
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Artist name in <h3> format (fontSize = 17.sp)
                            Text(
                                text = uiState.trackArtist,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                color = RadioLightCyan,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .basicMarquee()
                            )
                        }
                    }
                }

                // 3. BOOKMARKED SONGS SECTION
                if (uiState.favorites.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "BOOKMARKS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.4f),
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            uiState.favorites.toList().forEach { f ->
                                val parts = f.split("|||", limit = 2)
                                val fTitle = parts.getOrNull(0) ?: "Unknown Track"
                                val fArtist = parts.getOrNull(1) ?: "Live Broadcast"

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.03f))
                                        .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                                        .padding(vertical = 12.dp, horizontal = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = fArtist,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 11.sp,
                                                color = RadioLightCyan,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.toggleFavorites(fTitle, fArtist) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Remove bookmark",
                                                tint = Color.White.copy(alpha = 0.3f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. PREVIOUS TRACK HISTORY
                if (uiState.songHistory.size > 1) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "RECENT TRACKS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.4f),
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            // Skip index 0 as it is actively playing
                            uiState.songHistory.drop(1).take(5).forEach { historyItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = historyItem.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = historyItem.artist,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.4f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // B. STREAM SETTINGS DIALOG (CUSTOM URL CONFIGURATION)
    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("Radio Live Stream Settings", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = SurfaceDark,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Enter live Icecast stream URL to load and parse metadata in real time:",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadioCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pre-configured High-Quality Streams:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )

                    listOf(
                        "Mixer FM Stream (Icecast)" to "https://icecast.mixerfm.com:9118/mixerfm"
                    ).forEach { (label, url) ->
                        Button(
                            onClick = {
                                inputUrl = url
                                viewModel.updateStreamUrl(url)
                                showUrlDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.05f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStreamUrl(inputUrl)
                        showUrlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RadioCyan)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
fun CoverArtSection(
    isPlaying: Boolean,
    isBuffering: Boolean,
    artworkUrl: String?,
    modifier: Modifier = Modifier.fillMaxWidth(0.95f).aspectRatio(1f),
    onClick: () -> Unit
) {
    // Pulsing Backdrop Cyan Neon Glowing Aura
    val auraScale by if (isPlaying) {
        val pulseTransition = rememberInfiniteTransition(label = "aura_pulse_job")
        pulseTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Box(
        modifier = modifier
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Pulse Backdrop Screen Depth Accent (Neon Square Glow)
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .graphicsLayer {
                    scaleX = auraScale
                    scaleY = auraScale
                }
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                RadioCyan.copy(alpha = if (isPlaying) 0.35f else 0.08f),
                                Color.Transparent
                            ),
                            radius = size.maxDimension * 0.8f
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx(), 28.dp.toPx())
                    )
                }
        )

        // Square Album Cover Card
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .shadow(16.dp, RoundedCornerShape(28.dp), clip = false)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF141416))
                .border(2.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = artworkUrl ?: "https://mixerfm.com/img/cover.png",
                placeholder = painterResource(R.drawable.og),
                error = painterResource(R.drawable.og),
                contentDescription = "Cover Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dynamic Gradient Overlay to make the image blend beautifully & show depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.35f)
                            )
                        )
                    )
            )

            // Inner visual border highlights
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
            )
        }

        // Tappable floating Overlay trigger indicators when offline or buffering
        if (!isPlaying || isBuffering) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { onClick() }
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = RadioCyan,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Press Play",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}
