package com.example.careemui.booking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.careemui.R
import com.example.careemui.booking.adapters.CarModelListAdapter
import com.example.careemui.booking.data.CarModelData
import com.example.careemui.databinding.ActivityBookingBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
        val marker1 = LatLng(-34.0, 151.0)
        mMap.addMarker(
            MarkerOptions()
                .position(marker1)
                .anchor(.5f, .5f)
                .icon(
                    BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView("1", this))
                )
                .title("Marker in Sydney")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker1))
    }

    private fun getMarkerBitmapFromView(time: String, c: Context): Bitmap? {
        val customMarkerView: View =
            (c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.layout_marker,
                null
            )
        val time_to_reach = customMarkerView.findViewById<View>(R.id.markerText) as TextView
        time_to_reach.text = "$time\nmin"
        if (time == "0") time_to_reach.visibility = View.GONE else time_to_reach.visibility =
            View.VISIBLE
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        customMarkerView.layout(
            0,
            0,
            customMarkerView.measuredWidth,
            customMarkerView.measuredHeight
        )
        customMarkerView.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(
            customMarkerView.measuredWidth, customMarkerView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val drawable = customMarkerView.background
        drawable?.draw(canvas)
        customMarkerView.draw(canvas)
        return returnedBitmap
    }

    private fun setAdapter() {
        val adapter = CarModelListAdapter(this, carModelList)
        binding.recyclerView.adapter = adapter
    }
}