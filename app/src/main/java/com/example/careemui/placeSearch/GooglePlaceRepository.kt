package com.example.careemui.placeSearch

import android.content.Context
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.cos


class GooglePlaceRepository(private val mContext: Context, private val listener: PlaceSearchList) :
    OnLocationSearched, Filterable {
    private var mResultList: ArrayList<PlacesDetail>? = null
    private var mBounds: RectangularBounds? = null

    private var placesClient: PlacesClient
    private val token: AutocompleteSessionToken

    init {
        /*if (SessionSave.getSession("PLAT", mContext) != "") {
            try {
                val lat = java.lang.Double.parseDouble(SessionSave.getSession("PLAT", mContext))
                val lon = java.lang.Double.parseDouble(SessionSave.getSession("PLNG", mContext))
                mBounds = RectangularBounds.newInstance(
                    getBoundingBox(lat, lon, 50000).southwest,
                    getBoundingBox(lat, lon, 50000).northeast
                )
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

        } else*/
        mBounds = null

        placesClient = Places.createClient(mContext)
        token = AutocompleteSessionToken.newInstance()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint != null) {
                    mResultList = getAutocomplete(constraint)
                    if (mResultList != null) {
                        results.values = mResultList
                        results.count = mResultList?.size ?: 0

                    }
                }
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults?) {
            }
        }
    }

    override fun onLocationSearched(queryString: String) {
        this.filter.filter(queryString)
    }

    override fun onItemClicked(placesDetail: PlacesDetail) {
        if (placesDetail.placeType == 0) {

            val placeId = placesDetail.placeId
            val placeFields =
                listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.builder(placeId, placeFields).build()
            placesClient.fetchPlace(request).addOnSuccessListener {
                if (it != null) {
                    val places = it.place
                    places.latLng?.let { latLng ->
                        listener.setPlaceDetail(
                            PlacesDetail(
                                places.address.toString(),
                                places.name.toString(),
                                placeId,
                                latitude = latLng.latitude,
                                longitude = latLng.longitude
                            )
                        )
                    }
                } else {
                    Toast.makeText(mContext, "Place search failed", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                it.printStackTrace()
            }
        } else {
            listener.setPlaceDetail(placesDetail)

        }
    }

    private fun getAutocomplete(constraint: CharSequence): ArrayList<PlacesDetail> {
        val resultList = ArrayList<PlacesDetail>()
        val request = FindAutocompletePredictionsRequest.builder().let {
            it.locationBias = mBounds
            it.sessionToken = token
            it.query = constraint.toString()
            /*it.setCountry("AE")*/
            it.build()
        }

        val task =
            placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                for (prediction in response.autocompletePredictions) {
                    resultList.add(
                        PlacesDetail(
                            prediction.getPrimaryText(null).toString(),
                            prediction.getSecondaryText(null).toString(),
                            prediction.placeId,
                            placeType = 0
                        )
                    )
                }

                if (resultList.count() > 0)
                    listener.setPlaceList(resultList)
                else
                    listener.setPlaceList(null)

            }.addOnFailureListener { exception ->
                exception.printStackTrace()
                resultList.clear()
                listener.setPlaceList(null)
            }
        try {
            Tasks.await(task, 5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    private fun getBoundingBox(
        pLatitude: Double,
        pLongitude: Double,
        pDistanceInMeters: Int
    ): LatLngBounds {

        val boundingBox = DoubleArray(4)

        val latRadian = Math.toRadians(pLatitude)

        val degLatKm = 110.574235
        val degLongKm = 110.572833 * cos(latRadian)
        val deltaLat = pDistanceInMeters.toDouble() / 1000.0 / degLatKm
        val deltaLong = pDistanceInMeters.toDouble() / 1000.0 /
                degLongKm

        val minLat = pLatitude - deltaLat
        val minLong = pLongitude - deltaLong
        val maxLat = pLatitude + deltaLat
        val maxLong = pLongitude + deltaLong

        boundingBox[0] = minLat
        boundingBox[1] = minLong
        boundingBox[2] = maxLat
        boundingBox[3] = maxLong
        return LatLngBounds(LatLng(minLat, minLong), LatLng(maxLat, maxLong))
    }

}