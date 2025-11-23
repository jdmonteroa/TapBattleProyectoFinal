package com.example.tapbattleproyectofinal.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tapbattleproyectofinal.data.BackendService
import com.example.tapbattleproyectofinal.databinding.ActivityLobbyBinding
import com.example.tapbattleproyectofinal.data.GameDatabase
import com.example.tapbattleproyectofinal.data.GameRepository
import com.example.tapbattleproyectofinal.utils.Constants
import com.example.tapbattleproyectofinal.viewmodel.LobbyViewModel

/**
 * Pantalla de lobby donde el jugador se une a una sala
 */
class LobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLobbyBinding
    private lateinit var playerName: String
    private var roomId: String? = null
    private var roomCode: String? = null

    private val viewModel: LobbyViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = GameDatabase.getDatabase(applicationContext)
                val repository = GameRepository(BackendService(), database.gameDao())
                @Suppress("UNCHECKED_CAST")
                return LobbyViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerName = intent.getStringExtra(Constants.EXTRA_PLAYER_NAME) ?: "Jugador"

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvPlayerName.text = "Jugador: $playerName"

        // Botón volver
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Botón unirse a sala
        binding.btnJoinRoom.setOnClickListener {
            val code = binding.etRoomCode.text.toString().trim().uppercase()

            if (code.isEmpty()) {
                Toast.makeText(this, "Ingresa un código de sala",
                    Toast.LENGTH_SHORT).show()
                binding.etRoomCode.error = "Código requerido"
                return@setOnClickListener
            }

            roomCode = code
            viewModel.joinRoom(code)
        }

        // Botón iniciar juego
        binding.btnStartGame.setOnClickListener {
            startGame()
        }
    }

    private fun observeViewModel() {
        // Observar loading
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnJoinRoom.isEnabled = !isLoading
        }

        // Observar errores
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                binding.tvStatus.text = it
                binding.tvStatus.visibility = View.VISIBLE
            }
        }

        // Observar éxito al unirse
        viewModel.joinSuccess.observe(this) { success->
            if (success == true) {
                showWaitingRoom()
            }
        }

        // Observar roomId
        viewModel.roomId.observe(this) { id ->
            roomId = id
        }
    }

    private fun showWaitingRoom() {
        binding.cardWaiting.visibility = View.VISIBLE
        binding.tvRoomCodeDisplay.text = "Sala: $roomCode"
        binding.tvStatus.text = "Conectado a la sala"
        binding.tvStatus.visibility = View.VISIBLE

        Toast.makeText(this, "¡Conectado! Esperando jugadores...", Toast.LENGTH_SHORT).show()
    }

    private fun startGame() {
        val id = roomId
        val code = roomCode

        if (id == null || code == null) {
            Toast.makeText(this, "Error: No estás en una sala", Toast.LENGTH_SHORT).show()
            return
        }

        // Ir a GameActivity
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(Constants.EXTRA_ROOM_ID, id)
            putExtra(Constants.EXTRA_ROOM_CODE, code)
            putExtra(Constants.EXTRA_PLAYER_NAME, playerName)
        }
        startActivity(intent)
        finish()
    }
}