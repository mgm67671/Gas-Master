package com.uta.gasmaster

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

data class PriceDetail(
    val credit: String,
    val price: Double,
    val last_updated: String
)

data class Prices(
    val station_id: String,
    val unit_of_measure: String,
    val currency: String,
    val latitude: Double,
    val longitude: Double,
    val regular_gas: PriceDetail,
    val premium_gas: PriceDetail,
    val diesel: PriceDetail
)

data class GasStationResponse(
    val stationName: String,
    val stationId: String,
    val address: String,
    val prices: Prices
)


class MainActivity : AppCompatActivity() {
    /*companion object {
        private const val DEFAULT_LATITUDE = 37.337
        private const val DEFAULT_LONGITUDE = -121.89
        private const val DEFAULT_RADIUS = 3000
        private const val LOCATION_PERMISSION_REQUEST_CODE = 0
        private const val LOCATION_UPDATE_INTERVAL = 10000L
        private const val LOCATION_FASTEST_INTERVAL = 1000L
        private const val KEY = "4OcLWbam2Vh83vTf9bTYdy3x8aZhfhVo"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GasStationsAdapter
    private var latitude: Double = 37.337
    private var longitude: Double = DEFAULT_LONGITUDE
    private var radius: Int = DEFAULT_RADIUS*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fetchGasPrices()
    }


    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastKnownLocation()
    }*/

    /*private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        val locationRequest = LocationRequest.Builder(LOCATION_UPDATE_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setIntervalMillis(LOCATION_FASTEST_INTERVAL)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude

                        //Call the Retrofit service here with the updated location
                        val call = RetrofitClient.service.searchPOI(
                            "gas-station",
                            KEY,
                            latitude,
                            longitude,
                            radius
                        )
                        call.enqueue(
                            object : Callback<PoiSearchResponse> {
                                override fun onResponse(
                                    call: Call<PoiSearchResponse>,
                                    response: Response<PoiSearchResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        val poiResults = response.body()?.results ?: emptyList()
                                        adapter = GasStationsAdapter(poiResults)
                                        recyclerView.adapter = adapter
                                    } else {
                                        TODO("Handle error case")
                                    }
                                }

                                override fun onFailure(
                                    call: Call<PoiSearchResponse>,
                                    t: Throwable
                                ) {
                                    TODO("Not yet implemented")
                                }
                            }
                        )

                        fusedLocationClient.removeLocationUpdates(this)
                        break
                    }
                }
            }
        }




        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }*/
    fun fetchGasPrices(zipCode: String = "76063") {
        Thread {
            try {
                val client = OkHttpClient()
                val url = "http://10.0.2.2:5000/getGasPrices?zipcode=$zipCode"
                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseData = response.body?.string()
                    Log.d("GasPrices", "Response: $responseData")

                    if (responseData != null) {
                        val gson = Gson()
                        val gasStationResponse = gson.fromJson(responseData, GasStationResponse::class.java)

                        runOnUiThread {
                            val textView = findViewById<TextView>(R.id.textView)
                            val pricesText = "Regular: $${gasStationResponse.prices.regular_gas.price}, " +
                                    "Premium: $${gasStationResponse.prices.premium_gas.price}, " +
                                    "Diesel: $${gasStationResponse.prices.diesel.price}"
                            textView.text = pricesText
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GasPrices", "Error: ${e.message}")
            }
        }.start()
    }




    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation()
            } else {
                TODO("Handle error case")
            }
        }
    }*/
}
