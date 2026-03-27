package com.musify.ui.screens.user.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.data.api.MusifyApiService
import com.musify.data.models.Album
import com.musify.data.models.Artist
import com.musify.data.models.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val trendingSongs: List<Song> = emptyList(),
    val trendingArtists: List<Artist> = emptyList(),
    val autocompleteResults: List<String> = emptyList(),
    val hasSearched: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val apiService: MusifyApiService
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _queryFlow = MutableStateFlow("")
    private val gson = Gson()

    init {
        loadTrending()
        observeQuery()
    }

    private fun observeQuery() {
        viewModelScope.launch {
            _queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun onQueryChanged(query: String) {
        _state.value = _state.value.copy(query = query)
        _queryFlow.value = query
        if (query.isBlank()) {
            _state.value = _state.value.copy(
                songs = emptyList(),
                artists = emptyList(),
                albums = emptyList(),
                autocompleteResults = emptyList(),
                hasSearched = false,
                errorMessage = null
            )
        }
    }

    fun clearSearch() {
        _state.value = _state.value.copy(
            query = "",
            songs = emptyList(),
            artists = emptyList(),
            albums = emptyList(),
            autocompleteResults = emptyList(),
            hasSearched = false,
            errorMessage = null
        )
        _queryFlow.value = ""
    }

    private fun loadTrending() {
        viewModelScope.launch {
            try {
                val response = apiService.getTrending(limit = 10)
                if (response.isSuccessful) {
                    val body = response.body() ?: return@launch
                    val songs = parseList<Song>(body["songs"])
                    val artists = parseList<Artist>(body["artists"])
                    _state.value = _state.value.copy(
                        trendingSongs = songs,
                        trendingArtists = artists
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Failed to load trending content"
                )
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val request = mapOf<String, Any>("query" to query)
                val response = apiService.search(request)

                if (response.isSuccessful) {
                    val body = response.body() ?: emptyMap()
                    val songs = parseList<Song>(body["songs"])
                    val artists = parseList<Artist>(body["artists"])
                    val albums = parseList<Album>(body["albums"])

                    _state.value = _state.value.copy(
                        isLoading = false,
                        songs = songs,
                        artists = artists,
                        albums = albums,
                        hasSearched = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Search failed",
                        hasSearched = true
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Network error. Please try again.",
                    hasSearched = true
                )
            }
        }
    }

    private inline fun <reified T> parseList(data: Any?): List<T> {
        if (data == null) return emptyList()
        val json = gson.toJson(data)
        val type = TypeToken.getParameterized(List::class.java, T::class.java).type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
