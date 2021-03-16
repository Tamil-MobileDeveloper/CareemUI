package com.example.careemui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.careemui.booking.ARG_DROP_DETAIL
import com.example.careemui.booking.ARG_PICK_UP_DETAIL
import com.example.careemui.booking.BookingActivity
import com.example.careemui.databinding.ActivityPickupDropLocationBinding
import com.example.careemui.placeSearch.*
import com.example.careemui.placeSearch.`interface`.OnGeoCodeResult
import com.example.careemui.placeSearch.ui.DropLocationActivity
import com.example.careemui.placeSearch.utils.GetAddressFromLatLng
import com.example.careemui.placeSearch.utils.getFilledMarkerBitmap
import com.example.careemui.placeSearch.utils.getMarkerBitmapFromView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions

private const val REQUEST_LOCATION_PERMISSION = 123
private const val REQUEST_LOCATION_SEARCH = 125
private const val REQUEST_DROP_LOCATION_SEARCH = 128

class PickUpDropLocationActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener, OnGeoCodeResult {
    private lateinit var binding: ActivityPickupDropLocationBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val addressHandler by lazy { Handler(Looper.getMainLooper()) }
    private var canGetAddress = false
    private var canGetDropAddress = false
    private var pickUpLatLng: LatLng? = null
    private var placeDetail: PlacesDetail? = null
    private var dropPlaceDetail: PlacesDetail? = null
    private var dropOffMarker: Marker? = null
    private var dropLatLng: LatLng? = null
    private var viewType: Int = 1
    private var addressRunnable = Runnable {
        pickUpLatLng?.let {
            GetAddressFromLatLng(
                this, this, it
            ).execute()
        }
    }
    private var dropAddressRunnable = Runnable {
        dropLatLng?.let {
            GetAddressFromLatLng(
                this, this, it
            ).execute()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickupDropLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showTransparentStatusBar()
        showPickUpAnimation()
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
                        this@PickUpDropLocationActivity,
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
                showDropLocation()
            }

            fabCurrentLocation.setOnClickListener {
                mMap.clear()
                getLastKnownLocationAndSetMarker()
            }

            tvDropLabel.setOnClickListener {
                startActivityForResult(
                    Intent(
                        this@PickUpDropLocationActivity,
                        LocationSearchActivity::class.java
                    ).apply {
                        putExtras(Bundle().apply {
                            putInt(ARG_PLACE_SEARCH_TYPE, ARG_PLACE_SEARCH_TYPE_DROP)
                        })
                    },
                    REQUEST_DROP_LOCATION_SEARCH
                )
            }
            ivDropSearch.setOnClickListener { tvDropLabel.performClick() }
            tvDropAddress.setOnClickListener { tvDropLabel.performClick() }
            fabCurrent.setOnClickListener {
                getLastKnownLocationAndSetMarker()
            }
            btnAddDropOff.setOnClickListener {
                startActivity(
                    Intent(
                        this@PickUpDropLocationActivity,
                        BookingActivity::class.java
                    ).apply {
                        putExtras(Bundle().apply {
                            putParcelable(ARG_PICK_UP_DETAIL, placeDetail)
                            putParcelable(ARG_DROP_DETAIL, dropPlaceDetail)
                        })
                    }
                )
            }
            btnSkip.setOnClickListener {
                startActivity(
                    Intent(
                        this@PickUpDropLocationActivity,
                        BookingActivity::class.java
                    ).apply {
                        putExtras(Bundle().apply {
                            putParcelable(ARG_PICK_UP_DETAIL, placeDetail)
                            putParcelable(ARG_DROP_DETAIL, dropPlaceDetail)
                        })
                    }
                )
            }

            fabBack.setOnClickListener {
                showPickUpLocation()
            }
        }
    }

    private fun showDropLocation() {
        viewType = 2
        with(binding) {
            layoutPickUpLocation.visibility = View.GONE
            layoutDropLocation.visibility = View.VISIBLE
            cardDrop.startAnimation(
                AnimationUtils.loadAnimation(
                    this@PickUpDropLocationActivity,
                    R.anim.slide_down
                )
            )
            btnDrop.startAnimation(
                AnimationUtils.loadAnimation(
                    this@PickUpDropLocationActivity,
                    R.anim.slide_up
                )
            )

            dropLocationLabel.background =
                ContextCompat.getDrawable(
                    this@PickUpDropLocationActivity,
                    R.drawable.map_gradient_drawable
                )
        }
        mMap.clear()
        dropPlaceDetail = null
        dropLatLng = null
        binding.tvDropLabel.text = null
        binding.tvDropAddress.apply {
            text = null
            visibility = View.GONE
        }
        setPickUpAddress(placeDetail)
        setMarkerForPickUp()
    }

    override fun onBackPressed() {
        showPickUpLocation()
    }

    private fun showPickUpLocation() {
        if (viewType == 2) {
            viewType = 1
            val mSlideDownAnim = AnimationUtils.loadAnimation(
                this@PickUpDropLocationActivity,
                R.anim.slide_down_out
            )
            binding.cardDrop.startAnimation(mSlideDownAnim)
            binding.btnDrop.startAnimation(
                AnimationUtils.loadAnimation(
                    this@PickUpDropLocationActivity,
                    R.anim.slide_up_out
                )
            )
            mSlideDownAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    binding.layoutDropLocation.visibility = View.GONE
                    binding.layoutPickUpLocation.visibility = View.VISIBLE
                    binding.cardPickUp.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@PickUpDropLocationActivity,
                            R.anim.slide_down
                        )
                    )
                    binding.confirm.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@PickUpDropLocationActivity,
                            R.anim.slide_up
                        )
                    )
                    mMap.clear()
                    pickUpLatLng?.let { it ->
                        setMarkerAndAnimate(it)
                    }
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }
            })
        } else {
            finish()
        }
    }

    private fun showTransparentStatusBar() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
    }

    private fun showPickUpAnimation() {
        binding.cardPickUp.startAnimation(
            AnimationUtils.loadAnimation(
                this@PickUpDropLocationActivity,
                R.anim.slide_down
            )
        )
        binding.confirm.startAnimation(
            AnimationUtils.loadAnimation(
                this@PickUpDropLocationActivity,
                R.anim.slide_up
            )
        )
        binding.pickUpLabel.background =
            ContextCompat.getDrawable(this, R.drawable.map_gradient_drawable)
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

    private fun initializeMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun updateSearchResult(
        placeDetail: PlacesDetail?,
        setMarkerAndAnimate: Boolean = false
    ) {
        placeDetail?.let {
            this.placeDetail = it
            with(binding) {
                tvSearchLocation.text = it.labelName
                tvAddress.text = it.address
                tvAddress.visibility = View.VISIBLE
            }
            if (setMarkerAndAnimate)
                setMarkerAndAnimate(it.latLng)
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
                    if (viewType == 1) {
                        val lastKnownLatLng =
                            LatLng(it.latitude, it.longitude).also { latLng ->
                                pickUpLatLng = latLng
                            }
                        setMarkerAndAnimate(lastKnownLatLng)
                        showAddressFetchingLoader()
                        addressHandler.postDelayed(addressRunnable, 1500)
                    } else {
                        if (it.latitude != 0.0 && it.longitude != 0.0) {
                            val lastKnownLatLng =
                                LatLng(it.latitude, it.longitude).also { latLng ->
                                    dropLatLng = latLng
                                }
//                            setMarkerAndAnimate(lastKnownLatLng)
                            getAddressForCurrentLocation()
                        }
                    }
                }
            }
    }

    private fun getAddressForCurrentLocation() {
        if (canGetDropAddress) {
            addressHandler.removeCallbacks(dropAddressRunnable)
            showAddressLoader()
            addressHandler.postDelayed(dropAddressRunnable, 1500)
        }
    }

    private fun setMarkerAndAnimate(latLng: LatLng) {
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(this, "1")))
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
    }

    private fun setMarkerForLocation(latLng: LatLng) {
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(this, "1")))
        )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOCATION_SEARCH) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    updateSearchResult(
                        it.extras?.getParcelable(
                            ARG_PLACE_SEARCH_DETAIL
                        ), setMarkerAndAnimate = true
                    )
                }
            }
        } else if (requestCode == REQUEST_DROP_LOCATION_SEARCH) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    updateDropLocationResult(
                        it.extras?.getParcelable(
                            ARG_PLACE_SEARCH_DETAIL
                        )
                    )
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

    private fun updateDropLocationResult(dropDetail: PlacesDetail?) {
        dropDetail?.let {
            dropPlaceDetail = it
            binding.tvDropLabel.text = it.labelName
            binding.tvDropAddress.apply {
                text = it.address
                visibility = View.VISIBLE
            }
            setMarkerForDropLocation(LatLng(it.latitude, it.longitude), moveCameraToPosition = true)
        }
        toggleDropOffButtonState(placeDetail != null)
    }

    private fun setMarkerForDropLocation(latLng: LatLng, moveCameraToPosition: Boolean = false) {
        if (moveCameraToPosition)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
    }

    private fun setMarkerForPickUp() {
        if (viewType == 2) {
            placeDetail?.let {
                mMap.let { googleMap ->
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(it.latLng)
                            .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    getMarkerBitmapFromView(this, "1")
                                )
                            )
                    )
                    val pickUp = LatLng(it.latLng.latitude - 0.0004, it.latLng.longitude + 0.0004)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickUp, 17.0f))
                }
            }
        }
    }

    private fun toggleDropOffButtonState(isEnabled: Boolean) {
        binding.btnAddDropOff.isEnabled = isEnabled
    }

    private fun showAddressLoader() {
        binding.progressAddressView.visibility = View.VISIBLE
    }

    private fun hideAddressLoader() {
        binding.progressAddressView.visibility = View.GONE
    }

    override fun onCameraIdle() {
        if (viewType == 2) {
            if (canGetDropAddress) {
                val latLng = mMap.cameraPosition.target
                addressHandler.removeCallbacks(dropAddressRunnable)
                if (latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                    showAddressLoader()
                    setMarkerForDropLocation(latLng)
                    dropLatLng = latLng
                    addressHandler.postDelayed(dropAddressRunnable, 1500)
                }
            }
        } else {
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
    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            if (viewType == 2) {
                canGetDropAddress = true
                dropOffMarker?.remove()
            } else {
                canGetAddress = true
                mMap.clear()
                binding.ivPickLocation.visibility = View.VISIBLE
            }
        }
    }

    override fun onGeoCodeResult(placesDetail: PlacesDetail?) {
        if (viewType == 2) {
            updateDropLocationResult(placesDetail)
            canGetDropAddress = false
            hideAddressLoader()
        } else {
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
}