package com.example.careemui.booking.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.example.careemui.R
import com.example.careemui.`interface`.CarBookingInterface
import com.example.careemui.booking.ARG_CONFIRM_DROP_OFF_DETAIL
import com.example.careemui.booking.ARG_DROP_PLACE_DETAIL
import com.example.careemui.booking.DropOffActivity
import com.example.careemui.booking.adapters.CarModelListAdapter
import com.example.careemui.booking.data.CarModelData
import com.example.careemui.databinding.FragmentBookingBinding
import com.example.careemui.placeSearch.*
import com.example.careemui.placeSearch.ui.DropLocationActivity
import com.example.careemui.placeSearch.utils.getFilledMarkerBitmap
import com.example.careemui.placeSearch.utils.getMarkerBitmapFromView
import com.example.careemui.placeSearch.utils.resetCameraBearing
import com.example.careemui.placeSearch.utils.updateCameraBearing
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.LatLngBounds
import com.google.android.libraries.maps.model.MarkerOptions

private const val ARG_PICK_UP_DETAIL = "pick_up_detail"
private const val ARG_DROP_DETAIL = "drop_detail"
private const val DROP_LOCATION = 128

/**
 * A simple [Fragment] subclass.
 * Use the [BookingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class BookingFragment : Fragment(), OnMapReadyCallback, CarBookingInterface,
    AdapterView.OnItemSelectedListener {
    private lateinit var binding: FragmentBookingBinding
    private var mMap: GoogleMap? = null
    private var pickUpPlaceDetail: PlacesDetail? = null
    private var dropPlaceDetail: PlacesDetail? = null
    private var placeDetail: PlacesDetail? = null
    private val carModelList: ArrayList<CarModelData> = ArrayList()
    private val spinnerList: ArrayList<String> = ArrayList()

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param pickUpPlace Parameter 1.
         * @param dropPlace Parameter 2.
         * @return A new instance of fragment BookingFragment.
         */
        @JvmStatic
        fun newInstance(pickUpPlace: PlacesDetail?, dropPlace: PlacesDetail?) =
            BookingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PICK_UP_DETAIL, pickUpPlace)
                    putParcelable(ARG_DROP_DETAIL, dropPlace)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pickUpPlaceDetail = it.getParcelable(ARG_PICK_UP_DETAIL)
            dropPlaceDetail = it.getParcelable(ARG_DROP_DETAIL)
        }
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.cardBookingDetails.visibility == View.VISIBLE && binding.cardPickUpDropLocation.visibility == View.VISIBLE) {
                    hideAnimation()
                } else {
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })
        activity?.window?.apply {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setPickUpDropAddress(pickUpPlaceDetail, dropPlaceDetail)

        binding.cardView.background =
            ContextCompat.getDrawable(requireActivity(), R.drawable.map_gradient_drawable)

        for (i in 0 until 10) {
            carModelList.add(
                (CarModelData(
                    R.drawable.car,
                    if (i % 2 == 0) "Sedan" else if (i % 3 == 0) "VIP" else "Business",
                    "Premium service rides",
                    "1 min"
                ))
            )
        }
        for (i in 0 until 5) {
            spinnerList.add(if (i == 0) "Select" else if (i % 2 == 0) "AED 30-40" else "AED 35-45")
        }
        setAdapter()
        setSpinnerList()
    }

    private fun setPickUpDropAddress(
        pickUpPlaceDetail: PlacesDetail?,
        dropPlaceDetail: PlacesDetail?
    ) {
        with(binding) {
            pickUpPlaceDetail?.let {
                tvPickUpLocation.text = it.labelName
                tvPickUpAddress.text = it.address
            }
            dropPlaceDetail?.let {
                tvDropLabel.text = it.labelName
                tvDropAddress.text = it.address
                tvDropAddress.visibility = View.VISIBLE
            }
        }
    }

    private fun setAdapter() {
        val adapter = CarModelListAdapter(requireActivity(), carModelList, this)
        binding.recyclerView.adapter = adapter
    }

    private fun setSpinnerList() {
        val arrayCash: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            spinnerList
        )
        arrayCash.setDropDownViewResource(android.R.layout.select_dialog_singlechoice)
        binding.spinAED.onItemSelectedListener = this
        binding.spinAED.adapter = arrayCash
        binding.spinCash.adapter = arrayCash
        binding.spinDiscount.adapter = arrayCash
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {
            if (position != 0) {
                val item = parent.getItemAtPosition(position).toString()
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            fabBack.setOnClickListener {
                onBackPress()
            }
            btnSelectRide.setOnClickListener {
                showAnimation()
            }
            tvDropLabel.setOnClickListener {
                startActivityForResult(
                    Intent(
                        requireActivity(),
                        DropOffActivity::class.java
                    ).apply {
                        putExtras(Bundle().apply {
                            putParcelable(ARG_DROP_PLACE_DETAIL, dropPlaceDetail)
                        })
                    },
                    DROP_LOCATION
                )
            }
            tvDropAddress.setOnClickListener { tvDropLabel.performClick() }
            ivFavorite.setOnClickListener { tvDropLabel.performClick() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DROP_LOCATION) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    updateSearchResult(
                        it.extras?.getParcelable(
                            ARG_CONFIRM_DROP_OFF_DETAIL
                        )
                    )
                }
            }
        }
    }

    private fun updateSearchResult(
        dropPlaceDetail: PlacesDetail?,
    ) {
        dropPlaceDetail?.let {
            this.dropPlaceDetail = it
            with(binding) {
                tvDropLabel.text = it.labelName
                tvDropAddress.apply {
                    text = it.address
                    visibility = View.VISIBLE
                }
            }
            mMap?.clear()
            mMap?.let { mMap -> setPickUpDropMarker(mMap) }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let {
            it.setPadding(10, 0, 0, 380)
            setPickUpDropMarker(it)
        }
    }

    private fun setPickUpDropMarker(mMap: GoogleMap) {
        mMap.let { googleMap ->
            pickUpPlaceDetail?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                setPickUpMarker(latLng, googleMap)
            }
            dropPlaceDetail?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                setDropOffMarker(latLng, googleMap)
            }
            if (pickUpPlaceDetail != null && dropPlaceDetail != null)
                setLatLngBounds(pickUpPlaceDetail!!, dropPlaceDetail!!)
        }
    }

    private fun setPickUpMarker(latLng: LatLng, googleMap: GoogleMap) {
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        getMarkerBitmapFromView(
                            requireActivity(),
                            "1"
                        )
                    )
                )
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
    }

    private fun setDropOffMarker(latLng: LatLng, googleMap: GoogleMap) {
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        getFilledMarkerBitmap(
                            requireActivity()
                        )
                    )
                )
        )
    }


    private fun setLatLngBounds(pickUpPlaceDetail: PlacesDetail, dropPlaceDetail: PlacesDetail) {
        val builder = LatLngBounds.Builder()
        builder.include(LatLng(pickUpPlaceDetail.latitude, pickUpPlaceDetail.longitude))
        builder.include(LatLng(dropPlaceDetail.latitude, dropPlaceDetail.longitude))
        val bounds = builder.build()
        val width = binding.fragmentContainer.width
        val padding = (width * 0.15).toInt() // offset from edges of the map 10% of screen
        binding.bottomSheet.post {
            val adjustHeight =
                binding.fragmentContainer.height - (binding.scrollView.height + binding.fabBack.height + 50)
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, adjustHeight, padding)
            mMap?.let { map ->
                map.moveCamera(cu)
                if (map.projection.toScreenLocation(pickUpPlaceDetail.latLng).x > width / 2) {
                    updateCameraBearing(map)
                } else {
                    resetCameraBearing(map)
                }
            }
        }
    }

    private fun onBackPress() {
        if (binding.cardBookingDetails.visibility == View.VISIBLE && binding.cardPickUpDropLocation.visibility == View.VISIBLE) {
            hideAnimation()
        } else {
            activity?.onBackPressed()
        }
    }

    private fun hideAnimation() {
        val mSlideDownAnim = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_down_out)
        binding.cardPickUpDropLocation.startAnimation(mSlideDownAnim)
        binding.cardBookingDetails.startAnimation(
            AnimationUtils.loadAnimation(
                requireActivity(),
                R.anim.slide_up_out
            )
        )
        mSlideDownAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                /*  activity?.window?.apply {
                      addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                      addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                      clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                      statusBarColor =
                          ContextCompat.getColor(requireActivity(), android.R.color.transparent)
                  }*/
                binding.btnSelectRide.visibility = View.VISIBLE
                binding.cardPickUpDropLocation.visibility = View.GONE
                binding.cardBookingDetails.visibility = View.GONE
                binding.selectCarLayout.visibility = View.VISIBLE
                binding.btnSelectRide.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireActivity(),
                        R.anim.slide_up
                    )
                )

                binding.selectCarLayout.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireActivity(),
                        R.anim.slide_up
                    )
                )
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
    }

    override fun onModelSelect(carModelData: CarModelData, isModelReselected: Boolean) {
        Log.e("Selected CarModel", carModelData.name)
        binding.btnSelectRide.visibility = View.VISIBLE
        binding.btnSelectRide.text = getString(R.string.select_ride_model, carModelData.name)
        if (isModelReselected) {
            showAnimation()
        }
    }

    private fun showAnimation() {
        /* activity?.window?.apply {
             clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
             addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 decorView.systemUiVisibility =
                     View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
             } else {
                 decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
             }
         }*/
        binding.selectCarLayout.visibility = View.GONE
        binding.btnSelectRide.visibility = View.GONE
        binding.cardPickUpDropLocation.visibility = View.VISIBLE
        binding.cardBookingDetails.visibility = View.VISIBLE

        binding.cardPickUpDropLocation.startAnimation(
            AnimationUtils.loadAnimation(
                requireActivity(),
                R.anim.slide_down
            )
        )
        binding.cardBookingDetails.startAnimation(
            AnimationUtils.loadAnimation(
                requireActivity(),
                R.anim.slide_up
            )
        )
    }
}