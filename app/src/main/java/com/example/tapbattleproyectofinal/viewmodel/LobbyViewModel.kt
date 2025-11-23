package com.example.tapbattleproyectofinal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tapbattleproyectofinal.data.GameRepository
import kotlinx.coroutines.launch

/** ViewModel para manejar el lobby (unirse a sala) */
class LobbyViewModel(private val repository: GameRepository) : ViewModel() {

    private val _roomId = MutableLiveData<String?>()
    val roomId: LiveData<String?> = _roomId

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _joinSuccess = MutableLiveData<Boolean?>()
    val joinSuccess: LiveData<Boolean?> = _joinSuccess

    /** Une al jugador a una sala */
    fun joinRoom(roomCode: String) {
        if (roomCode.isBlank()) {
            _error.value = "Ingresa un código de sala"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _joinSuccess.value = null

                val roomId = repository.joinRoom(roomCode.trim().uppercase())

                _roomId.value = roomId
                _joinSuccess.value = true

            } catch (e: Exception) {
                _error.value = "Error al unirse: ${e.message}"
                _joinSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Limpia el estado de error */
    fun clearError() {
        _error.value = null
    }
}