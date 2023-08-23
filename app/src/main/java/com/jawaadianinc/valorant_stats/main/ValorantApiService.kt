package com.jawaadianinc.valorant_stats.main

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers

interface ValorantApiService {
    @Headers("X-Riot-Token: RGAPI-77322163-520c-492f-aabe-6c29a39f44ff")
    @GET("val/content/v1/contents")
    suspend fun getContents(): ResponseBody // Adjust the response type as needed
}
