package com.example.tapbattleproyectofinal.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tapbattleproyectofinal.databinding.ActivityMainBinding
import com.example.tapbattleproyectofinal.utils.Constants

/**
 * Pantalla principal de la aplicación
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Botón Jugar
        binding.btnPlay.setOnClickListener {
            val playerName = binding.etPlayerName.text.toString().trim()

            if (playerName.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu nombre", Toast.LENGTH_SHORT).show()
                binding.etPlayerName.error = "Nombre requerido"
                return@setOnClickListener
            }

            // Ir a LobbyActivity
            val intent = Intent(this, LobbyActivity::class.java).apply {
                putExtra(Constants.EXTRA_PLAYER_NAME, playerName)
            }
            startActivity(intent)
        }

        // Botón Historial
        binding.btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}