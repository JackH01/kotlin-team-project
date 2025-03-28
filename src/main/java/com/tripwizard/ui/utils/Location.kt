package com.tripwizard.ui.utils

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat

fun hasFineLocationPermission(context: Context) = ActivityCompat.checkSelfPermission(
    context,
    android.Manifest.permission.ACCESS_FINE_LOCATION
) == PackageManager.PERMISSION_GRANTED

fun hasCoarseLocationPermission(context: Context) = ActivityCompat.checkSelfPermission(
    context,
    android.Manifest.permission.ACCESS_COARSE_LOCATION
) == PackageManager.PERMISSION_GRANTED

@Composable
fun getLocation(callback: (Location?) -> Unit) {
    val context = LocalContext.current
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (hasFineLocationPermission(LocalContext.current)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                locationManager.getCurrentLocation(
                    LocationManager.GPS_PROVIDER,
                    null,
                    context.mainExecutor
                ) { location: Location? ->
                    if (location == null) {
                        try {
                            val lastKnown = locationManager.getLastKnownLocation(
                                LocationManager.GPS_PROVIDER
                            )

                            callback(lastKnown)
                        } catch (e: SecurityException) {
                            Log.e("Exception: %s", e.message, e)
                        }
                    } else {
                        callback(location)
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        } else {
            try {
                locationManager.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) = callback(location)
                    },
                    null
                )
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)

            }
        }
    } else if (hasCoarseLocationPermission(LocalContext.current)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                locationManager.getCurrentLocation(
                    LocationManager.NETWORK_PROVIDER,
                    null,
                    context.mainExecutor
                ) { location: Location? ->
                    if (location == null) {
                        try {
                            val lastKnown = locationManager.getLastKnownLocation(
                                LocationManager.NETWORK_PROVIDER
                            )

                            callback(lastKnown)
                        } catch (e: SecurityException) {
                            Log.e("Exception: %s", e.message, e)
                        }
                    } else {
                        callback(location)
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        } else {
            try {
                locationManager.requestSingleUpdate(
                    LocationManager.NETWORK_PROVIDER,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) = callback(location)
                    },
                    null
                )
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)

            }
        }
    }
}