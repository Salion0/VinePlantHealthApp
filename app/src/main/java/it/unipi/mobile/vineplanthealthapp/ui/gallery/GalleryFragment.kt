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
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.graphics.Color
import android.view.Window
import android.widget.TextView
import com.github.chrisbanes.photoview.PhotoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher


data class Image(
    val bitmap: Bitmap,
    val uri: Uri,
    val name: String? = null,
    var plantStatus: String? = null
)

class GalleryFragment : Fragment() {

    private lateinit var galleryGridView: GridView
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        galleryGridView = view.findViewById(R.id.gallery_grid_view)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions.entries.all { it.value }
                if (!granted) {
                    showPermissionToast()
                    parentFragmentManager.popBackStack()
                }
                else {
                    loadImages()
                    showLocation()
                }
            }

        if (hasPermissions()) {
            loadImages()
            showLocation()
        }
        else {
            if (shouldShowRequestPermissionRationale()) {
                showPermissionToast()
                parentFragmentManager.popBackStack()
            } else {
                requestPermissions()
            }
        }

        return view
    }



    private fun hasPermissions(): Boolean {
        val checkMediaPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_MEDIA_IMAGES
        )
        val checkLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_MEDIA_LOCATION
        )
        return checkMediaPermission == PackageManager.PERMISSION_GRANTED && checkLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        val permissionMedia = ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.READ_MEDIA_IMAGES
        )
        val permissionLocation = ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_MEDIA_LOCATION
        )
        return permissionMedia || permissionLocation
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.ACCESS_MEDIA_LOCATION
            )
        )
    }

    private fun showPermissionToast() {
        Toast.makeText(
            requireContext(),
            "Permission required to access images",
            Toast.LENGTH_SHORT
        ).show()
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
                    images.add(Image(bitmap, uri, file.name))
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
            if (geoLocation != null) {
                image.plantStatus = "Healthy"
                showImageDialog(image, geoLocation);
            }
        }

    }

    //function that open a dialog to show the image in full screen + other
    private fun showImageDialog(image: Image, geoLocation: Pair<Double, Double>) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_image)

        val imageView = dialog.findViewById<ImageView>(R.id.image)
        val imagePath = dialog.findViewById<TextView>(R.id.imagePath)
        imageView.setImageBitmap(image.bitmap)

        //al tocco ingrandisce l'immagine e permette lo zoom con tocco
/*        imageView.setOnClickListener {
            val zoomDialog = Dialog(requireContext())
            zoomDialog.setContentView(R.layout.dialog_image)
            val photoView = zoomDialog.findViewById<PhotoView>(R.id.photoView)
            photoView.setImageResource(image.bitmap)
            zoomDialog.show()
        }*/


        imagePath.text = image.name

        //aggiungere if per controllo stato pianta
        val plantStatus = dialog.findViewById<TextView>(R.id.plantStatus)
        plantStatus.text = "HEALTHY"
        plantStatus.setTextColor(Color.GREEN)

        val lat = dialog.findViewById<TextView>(R.id.lat)
        val lon = dialog.findViewById<TextView>(R.id.lon)
        lat.text = "Lat: ${geoLocation.first}"
        lon.text = "Lon: ${geoLocation.second}"

        dialog.show()
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