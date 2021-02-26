package com.example.careemui.placeSearch

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.careemui.databinding.PlaceSearchListItemBinding
import com.example.careemui.placeSearch.PlacesAutoCompleteAdapter.PredictionHolder
import com.example.careemui.placeSearch.`interface`.OnListItemClickListener
import java.util.*

/**
 * Created by developer on 14/3/17.
 */
class PlacesAutoCompleteAdapter(
    private val mContext: Context,
    private val listener: OnListItemClickListener
) :
    RecyclerView.Adapter<PredictionHolder>() {
    private var mResultList: ArrayList<PlacesDetail>? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PredictionHolder {
        val layoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return PredictionHolder(
            PlaceSearchListItemBinding.inflate(
                layoutInflater,
                viewGroup,
                false
            )
        )
    }

    override fun onBindViewHolder(mPredictionHolder: PredictionHolder, position: Int) {
        var placesDetail: PlacesDetail? = null
        if (mResultList != null) placesDetail = mResultList!![position]
        placesDetail?.let {
            mPredictionHolder.bind(it, position)
        }
    }

    fun submitList(mResultList: ArrayList<PlacesDetail>?) {
        this.mResultList = mResultList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (mResultList != null) mResultList!!.size else 0
    }

    fun getItem(position: Int): PlacesDetail? {
        return if (mResultList != null) mResultList!![position] else null
    }

    inner class PredictionHolder(private val placeSearchListItemBinding: PlaceSearchListItemBinding) :
        RecyclerView.ViewHolder(placeSearchListItemBinding.root) {
        fun bind(placesDetail: PlacesDetail, position: Int) {
            placeSearchListItemBinding.tvPlaceName.text = placesDetail.labelName
            placeSearchListItemBinding.tvAddress.text = placesDetail.address

            placeSearchListItemBinding.root.setOnClickListener {
                listener.onListItemClick(placesDetail, position)
            }
        }

    }

    companion object {
        private const val TAG = "PlacesAutoCompleteAdapter"
    }
}