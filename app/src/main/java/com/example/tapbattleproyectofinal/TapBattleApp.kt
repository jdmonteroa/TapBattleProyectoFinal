package com.example.tapbattleproyectofinal

import android.app.Application
import android.util.Log
import com.parse.Parse
import com.parse.livequery.ParseLiveQueryClient
import java.net.URI

class TapBattleApp : Application() {

    companion object {
        // Se declara una variable que almacena el LiveQuery
        // para recibir actualizaciones en tiempo real
        lateinit var parseLiveQueryClient: ParseLiveQueryClient
    }

    override fun onCreate() {
        super.onCreate()

        // Se activan los logs detallados para
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG)

        // Inicializar Parse con la configuración de Back4App
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("lnDThU7eRKTT3of6hvkw1SxowOMox2bp36GCvBji")
                .clientKey("2e9m6wSNvV0sbOJlcVVK0ltFBhdHKX0Jdf3WVEeP")
                .server("https://parseapi.back4app.com/")
                .build()
        )

        // URL del servidor LiveQuery para la actualización en tiempo real
        val liveQueryUrl = "wss://tapbattleproyecto.b4a.io/"

        // Se crea el cliente con la URL
        parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(URI.create(liveQueryUrl))

        // Se imprime en Logcat la URL usada para LiveQuery
        Log.d("APP", "LiveQuery URL: $liveQueryUrl")
    }
}
