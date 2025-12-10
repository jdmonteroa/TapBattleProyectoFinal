package com.example.tapbattleproyectofinal.models

import org.json.JSONObject


// Representa un objetivo (círculo) en el juego

data class Target(
    val spawnId: String,
    val cx: Float,      // Coordenada X del centro
    val cy: Float,      // Coordenada Y del centro
    val r: Float,       // Radio del círculo
    val ttlMs: Long,    // Tiempo de vida en milisegundos
    val spawnTime: Long = System.currentTimeMillis()
) {

    //Verifica si el jugador toco en los limites del circulo
    fun containsPoint(x: Float, y: Float): Boolean {
        val dx = x - cx
        val dy = y - cy
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
        return distance <= r
    }


    //Calcula si el objetivo ya expiró
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - spawnTime >= ttlMs
    }

    //Obtiene el progreso de vida del circulo
    fun getLifeProgress(): Float {
        val elapsed = System.currentTimeMillis() - spawnTime
        return (elapsed.toFloat() / ttlMs).coerceIn(0f, 1f)
    }
}

//Tipos de eventos del servidor

sealed class GameEvent {
    data class Start(
        val score: Map<String, Int>,
        val round: Int,
        val maxRounds: Int
    ) : GameEvent()

    data class Spawn(val target: Target) : GameEvent()

    data class Score(
        val winner: String,
        val score: Map<String, Int>,
        val round: Int,
        val maxRounds: Int,
        val spawnId: String
    ) : GameEvent()

    data class End(
        val champion: String,
        val score: Map<String, Int>,
        val roundsPlayed: Int,
        val maxRounds: Int
    ) : GameEvent()

    companion object {

        //Parsea un JSONObject a un GameEvent
        fun fromJson(type: String, payload: JSONObject): GameEvent? {
            return try {
                when (type) {
                    "START" -> Start(
                        score = parseScore(payload.optJSONObject("score")),
                        round = payload.optInt("round", 1),
                        maxRounds = payload.optInt("maxRounds", 5)
                    )

                    "SPAWN" -> {
                        val target = Target(
                            spawnId = payload.optString("spawnId", ""),
                            cx = payload.optDouble("cx", 0.0).toFloat(),
                            cy = payload.optDouble("cy", 0.0).toFloat(),
                            r = payload.optDouble("r", 50.0).toFloat(),
                            ttlMs = payload.optLong("ttlMs", 2500L)
                        )
                        Spawn(target)
                    }

                    "SCORE" -> Score(
                        winner = payload.optString("winner", ""),
                        score = parseScore(payload.optJSONObject("score")),
                        round = payload.optInt("round", 1),
                        maxRounds = payload.optInt("maxRounds", 5),
                        spawnId = payload.optString("spawnId", "")
                    )

                    "END" -> End(
                        champion = payload.optString("champion", ""),
                        score = parseScore(payload.optJSONObject("score")),
                        roundsPlayed = payload.optInt("roundsPlayed", 0),
                        maxRounds = payload.optInt("maxRounds", 5)
                    )

                    else -> null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun parseScore(json: JSONObject?): Map<String, Int> {
            if (json == null) return emptyMap()
            val map = mutableMapOf<String, Int>()
            json.keys().forEach { key ->
                map[key] = json.optInt(key, 0)
            }
            return map
        }
    }
}


//Estado actual del juego
data class GameState(
    val roomId: String = "",
    val roomCode: String = "",
    val playerName: String = "",
    val currentTarget: Target? = null,
    val score: Map<String, Int> = emptyMap(),
    val round: Int = 0,
    val maxRounds: Int = 5,
    val isGameStarted: Boolean = false,
    val isGameEnded: Boolean = false,
    val champion: String? = null
) {
    //Puntaje del jugador actual
    fun getPlayerScore(): Int = score[playerName] ?: 0

    //Puntaje del oponente
    fun getOpponentScore(): Int {
        return score.entries.firstOrNull { it.key != playerName }?.value ?: 0
    }

    //Nombre del oponente
    fun getOpponentName(): String {
        return score.keys.firstOrNull { it != playerName } ?: "Esperando..."
    }
}