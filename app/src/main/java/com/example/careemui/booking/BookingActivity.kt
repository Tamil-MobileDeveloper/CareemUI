package com.example.careemui.booking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.careemui.R
import com.example.careemui.booking.adapters.CarModelListAdapter
import com.example.careemui.booking.data.CarModelData
import com.example.careemui.databinding.ActivityBookingBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class BookingActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityBookingBinding
    private lateinit var mMap: GoogleMap
    private val carModelList: ArrayList<CarModelData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        carModelList.add(
            CarModelData(
                R.drawable.car,
                "Max",
                "Rides upto 4 people",
                "3 mins"
            )
        )
        carModelList.add(
            CarModelData(
                R.drawable.car,
                "Max",
                "Rides upto 4 people",
                "3 mins"
            )
        )

        carModelList.add(
            CarModelData(
                R.drawable.car,
                "Max",
                "Rides upto 4 people",
                "3 mins"
            )
        )

        carModelList.add(
            CarModelData(
                R.drawable.car,
                "Max",
                "Rides upto 4 people",
                "3 mins"
            )
        )
        carModelList.add(
            CarModelData(
                R.drawable.car,
                "Max",
                "Rides upto 4 people",
                "3 mins"
            )
        )
        carModelList.add(
            CarModelData(
                R.drawable.car,
                "Max",
                "Rides upto 4 people",
                "3 mins"
            )
        )
        carModelList.add(
            CarModelData(
                R.drawable.car,
                "Max",
                "Rides upto 4 people",
                "3 mins"
            )
        )
        carModelList.add(
            CarModelData(
                R.drawable.car,
                "Max",
                "Rides upto 4 people",
                "3 mins"
            )
        )
        carModelList.add(
            CarModelData(
                R.drawable.car,
                "Max",
                "Rides upto 4 people",
                "3 mins"
            )
        )
        carModelList.add(
            CarModelData(
                R.drawable.car,
                "Max",
                "Rides upto 4 people",
                "3 mins"
            )
        )
        setAdapter()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun setAdapter() {
        val adapter = CarModelListAdapter(this, carModelList)
        binding.recyclerView.adapter = adapter
    }
}