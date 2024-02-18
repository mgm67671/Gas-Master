package com.uta.gasmaster

data class PoiSearchResponse(val results: List<PoiResult>)
data class PoiResult(val id: String, val poi: Poi, val address: Address, val position: Position)
data class Poi(val name: String)
data class Address(val freeformAddress: String)
data class Position(val lat: Double, val lon: Double)
