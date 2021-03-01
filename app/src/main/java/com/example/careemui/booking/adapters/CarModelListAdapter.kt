package com.example.careemui.booking.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.careemui.booking.data.CarModelData
import com.example.careemui.databinding.LayoutCarModelListBinding

class CarModelListAdapter(
    private var context: Context,
    private var carModelList: ArrayList<CarModelData>?,
    private var mCheckedPosition: Int = -1
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
                this.ivImage.setImageResource(carModelData.image)
                this.name.text = carModelData.name
                this.specification.text = carModelData.specification
                this.time.text = carModelData.time
                if (mCheckedPosition == position) {
                    this.cardView.isSelected = true
                }
                this.cardView.setOnClickListener {
                    mCheckedPosition = if (mCheckedPosition != position) {
                        position
                    } else {
                        -1
                    }
                    notifyDataSetChanged()
                }

            }
        }
    }
}