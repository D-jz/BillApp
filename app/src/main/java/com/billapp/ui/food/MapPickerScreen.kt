package com.billapp.ui.food

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult

/**
 * 高德地图选点页面
 *
 * @param initialLat 初始纬度（编辑已有记录时传入）
 * @param initialLng 初始经度
 * @param onConfirm  选点确认回调：纬度、经度、地址
 * @param onDismiss  返回
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLat: Double?,
    initialLng: Double?,
    onConfirm: (lat: Double, lng: Double, address: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var aMap by remember { mutableStateOf<AMap?>(null) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    var selectedLat by remember { mutableStateOf<Double?>(initialLat) }
    var selectedLng by remember { mutableStateOf<Double?>(initialLng) }
    var address by remember { mutableStateOf(if (initialLat != null) "正在获取地址…" else "点击地图选择位置") }
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        // 授权后刷新地图的定位层
        if (locationGranted) {
            aMap?.isMyLocationEnabled = true
        }
    }

    // 进入页面时请求定位权限
    LaunchedEffect(Unit) {
        if (!locationGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择位置") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 高德地图填满背景
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).also { mv ->
                        mv.onCreate(null)
                        mapView = mv
                        val map = mv.map
                        aMap = map
                        map.uiSettings.apply {
                            isZoomControlsEnabled = true
                            isMyLocationButtonEnabled = true
                        }
                        // 当前位置蓝点
                        map.myLocationStyle = MyLocationStyle().apply {
                            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
                        }
                        map.isMyLocationEnabled = locationGranted

                        // 点击地图选点
                        map.setOnMapClickListener { latLng ->
                            selectedLat = latLng.latitude
                            selectedLng = latLng.longitude
                            address = "正在获取地址…"
                            currentMarker = placeMarker(map, latLng, currentMarker)
                            reverseGeocode(context, latLng.latitude, latLng.longitude) { addr ->
                                address = addr
                            }
                        }

                        // 拖拽标记更新位置
                        map.setOnMarkerDragListener(object : AMap.OnMarkerDragListener {
                            override fun onMarkerDragStart(marker: Marker?) {}
                            override fun onMarkerDrag(marker: Marker?) {}
                            override fun onMarkerDragEnd(m: Marker?) {
                                val pos = m?.position ?: return
                                selectedLat = pos.latitude
                                selectedLng = pos.longitude
                                address = "正在获取地址…"
                                reverseGeocode(context, pos.latitude, pos.longitude) { addr ->
                                    address = addr
                                }
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 底部信息栏 + 确认按钮（叠加在地图上）
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("位置：$address", style = MaterialTheme.typography.bodyMedium)
                if (selectedLat != null && selectedLng != null) {
                    Text(
                        "经纬度：${"%.6f".format(selectedLat)}, ${"%.6f".format(selectedLng)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FloatingActionButton(
                        onClick = { /* 蓝点已自动定位到当前位置，可再次移动相机 */
                            aMap?.let { map ->
                                map.myLocationStyle = map.myLocationStyle.myLocationType(
                                    MyLocationStyle.LOCATION_TYPE_LOCATE
                                )
                            }
                        },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "我的位置")
                    }
                    Button(
                        onClick = {
                            val la = selectedLat
                            val ln = selectedLng
                            if (la != null && ln != null) {
                                onConfirm(la, ln, address)
                            }
                        },
                        enabled = selectedLat != null && selectedLng != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("确认选择")
                    }
                }
            }
        }
    }

    // 初始定位到传入坐标
    LaunchedEffect(initialLat, initialLng, aMap) {
        val map = aMap ?: return@LaunchedEffect
        if (initialLat != null && initialLng != null) {
            val target = LatLng(initialLat, initialLng)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 16f))
            currentMarker = placeMarker(map, target, currentMarker)
        }
    }

    // 生命周期转发给 MapView
    // onResume/onPause 跟随 Activity 生命周期；onDestroy 仅在 onDispose 中调用一次，避免重复
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val mv = mapView ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_RESUME -> mv.onResume()
                Lifecycle.Event.ON_PAUSE -> mv.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDestroy()
        }
    }
}

/** 在地图上放置标记（仅移除上一个标记，保留定位蓝点等其它图层） */
private fun placeMarker(map: AMap, latLng: LatLng, oldMarker: Marker?): Marker {
    oldMarker?.remove()
    val options = MarkerOptions()
        .position(latLng)
        .draggable(true)
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
    return map.addMarker(options) ?: error("无法添加地图标记")
}

/** 逆地理编码：经纬度 -> 地址 */
private fun reverseGeocode(
    context: android.content.Context,
    lat: Double,
    lng: Double,
    onResult: (String) -> Unit
) {
    runCatching {
        val search = GeocodeSearch(context)
        search.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
                val addr = result?.regeocodeAddress?.formatAddress
                if (rCode == 1000 && !addr.isNullOrEmpty()) {
                    onResult(addr)
                } else {
                    onResult("未知地址")
                }
            }

            override fun onGeocodeSearched(result: GeocodeResult?, rCode: Int) {}
        })
        val query = RegeocodeQuery(LatLonPoint(lat, lng), 200f, GeocodeSearch.AMAP)
        search.getFromLocationAsyn(query)
    }.onFailure { onResult("地址获取失败") }
}
