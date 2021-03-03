package com.example.careemui.placeSearch.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RotateDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
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

    /*Handler().postDelayed({
        val layerDrawable: LayerDrawable = timeToReach.background as LayerDrawable
        val drawables = layerDrawable.getDrawable(1) as RotateDrawable
        val mAnimator = ObjectAnimator.ofInt(drawables, "level", 0, 10000)
        mAnimator.duration = 5000
        mAnimator.start()
    }, 3000)*/

    return returnedBitmap
}