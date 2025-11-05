package com.ramonrojas.doloreshidalgoturismo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.ramonrojas.doloreshidalgoturismo.ui.components.DrawerContent
import com.ramonrojas.doloreshidalgoturismo.ui.components.PlacesList
import com.ramonrojas.doloreshidalgoturismo.ui.components.PlaceDialog
import com.ramonrojas.doloreshidalgoturismo.ui.viewmodel.MapViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla principal que muestra el mapa con los marcadores
 * @param viewModel: ViewModel que maneja la lógica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel
) {
    val places by viewModel.places.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val statistics by viewModel.placeStatistics.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val doloreshidalgoCenter = LatLng(21.1560, -100.9318)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(doloreshidalgoCenter, 14f)



    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    statistics = statistics,
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    },
                    onExportData = {
                        // TODO: Implementar exportación
                    },
                    onImportData = {
                        // TODO: Implementar importación
                    }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Turismo Dolores Hidalgo") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
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
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false
                    ),
                    onMapLongClick = { latLng ->
                        viewModel.showAddDialog(latLng)
                    }
                ) {
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

                PlacesList(
                    places = places,
                    onPlaceClick = { place ->
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(place.latitude, place.longitude),
                            16f
                        )
                    },
                    onEditClick = { place -> viewModel.showEditDialog(place) },
                    onDeleteClick = { place -> viewModel.deletePlace(place) },
                    onFavoriteClick = { place -> viewModel.toggleFavorite(place) }
                )
            }

            if (showDialog) {
                PlaceDialog(
                    place = selectedPlace,
                    onDismiss = { viewModel.dismissDialog() },
                    onSave = { name, description, latLng, category, color ->
                        if (selectedPlace != null) {
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
                            viewModel.addPlace(name, description, latLng, category, color)
                        }
                    }
                )
            }
        }
    }
}
