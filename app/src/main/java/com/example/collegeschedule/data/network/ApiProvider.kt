package com.example.collegeschedule.data.network

import com.example.collegeschedule.data.api.ScheduleApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiProvider {

    private const val EMULATOR_URL = "http://10.0.2.2:5085/"
    private const val LOCAL_NETWORK_URL = "http://192.168.0.12:5085/"

    private fun createRetrofit(baseUrl: String): ScheduleApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScheduleApi::class.java)
    }

    private val emulatorApi = createRetrofit(EMULATOR_URL)
    private val localNetworkApi = createRetrofit(LOCAL_NETWORK_URL)

    suspend fun <T> request(block: suspend (ScheduleApi) -> T): T {
        return try {
            block(emulatorApi)
        } catch (e: Exception) {
            block(localNetworkApi)
        }
    }
}
