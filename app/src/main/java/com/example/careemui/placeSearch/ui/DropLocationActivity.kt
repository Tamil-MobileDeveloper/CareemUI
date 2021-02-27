package com.example.careemui.placeSearch.ui

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
import com.example.careemui.R
import com.example.careemui.booking.BookingActivity
import com.example.careemui.databinding.ActivityDropLocationBinding
import com.example.careemui.placeSearch.*
import com.example.careemui.placeSearch.`interface`.OnGeoCodeResult
import com.example.careemui.placeSearch.utils.GetAddressFromLatLng
import com.example.careemui.placeSearch.utils.resetCameraBearing
import com.example.careemui.placeSearch.utils.updateCameraBearing
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*

private const val REQUEST_LOCATION_SEARCH = 124
private const val REQUEST_LOCATION_PERMISSION = 123

class DropLocationActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener, OnGeoCodeResult {
    private lateinit var binding: ActivityDropLocationBinding
    private var placeDetail: PlacesDetail? = null
    private var dropPlaceDetail: PlacesDetail? = null
    private var mMap: GoogleMap? = null
    private var dropOffMarker: Marker? = null
    private val addressHandler by lazy { Handler(Looper.getMainLooper()) }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var canGetAddress = false
    private var dropLatLng: LatLng? = null
    private var addressRunnable = Runnable {
        dropLatLng?.let {
            GetAddressFromLatLng(
                this, this, it
            ).execute()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDropLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeMap()
        handleIntentValues()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            tvDropAddress.setOnClickListener {
                startActivityForResult(
                    Intent(
                        this@DropLocationActivity,
                        LocationSearchActivity::class.java
                    ).apply {
                        putExtras(Bundle().apply {
                            putInt(ARG_PLACE_SEARCH_TYPE, ARG_PLACE_SEARCH_TYPE_DROP)
                        })
                    },
                    REQUEST_LOCATION_SEARCH
                )
            }
            ivDropSearch.setOnClickListener { tvDropAddress.performClick() }
            fabBack.setOnClickListener { finish() }
            fabCurrentLocation.setOnClickListener {
                getLastKnownLocationAndSetMarker()
            }
            btnConfirmLocation.setOnClickListener {
                startActivity(
                    Intent(
                        this@DropLocationActivity,
                        BookingActivity::class.java
                    )
                )
            }
            btnSkip.setOnClickListener {
                startActivity(
                    Intent(
                        this@DropLocationActivity,
                        BookingActivity::class.java
                    )
                )
            }
        }
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun handleIntentValues() {
        intent?.extras?.let {
            placeDetail = it.getParcelable(ARG_PLACE_SEARCH_DETAIL)
        }
        setPickUpAddress(placeDetail)
    }

    private fun showAddressFetchingLoader() {
        binding.ivDropLocationPin.visibility = View.GONE
        binding.progressAddress.visibility = View.VISIBLE
    }

    private fun hideAddressFetchingLoader() {
        binding.progressAddress.visibility = View.GONE
    }

    private fun setMarkerForLocation(latLng: LatLng, moveCameraToPosition: Boolean = false) {
        dropOffMarker?.remove()
        mMap?.let {
            dropOffMarker = it.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin))
            )
            if (moveCameraToPosition)
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
        }
    }

    private fun setMarkerForPickUp() {
        placeDetail?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            mMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup_location_pin))
            )
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
        }
    }

    private fun toggleConfirmButtonState(isEnabled: Boolean) {
        binding.btnConfirmLocation.isEnabled = isEnabled
    }

    private fun updateSearchResult(placeDetail: PlacesDetail?) {
        placeDetail?.let {
            dropPlaceDetail = it
            binding.tvDropAddress.text = it.address
            setMarkerForLocation(LatLng(it.latitude, it.longitude), moveCameraToPosition = true)
//            setLatLngBounds(it)
        }
        toggleConfirmButtonState(placeDetail != null)
    }

    private fun setLatLngBounds(dropPlaceDetail: PlacesDetail) {
        placeDetail?.let {
            val pickUpLatLng = LatLng(it.latitude, it.longitude)
            val builder = LatLngBounds.Builder()
            builder.include(pickUpLatLng)
            builder.include(LatLng(dropPlaceDetail.latitude, dropPlaceDetail.longitude))
            val bounds = builder.build()
            val width = binding.container.width
            val padding = (width * 0.15).toInt() // offset from edges of the map 10% of screen
            binding.cardPickUpLocation.post {
                val adjustHeight =
                    binding.container.height - (binding.cardPickUpLocation.height + binding.btnConfirmLocation.height + binding.fabBack.height + 50)
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, adjustHeight, padding)
                mMap?.let { map ->
                    map.moveCamera(cu)
                    if (map.projection.toScreenLocation(pickUpLatLng).x > width / 2) {
                        updateCameraBearing(map)
                    } else {
                        resetCameraBearing(map)
                    }
                }

            }
        }
    }

    private fun setPickUpAddress(placeDetail: PlacesDetail?) {
        placeDetail?.let {
            binding.tvPickUpAddress.text = it.address
            binding.tvPickUpLabel.text = it.labelName
        }
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
                    if (it.latitude != 0.0 && it.longitude != 0.0) {
                        val lastKnownLatLng =
                            LatLng(it.latitude, it.longitude).also { latLng -> dropLatLng = latLng }
                        setMarkerAndAnimate(lastKnownLatLng)
                        getAddressForCurrentLocation()
                    }
                }
            }
    }

    private fun getAddressForCurrentLocation() {
        if (canGetAddress && mMap != null) {
            addressHandler.removeCallbacks(addressRunnable)
            showAddressFetchingLoader()
            addressHandler.postDelayed(addressRunnable, 1500)
        }
    }

    private fun setMarkerAndAnimate(latLng: LatLng) {
        mMap?.let {
            it.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin))
            )
            it.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
        }
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let {
            it.setOnCameraIdleListener(this)
            it.setOnCameraMoveStartedListener(this)
            setMarkerForPickUp()
        }
    }

    override fun onCameraIdle() {
        if (canGetAddress && mMap != null) {
            val latLng = mMap!!.cameraPosition.target
            addressHandler.removeCallbacks(addressRunnable)
            if (latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                showAddressFetchingLoader()
                setMarkerForLocation(latLng)
                dropLatLng = latLng
                addressHandler.postDelayed(addressRunnable, 1500)
            }
        }
    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            canGetAddress = true
            dropOffMarker?.remove()
            binding.ivDropLocationPin.visibility = View.VISIBLE
        }
    }

    override fun onGeoCodeResult(placesDetail: PlacesDetail?) {
        updateSearchResult(placesDetail)
        canGetAddress = false
        hideAddressFetchingLoader()
    }
}