package com.example.tapbattleproyectofinal.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tapbattleproyectofinal.data.GameDatabase
import com.example.tapbattleproyectofinal.databinding.ActivityHistoryBinding

/**
 * Pantalla de historial de partidas
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadHistory()
    }

    private fun setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Configurar RecyclerView
        adapter = HistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }
    }

    private fun loadHistory() {
        val database = GameDatabase.getDatabase(applicationContext)
        val gameDao = database.gameDao()

        // Observar cambios en el historial
        gameDao.getAllGames().observe(this) { games ->
            if (games.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
                adapter.submitList(games)
            }
        }
    }
}