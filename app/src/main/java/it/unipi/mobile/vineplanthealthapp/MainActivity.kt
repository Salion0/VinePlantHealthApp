package it.unipi.mobile.vineplanthealthapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import it.unipi.mobile.vineplanthealthapp.databinding.ActivityMainBinding
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.content.Context
import it.unipi.mobile.vineplanthealthapp.utils.MainUtils

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainUtils: MainUtils

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
            Toast.makeText(this, "Image captured successful", Toast.LENGTH_SHORT).show()
            mainUtils.saveImage(contentResolver, imageUri, currentLocation?.latitude ?: 0.0, currentLocation?.longitude ?: 0.0)
        }
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

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        currentLocation = location
                    }
                    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {
                        Toast.makeText(this@MainActivity, "GPS is disabled", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

            manageCameraPermissions()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
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

    fun manageCameraPermissions(){
        if(hasCameraPermissions()){
            Log.d("TAG", "Camera permissions OK")
            manageCameraButton()
        } else {
            Log.d("TAG", "Requesting camera permissions")
            requestPermissions()
        }

    }

    fun manageCameraButton(){
        val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "new_image.jpg")
        imageUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.provider",
            imageFile)
        takePictureLauncher.launch(imageUri)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        requestPermissionLauncher.launch(permissions)
    }

    private fun hasCameraPermissions(): Boolean {
        return arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

}