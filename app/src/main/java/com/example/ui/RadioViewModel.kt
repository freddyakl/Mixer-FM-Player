package com.example.ui

import android.app.Application
import android.content.Context
import android.content.ComponentName
import android.os.Build
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.playback.RadioPlaybackService
import com.example.ui.theme.RadioCyan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

data class RadioUiState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val trackTitle: String = "Mixer FM Live",
    val trackArtist: String = "Station Broadcast",
    val artworkUrl: String? = null,
    val sleepTimerMinutes: Int? = null,
    val favorites: Set<String> = emptySet(),
    val songHistory: List<SongHistoryItem> = emptyList(),
    val currentStreamUrl: String = "https://icecast.mixerfm.com:9118/mixerfm"
)

data class SongHistoryItem(
    val title: String,
    val artist: String,
    val timestamp: Long = System.currentTimeMillis()
)

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    private var exoPlayer: Player? = null
    private var sleepTimerJob: Job? = null
    private var metadataPollJob: Job? = null

    init {
        initPlayer()
        loadFavorites()
        startMetadataPolling()
    }

    @OptIn(UnstableApi::class)
    private fun initPlayer() {
        val app = getApplication<Application>()
        val context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            app.createAttributionContext("media")
        } else {
            app
        }
        val sessionToken = SessionToken(context, ComponentName(context, RadioPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(Runnable {
            try {
                val controller = controllerFuture.get()
                this.exoPlayer = controller
                
                controller.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _uiState.update { it.copy(isPlaying = playing) }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        _uiState.update { 
                            it.copy(
                                isBuffering = (state == Player.STATE_BUFFERING)
                            )
                        }
                    }

                    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                        val title = mediaMetadata.title?.toString()
                        val artist = mediaMetadata.artist?.toString() ?: mediaMetadata.subtitle?.toString()
                        updateMetadata(title, artist)
                    }
                })

                // Set or update the media item with metadata if the list is empty
                if (controller.mediaItemCount == 0) {
                    val mediaItem = MediaItem.Builder()
                        .setUri(_uiState.value.currentStreamUrl)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(_uiState.value.trackTitle)
                                .setArtist(_uiState.value.trackArtist)
                                .setArtworkUri(android.net.Uri.parse(_uiState.value.artworkUrl ?: "https://mixerfm.com/img/cover.png"))
                                .build()
                        )
                        .build()
                    controller.setMediaItem(mediaItem)
                    controller.prepare()
                    controller.play()
                } else {
                    // Update state with existing player state
                    _uiState.update { 
                        it.copy(
                            isPlaying = controller.isPlaying,
                            isBuffering = (controller.playbackState == Player.STATE_BUFFERING)
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, { command -> command.run() })
    }

    private fun updatePlayerMetadata(title: String, artist: String, artworkUrl: String?) {
        val p = exoPlayer ?: return
        val newMetadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(android.net.Uri.parse(artworkUrl ?: "https://mixerfm.com/img/cover.png"))
            .build()
            
        p.setPlaylistMetadata(newMetadata)
        
        val currentItem = p.currentMediaItem
        if (currentItem != null) {
            val updatedItem = currentItem.buildUpon()
                .setMediaMetadata(newMetadata)
                .build()
            p.replaceMediaItem(p.currentMediaItemIndex, updatedItem)
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.mediaItemCount == 0) {
                    // Refresh the stream source to ensure minimal buffer delay
                    val mediaItem = MediaItem.Builder()
                        .setUri(_uiState.value.currentStreamUrl)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(_uiState.value.trackTitle)
                                .setArtist(_uiState.value.trackArtist)
                                .setArtworkUri(android.net.Uri.parse(_uiState.value.artworkUrl ?: "https://mixerfm.com/img/cover.png"))
                                .build()
                        )
                        .build()
                    player.setMediaItem(mediaItem)
                }
                player.prepare()
                player.play()
            }
        }
    }

    fun updateStreamUrl(newUrl: String) {
        if (newUrl.isNotBlank() && newUrl != _uiState.value.currentStreamUrl) {
            _uiState.update { it.copy(currentStreamUrl = newUrl) }
            exoPlayer?.let { player ->
                val playing = player.isPlaying
                val mediaItem = MediaItem.Builder()
                    .setUri(newUrl)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(_uiState.value.trackTitle)
                            .setArtist(_uiState.value.trackArtist)
                            .setArtworkUri(android.net.Uri.parse(_uiState.value.artworkUrl ?: "https://mixerfm.com/img/cover.png"))
                            .build()
                    )
                    .build()
                player.setMediaItem(mediaItem)
                player.prepare()
                if (playing) {
                    player.play()
                }
            }
        }
    }

    private fun updateMetadata(title: String?, artist: String?) {
        val cleanTitle = if (title.isNullOrBlank()) "Mixer FM stream" else title.trim()
        val cleanArtist = if (artist.isNullOrBlank()) "Station Broadcast" else artist.trim()

        if (_uiState.value.trackTitle != cleanTitle || _uiState.value.trackArtist != cleanArtist) {
            _uiState.update { state ->
                // Guard history list from redundant updates
                val updatedHistory = if (state.songHistory.firstOrNull()?.title != cleanTitle) {
                    val newItem = SongHistoryItem(cleanTitle, cleanArtist)
                    listOf(newItem) + state.songHistory.take(19)
                } else {
                    state.songHistory
                }

                state.copy(
                    trackTitle = cleanTitle,
                    trackArtist = cleanArtist,
                    songHistory = updatedHistory
                )
            }
            updatePlayerMetadata(cleanTitle, cleanArtist, null)
            fetchiTunesArtwork(cleanTitle, cleanArtist)
        }
    }

    private fun fetchiTunesArtwork(title: String, artist: String) {
        val isDefaultTitle = title.equals("Mixer FM stream", ignoreCase = true) || title.equals("Mixer FM live", ignoreCase = true)
        val isDefaultArtist = artist.equals("Station Broadcast", ignoreCase = true)
        if (title.isBlank() || artist.isBlank() || isDefaultTitle || isDefaultArtist) {
            _uiState.update { it.copy(artworkUrl = null) }
            updatePlayerMetadata(title, artist, null)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = java.net.URLEncoder.encode("$artist $title", "UTF-8")
                val url = URL("https://itunes.apple.com/search?term=$query&entity=song&limit=1")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(responseText)
                val results = json.optJSONArray("results")
                if (results != null && mapOf("results" to results).isNotEmpty() && results.length() > 0) {
                    val firstResult = results.getJSONObject(0)
                    var artworkUrl = firstResult.optString("artworkUrl100", null)
                    if (artworkUrl != null) {
                        artworkUrl = artworkUrl.replace("100x100bb", "800x800bb")
                    }
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(artworkUrl = artworkUrl) }
                        updatePlayerMetadata(title, artist, artworkUrl)
                    }
                } else {
                    val cleanedTitle = title.split("-", "(", "[", "/").first().trim()
                    val fallbackQuery = java.net.URLEncoder.encode("$artist $cleanedTitle", "UTF-8")
                    val fallbackUrl = URL("https://itunes.apple.com/search?term=$fallbackQuery&entity=song&limit=1")
                    val fallbackConnection = fallbackUrl.openConnection() as java.net.HttpURLConnection
                    fallbackConnection.connectTimeout = 5000
                    fallbackConnection.readTimeout = 5000
                    
                    val fallbackResponse = fallbackConnection.inputStream.bufferedReader().use { it.readText() }
                    val fallbackJson = org.json.JSONObject(fallbackResponse)
                    val fallbackResults = fallbackJson.optJSONArray("results")
                    if (fallbackResults != null && fallbackResults.length() > 0) {
                        val firstResult = fallbackResults.getJSONObject(0)
                        var artworkUrl = firstResult.optString("artworkUrl100", null)
                        if (artworkUrl != null) {
                            artworkUrl = artworkUrl.replace("100x100bb", "800x800bb")
                        }
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(artworkUrl = artworkUrl) }
                            updatePlayerMetadata(title, artist, artworkUrl)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(artworkUrl = null) }
                            updatePlayerMetadata(title, artist, null)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(artworkUrl = null) }
                    updatePlayerMetadata(title, artist, null)
                }
            }
        }
    }

    // Explicitly parse ICY Metadata from direct http stream header
    private fun startMetadataPolling() {
        metadataPollJob?.cancel()
        metadataPollJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val streamUrl = _uiState.value.currentStreamUrl
                val icyMetadata = fetchIcyMetadataDirectly(streamUrl)
                if (icyMetadata != null) {
                    withContext(Dispatchers.Main) {
                        updateMetadata(icyMetadata.first, icyMetadata.second)
                    }
                }
                delay(15000) // Poll stream every 15 seconds
            }
        }
    }

    private fun fetchIcyMetadataDirectly(streamUrl: String): Pair<String, String>? {
        try {
            val url = URL(streamUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.setRequestProperty("Icy-Metadata", "1")
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            
            val metaIntStr = connection.getHeaderField("icy-metaint")
            if (metaIntStr != null) {
                val metaInt = metaIntStr.toIntOrNull() ?: return null
                val inputStream = connection.inputStream
                
                // Skip initial audio segment
                val skipBuffer = ByteArray(metaInt)
                var bytesRead = 0
                while (bytesRead < metaInt) {
                    val read = inputStream.read(skipBuffer, bytesRead, metaInt - bytesRead)
                    if (read == -1) break
                    bytesRead += read
                }
                if (bytesRead < metaInt) {
                    inputStream.close()
                    return null
                }
                
                // Read dynamic metadata length byte
                val lengthByte = inputStream.read()
                if (lengthByte > 0) {
                    val metaLength = lengthByte * 16
                    val metaBuffer = ByteArray(metaLength)
                    var metaBytesRead = 0
                    while (metaBytesRead < metaLength) {
                        val read = inputStream.read(metaBuffer, metaBytesRead, metaLength - metaBytesRead)
                        if (read == -1) break
                        metaBytesRead += read
                    }
                    inputStream.close()
                    
                    val metaString = String(metaBuffer, 0, metaBytesRead, Charsets.UTF_8).trim()
                    if (metaString.contains("StreamTitle='")) {
                        val start = metaString.indexOf("StreamTitle='") + "StreamTitle='".length
                        val end = metaString.indexOf("';", start)
                        if (end > start) {
                            val fullMetadata = metaString.substring(start, end).trim()
                            val parts = fullMetadata.split(" - ", limit = 2)
                            return if (parts.size == 2) {
                                Pair(parts[1].trim(), parts[0].trim()) // Title, Artist
                            } else {
                                Pair(fullMetadata, "Radio 61 Live")
                            }
                        }
                    }
                }
                inputStream.close()
            }
        } catch (e: Exception) {
            // Silence exceptions to avoid disrupting main thread
        }
        return null
    }

    // Sleep Timer logic
    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _uiState.update { it.copy(sleepTimerMinutes = minutes) }
        
        if (minutes != null) {
            sleepTimerJob = viewModelScope.launch {
                var remaining = minutes
                while (remaining > 0) {
                    delay(60000) // tick every minute
                    remaining--
                    _uiState.update { it.copy(sleepTimerMinutes = remaining) }
                }
                // Pause player safely on completion
                exoPlayer?.pause()
                _uiState.update { it.copy(sleepTimerMinutes = null) }
            }
        }
    }

    // Favorites persistence in SharedPreferences
    private fun loadFavorites() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("Radio61Prefs", Context.MODE_PRIVATE)
        val favoritesSet = sharedPrefs.getStringSet("favorite_tracks2", emptySet()) ?: emptySet()
        _uiState.update { it.copy(favorites = favoritesSet) }
    }

    fun isFavorite(title: String, artist: String): Boolean {
        return _uiState.value.favorites.contains("$title|||$artist")
    }

    fun toggleFavorites(title: String, artist: String) {
        val target = "$title|||$artist"
        val updatedSet = _uiState.value.favorites.toMutableSet().apply {
            if (contains(target)) remove(target) else add(target)
        }
        
        val sharedPrefs = getApplication<Application>().getSharedPreferences("Radio61Prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putStringSet("favorite_tracks2", updatedSet).apply()
        _uiState.update { it.copy(favorites = updatedSet) }
    }

    override fun onCleared() {
        sleepTimerJob?.cancel()
        metadataPollJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
        super.onCleared()
    }
}
