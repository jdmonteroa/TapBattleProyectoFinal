package com.example.tapbattleproyectofinal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


 //Entidad de Room para guardar historial de partidas
@Entity(tableName = "game_history")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomCode: String,
    val playerName: String,
    val opponentName: String,
    val playerScore: Int,
    val opponentScore: Int,
    val champion: String,
    val roundsPlayed: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val didWin: Boolean
)


//Conversor de tipos para Room
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(value: String): Map<String, Int> {
        val mapType = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun toStringMap(map: Map<String, Int>): String {
        return gson.toJson(map)
    }
}