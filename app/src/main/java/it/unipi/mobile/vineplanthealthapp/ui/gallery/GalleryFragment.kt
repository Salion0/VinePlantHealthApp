package it.unipi.mobile.vineplanthealthapp.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.unipi.mobile.vineplanthealthapp.R
import android.Manifest
import android.widget.GridView
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.widget.ImageView
import android.graphics.Bitmap
import android.widget.BaseAdapter
import android.content.Context
import android.media.ExifInterface


data class Image(val bitmap: Bitmap, val uri: Uri)
private const val MY_PERMISSIONS_REQUEST_READ_MEDIA_IMAGES = 1

class GalleryFragment : Fragment() {

    private lateinit var galleryGridView: GridView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        // Retrieve the GridView from the layout
        galleryGridView = view.findViewById(R.id.gallery_grid_view)

        val checkMediaPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_MEDIA_IMAGES
        )
        val checkLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_MEDIA_LOCATION
        )

        if (checkMediaPermission != PackageManager.PERMISSION_GRANTED && checkLocationPermission != PackageManager.PERMISSION_GRANTED) {
            val permissionMedia = ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_MEDIA_IMAGES
            );
            val permissionLocation = ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_MEDIA_LOCATION
            );
            // Permission is not granted
            if (!permissionMedia || !permissionLocation) {
                Toast.makeText(
                    requireContext(),
                    "Gallery and Location permissions needed!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.ACCESS_MEDIA_LOCATION
                    ),
                    MY_PERMISSIONS_REQUEST_READ_MEDIA_IMAGES
                )
                loadImages()
                showLocation()
            }
        }
        else{
            loadImages()
            showLocation()
        }
        return view
    }

    private fun loadImages() {
        val images = mutableListOf<Image>()
        val picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val imageFiles = picturesDirectory.listFiles()

        if (imageFiles != null) {
            for (file in imageFiles) {
                if (file.isFile && (file.path.endsWith(".jpg") || file.path.endsWith(".png"))) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    val uri = Uri.fromFile(file)
                    images.add(Image(bitmap, uri))
                }
            }
        }

        val imageAdapter = ImageAdapter(requireContext(), images)
        galleryGridView.adapter = imageAdapter
    }

    private fun showLocation(){
        val imagesList = loadImages()
        if (listOf(imagesList).isEmpty()) {
            Toast.makeText(requireContext(), "No images found", Toast.LENGTH_SHORT).show()
        }

        galleryGridView.setOnItemClickListener { parent, view, position, id ->
            val image = galleryGridView.adapter.getItem(position) as Image
            val geoLocation = image.uri.path?.let { getGeoLocation(it) }
            if (geoLocation != null)
                Toast.makeText(
                    requireContext(),
                    "Lat: ${geoLocation.first}, Lon: ${geoLocation.second}",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    class ImageAdapter(private val context: Context, var images: List<Image>) : BaseAdapter() {
        override fun getCount(): Int {
            return images.size
        }

        override fun getItem(position: Int): Any {
            return images[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val imageView: ImageView
            if (convertView == null) {
                imageView = ImageView(context)
                imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1000)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setPadding(10, 10, 10, 10)
            } else {
                imageView = convertView as ImageView
            }

            imageView.setImageBitmap(images[position].bitmap)
            return imageView
        }
    }

    fun getGeoLocation(imagePath: String): Pair<Double, Double>? {
        val exifInterface = ExifInterface(imagePath)

        val latLong = FloatArray(2)
        val hasLatLong = exifInterface.getLatLong(latLong)
        if (hasLatLong) {
            return Pair(latLong[0].toDouble(), latLong[1].toDouble())
        }
        Toast.makeText(requireContext(), "No geolocation found for this image", Toast.LENGTH_SHORT).show()
        return null
    }
}