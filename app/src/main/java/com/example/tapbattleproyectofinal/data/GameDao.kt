package com.example.tapbattleproyectofinal.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GameDao {

    //Inserta una nueva partida en el historial
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    //Obtiene todas las partidas ordenadas por tiempo
    @Query("SELECT * FROM game_history ORDER BY timestamp DESC")
    fun getAllGames(): LiveData<List<GameEntity>>

    //Obtiene las últimas partidas
    @Query("SELECT * FROM game_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentGames(limit: Int): List<GameEntity>

    //Obtiene partidas por jugador
    @Query("SELECT * FROM game_history WHERE playerName = :playerName ORDER BY timestamp DESC")
    fun getGamesByPlayer(playerName: String): LiveData<List<GameEntity>>

    //Cuenta las victorias de un jugador
    @Query("SELECT COUNT(*) FROM game_history WHERE playerName = :playerName AND didWin = 1")
    suspend fun getWinCount(playerName: String): Int

    //Elimina todo el historial
    @Query("DELETE FROM game_history")
    suspend fun deleteAllGames()

    //Elimina una partida específica
    @Delete
    suspend fun deleteGame(game: GameEntity)
}