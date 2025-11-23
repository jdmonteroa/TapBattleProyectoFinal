package com.example.tapbattleproyectofinal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tapbattleproyectofinal.data.GameRepository
import com.example.tapbattleproyectofinal.data.GameEntity
import com.example.tapbattleproyectofinal.models.GameEvent
import com.example.tapbattleproyectofinal.models.GameState
import com.example.tapbattleproyectofinal.models.Target
import kotlinx.coroutines.launch

/** ViewModel para manejar la lógica del juego */
class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // Estado del juego
    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    // Eventos del juego
    private val _gameEvent = MutableLiveData<GameEvent>()
    val gameEvent: LiveData<GameEvent> = _gameEvent

    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Errores
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /** Inicializa el estado del juego */
    fun initGame(roomId: String, roomCode: String, playerName: String) {
        _gameState.value = GameState(
            roomId = roomId,
            roomCode = roomCode,
            playerName = playerName
        )
        subscribeToEvents(roomId)
    }

    /** Se suscribe a los eventos de la sala */
    private fun subscribeToEvents(roomId: String) {
        repository.subscribeToGameEvents(
            roomId = roomId,
            onEvent = { event ->
                handleGameEvent(event)
            },
            onError = { exception ->
                _error.postValue("Error de conexión: ${exception.message}")
            }
        )
    }

    /** Maneja los eventos recibidos del servidor */
    private fun handleGameEvent(event: GameEvent) {
        _gameEvent.postValue(event)

        val currentState = _gameState.value ?: return

        when (event) {
            is GameEvent.Start -> {
                _gameState.postValue(
                    currentState.copy(
                        score = event.score,
                        round = event.round,
                        maxRounds = event.maxRounds,
                        isGameStarted = true,
                        isGameEnded = false
                    )
                )
            }

            is GameEvent.Spawn -> {
                _gameState.postValue(
                    currentState.copy(
                        currentTarget = event.target
                    )
                )
            }

            is GameEvent.Score -> {
                _gameState.postValue(
                    currentState.copy(
                        score = event.score,
                        round = event.round,
                        currentTarget = null // Limpia el objetivo actual
                    )
                )
            }

            is GameEvent.End -> {
                _gameState.postValue(
                    currentState.copy(
                        score = event.score,
                        isGameEnded = true,
                        champion = event.champion,
                        currentTarget = null
                    )
                )

                // Guardar en historial
                saveGameToHistory(event)
            }
        }
    }

    /** Inicia el juego */
    fun startGame() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val roomId = _gameState.value?.roomId ?: return@launch
                repository.startGame(roomId)
            } catch (e: Exception) {
                _error.value = "Error al iniciar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Registra que el jugador tocó un objetivo */
    fun onTargetHit(target: Target) {
        viewModelScope.launch {
            try {
                val state = _gameState.value ?: return@launch

                // Verificar que el objetivo no haya expirado
                if (target.isExpired()) {
                    return@launch
                }

                repository.hitTarget(
                    roomId = state.roomId,
                    spawnId = target.spawnId,
                    playerName = state.playerName
                )
            } catch (e: Exception) {
                _error.value = "Error al registrar hit: ${e.message}"
            }
        }
    }

    /** Guarda la partida en el historial local */
    private fun saveGameToHistory(endEvent: GameEvent.End) {
        viewModelScope.launch {
            try {
                val state = _gameState.value ?: return@launch

                val opponentName = state.getOpponentName()
                val playerScore = state.score[state.playerName] ?: 0
                val opponentScore = state.score[opponentName] ?: 0

                val gameEntity = GameEntity(
                    roomCode = state.roomCode,
                    playerName = state.playerName,
                    opponentName = opponentName,
                    playerScore = playerScore,
                    opponentScore = opponentScore,
                    champion = endEvent.champion,
                    roundsPlayed = endEvent.roundsPlayed,
                    didWin = endEvent.champion == state.playerName
                )

                repository.saveGameToHistory(gameEntity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Obtiene el estado actual de la sala (útil para reconexión) */
    fun refreshRoomState() {
        viewModelScope.launch {
            try {
                val roomId = _gameState.value?.roomId ?: return@launch
                val result = repository.getRoomState(roomId)

                if (result != null) {
                    val (score, round, maxRounds) = result
                    val currentState = _gameState.value ?: return@launch

                    _gameState.postValue(
                        currentState.copy(
                            score = score,
                            round = round,
                            maxRounds = maxRounds
                        )
                    )
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar estado: ${e.message}"
            }
        }
    }
}