package com.uta.gasmaster

import android.content.ContentProviderClient
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GasStationsAdapter
    // location stuff
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //Creates some UI elements
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("Hello","Hello")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Set the GUI to the one specified in activity_main

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        //Set up the preestablihed view

        //TODO: Retrieve user location for latitude and longitude for API call

        // if we do not already have permissions to fine location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()



        val call = RetrofitClient.service.searchPOI("gas-station", "4OcLWbam2Vh83vTf9bTYdy3x8aZhfhVo", latitude, longitude, 10, 7315) //Queries the API for all gas stations within 10 miles of a given latitude and longitude
        call.enqueue(object : Callback<PoiSearchResponse> {
            override fun onResponse(call: Call<PoiSearchResponse>, response: Response<PoiSearchResponse>) {
                if (response.isSuccessful) {
                    val poiResults = response.body()?.results ?: emptyList()
                    adapter = GasStationsAdapter(poiResults)
                    recyclerView.adapter = adapter
                } else {
                    // TODO: Handle error case
                }
            }
            //Inputs the results into the GUI to display the nearby gas stations

            //TODO: Send gas stations to fuel price API to get the prices for each station

            override fun onFailure(call: Call<PoiSearchResponse>, t: Throwable) {
                // TODO: Handle failure case
            }
        })
    }

    private fun getLocation()
    {
        // if we do not have fine location permissions
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Ask for them
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION
            ), 100)
        }
        // get the latest location data
        val location =  fusedLocationClient.lastLocation
        location.addOnSuccessListener{
            if(it != null)
            {
                latitude = it.latitude
                longitude = it.longitude
                val cordsText: TextView = findViewById(R.id.textView2)
                cordsText.setText("Cords: " + latitude.toString() + " " + longitude.toString())
            }
        }
    }
}
