package it.unipi.mobile.vineplanthealthapp.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.net.Uri
import android.os.Environment
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.widget.Toast
import android.util.Log


class GalleryViewModel : ViewModel() {

    private val _images = MutableLiveData<List<Image>>()
    val images: LiveData<List<Image>> = _images
}