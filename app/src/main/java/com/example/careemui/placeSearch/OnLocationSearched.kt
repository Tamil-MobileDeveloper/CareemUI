package com.example.careemui.placeSearch


interface OnLocationSearched {
    fun onLocationSearched(queryString: String)
    fun onItemClicked(placesDetail: PlacesDetail)
}