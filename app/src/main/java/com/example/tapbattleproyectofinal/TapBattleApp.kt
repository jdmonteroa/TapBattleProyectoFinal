package com.example.tapbattleproyectofinal

import android.app.Application
import com.parse.Parse
import com.parse.livequery.ParseLiveQueryClient

class TapBattleApp : Application() {

    companion object {
        lateinit var parseLiveQueryClient: ParseLiveQueryClient
    }

    override fun onCreate() {
        super.onCreate()

        // Configuración de Parse SDK para Back4App
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("lnDThU7eRKTT3of6hvkw1SxowOMox2bp36GCvBji")
                .clientKey("2e9m6wSNvV0sbOJlcVVK0ltFBhdHKX0Jdf3WVEeP")
                .server("https://parseapi.back4app.com/")
                .build()
        )

        // Inicializar LiveQuery Client
        parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
    }
}