package com.example.tapbattleproyectofinal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tapbattleproyectofinal.data.GameRepository
import kotlinx.coroutines.launch

// ViewModel encargado de manejar la lógica del lobby o sala de espera
class LobbyViewModel(private val repository: GameRepository) : ViewModel() {

    // ID de la sala a la que se une el jugador
    private val _roomId = MutableLiveData<String>()
    val roomId: LiveData<String> = _roomId

    // Recibe al información si el jugador actual es el creador de la sala
    private val _isCreator = MutableLiveData<Boolean>()
    val isCreator: LiveData<Boolean> = _isCreator

    // Cuenta el numero de jugadores que estan en la sala
    private val _playersCount = MutableLiveData<Int>()
    val playersCount: LiveData<Int> = _playersCount

    // Indica si está cargando (spinner / loading)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Mensaje de error por si ocurre un problema
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Indica si unirse a la sala fue exitoso
    private val _joinSuccess = MutableLiveData<Boolean>()
    val joinSuccess: LiveData<Boolean> = _joinSuccess

    // Indica si el juego ya inició
    private val _gameStarted = MutableLiveData<Boolean>()
    val gameStarted: LiveData<Boolean> = _gameStarted

    // Función para unirse a una sala existente
    fun joinRoom(roomCode: String, playerName: String) {

        // Validar si el usuario no ingreso codigo valido
        if (roomCode.isBlank()) {
            _error.value = "Ingresa un código de sala"
            return
        }

        // Se realiza la llamada a una coroutine dentro del ViewModel
        // Con esto realiza una tarea asincrona que si esta ejecuantando en el lobby
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null // Limpia error previo

                // Llamada al repositorio para unirse a la sala
                val (roomId, isCreator, creator) =
                    repository.joinRoom(roomCode.trim().uppercase(), playerName)

                // Actualizar LiveData con los datos obtenidos
                _roomId.value = roomId
                _isCreator.value = isCreator
                _joinSuccess.value = true

                Log.d("LOBBY_VM", "Unido - RoomID: $roomId, isCreator: $isCreator")

                // Suscribirse a eventos en tiempo real via LiveQuery
                subscribeToLobbyEvents(roomId)

            } catch (e: Exception) {

                // Manejo de errores al intentar unirse
                Log.e("LOBBY_VM", "Error: ${e.message}")
                _error.value = "Error al unirse: ${e.message}"
                _joinSuccess.value = false

            } finally {
                // Finaliza estado de carga
                _isLoading.value = false
            }
        }
    }

    // Suscribe el lobby a eventos en tiempo real del servidor
    private fun subscribeToLobbyEvents(roomId: String) {
        repository.subscribeToLobbyEvents(
            roomId = roomId,

            // Evento: un jugador se unió
            onPlayerJoined = { count ->
                Log.d("LOBBY_VM", "Jugador unido. Total: $count")
                _playersCount.postValue(count)
            },

            // Evento: el juego comenzó
            onGameStart = {
                Log.d("LOBBY_VM", "Juego iniciado desde servidor")
                _gameStarted.postValue(true)
            },

            // Evento: error en LiveQuery
            onError = { exception ->
                Log.e("LOBBY_VM", "Error LiveQuery: ${exception.message}")
            }
        )
    }

    // Función para cuando el creador de la sala inicia el juego
    fun startGame() {
        viewModelScope.launch {
            try {
                val roomId = _roomId.value ?: return@launch
                repository.startGame(roomId)

            } catch (e: Exception) {
                Log.e("LOBBY_VM", "Error al iniciar: ${e.message}")
                _error.value = "Error al iniciar: ${e.message}"
            }
        }
    }

    // Limpia el mensaje de error actual
    fun clearError() {
        _error.value = null
    }
}
