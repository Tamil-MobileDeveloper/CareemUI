package com.example.careemui.booking

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.careemui.R
import com.example.careemui.databinding.ActivityDropoffBinding
import com.example.careemui.placeSearch.*
import com.example.careemui.placeSearch.`interface`.OnGeoCodeResult
import com.example.careemui.placeSearch.utils.GetAddressFromLatLng
import com.example.careemui.placeSearch.utils.getMarkerBitmapFromView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.*
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_dropoff.*

private const val REQUEST_LOCATION_PERMISSION = 124
private const val REQUEST_LOCATION_SEARCH = 126
const val ARG_DROP_PLACE_DETAIL = "drop_place_detail"
const val ARG_CONFIRM_DROP_OFF_DETAIL = "confirm_drop_off_detail"

class DropOffActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener, OnGeoCodeResult {
    private lateinit var binding: ActivityDropoffBinding
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
        binding = ActivityDropoffBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        showAnimation()
        initializeMap()
        handleIntentValues()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            fabBack.setOnClickListener {
                hideAnimation()
            }
            cardDropOffLocation.setOnClickListener {
                startActivityForResult(
                    Intent(
                        this@DropOffActivity,
                        LocationSearchActivity::class.java
                    ).apply {
                        putExtras(Bundle().apply {
                            putInt(ARG_PLACE_SEARCH_TYPE, ARG_PLACE_SEARCH_TYPE_DROP)
                        })
                    },
                    REQUEST_LOCATION_SEARCH
                )
            }
            fabCurrentLocation.setOnClickListener {
                mMap.clear()
                getLastKnownLocationAndSetMarker()
            }

            btnConfirmDropOff.setOnClickListener {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtras(Bundle().apply {
                        putParcelable(ARG_CONFIRM_DROP_OFF_DETAIL, placeDetail)
                    })
                })
                hideAnimation()
//                finish()
            }
        }
    }

    override fun onBackPressed() {
        hideAnimation()
    }

    private fun showAnimation() {
        binding.cardView.background =
            ContextCompat.getDrawable(this, R.drawable.map_gradient_drawable)

        binding.cardDropOff.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.slide_down
            )
        )
        binding.btnDropOff.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.slide_up
            )
        )
    }

    private fun hideAnimation() {
        val mSlideDownAnim = AnimationUtils.loadAnimation(
            this@DropOffActivity,
            R.anim.slide_down_out
        )
        binding.cardDropOff.startAnimation(mSlideDownAnim)
        binding.btnDropOff.startAnimation(
            AnimationUtils.loadAnimation(
                this@DropOffActivity,
                R.anim.slide_up_out
            )
        )
        mSlideDownAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.cardDropOff.visibility = View.GONE
                binding.btnDropOff.visibility = View.GONE
                finish()
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
    }

    override fun onDestroy() {
        addressHandler.removeCallbacks(addressRunnable)
        super.onDestroy()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.let {
            it.setOnCameraIdleListener(this)
            it.setOnCameraMoveStartedListener(this)
            it.uiSettings.isMyLocationButtonEnabled = false
            if (checkLocationPermission())
                it.isMyLocationEnabled = true
            setMarkerForDropOff()
        }
    }

    private fun setMarkerForDropOff() {
        placeDetail?.let {
            mMap.let { googleMap ->
                googleMap.addMarker(
                    MarkerOptions()
                        .position(it.latLng)
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                getMarkerBitmapFromView(
                                    this,
                                    "1"
                                )
                            )
                        )
                )
                val dropOff = LatLng(it.latLng.latitude - 0.0004, it.latLng.longitude + 0.0004)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dropOff, 17.0f))
            }
        }
    }


    private fun initializeMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun handleIntentValues() {
        intent?.extras?.let {
            placeDetail = it.getParcelable(ARG_DROP_PLACE_DETAIL)
        }
        setDropOffAddress(placeDetail)
    }

    private fun setDropOffAddress(placeDetail: PlacesDetail?) {
        with(binding) {
            placeDetail?.let {
                tvSearchLocation.text = it.labelName
                tvAddress.text = it.address
                tvAddress.visibility = View.VISIBLE
            }
        }
    }

    private fun getLastKnownLocationAndSetMarker() {
        if (checkLocationPermission())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val lastKnownLatLng =
                            LatLng(it.latitude, it.longitude).also { latLng ->
                                pickUpLatLng = latLng
                            }
                        setMarkerAndAnimate(lastKnownLatLng)
                        showAddressFetchingLoader()
                        addressHandler.postDelayed(addressRunnable, 1500)
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
                        ), setMarkerAndAnimate = true
                    )
                }
            }
        }
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

    private fun setMarkerAndAnimate(latLng: LatLng) {
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(this, "1")))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
    }

    private fun toggleConfirmButtonState(isEnabled: Boolean) {
        binding.btnConfirmDropOff.isEnabled = isEnabled
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

    private fun setMarkerForLocation(latLng: LatLng) {
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(this, "1")))
        )
    }


    override fun onCameraMoveStarted(reason: Int) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            canGetAddress = true
            mMap.clear()
            binding.ivPickLocation.visibility = View.VISIBLE
        }
    }

    override fun onGeoCodeResult(placesDetail: PlacesDetail?) {
        updateSearchResult(placesDetail)
        canGetAddress = false
        hideAddressFetchingLoader()
    }

    private fun showAddressFetchingLoader() {
        binding.ivPickLocation.visibility = View.GONE
        binding.progressAddress.visibility = View.VISIBLE
    }

    private fun hideAddressFetchingLoader() {
        binding.progressAddress.visibility = View.GONE
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