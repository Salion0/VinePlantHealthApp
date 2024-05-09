package it.unipi.mobile.vineplanthealthapp.ui.gallery

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import it.unipi.mobile.vineplanthealthapp.R
import it.unipi.mobile.vineplanthealthapp.utils.GalleryUtils
import it.unipi.mobile.vineplanthealthapp.utils.LocationUtils
import it.unipi.mobile.vineplanthealthapp.utils.MainUtils
import java.io.File
import java.sql.Timestamp


class GalleryFragment : Fragment() {

    private lateinit var galleryGridView: GridView
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var galleryUtils : GalleryUtils
    private var mainUtils: MainUtils = MainUtils()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        galleryGridView = view.findViewById(R.id.gallery_grid_view)
        galleryUtils = GalleryUtils()

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions.entries.all { it.value }
                if (!granted) {
                    showPermissionToast()
                    parentFragmentManager.popBackStack()
                }
                else {
                    loadImages()
                }
            }

        if (hasPermissions()) {
            loadImages()
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

    override fun onResume() {
        super.onResume()
        if (hasPermissions()) {
            loadImages()
        }
    }

    override fun onPause() {
        super.onPause()
        galleryGridView.adapter = null
    }

    private fun loadImages() {
        var images = mutableListOf<Image>()
        val picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val imageFiles = picturesDirectory.listFiles()

        if (imageFiles != null) {
            images = mainUtils.createArrayImages(imageFiles)
        }else{
            Toast.makeText(requireContext(), "No images found", Toast.LENGTH_SHORT).show()
        }

        galleryGridView.setOnItemClickListener { parent, view, position, id ->
            val image = galleryGridView.adapter.getItem(position) as Image
            image.plantStatus = galleryUtils.getPlantStatus(requireContext(), image.uri.path?:"")
            try {
                val geoLocation = galleryUtils.getGeoLocation(image.uri.path?:"")
                showImageDialog(image, geoLocation)
            } catch (Exception: Exception){
                showImageDialog(image, null)
            }
        }

        val imageAdapter = ImageAdapter(requireContext(), images)
        galleryGridView.adapter = imageAdapter
    }


    //function that open a dialog to show the image in full screen + other
    private fun showImageDialog(image: Image, geoLocation: Pair<Double, Double> ?) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_image)
        val imageView = dialog.findViewById<ImageView>(R.id.image)
        imageView.setImageBitmap(image.bitmap)

        //rename listener
        val imageName = dialog.findViewById<EditText>(R.id.imageName)
        imageName.setText(image.name)

        val revertButton = dialog.findViewById<ImageButton>(R.id.revertNameButton)
        val editButton = dialog.findViewById<ImageButton>(R.id.editNameButton)
        val saveButton = dialog.findViewById<ImageButton>(R.id.saveNameButton)

        revertButton.setOnClickListener {
            imageName.setText(image.name)
            galleryUtils.setNotEditable(imageName, saveButton, revertButton,editButton)
        }

        saveButton.setOnClickListener {
            val newName = imageName.text.toString()
                val file = File(image.uri.path)
                val newFile = File(file.parent, newName)
                if(file.renameTo(newFile)){
                    image.name = newName
                    imageName.setText(newName)
                    Toast.makeText(requireContext(), "Image renamed successfully", Toast.LENGTH_SHORT).show()
                    galleryUtils.setNotEditable(imageName, saveButton, revertButton, editButton)
                    view.let { activity?.currentFocus?.clearFocus() }
                    dialog.setOnDismissListener {
                        loadImages()
                    }
                }
                else{
                    Toast.makeText(requireContext(), "Invalid image name", Toast.LENGTH_SHORT).show()
                }
        }

        editButton.setOnClickListener {
            galleryUtils.setEditable(imageName, saveButton, revertButton,editButton)
        }

        //aggiungere if per controllo stato pianta
        val plantStatus = dialog.findViewById<TextView>(R.id.plantStatus)
        plantStatus.text = image.plantStatus
        galleryUtils.setPlantStatusTextColor(image.plantStatus?:"", plantStatus)

        val lat = dialog.findViewById<TextView>(R.id.lat)
        val lon = dialog.findViewById<TextView>(R.id.lon)
        if(geoLocation == null){
            val notAvailableLabel = getString(R.string.label_not_available)
            lat.text = getString(R.string.label_lat).plus(notAvailableLabel)
            lon.text = getString(R.string.label_lon).plus(notAvailableLabel)
        }
        else{
            lat.text = getString(R.string.label_lat).plus("${geoLocation.first}")
            lon.text = getString(R.string.label_lon).plus("${geoLocation.second}")
        }

        val closeButton = dialog.findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        val classifyButton = dialog.findViewById<Button>(R.id.classifyButton)
        classifyButton.setOnClickListener {
            classify(image, plantStatus)
        }

        //open dialog to confirm deletion
        val deleteButton = dialog.findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            val deleteDialog = Dialog(requireContext())
            deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            deleteDialog.setContentView(R.layout.confirm_delete)
            val confirmDelete = deleteDialog.findViewById<Button>(R.id.confirmDelete)
            val cancelDelete = deleteDialog.findViewById<Button>(R.id.cancelDelete)
            confirmDelete.setOnClickListener {
                val file = File(image.uri.path)
                if (file.exists()) {
                    file.delete()
                    Toast.makeText(requireContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show()
                    deleteDialog.dismiss()
                    dialog.dismiss()
                    loadImages()
                }
            }
            cancelDelete.setOnClickListener {
                deleteDialog.dismiss()
            }
            deleteDialog.show()
        }

        dialog.show()
    }

    private fun classify(image: Image, plantStatus: TextView){
        if(image.uri.path == null){
            Toast.makeText(requireContext(), "Image not found", Toast.LENGTH_SHORT).show()
            return
        }

        //loading text
        plantStatus.text = "..."
        plantStatus.setTextColor(Color.BLACK)

        //classify image
        //TODO implement classification
        val res = getString(R.string.plant_status_healthy) //example

        //save result
        val exifInterface = ExifInterface(image.uri.path!!)
        exifInterface.setAttribute(getString(R.string.plant_status_tag), res)
        exifInterface.saveAttributes()

        //set text color
        plantStatus.text = res
        galleryUtils.setPlantStatusTextColor(res, plantStatus)

    }
}

data class Image(
    val bitmap: Bitmap,
    val uri: Uri,
    var name: String? = null,
    var timestamp: Timestamp? = null,
    var plantStatus: String? = null
)

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