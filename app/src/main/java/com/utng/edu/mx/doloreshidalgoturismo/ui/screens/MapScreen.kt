package com.utng.edu.mx.doloreshidalgoturismo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.utng.edu.mx.doloreshidalgoturismo.data.model.PlaceEntity
import com.utng.edu.mx.doloreshidalgoturismo.ui.components.PlaceDialog
import com.utng.edu.mx.doloreshidalgoturismo.ui.components.PlacesList
import com.utng.edu.mx.doloreshidalgoturismo.ui.viewmodel.MapViewModel

/**
 * Pantalla principal que muestra el mapa con los marcadores
 * @param viewModel: ViewModel que maneja la lógica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel
) {
    // Observar los lugares desde el ViewModel
    val places by viewModel.places.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()

    // Estado del mapa
    val doloreshidalgoCenter = LatLng(21.1560, -100.9318)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(doloreshidalgoCenter, 14f)
    }

    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

// Filtrar lugares según búsqueda
    val filteredPlaces = remember(places, searchQuery) {
        if (searchQuery.isBlank()) {
            places
        } else {
            places.filter { place ->
                place.name.contains(searchQuery, ignoreCase = true) ||
                        place.description.contains(searchQuery, ignoreCase = true) ||
                        place.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

// En el Scaffold, agregar un IconButton en el TopAppBar:
    TopAppBar(
        title = { Text("Turismo Dolores Hidalgo") },
        actions = {
            IconButton(onClick = { showSearchBar = !showSearchBar }) {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )

// Debajo del TopAppBar, agregar la barra de búsqueda
    if (showSearchBar) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { },
            active = false,
            onActiveChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar lugares...") }
        ) { }
    }
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Turismo Dolores Hidalgo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            // Botón para agregar nuevo lugar en el centro del mapa
            FloatingActionButton(
                onClick = {
                    viewModel.showAddDialog(cameraPositionState.position.target)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar lugar")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mapa de Google
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false // Cambiar a true si necesitas ubicación
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                ),
                onMapLongClick = { latLng ->
                    // Agregar marcador con pulsación larga
                    viewModel.showAddDialog(latLng)
                }
            ) {
                // Dibujar marcadores para cada lugar
                places.forEach { place ->
                    val position = LatLng(place.latitude, place.longitude)

                    Marker(
                        state = MarkerState(position = position),
                        title = place.name,
                        snippet = place.description,
                        onInfoWindowClick = {
                            viewModel.showEditDialog(place)
                        }
                    )
                }
            }

            // Lista de lugares en la parte inferior
            PlacesList(
                places = places,
                onPlaceClick = { place ->
                    // Mover cámara al lugar seleccionado
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(place.latitude, place.longitude),
                        16f
                    )
                },
                onEditClick = { place ->
                    viewModel.showEditDialog(place)
                },
                onDeleteClick = { place ->
                    viewModel.deletePlace(place)
                },
                onFavoriteClick = { place ->
                    viewModel.toggleFavorite(place)
                }
            )
        }

        // Diálogo para agregar/editar lugar
        if (showDialog) {
            PlaceDialog(
                place = selectedPlace,
                onDismiss = { viewModel.dismissDialog() },
                onSave = { name, description, latLng, category, color ->
                    if (selectedPlace != null) {
                        // Editar lugar existente
                        viewModel.updatePlace(
                            selectedPlace!!.copy(
                                name = name,
                                description = description,
                                latitude = latLng.latitude,
                                longitude = latLng.longitude,
                                category = category,
                                markerColor = color
                            )
                        )
                    } else {
                        // Agregar nuevo lugar
                        viewModel.addPlace(name, description, latLng, category, color)
                    }
                }
            )
        }
    }
}
