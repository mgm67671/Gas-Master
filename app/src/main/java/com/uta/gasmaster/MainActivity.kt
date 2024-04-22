package com.uta.gasmaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Locale

data class AddressLine
(
    val line1: String
) // Where the actual address for the station is stored
data class PriceDetail
(
    val credit: String?,
    val price: Double?,
    val lastUpdated: String?
) // Where further details of each type of gas is stored

data class Prices
(
    val stationID: String,
    val units: String,
    val currency: String,
    val latitude: Double,
    val longitude: Double,
    val regular_gas: PriceDetail?,
    val midgrade_gas: PriceDetail?,
    val premium_gas: PriceDetail?,
    val diesel: PriceDetail?
) // Where the details of all the gases for a station are

data class GasStationResponse
(
    val stationName: String,
    val stationId: String,
    val address: AddressLine,
    val prices: Prices
) // Where the basic data for each station goes

data class GasStationListResponse
(
    val stations: List<GasStationResponse>
) // An "array" of all the gas stations retrieved

interface ZipcodeCallback
{
    fun onZipcodeReceived(zipcode: String)
    fun onError(error: String)
} // Just tests if the zipcode is recieved correctly, so error checking

class MainActivity : AppCompatActivity(), ZipcodeCallback
{
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GasStationAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRecyclerView()

        val gasType = intent.getStringExtra("GAS_TYPE") ?: "Regular"
        Log.d("MainActivity", "Selected gas type: $gasType")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1000)
        }
        else
        {
            getLastKnownLocation(this)
        }
    } // Sets up the view and location clients and recieves the chosen gas type
    private fun getLastKnownLocation(callback: ZipcodeCallback)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            callback.onError("Location permission not granted")
            return
        } // Checks if location services are enabled

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null)
            {
                try
                {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val zipcode = addresses?.get(0)?.postalCode
                    if (zipcode != null)
                    {
                        callback.onZipcodeReceived(zipcode)
                    }
                    else
                    {
                        callback.onError("Zipcode not found")
                    }
                }
                catch (e: IOException)
                {
                    callback.onError("Error retrieving zipcode: ${e.localizedMessage}")
                }
            }
            else
            {
                callback.onError("Location is null")
            }
        }.addOnFailureListener { exception -> callback.onError("Error getting location: ${exception.message}") }
    } // Finds the user's location and finds the zipcode based on that

    override fun onZipcodeReceived(zipcode: String)
    {
        val gasType = intent.getStringExtra("GAS_TYPE") ?: "Regular"
        fetchGasPrices(zipcode, gasType)
    } // Once the zipcode is confirmed received, get the gas prices

    override fun onError(error: String)
    {
        Log.e("MainActivity", error)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    } // Displays message to user on error

    private fun fetchGasPrices(zipCode: String = "76063", sortBy: String = "Regular")
    {
        Thread {
            try
            {
                val client = OkHttpClient()
                val url = "http://10.0.2.2:5000/getGasPrices?zipcode=$zipCode"
                val request = Request.Builder().url(url).build()

                client.newCall(request).execute().use {
                    response -> val responseData = response.body?.string()
                    Log.d("GasPrices", "Response: $responseData")

                    if (responseData != null)
                    {
                        val gson = Gson()
                        try
                        {
                            val gasStationListResponse = gson.fromJson(responseData, GasStationListResponse::class.java)

                            val validStations = gasStationListResponse.stations.filter { it.hasValidPrice(sortBy) }
                            val sortedStations = validStations.sortedBy {
                                when (sortBy)
                                {
                                    "Regular" -> it.prices.regular_gas?.price ?: Double.MAX_VALUE
                                    "Mid-grade" -> it.prices.midgrade_gas?.price ?: Double.MAX_VALUE
                                    "Premium" -> it.prices.premium_gas?.price ?: Double.MAX_VALUE
                                    "Diesel" -> it.prices.diesel?.price ?: Double.MAX_VALUE
                                    else -> Double.MAX_VALUE
                                }
                            }
                            runOnUiThread { displayStations(sortedStations) }
                        }
                        catch (e: JsonSyntaxException)
                        {
                            Log.e("GasPrices", "JSON Parsing Error: ${e.message}")
                        }
                    }
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                Log.e("GasPrices", "Error: ${e.message}")
            }
        }.start()
    } // Calls the server and gets the stations and prices of stations in your zip code
    private fun setupRecyclerView()
    {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    } // Sets up the view displaying the stations

    private fun displayStations(stations: List<GasStationResponse>)
    {
        adapter = GasStationAdapter(stations)
        {
            station -> openMap(station.address.line1)
        }
        recyclerView.adapter = adapter
    } // Displays the gas stations, its prices, and its address

    private fun openMap(address: String)
    {
        val encodedAddress = Uri.encode(address)
        val uri = Uri.parse("geo:0,0?q=$encodedAddress")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        if (isGoogleMapsInstalled())
        {
            intent.setPackage("com.google.android.apps.maps")
        }

        if (intent.resolveActivity(packageManager) != null)
        {
            startActivity(intent)
        }
        else
        {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedAddress"))
            if (webIntent.resolveActivity(packageManager) != null)
            {
                startActivity(webIntent)
            }
            else
            {
                showToast("No application available to view maps.")
            }
        }
    } //  When a station is clicked on it opens maps to the station or displays a message to the user on failure


    private fun isGoogleMapsInstalled(): Boolean
    {
        return try
        {
            packageManager.getPackageInfo("com.google.android.apps.maps", PackageManager.GET_ACTIVITIES)
            true
        }
        catch (e: PackageManager.NameNotFoundException)
        {
            false
        }
    } // Checks if the user has Google Maps or not



    private fun showToast(message: String)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    } // Shows user a messae on an error
}

private fun GasStationResponse.hasValidPrice(gasType: String): Boolean
{
    return when (gasType)
    {
        "Regular" -> this.prices.regular_gas?.price?.let { it > 0 } ?: false
        "Mid-grade" -> this.prices.midgrade_gas?.price?.let { it > 0 } ?: false
        "Premium" -> this.prices.premium_gas?.price?.let { it > 0 } ?: false
        "Diesel" -> this.prices.diesel?.price?.let { it > 0 } ?: false
        else -> false
    }
} // Formats the prices of the gas

