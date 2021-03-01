package com.example.careemui.booking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.careemui.booking.ui.BookingFragment
import com.example.careemui.databinding.ActivityBookingBinding
import com.example.careemui.placeSearch.PlacesDetail

const val ARG_PICK_UP_DETAIL = "pick_up_detail"
const val ARG_DROP_DETAIL = "drop_detail"

class BookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding
    private var pickUpPlaceDetail: PlacesDetail? = null
    private var dropPlaceDetail: PlacesDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleIntentValues()
        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .add(
                    binding.container.id,
                    BookingFragment.newInstance(pickUpPlaceDetail, dropPlaceDetail)
                ).commitNow()
    }

    private fun handleIntentValues() {
        intent?.extras?.let {
            pickUpPlaceDetail = it.getParcelable(ARG_PICK_UP_DETAIL)
            dropPlaceDetail = it.getParcelable(ARG_DROP_DETAIL)
        }
    }
}