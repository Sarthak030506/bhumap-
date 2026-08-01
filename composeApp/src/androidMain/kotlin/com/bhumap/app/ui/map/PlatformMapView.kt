package com.bhumap.app.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.bhumap.app.domain.model.Plot
import com.bhumap.app.domain.model.PlotStatus
import com.bhumap.app.ui.theme.Evergreen
import com.bhumap.app.ui.theme.Paper50
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
actual fun PlatformMapView(
    plots: List<Plot>,
    selectedPlot: Plot?,
    onPlotClick: (Plot) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var locationOverlayInstance by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            locationOverlayInstance?.enableMyLocation()
            locationOverlayInstance?.enableFollowLocation()
            locationOverlayInstance?.runOnFirstFix {
                val loc = locationOverlayInstance?.myLocation
                if (loc != null && mapViewInstance != null) {
                    mapViewInstance?.post {
                        mapViewInstance?.controller?.animateTo(loc, 16.5, 1000L)
                    }
