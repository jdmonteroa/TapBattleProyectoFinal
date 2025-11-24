package com.example.tapbattleproyectofinal

import android.app.Application
import android.util.Log
import com.parse.Parse
import com.parse.livequery.ParseLiveQueryClient
import java.net.URI

class TapBattleApp : Application() {

    companion object {
        lateinit var parseLiveQueryClient: ParseLiveQueryClient
    }

    override fun onCreate() {
        super.onCreate()

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG)

        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("lnDThU7eRKTT3of6hvkw1SxowOMox2bp36GCvBji")
                .clientKey("2e9m6wSNvV0sbOJlcVVK0ltFBhdHKX0Jdf3WVEeP")
                .server("https://parseapi.back4app.com/")
                .build()
        )

        val liveQueryUrl = "wss://tapbattleproyecto.b4a.io/"

        parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(URI.create(liveQueryUrl))

        Log.d("APP", "LiveQuery URL: $liveQueryUrl")
    }
}