package com.example.careemui.placeSearch.utils

import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.CameraPosition


fun updateCameraBearing(mMap: GoogleMap) {
    val camPos = CameraPosition
        .builder(
            mMap.cameraPosition
        )
        .bearing(200f)
        .build()
    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
}

fun resetCameraBearing(mMap: GoogleMap) {
    val camPos = CameraPosition
        .builder(
            mMap.cameraPosition
        )
        .bearing(0f)
        .build()
    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
}