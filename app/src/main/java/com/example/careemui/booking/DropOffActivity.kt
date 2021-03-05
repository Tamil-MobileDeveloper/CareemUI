package com.example.careemui.booking

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.careemui.R
import com.example.careemui.databinding.ActivityDropoffBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.*
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_dropoff.*

private const val REQUEST_LOCATION_PERMISSION = 123

class DropOffActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityDropoffBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDropoffBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeMap()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()
        binding.fabBack.setOnClickListener {
            finish()
        }
        binding.fabCurrentLocation.setOnClickListener {
            mMap.clear()
            getLastKnownLocationAndSetMarker()
        }
    }

    private fun initializeMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLastKnownLocationAndSetMarker()
    }

    private fun getLastKnownLocationAndSetMarker() {
        if (checkLocationPermission())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val lastKnownLatLng = LatLng(it.latitude, it.longitude)
                        setMarkerAndAnimate(lastKnownLatLng)
                    }
                }
    }


    private fun setMarkerAndAnimate(latLng: LatLng) {
        val px = resources.getDimensionPixelSize(R.dimen.marker_size)
        val filledMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas1 = Canvas(filledMarkerBitmap)
        val shape1 = ContextCompat.getDrawable(applicationContext, R.drawable.filled_marker);
        shape1?.setBounds(0, 0, filledMarkerBitmap.width, filledMarkerBitmap.height)
        shape1?.draw(canvas1)
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(filledMarkerBitmap))
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
    }

    private fun checkLocationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            false
        } else true
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocationAndSetMarker()
            }
        }
    }
}