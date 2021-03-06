package com.example.careemui.placeSearch.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.careemui.R
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.CameraPosition


fun updateCameraBearing(mMap: GoogleMap) {
    val camPos = CameraPosition
        .builder(
            mMap.cameraPosition
        )
        .bearing(200f)
        .build()
    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
}

fun resetCameraBearing(mMap: GoogleMap) {
    val camPos = CameraPosition
        .builder(
            mMap.cameraPosition
        )
        .bearing(0f)
        .build()
    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
}

fun getMarkerBitmapFromView(context: Context, estimatedTime: String): Bitmap? {
    val customMarkerView: View =
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.layout_marker,
            null
        )
    val timeToReach = customMarkerView.findViewById<View>(R.id.markerText) as TextView
    timeToReach.text = context.getString(R.string.marker_estimated_time, estimatedTime)
    customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    customMarkerView.layout(
        0,
        0,
        customMarkerView.measuredWidth,
        customMarkerView.measuredHeight
    )
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

fun getFilledMarkerBitmap(context: Context): Bitmap? {
    val px = context.resources.getDimensionPixelSize(R.dimen.marker_size)
    val filledMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
    val canvas1 = Canvas(filledMarkerBitmap)
    val shape1 = ContextCompat.getDrawable(context, R.drawable.filled_marker)
    shape1?.setBounds(0, 0, filledMarkerBitmap.width, filledMarkerBitmap.height)
    shape1?.draw(canvas1)
    return filledMarkerBitmap
}