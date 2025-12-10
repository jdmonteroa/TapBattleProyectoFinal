package com.example.tapbattleproyectofinal.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tapbattleproyectofinal.data.GameEntity
import com.example.tapbattleproyectofinal.databinding.ItemGameHistoryBinding

//Adapter que nos ayuda mostrar el historial de partidas jugadas
class HistoryAdapter : ListAdapter<GameEntity, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemGameHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemGameHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: GameEntity) {
            // Emoji y texto de resultado
            if (game.didWin) {
                binding.tvResultEmoji.text = "🏆"
                binding.tvResultText.text = "VICTORIA"
            } else {
                binding.tvResultEmoji.text = "😔"
                binding.tvResultText.text = "DERROTA"
            }

            // Fecha relativa
            val timeAgo = DateUtils.getRelativeTimeSpanString(
                game.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            binding.tvDate.text = timeAgo

            // Código de sala
            binding.tvRoomCode.text = game.roomCode

            // Nombres y puntuaciones
            binding.tvPlayerName.text = game.playerName
            binding.tvPlayerScore.text = game.playerScore.toString()
            binding.tvOpponentName.text = game.opponentName
            binding.tvOpponentScore.text = game.opponentScore.toString()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GameEntity>() {
        override fun areItemsTheSame(oldItem: GameEntity, newItem: GameEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GameEntity, newItem: GameEntity): Boolean {
            return oldItem == newItem
        }
    }
}