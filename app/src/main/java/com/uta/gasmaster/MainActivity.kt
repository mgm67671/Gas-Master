package com.uta.gasmaster

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

data class AddressLine
(
    val line1: String
)
data class PriceDetail(
    val credit: String?,
    val price: Double?,
    val lastUpdated: String?
)

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
)

data class GasStationResponse
(
    val stationName: String,
    val stationId: String,
    val address: AddressLine,
    val prices: Prices
)

data class GasStationListResponse
(
    val stations: List<GasStationResponse>
)


class MainActivity : AppCompatActivity()
{
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gasType = intent.getStringExtra("GAS_TYPE") ?: "Regular"
        Log.d("MainActivity", "Selected gas type: $gasType")
        fetchGasPrices(gasType)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1000)
        }
        else
        {
            getLastKnownLocation()
        }
    }

    private fun getLastKnownLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
                location -> if (location != null)
                {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val zipcode = addresses?.get(0)?.postalCode
                    if (zipcode != null)
                    {
                        fetchGasPrices(zipCode = zipcode)
                    }
                }
        }.addOnFailureListener{ Log.e("MainActivity", "Error getting location", it) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            getLastKnownLocation()
        }
    }

    private fun fetchGasPrices(zipCode: String, sortBy: String = "Regular")
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

                            val sortedStations = gasStationListResponse.stations.filter { it.hasValidPrice(sortBy)}.sortedBy {
                                station -> when (sortBy)
                                {
                                    "Regular" -> station.prices.regular_gas?.price ?: Double.MAX_VALUE
                                    "Mid-grade" -> station.prices.midgrade_gas?.price ?: Double.MAX_VALUE
                                    "Premium" -> station.prices.premium_gas?.price ?: Double.MAX_VALUE
                                    "Diesel" -> station.prices.diesel?.price ?: Double.MAX_VALUE
                                    else -> Double.MAX_VALUE
                                }
                            }

                            runOnUiThread {
                                val textView = findViewById<TextView>(R.id.textView)
                                var displayText = ""
                                for (station in sortedStations)
                                {
                                    displayText +=
                                        "Station: ${station.stationName}\n" +
                                        "Address: ${station.address.line1}\n" +
                                        when (sortBy)
                                        {
                                            "Regular" -> "Regular: ${formatPrice(station.prices.regular_gas)}\n"
                                            "Mid-grade" -> "Mid-grade: ${formatPrice(station.prices.midgrade_gas)}\n"
                                            "Premium" -> "Premium: ${formatPrice(station.prices.premium_gas)}\n"
                                            "Diesel" -> "Diesel: ${formatPrice(station.prices.diesel)}\n\n"
                                            else -> ""
                                        } + "\n"
                                }
                                textView.text = displayText
                            }
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
    }
}

private fun formatPrice(priceDetail: PriceDetail?): String {
    return if (priceDetail?.price != null && priceDetail.price != 0.0)
    {
        "$${priceDetail.price}"
    }
    else
    {
        "N/A"
    }
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
}

