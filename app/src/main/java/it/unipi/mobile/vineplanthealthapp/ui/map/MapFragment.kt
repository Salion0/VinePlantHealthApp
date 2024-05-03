package it.unipi.mobile.vineplanthealthapp.ui.map

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
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
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.views.MapView
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import it.unipi.mobile.vineplanthealthapp.databinding.FragmentMapBinding
import it.unipi.mobile.vineplanthealthapp.utils.LocationUtils
import org.osmdroid.util.GeoPoint

class MapFragment : Fragment(), MapListener {
    private lateinit var mMap: MapView
    private lateinit var controller: IMapController
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var _binding: FragmentMapBinding? = null
    private lateinit var locationUtils: LocationUtils
    private val binding get() = _binding!!
    val centroItalia = GeoPoint(42.8333, 12.8333, 53.0)
    var zoomItalia = 7.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationUtils = LocationUtils(requireContext())
        // inizializzo la mappa
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.minZoomLevel = 5.0
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
    private fun goToPosition() {
        var entrato = false
        if(!locationUtils.isLocationEnabled()) {
            Log.e("TAG", "gps non attivo")
            controller.setCenter(centroItalia)
            controller.setZoom(zoomItalia)
            entrato = true
        }
        else {
            mMyLocationOverlay.enableMyLocation()
            mMyLocationOverlay.enableFollowLocation()
        }
        Log.e("TAG", "entrato: $entrato")

        // La posizione non è ancora disponibile, aspetta che lo diventi
        mMyLocationOverlay.runOnFirstFix {
            activity?.runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation)
                controller.animateTo(mMyLocationOverlay.myLocation)
                controller.setZoom(15.0)
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
        goToPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // mostro il pulsante di email
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.show()
        _binding = null
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        return false
    }
}
