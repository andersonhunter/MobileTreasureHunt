/*Name: Hunter Anderson
* OSU ONID: andershu
* Email: andershu@oregonstate.edu */

package com.example.mobiletreasurehunt

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mobiletreasurehunt.ui.theme.MobileTreasureScreen
import com.example.mobiletreasurehunt.ui.theme.MobileTreasureHuntTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()
        setContent {
            MobileTreasureHuntTheme {
                val location = remember {mutableStateOf<Location?>(null)}
                val permissionGranted = remember { mutableStateOf(false) }
                val locationPermissionRequest = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()) {
                        isGranted: Boolean ->
                        permissionGranted.value = isGranted
                    }
                MobileTreasureScreen(
                    permissionGranted = permissionGranted,
                    fusedLocationClient = fusedLocationClient,
                    permissionRequest = {
                        when {
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                permissionGranted.value = true
                            }

                            else -> {
                                locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    },
                    location = location
                )
            }
        }
    }
}