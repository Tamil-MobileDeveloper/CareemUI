package com.example.careemui.booking.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
                /*  googleMap.addMarker(
                      MarkerOptions()
                          .position(latLng)
                          .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup_location_pin))
                  )*/
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                getMarkerBitmapFromView(
                                    "1",
                                    requireActivity()
                                )
                            )
                        )
                )
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f))
            }

            dropPlaceDetail?.let {
                /*  googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin))
                )*/
                val latLng = LatLng(it.latitude, it.longitude)
                val px = resources.getDimensionPixelSize(R.dimen.marker_size)
                val filledMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
                val canvas1 = Canvas(filledMarkerBitmap)
                val shape1 = ContextCompat.getDrawable(requireActivity(), R.drawable.filled_marker);
                shape1?.setBounds(0, 0, filledMarkerBitmap.width, filledMarkerBitmap.height)
                shape1?.draw(canvas1)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(filledMarkerBitmap))
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getMarkerBitmapFromView(time: String, context: Context): Bitmap? {
        val customMarkerView: View =
            (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.layout_marker,
                null
            )
        val timeToReach = customMarkerView.findViewById<View>(R.id.markerText) as TextView
        timeToReach.text = "$time\nmin"
        if (time == "0") timeToReach.visibility = View.GONE else timeToReach.visibility =
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