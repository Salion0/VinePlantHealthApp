package it.unipi.mobile.vineplanthealthapp.utils

import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.exifinterface.media.ExifInterface

class GalleryUtils {

    public fun setEditable(imageName: EditText , saveButton: ImageButton, revertButton:ImageButton, editButton:ImageButton) {
        saveButton.setVisibility(View.VISIBLE);
        revertButton.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.GONE);
        imageName.inputType = 1;
        imageName.isFocusableInTouchMode = true;
        imageName.isFocusable = true;
        imageName.requestFocus();
    }

    public fun setNotEditable(imageName: EditText , saveButton: ImageButton, revertButton:ImageButton, editButton:ImageButton) {
        saveButton.setVisibility(View.GONE);
        revertButton.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);
        imageName.inputType = 0;
        imageName.isFocusableInTouchMode = false;
        imageName.isFocusable = false;
        imageName.clearFocus();
    }

    public fun getGeoLocation(imagePath: String): Pair<Double, Double>? {
        val exifInterface = ExifInterface(imagePath)
        val latLong = FloatArray(2)
        val hasLatLong = exifInterface.getLatLong(latLong)
        if (hasLatLong && latLong[0] != 0.0f && latLong[1] != 0.0f) {
            return Pair(latLong[0].toDouble(), latLong[1].toDouble())
        }
        return null
    }
}