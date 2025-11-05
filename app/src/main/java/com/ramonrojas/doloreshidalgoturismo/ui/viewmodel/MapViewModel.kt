package com.ramonrojas.doloreshidalgoturismo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.ramonrojas.doloreshidalgoturismo.data.model.PlaceEntity
import com.ramonrojas.doloreshidalgoturismo.data.repository.PlaceRepository
import com.ramonrojas.doloreshidalgoturismo.utils.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel que maneja el estado del mapa y los lugares
 * Sigue el patrón MVVM (Model-View-ViewModel)
 */
class MapViewModel(
    private val repository: PlaceRepository
) : ViewModel() {

    val places: StateFlow<List<PlaceEntity>> = repository.allPlaces
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val placeStatistics: StateFlow<PlaceStatistics> = places
        .map { placesList ->
            PlaceStatistics(
                totalPlaces = placesList.size,
                favoriteCount = placesList.count { it.isFavorite },
                categoryCounts = placesList.groupingBy { it.category }.eachCount(),
                mostRecentPlace = placesList.maxByOrNull { it.createdAt }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlaceStatistics()
        )

    private val _selectedPlace = MutableStateFlow<PlaceEntity?>(null)
    val selectedPlace: StateFlow<PlaceEntity?> = _selectedPlace.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _mapCenter = MutableStateFlow(LatLng(21.1560, -100.9318))
    val mapCenter: StateFlow<LatLng> = _mapCenter.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    init {
        viewModelScope.launch {
            places.first().let { placesList ->
                if (placesList.isEmpty()) {
                    repository.insertDefaultPlaces()
                }
            }
        }
    }

    fun addPlace(
        name: String,
        description: String,
        latLng: LatLng,
        category: String,
        markerColor: String
    ) {
        viewModelScope.launch {
            try {
                val newPlace = PlaceEntity(
                    name = name,
                    description = description,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    category = category,
                    markerColor = markerColor,
                    createdAt = System.currentTimeMillis()
                )

                repository.insertPlace(newPlace)
                _showDialog.value = false

                Logger.d("Agregando nuevo lugar: $name")
                Logger.i("Lugar agregado exitosamente con ID: ${newPlace.id}")
            } catch (e: Exception) {
                Logger.e("Error al agregar lugar", e)
                _errorMessage.value = "No se pudo agregar el lugar. Intenta nuevamente."
            }
        }
    }

    fun updatePlace(place: PlaceEntity) {
        viewModelScope.launch {
            repository.updatePlace(place)
            _selectedPlace.value = null
            _showDialog.value = false
        }
    }

    fun deletePlace(place: PlaceEntity) {
        viewModelScope.launch {
            repository.deletePlace(place)
        }
    }

    fun toggleFavorite(place: PlaceEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(place.id, place.isFavorite)
        }
    }

    fun filterByCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun showAddDialog(latLng: LatLng) {
        _mapCenter.value = latLng
        _selectedPlace.value = null
        _showDialog.value = true
    }

    fun showEditDialog(place: PlaceEntity) {
        _selectedPlace.value = place
        _showDialog.value = true
    }

    fun dismissDialog() {
        _showDialog.value = false
        _selectedPlace.value = null
    }

    /**
     * Clase de datos para estadísticas
     */
    data class PlaceStatistics(
        val totalPlaces: Int = 0,
        val favoriteCount: Int = 0,
        val categoryCounts: Map<String, Int> = emptyMap(),
        val mostRecentPlace: PlaceEntity? = null
    )

}
