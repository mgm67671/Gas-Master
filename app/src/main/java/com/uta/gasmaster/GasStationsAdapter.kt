package com.uta.gasmaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GasStationsAdapter(private val gasStations: List<PoiResult>) : RecyclerView.Adapter<GasStationsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val addressTextView: TextView = view.findViewById(R.id.addressTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gas_station, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val poiResult = gasStations[position]
        holder.nameTextView.text = poiResult.poi.name
        holder.addressTextView.text = poiResult.address.freeformAddress
    }

    override fun getItemCount(): Int = gasStations.size
}
