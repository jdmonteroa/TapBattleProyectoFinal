package com.example.tapbattleproyectofinal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


// Entidad de Room para representar un registro en el historial de partidas
@Entity(tableName = "game_history")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)   // ID autogenerado por Room
    val id: Long = 0,

    val roomCode: String,              // Código de la sala donde se jugó
    val playerName: String,            // Nombre del jugador local
    val opponentName: String,          // Nombre del oponente
    val playerScore: Int,              // Puntaje del jugador local
    val opponentScore: Int,            // Puntaje del oponente
    val champion: String,              // Nombre del ganador
    val roundsPlayed: Int,             // Cantidad de rondas jugadas

    val timestamp: Long = System.currentTimeMillis(), // Fecha/hora del registro
    val didWin: Boolean                // Si el jugador ganó o no
)


// Conversor de tipos para Room
class Converters {
    private val gson = Gson() // Instancia de Gson para serializar/deserializar

    @TypeConverter
    fun fromStringMap(value: String): Map<String, Int> {
        // Convierte un JSON (String) de vuelta a Map<String, Int>
        val mapType = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun toStringMap(map: Map<String, Int>): String {
        // Convierte un Map<String, Int> a String en formato JSON
        return gson.toJson(map)
    }
}
