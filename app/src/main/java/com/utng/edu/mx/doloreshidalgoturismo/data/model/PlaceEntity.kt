package com.utng.edu.mx.doloreshidalgoturismo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
/**
 * Representa un lugar turístico en la base de datos
 * Es como una ficha de registro con toda la información importante
 */
@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String, // Ejemplo: &quot;Parroquia de Nuestra Señora&quot;
    val description: String, // Descripción del lugar
    val latitude: Double, // Coordenada latitud (21.1558° para Dolores)
    val longitude: Double, // Coordenada longitud (-100.9314° para Dolores)
    val category: String, // Ejemplo: &quot;Iglesia&quot;, &quot;Museo&quot;, &quot;Restaurante&quot;
    val markerColor: String, // Color del marcador en formato HEX (#FF0000)
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)