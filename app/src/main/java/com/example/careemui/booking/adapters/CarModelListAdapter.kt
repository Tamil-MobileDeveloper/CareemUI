package com.example.careemui.booking.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.careemui.R
import com.example.careemui.`interface`.CarBookingInterface
import com.example.careemui.booking.data.CarModelData
import com.example.careemui.databinding.LayoutCarModelListBinding

class CarModelListAdapter(
    private var context: Context,
    private var carModelList: ArrayList<CarModelData>?,
    private var bookingInterface: CarBookingInterface,
) : RecyclerView.Adapter<CarModelListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutCarModelListBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CarModelListAdapter.ViewHolder, position: Int) {
        carModelList?.get(position).let { holder.setValue(it!!, position) }
    }

    override fun getItemCount(): Int {
        return carModelList!!.size
    }

    inner class ViewHolder(private val binding: LayoutCarModelListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setValue(carModelData: CarModelData, position: Int) {
            with(binding) {
                ivImage.setImageResource(carModelData.image)
                name.text = carModelData.name
                specification.text = carModelData.specification
                time.text = carModelData.time
                if (carModelData.mCheckedPosition == position) {
                    cardView.isSelected = true
                    val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(18, 10, 18, 10)
                    cardView.layoutParams = params
                    name.isSelected = true
                    cardView.cardElevation = context.resources.getDimension(R.dimen.dp10)
                    cardView.radius = context.resources.getDimension(R.dimen.dp10)
                    cardView.preventCornerOverlap = true
                    cardView.useCompatPadding = true
                    carModelData.mCheckedPosition = -1
                } else {
                    cardView.cardElevation = context.resources.getDimension(R.dimen.dp0)
                    cardView.isSelected = false
                    name.isSelected = false
                }
                cardView.setOnClickListener {
                    if (carModelData.mCheckedPosition != position) {
                        carModelData.mCheckedPosition = position
                        if (cardView.isSelected) {
                            bookingInterface.confirmBooking(carModelData)
                        }
                    } else {
                        carModelData.mCheckedPosition = -1
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }
}