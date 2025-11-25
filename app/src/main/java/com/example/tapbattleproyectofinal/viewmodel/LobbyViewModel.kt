package com.example.tapbattleproyectofinal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tapbattleproyectofinal.data.GameRepository
import kotlinx.coroutines.launch

class LobbyViewModel(private val repository: GameRepository) : ViewModel() {

    private val _roomId = MutableLiveData<String>()
    val roomId: LiveData<String> = _roomId

    private val _isCreator = MutableLiveData<Boolean>()
    val isCreator: LiveData<Boolean> = _isCreator

    private val _playersCount = MutableLiveData<Int>()
    val playersCount: LiveData<Int> = _playersCount

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _joinSuccess = MutableLiveData<Boolean>()
    val joinSuccess: LiveData<Boolean> = _joinSuccess

    private val _gameStarted = MutableLiveData<Boolean>()
    val gameStarted: LiveData<Boolean> = _gameStarted

    fun joinRoom(roomCode: String, playerName: String) {
        if (roomCode.isBlank()) {
            _error.value = "Ingresa un código de sala"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null // Ahora sí puede ser null

                val (roomId, isCreator, creator) = repository.joinRoom(roomCode.trim().uppercase(), playerName)

                _roomId.value = roomId
                _isCreator.value = isCreator
                _joinSuccess.value = true

                Log.d("LOBBY_VM", "Unido - RoomID: $roomId, isCreator: $isCreator")

                subscribeToLobbyEvents(roomId)

            } catch (e: Exception) {
                Log.e("LOBBY_VM", "Error: ${e.message}")
                _error.value = "Error al unirse: ${e.message}"
                _joinSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun subscribeToLobbyEvents(roomId: String) {
        repository.subscribeToLobbyEvents(
            roomId = roomId,
            onPlayerJoined = { count ->
                Log.d("LOBBY_VM", "Jugador unido. Total: $count")
                _playersCount.postValue(count)
            },
            onGameStart = {
                Log.d("LOBBY_VM", "Juego iniciado desde servidor")
                _gameStarted.postValue(true)
            },
            onError = { exception ->
                Log.e("LOBBY_VM", "Error LiveQuery: ${exception.message}")
            }
        )
    }

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

    fun clearError() {
        _error.value = null
    }
}