package com.ramonrojas.doloreshidalgoturismo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramonrojas.doloreshidalgoturismo.data.database.AppDatabase
import com.ramonrojas.doloreshidalgoturismo.data.repository.PlaceRepository
import com.ramonrojas.doloreshidalgoturismo.ui.screens.MapScreen
import com.ramonrojas.doloreshidalgoturismo.ui.theme.DoloreshidalgoTurismoTheme
import com.ramonrojas.doloreshidalgoturismo.ui.viewmodel.MapViewModel
import com.ramonrojas.doloreshidalgoturismo.ui.viewmodel.MapViewModelFactory

/**
 * Actividad principal de la aplicación
 * Aquí se inicia todo el flujo
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar diseño edge-to-edge (pantalla completa)
        enableEdgeToEdge()

        // Inicializar la base de datos y el repositorio
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PlaceRepository(database.placeDao())

        setContent {
            DoloreshidalgoTurismoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Crear el ViewModel usando el Factory
                    val viewModel: MapViewModel = viewModel(
                        factory = MapViewModelFactory(repository)
                    )

                    // Mostrar la pantalla del mapa
                    MapScreen(viewModel = viewModel)
                }
            }
        }
    }
}
