package com.example.lasttime

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.lasttime.data.local.database.LastTimeDatabase
import com.example.lasttime.data.remote.CalendarApiService
import com.example.lasttime.data.repository.ActivityRepository
import com.example.lasttime.data.repository.CalendarRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Cria o banco, o cliente HTTP e os repositórios uma única vez para o app inteiro. */
class AppContainer(context: Context) {

    private val database: LastTimeDatabase =
        Room.databaseBuilder(context, LastTimeDatabase::class.java, "last_time.db")
            .fallbackToDestructiveMigration(true)
            .build()

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            // ignoreUnknownKeys: a API tem campos que o HolidayDto não declara
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 8_000
            requestTimeoutMillis = 10_000
            socketTimeoutMillis = 10_000
        }
    }

    val activityRepository: ActivityRepository by lazy {
        ActivityRepository(database.activityDao(), database.categoryDao())
    }

    val calendarRepository: CalendarRepository by lazy {
        CalendarRepository(CalendarApiService(httpClient))
    }
}

class LastTimeApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
