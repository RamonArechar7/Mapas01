package com.utng.edu.mx.doloreshidalgoturismo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.utng.edu.mx.doloreshidalgoturismo.data.repository.PlaceRepository

/**
 * Factory para crear el ViewModel con dependencias
 * Necesario porque el ViewModel requiere el repositorio
 */
class MapViewModelFactory(
    private val repository: PlaceRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
