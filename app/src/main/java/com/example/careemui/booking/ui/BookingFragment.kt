package com.example.careemui.booking.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.careemui.R
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

private const val ARG_PICK_UP_DETAIL = "pick_up_detail"
private const val ARG_DROP_DETAIL = "drop_detail"

/**
 * A simple [Fragment] subclass.
 * Use the [BookingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookingFragment : Fragment(), OnMapReadyCallback {
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

    private fun setAdapter() {
        val adapter = CarModelListAdapter(requireActivity(), carModelList)
        binding.recyclerView.adapter = adapter
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
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup_location_pin))
                )
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
            }

            dropPlaceDetail?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin))
                )
            }
        }
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
}