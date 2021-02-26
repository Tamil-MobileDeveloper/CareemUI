package com.example.careemui.placeSearch

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.example.careemui.databinding.FragmentLocationSearchBinding
import com.example.careemui.placeSearch.`interface`.OnListItemClickListener
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [LocationSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class LocationSearchFragment : Fragment(), OnLocationSearched, PlaceSearchList,
    OnListItemClickListener {

    //    lateinit var bookTaxiHomeRepository: BookTaxiHomeRepository
    private lateinit var binding: FragmentLocationSearchBinding
    private var str: String = ""

    private val mList: ArrayList<PlacesDetail> by lazy { ArrayList() }

    private var mResultList: ArrayList<PlacesDetail> = ArrayList()

    override fun onItemClicked(placesDetail: PlacesDetail) {
        //No need to handle anything here
    }

    override fun setPlaceList(placeDetailResult: ArrayList<PlacesDetail>?) {
        mResultList.clear()
        if (mList.size > 0) {
            mResultList.addAll(mList)
        }
        if (placeDetailResult != null) {
            mResultList.addAll(placeDetailResult)
        }
        mAutoCompleteAdapter.notifyDataSetChanged()
    }

    override fun setPlaceDetail(placeDetail: PlacesDetail) {
        listener?.onPlaceSelected(placeDetail)
    }

    private lateinit var onPlaceSearchedListener: OnLocationSearched
    override fun onLocationSearched(queryString: String) {
        println("onLocationSearched $queryString")
        /*bookTaxiHomeRepository.getFavPlaceWithFilter(queryString).observe(viewLifecycleOwner, android.arch.lifecycle.Observer {
            if (it != null) {
                mList.clear()
                if (it.isNotEmpty()) {
                    for (i in 0 until it.size) {
                        var placesDetail = PlacesDetail()
                        placesDetail.location_name = it[i].location_name
                        placesDetail.latitude = it[i].latitude
                        placesDetail.longtitute = it[i].longtitute
                        placesDetail.label_name = it[i].label_name
                        if (it[i].type.toInt() == 1) {
                            placesDetail.placeType = -1
                        } else if (it[i].type.toInt() == 2) {
                            placesDetail.placeType = -2
                        } else {
                            placesDetail.placeType = -3
                        }
                        placesDetail.placeId = it[i]._id
                        mList.add(placesDetail)
                    }
                }
            }
        })
        onPlaceSearchedListener.onLocationSearched(queryString)*/
        onPlaceSearchedListener.onLocationSearched(queryString)
    }

    private var isFourSquare = false
    private var listener: SetPlaceResult? = null
    private lateinit var mAutoCompleteAdapter: PlacesAutoCompleteAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        bookTaxiHomeRepository = BookTaxiHomeRepository(context)
        if (context is SetPlaceResult) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SetPlaceResult")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { context ->
            binding.imgPoweredBy.visibility = View.GONE
            onPlaceSearchedListener = GooglePlaceRepository(context, this@LocationSearchFragment)

            mAutoCompleteAdapter = PlacesAutoCompleteAdapter(context, this@LocationSearchFragment)
            mAutoCompleteAdapter.submitList(mResultList)
            binding.rvLocationItems.adapter = mAutoCompleteAdapter

        }
    }

    private fun getFavouritesList(context: Context): ArrayList<PlacesDetail> {
        val favouritesList = ArrayList<PlacesDetail>()
        /*if (!SessionSave.getSession("popular_places", context).isNullOrEmpty()) {
            try {
                val popularPlaces = JSONArray(SessionSave.getSession("popular_places", context))
                for (i in 0 until popularPlaces.length()) {
                    val jo = popularPlaces.getJSONObject(i)
                    favouritesList.add(PlacesDetail().apply {
                        setLabel_name(jo.getString("label_name"))
                        setLatitude(jo.getDouble("latitude"))
                        setLongtitute(jo.getDouble("longtitute"))
                        setLocation_name(jo.getString("location_name"))
                        setAndroid_image_unfocus(jo.getString("android_icon"))
                        setPlaceId("")
                        setPlaceType(1)
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }*/
        return favouritesList
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onListItemClick(placesDetail: PlacesDetail, position: Int) {
        val view = activity?.currentFocus
        if (view != null) {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if (imm.isAcceptingText) {
                val im =
                    requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                im.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            }
        }
        onPlaceSearchedListener.onItemClicked(
            placesDetail
        )
    }
}
