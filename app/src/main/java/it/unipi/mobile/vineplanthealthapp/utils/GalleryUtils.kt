package it.unipi.mobile.vineplanthealthapp.utils

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.exifinterface.media.ExifInterface
import it.unipi.mobile.vineplanthealthapp.Config
import it.unipi.mobile.vineplanthealthapp.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GalleryUtils(){

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

    public fun getTimestamp(imagePath: String): LocalDateTime? {
        val exifInterface = ExifInterface(imagePath)
        val timestamp = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)
        return timestamp?.let {
            val formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
            LocalDateTime.parse(it, formatter)
        }
    }

    public fun getPlantStatus(context: Context, imagePath: String): String?{
        val exifInterface = ExifInterface(imagePath)
        return exifInterface.getAttribute(Config.EXIF_PLANT_STATUS_TAG) ?: context.getString(R.string.plant_status_not_classified)
    }

    public fun setPlantStatusTextColor(res: String, plantStatus: TextView){
        val diseases: List<String> = Config.LABELS.filter { it -> !it.equals(Config.HEALTHY_LABEL) } ;
        if(diseases.contains(res)) {
            plantStatus.setTextColor(Color.RED)
        }else if(res==Config.HEALTHY_LABEL){
            plantStatus.setTextColor(Color.GREEN)
        }else{
            plantStatus.setTextColor(Color.BLACK)
        }
    }

    }
