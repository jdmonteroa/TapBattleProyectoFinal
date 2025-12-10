package com.example.tapbattleproyectofinal.ui


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tapbattleproyectofinal.data.BackendService
import com.example.tapbattleproyectofinal.data.GameDatabase
import com.example.tapbattleproyectofinal.data.GameRepository
import com.example.tapbattleproyectofinal.databinding.ActivityGameBinding
import com.example.tapbattleproyectofinal.models.GameEvent
import com.example.tapbattleproyectofinal.utils.Constants
import com.example.tapbattleproyectofinal.viewmodel.GameViewModel


class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private lateinit var roomId: String
    private lateinit var roomCode: String
    private lateinit var playerName: String

    private val handler = Handler(Looper.getMainLooper())

    private val viewModel: GameViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = GameDatabase.getDatabase(applicationContext)
                val repository = GameRepository(BackendService(), database.gameDao())
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos del intent
        roomId = intent.getStringExtra(Constants.EXTRA_ROOM_ID) ?: run {
            Toast.makeText(this, "Error: No hay roomId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        roomCode = intent.getStringExtra(Constants.EXTRA_ROOM_CODE) ?: ""
        playerName = intent.getStringExtra(Constants.EXTRA_PLAYER_NAME) ?: "Jugador"

        setupUI()
        initGame()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvPlayerName.text = playerName

        // Configurar callback del GameView
        binding.gameView.onTargetHit = { target ->
            viewModel.onTargetHit(target)
        }
    }

    private fun initGame() {
        // Inicializar el ViewModel con datos del juego
        viewModel.initGame(roomId, roomCode, playerName)

        // Iniciar el juego después de un pequeño delay
        handler.postDelayed({
            viewModel.startGame()
        }, 1000)
    }

    private fun observeViewModel() {
        // Observar estado del juego
        viewModel.gameState.observe(this) { state ->
            updateUI(state.score, state.round, state.maxRounds)

            // Actualizar objetivo en el canvas
            binding.gameView.setTarget(state.currentTarget)

            // Verificar si el juego terminó
            if (state.isGameEnded) {
                handler.postDelayed({
                    goToResults(state.champion ?: "", state.score)
                }, 1500)
            }
        }

        // Observar eventos del juego
        viewModel.gameEvent.observe(this) { event ->
            handleGameEvent(event)
        }

        // Observar errores
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(score: Map<String, Int>, round: Int, maxRounds: Int) {
        val playerScore = score[playerName] ?: 0
        val opponentEntry = score.entries.firstOrNull { it.key != playerName }
        val opponentName = opponentEntry?.key ?: "Esperando..."
        val opponentScore = opponentEntry?.value ?: 0

        binding.tvPlayerScore.text = playerScore.toString()
        binding.tvOpponentName.text = opponentName
        binding.tvOpponentScore.text = opponentScore.toString()
        //binding.tvRound.text = "$round/$maxRounds"
    }

    private fun handleGameEvent(event: GameEvent) {
        when (event) {
            is GameEvent.Start -> {
                // Ocultar mensaje de inicio
                binding.cardStartMessage.visibility = View.GONE
                Toast.makeText(this, "¡Juego iniciado!", Toast.LENGTH_SHORT).show()
            }

            is GameEvent.Spawn -> {
                // El objetivo ya se actualiza en gameState
            }

            is GameEvent.Score -> {
                // Mostrar quién ganó la ronda
                val message = if (event.winner == playerName) {
                    "¡Punto para ti! 🎯"
                } else {
                    "Punto para ${event.winner}"
                }

            }

            is GameEvent.End -> {
                // El fin del juego se maneja en gameState
            }
        }
    }


    private fun goToResults(champion: String, finalScore: Map<String, Int>) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(Constants.EXTRA_PLAYER_NAME, playerName)
            putExtra(Constants.EXTRA_WINNER, champion)
            putExtra(Constants.EXTRA_FINAL_SCORE, HashMap(finalScore))
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}