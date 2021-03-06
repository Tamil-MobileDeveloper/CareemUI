package com.example.careemui.placeSearch

import android.os.Parcelable
import com.google.android.libraries.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlacesDetail(
    val address: String,
    val labelName: String,
    val placeId: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val latLng: LatLng = LatLng(latitude, longitude),
    val placeType: Int = 0
) : Parcelable
