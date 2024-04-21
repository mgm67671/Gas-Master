package com.uta.gasmaster

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GasStationAdapter
(
    private val stations: List<GasStationResponse>,
    private val onStationClick: (GasStationResponse) -> Unit
) : RecyclerView.Adapter<GasStationAdapter.ViewHolder>()
{

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val stationName: TextView = view.findViewById(R.id.stationNameTextView)
        val address: TextView = view.findViewById(R.id.addressTextView)
        val regularGasPrice: TextView = view.findViewById(R.id.regularGasPriceTextView)
        val midGradeGasPrice: TextView = view.findViewById(R.id.midGradeGasPriceTextView)
        val premiumGasPrice: TextView = view.findViewById(R.id.premiumGasPriceTextView)
        val dieselPrice: TextView = view.findViewById(R.id.dieselPriceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val station = stations[position]

        holder.stationName.text = station.stationName
        holder.address.text = station.address.line1
        holder.regularGasPrice.text = buildString {
            append("Regular: ")
            append(formatPrice(station.prices.regular_gas))
        }
        holder.midGradeGasPrice.text = buildString {
            append("Mid-Grade: ")
            append(formatPrice(station.prices.midgrade_gas))
        }
        holder.premiumGasPrice.text = buildString {
            append("Premium: ")
            append(formatPrice(station.prices.premium_gas))
        }
        holder.dieselPrice.text = buildString {
            append("Diesel: ")
            append(formatPrice(station.prices.diesel))
        }
        holder.itemView.setOnClickListener {
            Log.d("GasStationAdapter", "Station clicked: ${station.stationName}")
            onStationClick(station)
        }
    }
    override fun getItemCount() = stations.size
}

private fun formatPrice(priceDetail: PriceDetail?): String
{
    return if (priceDetail?.price != null && priceDetail.price != 0.0)
    {
        "$${priceDetail.price}"
    }
    else
    {
        "N/A"
    }
}
