package it.unipi.mobile.vineplanthealthapp.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import it.unipi.mobile.vineplanthealthapp.R

class LocationUtils(private val context: Context) {
    public fun checkLocationPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    // Move requestLocationPermissions() and onRequestPermissionsResult() to your Activity or Fragment

    public fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    public fun showLocationSettingsDialog() {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.title_location_settings))
            .setMessage(context.getString(R.string.msg_location_settings))
            .setPositiveButton(context.getString(R.string.positive_button)) { dialog, which ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(context.getString(R.string.negative_button), null)
            .show()
    }
}