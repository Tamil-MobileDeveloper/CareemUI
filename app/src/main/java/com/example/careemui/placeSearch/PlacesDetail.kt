package com.example.careemui.placeSearch

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlacesDetail(
    val address: String,
    val labelName: String,
    val placeId: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val placeType: Int = 0
) : Parcelable
