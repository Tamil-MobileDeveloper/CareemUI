package com.example.careemui.placeSearch.`interface`

import com.example.careemui.placeSearch.PlacesDetail

interface OnGeoCodeResult {
    fun onGeoCodeResult(placesDetail: PlacesDetail?)
}