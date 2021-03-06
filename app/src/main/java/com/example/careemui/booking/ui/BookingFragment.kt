package com.example.careemui.booking.ui

import android.content.Intent
import android.os.Bundle
import android.os.Trace.isEnabled
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.careemui.placeSearch.utils.getMarkerBitmapFromView
import com.example.careemui.placeSearch.utils.getFilledMarkerBitmap
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.os.TraceCompat.isEnabled
import androidx.fragment.app.Fragment
import com.example.careemui.R
import com.example.careemui.`interface`.CarBookingInterface
import com.example.careemui.booking.DropOffActivity
import com.example.careemui.booking.adapters.CarModelListAdapter
import com.example.careemui.booking.data.CarModelData
import com.example.careemui.databinding.FragmentBookingBinding
import com.example.careemui.placeSearch.PlacesDetail
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_booking.*

private const val ARG_PICK_UP_DETAIL = "pick_up_detail"
private const val ARG_DROP_DETAIL = "drop_detail"

/**
 * A simple [Fragment] subclass.
 * Use the [BookingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookingFragment : Fragment(), OnMapReadyCallback, CarBookingInterface {
    private var pickUpPlaceDetail: PlacesDetail? = null
    private var dropPlaceDetail: PlacesDetail? = null
    private lateinit var binding: FragmentBookingBinding
    private var mMap: GoogleMap? = null
    private val carModelList: ArrayList<CarModelData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pickUpPlaceDetail = it.getParcelable(ARG_PICK_UP_DETAIL)
            dropPlaceDetail = it.getParcelable(ARG_DROP_DETAIL)
        }
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bookingLayout.visibility == View.VISIBLE) {
                    hideCardView()
                } else {
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })
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

        for (i in 0 until 10) {
            carModelList.add(
                (CarModelData(
                    R.drawable.car,
                    "Business",
                    "Premium service rides",
                    "1 min"
                ))
            )
        }
        setAdapter()
    }

    private fun setAdapter() {
        val adapter = CarModelListAdapter(requireActivity(), carModelList, this)
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        fabBack.setOnClickListener {
            onBackPress()
        }
        tvDropLabel.setOnClickListener {
            startActivity(Intent(requireActivity(), DropOffActivity::class.java))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let {
            setPickUpDropMarker()
        }
    }

    private fun setPickUpDropMarker() {
        mMap?.let { googleMap ->
            pickUpPlaceDetail?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                setMarkerAndAnimate(latLng);
            }
            dropPlaceDetail?.let {
                val latLng = LatLng(it.latitude, it.longitude)
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
        }
    }

    private fun setMarkerAndAnimate(latLng: LatLng) {
        mMap?.addMarker(
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
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
    }

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

    override fun confirmBooking(carModelData: CarModelData) {
        Log.e("Selected CarModel", carModelData.name)
        showAnimation()
        binding.bookingLayout.visibility = View.VISIBLE
        binding.selectCarLayout.visibility = View.GONE
    }

    private fun showAnimation() {
        binding.pickUpDropLocation.startAnimation(
            AnimationUtils.loadAnimation(
                requireActivity(),
                R.anim.slide_down
            )
        )
        binding.cardBooking.startAnimation(
            AnimationUtils.loadAnimation(
                requireActivity(),
                R.anim.slide_up
            )
        )
    }

    private fun onBackPress() {
        if (binding.bookingLayout.visibility == View.VISIBLE) {
            hideCardView()
        } else {
            activity?.onBackPressed()
        }
    }

    private fun hideCardView() {
        binding.selectCarLayout.visibility = View.VISIBLE
        binding.bookingLayout.visibility = View.GONE
    }
}