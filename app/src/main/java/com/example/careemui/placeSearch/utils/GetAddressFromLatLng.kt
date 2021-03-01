package com.example.careemui.placeSearch.utils

import android.content.Context
import android.location.Geocoder
import android.os.AsyncTask
import com.example.careemui.R
import com.example.careemui.placeSearch.PlacesDetail
import com.example.careemui.placeSearch.`interface`.OnGeoCodeResult
import com.google.android.libraries.maps.model.LatLng
import java.util.*


class GetAddressFromLatLng(
    private val context: Context,
    private val onGeoCodeResult: OnGeoCodeResult,
    private val latLng: LatLng
) :
    AsyncTask<String, String, PlacesDetail?>() {
    private val geoCoder: Geocoder by lazy { Geocoder(context, Locale.getDefault()) }
    private var address: String? = ""
    override fun doInBackground(vararg p0: String?): PlacesDetail? {

        if (Geocoder.isPresent()) {
            try {
                val addressList = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 2)
                if (!addressList.isNullOrEmpty()) {
                    for (i in 0 until addressList.size) {
                        address += addressList[i].getAddressLine(0).toString() + ", "
                    }
                    address?.let {
                        if (it.isNotEmpty()) {
                            address = it.substring(0, it.length - 2)

                            return PlacesDetail(
                                it,
                                context.getString(R.string.current_location),
                                Date().time.toString(),
                                latLng.latitude,
                                latLng.longitude,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        return null
    }

    override fun onPostExecute(result: PlacesDetail?) {
        super.onPostExecute(result)
        onGeoCodeResult.onGeoCodeResult(result)
    }
}