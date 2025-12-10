package com.example.tapbattleproyectofinal.utils

//Constantes globales del proyecto

object Constants {
    // Claves para Intent extras
    const val EXTRA_ROOM_ID = "ROOM_ID"
    const val EXTRA_ROOM_CODE = "ROOM_CODE"
    const val EXTRA_PLAYER_NAME = "PLAYER_NAME"
    const val EXTRA_WINNER = "WINNER"
    const val EXTRA_FINAL_SCORE = "FINAL_SCORE"

    // Configuración del juego
    const val MAX_ROUNDS = 5
    const val TARGET_TTL_MS = 2500L
    const val ANIMATION_DURATION = 300L

    // Dimensiones de pantalla por defecto
    const val DEFAULT_WIDTH = 1080
    const val DEFAULT_HEIGHT = 1920

    // Tamaños de objetivos
    const val MIN_TARGET_RADIUS = 50f
    const val MAX_TARGET_RADIUS = 100f

    // Tipos de eventos de Parse
    object EventType {
        const val START = "START" //Cuando comienza el juego
        const val SPAWN = "SPAWN" //Cuando presionan los objetivos
        const val SCORE = "SCORE" //Al momento de actualizar los puntajes
        const val END = "END" //Cuando finaliza el juego
    }

    // Nombres de Cloud Functions
    object CloudFunction {
        const val JOIN_ROOM = "joinRoom"
        const val START_GAME = "startGame"
        const val HIT_TARGET = "hitTarget"
        const val GET_ROOM_STATE = "getRoomState"
    }

    // Clases de Parse
    object ParseClass {
        const val EVENT = "Event"
        const val ROOM = "Room"
    }
}