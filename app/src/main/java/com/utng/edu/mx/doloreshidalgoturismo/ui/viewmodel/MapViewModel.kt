package com.utng.edu.mx.doloreshidalgoturismo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.utng.edu.mx.doloreshidalgoturismo.data.model.PlaceEntity
import com.utng.edu.mx.doloreshidalgoturismo.data.repository.PlaceRepository
import com.utng.edu.mx.doloreshidalgoturismo.utils.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel que maneja el estado del mapa y los lugares
 * Sigue el patrón MVVM (Model-View-ViewModel)
 */
class MapViewModel(
    private val repository: PlaceRepository
) : ViewModel() {

    // Estado de los lugares (observado por la UI)
    val places: StateFlow<List<PlaceEntity>> = repository.allPlaces
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Lugar seleccionado para editar
    private val _selectedPlace = MutableStateFlow<PlaceEntity?>(null)
    val selectedPlace: StateFlow<PlaceEntity?> = _selectedPlace.asStateFlow()

    // Estado del diálogo de agregar/editar
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    // Coordenadas del centro del mapa (Dolores Hidalgo por defecto)
    private val _mapCenter = MutableStateFlow(LatLng(21.1560, -100.9318))
    val mapCenter: StateFlow<LatLng> = _mapCenter.asStateFlow()

    // Estado de error para Snackbar
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Cargar lugares predeterminados si la base de datos está vacía
        viewModelScope.launch {
            try {
                places.first().let { placesList ->
                    if (placesList.isEmpty()) {
                        Logger.i("Insertando lugares predeterminados de Dolores Hidalgo")
                        repository.insertDefaultPlaces()
                    }
                }
            } catch (e: Exception) {
                Logger.e("Error al cargar lugares predeterminados", e)
            }
        }
    }

    /**
     * Agregar un nuevo lugar turístico
     */
    fun addPlace(
        name: String,
        description: String,
        latLng: LatLng,
        category: String,
        markerColor: String
    ) {
        viewModelScope.launch {
            try {
                Logger.d("Agregando nuevo lugar: $name")
                val newPlace = PlaceEntity(
                    name = name,
                    description = description,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    category = category,
                    markerColor = markerColor
                )
                repository.insertPlace(newPlace)
                Logger.i("Lugar agregado exitosamente: ${newPlace.name}")
                _showDialog.value = false
            } catch (e: Exception) {
                Logger.e("Error al agregar lugar", e)
                _errorMessage.value = "No se pudo agregar el lugar. Intenta nuevamente."

            }
        }
    }

    /**
     * Actualizar un lugar existente
     */
    fun updatePlace(place: PlaceEntity) {
        viewModelScope.launch {
            try {
                Logger.d("Actualizando lugar con ID ${place.id}")
                repository.updatePlace(place)
                _selectedPlace.value = null
                _showDialog.value = false
            } catch (e: Exception) {
                Logger.e("Error al actualizar lugar", e)
                _errorMessage.value = "No se pudo actualizar el lugar."
            }
        }
    }

    /**
     * Eliminar un lugar
     */
    fun deletePlace(place: PlaceEntity) {
        viewModelScope.launch {
            try {
                Logger.w("Eliminando lugar: ${place.name}")
                repository.deletePlace(place)
            } catch (e: Exception) {
                Logger.e("Error al eliminar lugar", e)
                _errorMessage.value = "No se pudo eliminar el lugar."
            }
        }
    }

    /**
     * Marcar/desmarcar como favorito
     */
    fun toggleFavorite(place: PlaceEntity) {
        viewModelScope.launch {
            try {
                Logger.d("Cambiando estado favorito para: ${place.name}")
                repository.toggleFavorite(place.id, place.isFavorite)
            } catch (e: Exception) {
                Logger.e("Error al actualizar favorito", e)
            }
        }
    }

    /**
     * Mostrar diálogo para agregar un nuevo lugar
     */
    fun showAddDialog(latLng: LatLng) {
        _mapCenter.value = latLng
        _selectedPlace.value = null
        _showDialog.value = true
    }

    /**
     * Mostrar diálogo para editar un lugar existente
     */
    fun showEditDialog(place: PlaceEntity) {
        _selectedPlace.value = place
        _showDialog.value = true
    }

    /**
     * Cerrar diálogo
     */
    fun dismissDialog() {
        _showDialog.value = false
        _selectedPlace.value = null
    }

    /**
     * Limpiar mensajes de error (para Snackbar)
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

private val _errorMessage = MutableStateFlow<String?>(null)
val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

fun clearError() {
    _errorMessage.value = null
}
