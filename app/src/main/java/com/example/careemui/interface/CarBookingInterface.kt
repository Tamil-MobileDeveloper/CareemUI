package com.example.careemui.`interface`

import com.example.careemui.booking.data.CarModelData

interface CarBookingInterface {
    fun onModelSelect(carModelData: CarModelData, isModelReselected: Boolean)
}