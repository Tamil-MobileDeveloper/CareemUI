package com.example.careemui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.careemui.databinding.ActivityMapLocationBinding
import com.example.careemui.placeSearch.*
import com.example.careemui.placeSearch.`interface`.OnGeoCodeResult
import com.example.careemui.placeSearch.ui.DropLocationActivity
import com.example.careemui.placeSearch.utils.GetAddressFromLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions

private const val REQUEST_LOCATION_PERMISSION = 123
private const val REQUEST_LOCATION_SEARCH = 124

class MapLocationActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener, OnGeoCodeResult {
    private lateinit var binding: ActivityMapLocationBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val addressHandler by lazy { Handler(Looper.getMainLooper()) }
    private var canGetAddress = false
    private var pickUpLatLng: LatLng? = null
    private var placeDetail: PlacesDetail? = null
    private var addressRunnable = Runnable {
        pickUpLatLng?.let {
            GetAddressFromLatLng(
                this, this, it
            ).execute()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeMap()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            cardPickUpLocation.setOnClickListener {
                startActivityForResult(
                    Intent(
                        this@MapLocationActivity,
                        LocationSearchActivity::class.java
                    ).apply {
                        putExtras(Bundle().apply {
                            putInt(ARG_PLACE_SEARCH_TYPE, ARG_PLACE_SEARCH_TYPE_PICK_UP)
                        })
                    },
                    REQUEST_LOCATION_SEARCH
                )
            }
            btnConfirmLocation.setOnClickListener {
                startActivity(
                    Intent(
                        this@MapLocationActivity,
                        DropLocationActivity::class.java
                    ).apply {
                        putExtras(Bundle().apply {
                            putParcelable(
                                ARG_PLACE_SEARCH_DETAIL,
                                placeDetail
                            )
                        })
                    }
                )
            }

            fabCurrentLocation.setOnClickListener {
                mMap.clear()
                getLastKnownLocationAndSetMarker()
            }
        }

    }

    override fun onDestroy() {
        addressHandler.removeCallbacks(addressRunnable)
        super.onDestroy()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnCameraIdleListener(this)
        mMap.setOnCameraMoveStartedListener(this)
        mMap.uiSettings.isMyLocationButtonEnabled = false
        if (checkLocationPermission())
            mMap.isMyLocationEnabled = true
        getLastKnownLocationAndSetMarker()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOCATION_SEARCH) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    updateSearchResult(
                        it.extras?.getParcelable(
                            ARG_PLACE_SEARCH_DETAIL
                        )
                    )
                }
            }
        }
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun updateSearchResult(placeDetail: PlacesDetail?) {
        placeDetail?.let {
            this.placeDetail = it
            with(binding) {
                tvSearchLocation.text = it.labelName
                tvAddress.text = it.address
                tvAddress.visibility = View.VISIBLE
            }
        }
        toggleConfirmButtonState(placeDetail != null)
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

    private fun getLastKnownLocationAndSetMarker() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val lastKnownLatLng =
                        LatLng(it.latitude, it.longitude).also { latLng -> pickUpLatLng = latLng }
                    setMarkerAndAnimate(lastKnownLatLng)
                    showAddressFetchingLoader()
                    addressHandler.postDelayed(addressRunnable, 1500)
                }
            }
    }

    private fun setMarkerAndAnimate(latLng: LatLng) {
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin))
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
    }

    private fun setMarkerForLocation(latLng: LatLng) {
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin))
        )
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
    }

    private fun showAddressFetchingLoader() {
        binding.ivPickLocation.visibility = View.GONE
        binding.progressAddress.visibility = View.VISIBLE
    }

    private fun hideAddressFetchingLoader() {
        binding.progressAddress.visibility = View.GONE
    }

    private fun toggleConfirmButtonState(isEnabled: Boolean) {
        binding.btnConfirmLocation.isEnabled = isEnabled
    }

    override fun onCameraIdle() {
        if (canGetAddress) {
            val latLng = mMap.cameraPosition.target
            addressHandler.removeCallbacks(addressRunnable)
            if (latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                showAddressFetchingLoader()
                setMarkerForLocation(latLng)
                pickUpLatLng = latLng
                addressHandler.postDelayed(addressRunnable, 1500)
            }
        }
    }


    override fun onCameraMoveStarted(reason: Int) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            canGetAddress = true
            mMap.clear()
            binding.ivPickLocation.visibility = View.VISIBLE
        }
    }

    override fun onGeoCodeResult(placesDetail: PlacesDetail?) {
        if (placesDetail != null)
            updateSearchResult(placesDetail)
        else
            pickUpLatLng?.let {
                updateSearchResult(
                    PlacesDetail(
                        "${String.format("%.2f", it.latitude)}, ${
                            String.format(
                                "%.2f",
                                it.longitude
                            )
                        }",
                        getString(R.string.current_location),
                        "",
                        it.latitude,
                        it.longitude
                    )
                )
            }

        canGetAddress = false
        hideAddressFetchingLoader()
    }
}