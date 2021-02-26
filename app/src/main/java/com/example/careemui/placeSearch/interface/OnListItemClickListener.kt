package com.example.careemui.placeSearch.`interface`

import com.example.careemui.placeSearch.PlacesDetail

interface OnListItemClickListener {
    fun onListItemClick(placesDetail: PlacesDetail, position: Int)
}