package com.uta.gasmaster

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TomTomApiService {
    @GET("search/2/poiSearch/{query}.json")
    fun searchPOI(
        @Path("query") query: String,
        @Query("key") apiKey: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("radius") radius: Int,
        @Query("categorySet") categorySet: Int,
        @Query("relatedPois") relatedPois: String
    ): Call<PoiSearchResponse>
}
//Sets up how the API is called and the information sent to it