package it.unipi.mobile.vineplanthealthapp.ui.map

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.unipi.mobile.vineplanthealthapp.R
import it.unipi.mobile.vineplanthealthapp.databinding.FragmentMapBinding
import it.unipi.mobile.vineplanthealthapp.ui.gallery.Image
import it.unipi.mobile.vineplanthealthapp.utils.GalleryUtils
import it.unipi.mobile.vineplanthealthapp.utils.LocationUtils
import it.unipi.mobile.vineplanthealthapp.utils.MainUtils
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.time.format.DateTimeFormatter

class MapFragment : Fragment(), MapListener {
    private lateinit var mMap: MapView
    private lateinit var controller: IMapController
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var _binding: FragmentMapBinding? = null
    private lateinit var locationUtils: LocationUtils
    private val mainUtils = MainUtils()
    private val galleryUtils = GalleryUtils()

    private val binding get() = _binding!!
    val centroItalia = GeoPoint(42.8333, 12.8333, 53.0)
    var zoomItalia = 7.0
    private var isFirstViewCreation = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        isFirstViewCreation = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationUtils = LocationUtils(requireContext())
        // inizializzo la mappa
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.minZoomLevel = 5.0
        mMap.maxZoomLevel = 21.5
        mMap.setMultiTouchControls(true)
        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mMap)
        controller = mMap.controller
        mMyLocationOverlay.isDrawAccuracyEnabled = true
        // funzione di callback per i permessi
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                Log.e("TAG", "Primary permission")
                val granted = permissions.entries.all {
                    it.value == true
                }
                if (!granted) {
                    // Show a message to the user explaining why the permissions are needed
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.msg_location_permission),
                        Toast.LENGTH_LONG
                    ).show()
                    // torna all home
                    parentFragmentManager.popBackStack()
                }
                else {
                    if (!locationUtils.isLocationEnabled()) {
                        Log.e("TAG", "mostra settings")
                        locationUtils.showLocationSettingsDialog()
                    }
                }
            }
        // nascondo il pulsante di email
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.hide()
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences(
                getString(R.string.app_name),
                Context.MODE_PRIVATE
            )
        )
        if (!locationUtils.checkLocationPermissions()) {
            Log.e("TAG", "permessi non ci sono")
            requestLocationPermissions()
        }
        else {
            if (!locationUtils.isLocationEnabled()) {
                Log.e("TAG", "mostra settings")
                locationUtils.showLocationSettingsDialog()
            }
        }

        goToPosition()
    }
    private fun goToPosition(){
        if(!locationUtils.isLocationEnabled()) {
            Log.e("TAG", "gps non attivo")
            controller.setCenter(centroItalia)
            controller.setZoom(zoomItalia)
        }
        else {
            mMyLocationOverlay.enableMyLocation()
            mMyLocationOverlay.enableFollowLocation()
        }

        var images = mutableListOf<Image>()
        val picturesDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"VinePlantApp")
        val imageFiles = picturesDirectory.listFiles()

        if (imageFiles != null) {
            images = mainUtils.createArrayImages(imageFiles)
            Log.e("TAG", "images: $images")
        }

        val markerMap = mutableMapOf<GeoPoint, Image>()
        for (image in images) {
            val imagePath = image.uri.path
            val geoLocation  = galleryUtils.getGeoLocation(imagePath!!)
            val timestamp = galleryUtils.getTimestamp(imagePath)

            if(geoLocation == null || timestamp == null) {
                continue
            }
            val geoPoint = GeoPoint(geoLocation.first, geoLocation.second)

            // Check if a marker already exists at this location
            if (markerMap.containsKey(geoPoint)) {
                // If the existing marker is older, replace it
                if (galleryUtils.getTimestamp(markerMap[geoPoint]!!.uri.path!!)!! < timestamp) {
                    markerMap[geoPoint] = image
                }
            } else {
                // If no marker exists at this location, add a new one
                markerMap[geoPoint] = image
            }
        }

        // Now add the markers to the map
        for ((geoPoint, image) in markerMap) {
            val marker = Marker(mMap)
            val timestamp = galleryUtils.getTimestamp(image.uri.path!!)
            val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = timestamp?.format(dateFormat)
            val plantStatus = galleryUtils.getPlantStatus(requireContext(), image.uri.path!!)
            marker.position = geoPoint
            marker.icon = resources.getDrawable(R.drawable.ic_map_marker, null)
            marker.title = "${image.name}\n${date}\n${plantStatus}"
            mMap.overlays.add(marker)
        }

        // La posizione non è ancora disponibile, aspetta che lo diventi
        mMyLocationOverlay.runOnFirstFix {
            activity?.runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation)
                controller.animateTo(mMyLocationOverlay.myLocation)
                controller.setZoom(18.5)
            }
        }
        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(this)
    }

    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG", "onresume")
        if (!isFirstViewCreation) {
            goToPosition()
        } else {
            isFirstViewCreation = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // mostro il pulsante di email
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.show()
        mMap.overlays.clear()
        _binding = null
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        Log.e("TAG", "onZoom: ${event?.zoomLevel}")
        return false
    }
}
