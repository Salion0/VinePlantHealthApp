package it.unipi.mobile.vineplanthealthapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import it.unipi.mobile.vineplanthealthapp.databinding.ActivityMainBinding
import it.unipi.mobile.vineplanthealthapp.utils.LocationUtils
import it.unipi.mobile.vineplanthealthapp.utils.MainUtils
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainUtils: MainUtils
    private lateinit var inferenceActivityLauncher : ActivityResultLauncher<String>

    private lateinit var imageUri: Uri
    private var requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (!granted) {
            Toast.makeText(this, "All permissions were not granted", Toast.LENGTH_SHORT).show()
        } else {
            manageCameraButton()
        }
    }

    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null
    private var takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            getGPSLocation()
            if(LocationUtils(this).isLocationEnabled() && currentLocation == null)
                Toast.makeText(this, "Location info not retrieved. Try again.", Toast.LENGTH_SHORT).show()
            else{
                mainUtils.saveImage(contentResolver, imageUri, currentLocation?.latitude ?: 0.0, currentLocation?.longitude ?: 0.0)
                Toast.makeText(this, "Image captured successful", Toast.LENGTH_SHORT).show()

                val uriLastImage = getLastImageFromGallery()
                Log.d("Image Uri",uriLastImage.toString())
                    val intent = Intent(baseContext,InferencePhaseActivity::class.java).putExtra(Config.URI_TAG,uriLastImage.toString())
                startActivity(intent)

            }
        }
    }
    private fun getLastImageFromGallery(): Uri? {
        val picturesDirectory =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"VinePlantApp")
        val imageFiles = picturesDirectory.listFiles()
        val arrayImage = mainUtils.createArrayImages(imageFiles)
        val lastSavedImage = arrayImage.sortedWith(compareByDescending { File(it.uri.path).lastModified() }).firstOrNull()
        if (lastSavedImage != null) {
            return lastSavedImage.uri
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainUtils = MainUtils()

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener {
            Log.i("TAG", "Camera button clicked")
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            manageCameraPermissions()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_map
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun manageCameraPermissions(){
        if(hasCameraPermissions()){
            Log.d("TAG", "Camera permissions OK")
            manageCameraButton()
        } else {
            Log.d("TAG", "Requesting camera and location permissions")
            requestAllPermissions()
        }

    }

    private fun manageCameraButton(){
        val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES+"/VinePlantApp"), "new_image.jpg")
        imageUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.provider",
            imageFile)
        takePictureLauncher.launch(imageUri)
    }

    private fun requestAllPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        requestPermissionLauncher.launch(permissions)
    }

    private fun hasCameraPermissions(): Boolean {
        return arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.CAMERA
        ).all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun getGPSLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Location permissions not granted", Toast.LENGTH_SHORT).show()
            return
        }

        if(!LocationUtils(this).isLocationEnabled()){
            return
        }

         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0f, object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {
                if(ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    currentLocation = locationManager.getLastKnownLocation(provider)
            }
            override fun onProviderDisabled(provider: String) {
                currentLocation = null
            }
         })
    }

}