package com.uta.gasmaster

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.tomtom.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: TomTomApiService = retrofit.create(TomTomApiService::class.java)
}
// Sets up the API call(s) and how they retrieve data
