package com.ramonrojas.doloreshidalgoturismo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ramonrojas.doloreshidalgoturismo.data.model.PlaceEntity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Share
import com.ramonrojas.doloreshidalgoturismo.utils.NavigationHelper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Lista horizontal de tarjetas de lugares turísticos
 * Aparece en la parte inferior del mapa
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesList(
    places: List<PlaceEntity>,
    onPlaceClick: (PlaceEntity) -> Unit,
    onEditClick: (PlaceEntity) -> Unit,
    onDeleteClick: (PlaceEntity) -> Unit,
    onFavoriteClick: (PlaceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(places) { place ->
            PlaceCard(
                place = place,
                onClick = { onPlaceClick(place) },
                onEditClick = { onEditClick(place) },
                onDeleteClick = { onDeleteClick(place) },
                onFavoriteClick = { onFavoriteClick(place) }
            )
        }
    }
}

/**
 * Tarjeta individual de un lugar turístico
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceCard(
    place: PlaceEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) +
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(500, easing = EaseOutCubic)
                ),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Encabezado con nombre y favorito
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (place.isFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (place.isFavorite) Color.Red else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Categoría
                AssistChip(
                    onClick = { },
                    label = { Text(place.category) },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(place.category),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Botones de navegación, ver y compartir
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(onClick = { NavigationHelper.openGoogleMapsView(context, place) }) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Ver en Maps", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { NavigationHelper.openGoogleMapsNavigation(context, place) }) {
                            Icon(Icons.Default.Search, contentDescription = "Navegar", tint = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = { NavigationHelper.sharePlaceLocation(context, place) }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir")
                        }
                    }

                    // Botones de editar y eliminar
                    Row {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (showDeleteConfirmation) {
        ConfirmationDialog(
            title = "Eliminar lugar",
            message = "¿Estás seguro de que deseas eliminar '${place.name}'? Esta acción no se puede deshacer.",
            confirmButtonText = "Eliminar",
            dismissButtonText = "Cancelar",
            onConfirm = {
                onDeleteClick()
                showDeleteConfirmation = false
            },
            onDismiss = {
                showDeleteConfirmation = false
            }
        )
    }
}




/**
 * Obtener icono según categoría
 */
fun getCategoryIcon(category: String) = when (category.lowercase()) {
    "iglesia" -> Icons.Default.Place
    "museo" -> Icons.Default.Add
    "restaurante" -> Icons.Default.Menu
    "plaza" -> Icons.Default.LocationOn
    else -> Icons.Default.LocationOn
}
