package com.example.tapbattleproyectofinal.data

import com.parse.ParseCloud
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.livequery.SubscriptionHandling
import com.example.tapbattleproyectofinal.models.GameEvent
import com.example.tapbattleproyectofinal.TapBattleApp
import com.example.tapbattleproyectofinal.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


//Servicio para comunicación con Back4App

class BackendService {

    //Une a un jugador a una sala
    suspend fun joinRoom(code: String): String = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val params = hashMapOf<String, Any>("code" to code)

            ParseCloud.callFunctionInBackground<HashMap<String, Any>>(
                Constants.CloudFunction.JOIN_ROOM,
                params
            ) { result, e ->
                if (e == null && result != null) {
                    val roomId = result["roomId"] as? String
                    if (roomId != null) {
                        continuation.resume(roomId)
                    } else {
                        continuation.resumeWithException(Exception("No se recibió roomId"))
                    }
                } else {
                    continuation.resumeWithException(e ?: Exception("Error desconocido"))
                }
            }
        }
    }


    //Inicia el juego
    suspend fun startGame(roomId: String): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val params = hashMapOf<String, Any>("roomId" to roomId)

            ParseCloud.callFunctionInBackground<String>(
                Constants.CloudFunction.START_GAME,
                params
            ) { result, e ->
                if (e == null) {
                    continuation.resume(true)
                } else {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    //Registra que un jugador tocó un objetivo
    suspend fun hitTarget(
        roomId: String,
        spawnId: String,
        playerName: String
    ): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val params = hashMapOf<String, Any>(
                "roomId" to roomId,
                "spawnId" to spawnId,
                "player" to playerName
            )

            ParseCloud.callFunctionInBackground<HashMap<String, Any>>(
                Constants.CloudFunction.HIT_TARGET,
                params
            ) { result, e ->
                if (e == null) {
                    continuation.resume(true)
                } else {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    //Suscribe a eventos de una sala usando LiveQuery
    fun subscribeToEvents(
        roomId: String,
        onEvent: (GameEvent) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            // Crear query para los eventos de la sala
            val room = ParseObject.createWithoutData(Constants.ParseClass.ROOM, roomId)
            val query = ParseQuery.getQuery<ParseObject>(Constants.ParseClass.EVENT)
            query.whereEqualTo("room", room)

            // Suscribirse a LiveQuery
            val subscription = TapBattleApp.parseLiveQueryClient.subscribe(query)

            // Manejar eventos CREATE (nuevos eventos)
            subscription.handleEvent(
                // ¡CORRECCIÓN! Usamos la clase simple 'SubscriptionHandling' ya importada.
                SubscriptionHandling.Event.CREATE
            ) { _, obj ->
                try {
                    val type = obj.getString("type") ?: return@handleEvent
                    val payload = obj.getJSONObject("payload") ?: return@handleEvent

                    val event = GameEvent.fromJson(type, payload)
                    if (event != null) {
                        onEvent(event)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onError(e)
                }
            }

            // Manejar errores de suscripción
            subscription.handleError { _, e ->
                e.printStackTrace()
                onError(Exception(e))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            onError(e)
        }
    }

    //Obtiene el estado actual de una sala
    suspend fun getRoomState(roomId: String): Triple<Map<String, Int>, Int, Int>? =
        withContext(Dispatchers.IO) {
            try {
                suspendCancellableCoroutine { continuation ->
                    val params = hashMapOf<String, Any>("roomId" to roomId)

                    ParseCloud.callFunctionInBackground<HashMap<String, Any>>(
                        Constants.CloudFunction.GET_ROOM_STATE,
                        params
                    ) { result, e ->
                        if (e == null && result != null) {
                            @Suppress("UNCHECKED_CAST")
                            val score = result["score"] as? Map<String, Int> ?: emptyMap()
                            val round = (result["round"] as? Number)?.toInt() ?: 0
                            val maxRounds = (result["maxRounds"] as? Number)?.toInt() ?: 5
                            continuation.resume(Triple(score, round, maxRounds))
                        } else {
                            continuation.resume(null)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}