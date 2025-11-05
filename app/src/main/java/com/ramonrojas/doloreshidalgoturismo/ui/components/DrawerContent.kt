package com.ramonrojas.doloreshidalgoturismo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramonrojas.doloreshidalgoturismo.ui.viewmodel.MapViewModel

/**
 * Contenido del menú lateral (Navigation Drawer)
 * Muestra opciones de configuración y estadísticas
 */
@Composable
fun DrawerContent(
    statistics: MapViewModel.PlaceStatistics,
    onCloseDrawer: () -> Unit,
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Encabezado del drawer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Aquí podrías agregar un logo
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Turismo Dolores Hidalgo",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Cuna de la Independencia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Estadísticas compactas
        StatisticsPanel(statistics = statistics)

        Spacer(modifier = Modifier.height(16.dp))

        // Opciones del menú
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text("Exportar lugares") },
            selected = false,
            onClick = {
                onExportData()
                onCloseDrawer()
            }
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            label = { Text("Importar lugares") },
            selected = false,
            onClick = {
                onImportData()
                onCloseDrawer()
            }
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text("Acerca de") },
            selected = false,
            onClick = {
                // TODO: Mostrar diálogo "Acerca de"
                onCloseDrawer()
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Versión de la app
        Text(
            text = "Versión 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}