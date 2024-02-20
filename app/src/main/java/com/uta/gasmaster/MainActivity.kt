package com.uta.gasmaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity()
{
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GasStationsAdapter
    private var latitude: Double = 37.337
    private var longitude: Double = -121.89
    private var radius: Int = 3000
    private val locationPermissionRequestCode = 0


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastKnownLocation()

            //TODO: Send gas stations to fuel price API to get the prices for each station
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), locationPermissionRequestCode)
            return
        }

        val locationRequest = LocationRequest.Builder(10000L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setIntervalMillis(1000L)
            .build()

        val locationCallback = object : LocationCallback()
        {
            override fun onLocationResult(locationResult: LocationResult)
            {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations)
                {
                    if (location != null)
                    {
                        latitude = location.latitude
                        longitude = location.longitude

                        // Call the Retrofit service here with the updated location
                        val call = RetrofitClient.service.searchPOI("gas-station", "4OcLWbam2Vh83vTf9bTYdy3x8aZhfhVo", latitude, longitude, radius, 7311, "all")
                        call.enqueue(object : Callback<PoiSearchResponse>
                        {
                            override fun onResponse(call: Call<PoiSearchResponse>, response: Response<PoiSearchResponse>)
                            {
                                if (response.isSuccessful)
                                {
                                    val poiResults = response.body()?.results ?: emptyList()
                                    adapter = GasStationsAdapter(poiResults)
                                    recyclerView.adapter = adapter
                                }
                                else
                                {
                                    //TODO: Handle error case
                                }
                            }

                            override fun onFailure(call: Call<PoiSearchResponse>, t: Throwable)
                            {
                                //TODO: Handle failure case
                            }
                        })

                        fusedLocationClient.removeLocationUpdates(this)
                        break
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getLastKnownLocation()
            }
            else
            {
                //TODO:Permissions were denied, handle accordingly
            }
        }
    }
}

