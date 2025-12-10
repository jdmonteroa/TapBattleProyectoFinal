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
import com.example.tapbattleproyectofinal.data.GameDatabase
import com.example.tapbattleproyectofinal.data.GameRepository
import com.example.tapbattleproyectofinal.databinding.ActivityLobbyBinding
import com.example.tapbattleproyectofinal.utils.Constants
import com.example.tapbattleproyectofinal.viewmodel.LobbyViewModel

class LobbyActivity : AppCompatActivity() {

    // Binding para acceder a los elementos del layout
    private lateinit var binding: ActivityLobbyBinding

    // Datos del jugador y la sala
    private lateinit var playerName: String
    private var roomId: String? = null
    private var roomCode: String? = null
    private var isCreator: Boolean = false

    // ViewModel con factory manual para pasar el repositorio
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

        // Inicializa el binding del layout
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtiene el nombre del jugador enviado desde la pantalla anterior
        playerName = intent.getStringExtra(Constants.EXTRA_PLAYER_NAME) ?: "Jugador"

        setupUI()
        observeViewModel()
    }

    // Configura listeners y textos iniciales del lobby
    private fun setupUI() {
        binding.tvPlayerName.text = "Jugador: $playerName"

        // Botón para unirse a una sala con código
        binding.btnJoinRoom.setOnClickListener {
            val code = binding.etRoomCode.text.toString().trim().uppercase()

            if (code.isEmpty()) {
                Toast.makeText(this, "Ingresa un código de sala", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            roomCode = code
            viewModel.joinRoom(code, playerName)
        }

        // Botón para iniciar el juego (solo visible si eres el creador)
        binding.btnStartGame.setOnClickListener {
            startGame()
        }
    }

    // Observa cambios del ViewModel y actualiza la UI
    private fun observeViewModel() {

        // Loading: mostrar/ocultar progress bar
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnJoinRoom.isEnabled = !isLoading
        }

        // Mostrar errores si ocurren
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                binding.tvStatus.text = it
                binding.tvStatus.visibility = View.VISIBLE
            }
        }

        // Cuando se une correctamente a la sala
        viewModel.joinSuccess.observe(this) { success ->
            if (success) {
                showWaitingRoom()
            }
        }

        // Obtener el ID real de la sala desde el servidor
        viewModel.roomId.observe(this) { id ->
            roomId = id
        }

        // Saber si es el creador para mostrar opciones especiales
        viewModel.isCreator.observe(this) { creator ->
            isCreator = creator

            // Mostrar botón de iniciar juego solo si es el creador
            binding.btnStartGame.visibility = if (creator) View.VISIBLE else View.GONE

            if (creator) {
                binding.tvWaitingText.text = "Eres el creador. Esperando jugador..."
            } else {
                binding.tvWaitingText.text = "Esperando que el creador inicie..."
            }
        }

        // Actualizar conteo de jugadores en la sala
        viewModel.playersCount.observe(this) { count ->
            binding.tvWaitingText.text = if (isCreator) {
                if (count >= 2) {
                    "¡Listo! Puedes iniciar el juego"
                } else {
                    "Esperando otro jugador... ($count/2)"
                }
            } else {
                "Esperando que el creador inicie... ($count jugadores)"
            }

            // Habilitar botón solo si hay 2 jugadores y eres creador
            binding.btnStartGame.isEnabled = (count >= 2 && isCreator)
        }

        // Cuando el servidor indica que el juego inicia
        viewModel.gameStarted.observe(this) { started ->
            if (started) {
                goToGame()
            }
        }
    }

    // Mostrar la tarjeta de "Sala en espera"
    private fun showWaitingRoom() {
        binding.cardWaiting.visibility = View.VISIBLE
        binding.tvRoomCodeDisplay.text = "Sala: $roomCode"
        binding.tvStatus.text = "Conectado a la sala"
        binding.tvStatus.visibility = View.VISIBLE

        Toast.makeText(this, "¡Conectado!", Toast.LENGTH_SHORT).show()
    }

    // Llama a la API para iniciar el juego
    private fun startGame() {
        val id = roomId ?: return
        viewModel.startGame()
    }

    // Navega hacia GameActivity enviando roomID, roomCode y playerName
    private fun goToGame() {
        val id = roomId
        val code = roomCode

        if (id == null || code == null) {
            Toast.makeText(this, "Error: No estás en una sala", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(Constants.EXTRA_ROOM_ID, id)
            putExtra(Constants.EXTRA_ROOM_CODE, code)
            putExtra(Constants.EXTRA_PLAYER_NAME, playerName)
        }
        startActivity(intent)
        finish()
    }
}
