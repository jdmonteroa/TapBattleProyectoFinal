package com.example.tapbattleproyectofinal.data


import androidx.lifecycle.LiveData
import com.example.tapbattleproyectofinal.models.GameEvent

// Repositorio que coordina datos locales (Room) y remotos (Back4App)
class GameRepository(
    private val backendService: BackendService,
    private val gameDao: GameDao
) {

    // ----- OPERACIONES REMOTAS EN BACK4APP -----


    // Une a un jugador a la sala
    suspend fun joinRoom(code: String, playerName: String): Triple<String, Boolean, String> {
        return backendService.joinRoom(code, playerName)
    }

    // Inicia el juego en el servidor
    suspend fun startGame(roomId: String): Boolean {
        return backendService.startGame(roomId)
    }

    // Registra un hit en el servidor
    suspend fun hitTarget(roomId: String, spawnId: String, playerName: String): Boolean {
        return backendService.hitTarget(roomId, spawnId, playerName)
    }

    // Suscribe a eventos de una sala
    fun subscribeToGameEvents(
        roomId: String,
        onEvent: (GameEvent) -> Unit,
        onError: (Exception) -> Unit
    ) {
        backendService.subscribeToEvents(roomId, onEvent, onError)
    }

    //Obtiene el estado actual de la sala
    suspend fun getRoomState(roomId: String): Triple<Map<String, Int>, Int, Int>? {
        return backendService.getRoomState(roomId)
    }

    // ----- OPERACIONES LOCALES (Room) -----

    // Guarda una partida en el historial local
    suspend fun saveGameToHistory(game: GameEntity): Long {
        return gameDao.insertGame(game)
    }

    // Obtiene todo el historial de partidas
    fun getAllGames(): LiveData<List<GameEntity>> {
        return gameDao.getAllGames()
    }

    // Obtiene partidas recientes
    suspend fun getRecentGames(limit: Int): List<GameEntity> {
        return gameDao.getRecentGames(limit)
    }

    // Obtiene partidas de un jugador específico
    fun getGamesByPlayer(playerName: String): LiveData<List<GameEntity>> {
        return gameDao.getGamesByPlayer(playerName)
    }

    // Cuenta victorias de un jugador
    suspend fun getWinCount(playerName: String): Int {
        return gameDao.getWinCount(playerName)
    }

    // Elimina todo el historial
    suspend fun clearHistory() {
        gameDao.deleteAllGames()
    }

    //Elimina una partida específica
    suspend fun deleteGame(game: GameEntity) {
        gameDao.deleteGame(game)
    }

    fun subscribeToLobbyEvents(
        roomId: String,
        onPlayerJoined: (Int) -> Unit,
        onGameStart: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        backendService.subscribeToLobbyEvents(roomId, onPlayerJoined, onGameStart, onError)
    }
}