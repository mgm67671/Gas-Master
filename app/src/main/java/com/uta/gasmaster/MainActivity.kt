package com.uta.gasmaster

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GasStationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val call = RetrofitClient.service.searchPOI("gas-station", "4OcLWbam2Vh83vTf9bTYdy3x8aZhfhVo", 37.337, -121.89, 10, 7315)
        call.enqueue(object : Callback<PoiSearchResponse> {
            override fun onResponse(call: Call<PoiSearchResponse>, response: Response<PoiSearchResponse>) {
                if (response.isSuccessful) {
                    val poiResults = response.body()?.results ?: emptyList()
                    adapter = GasStationsAdapter(poiResults)
                    recyclerView.adapter = adapter
                } else {
                    // Handle error case
                }
            }

            override fun onFailure(call: Call<PoiSearchResponse>, t: Throwable) {
                // Handle failure case
            }
        })
    }
}
