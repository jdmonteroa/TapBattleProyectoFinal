package com.example.tapbattleproyectofinal.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.tapbattleproyectofinal.databinding.ActivityResultBinding
import com.example.tapbattleproyectofinal.utils.Constants

//Pantalla que muestra el resultado del juego
class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var playerName: String
    private lateinit var champion: String
    private var finalScore: HashMap<String, Int> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos del intent
        playerName = intent.getStringExtra(Constants.EXTRA_PLAYER_NAME) ?: "Jugador"
        champion = intent.getStringExtra(Constants.EXTRA_WINNER) ?: ""

        @Suppress("UNCHECKED_CAST")
        finalScore = intent.getSerializableExtra(Constants.EXTRA_FINAL_SCORE) as? HashMap<String, Int>
            ?: hashMapOf()

        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(this@ResultActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        setupUI()

    }

    private fun setupUI() {
        val didWin = champion == playerName
        val playerScore = finalScore[playerName] ?: 0
        val opponentEntry = finalScore.entries.firstOrNull { it.key != playerName }
        val opponentName = opponentEntry?.key ?: "Oponente"
        val opponentScore = opponentEntry?.value ?: 0

        val roundsPlayed = maxOf(playerScore, opponentScore)

        // Configurar UI según resultado
        if (didWin) {
            binding.tvResultEmoji.text = "🏆"
            binding.tvResultTitle.text = "VICTORIA"
            binding.tvResultSubtitle.text = "Eres el campeón"
        } else {
            binding.tvResultEmoji.text = "😔"
            binding.tvResultTitle.text = "DERROTA"
            binding.tvResultSubtitle.text = "Mejor suerte la próxima"
        }

        // Mostrar puntuaciones
        binding.tvPlayerNameResult.text = playerName
        binding.tvPlayerScoreResult.text = playerScore.toString()
        binding.tvOpponentNameResult.text = opponentName
        binding.tvOpponentScoreResult.text = opponentScore.toString()
        binding.tvRoundsPlayed.text = roundsPlayed.toString()

        // Botón jugar de nuevo
        binding.btnPlayAgain.setOnClickListener {
            val intent = Intent(this, LobbyActivity::class.java).apply {
                putExtra(Constants.EXTRA_PLAYER_NAME, playerName)
            }
            startActivity(intent)
            finish()
        }

        // Botón volver al menú
        binding.btnBackToMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}