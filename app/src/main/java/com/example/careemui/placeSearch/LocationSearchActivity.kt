package com.example.careemui.placeSearch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.careemui.R
import com.example.careemui.databinding.ActivityLocationSearchBinding

const val ARG_ADDRESS = "place_address"
const val ARG_ADDRESS_LABEL = "place_label"
const val ARG_LATITUDE = "place_latitude"
const val ARG_LONGITUDE = "place_longitude"
const val ARG_PLACE_ID = "place_id"
const val ARG_PLACE_SEARCH_TYPE = "place_search_type"
const val ARG_PLACE_SEARCH_DETAIL = "place_search_detail"
const val ARG_PLACE_SEARCH_TYPE_PICK_UP = 1
const val ARG_PLACE_SEARCH_TYPE_DROP = 2

class LocationSearchActivity : AppCompatActivity(), SetPlaceResult {

    private lateinit var locationSearchFragment: LocationSearchFragment
    private lateinit var listener: OnLocationSearched
    private lateinit var binding: ActivityLocationSearchBinding

    private var placeSearchType: Int = ARG_PLACE_SEARCH_TYPE_PICK_UP


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationSearchBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        locationSearchFragment = LocationSearchFragment()
        listener = locationSearchFragment
        supportFragmentManager.beginTransaction().add(R.id.searchFrame, locationSearchFragment)
            .commitNow()

        handleIntentValues()
    }

    override fun onStart() {
        super.onStart()
        binding.toolbarLayout.tvTitle.text = if(placeSearchType == ARG_PLACE_SEARCH_TYPE_DROP) getString(R.string.drop_off_location) else getString(R.string.pickup_location)
    }

    override fun onResume() {
        super.onResume()
        val textWatcher = object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length > 2) {
                    binding.searchFrame.visibility = View.VISIBLE
                    binding.locationList.visibility = View.GONE
                    listener.onLocationSearched(s.toString())
                } else {
                    binding.locationList.visibility = View.VISIBLE
                    binding.searchFrame.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                binding.imgClearSearch.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable) {
                binding.imgClearSearch.visibility = View.VISIBLE
            }
        }
        with(binding) {
            edLocation.addTextChangedListener(textWatcher)
            imgClearSearch.setOnClickListener {
                edLocation.setText("")
            }

            toolbarLayout.ivBack.setOnClickListener {
                onBackPressed()
            }
        }

    }

    override fun onPlaceSelected(placesDetail: PlacesDetail) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtras(Bundle().apply {
                /*putExtra(ARG_ADDRESS, placesDetail.address)
                putExtra(ARG_LATITUDE, placesDetail.latitude)
                putExtra(ARG_LONGITUDE, placesDetail.longitude)
                putExtra(ARG_PLACE_ID, placesDetail.placeId)
                putExtra(ARG_ADDRESS_LABEL, placesDetail.labelName)*/
                putParcelable(ARG_PLACE_SEARCH_DETAIL, placesDetail)
            })
        })
        finish()
    }

    private fun handleIntentValues() {
        intent?.extras?.let {
            placeSearchType = it.getInt(ARG_PLACE_SEARCH_TYPE, ARG_PLACE_SEARCH_TYPE_PICK_UP)
        }
    }
}
