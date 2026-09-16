package com.billapp.ui.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.billapp.BillApp
import com.billapp.data.FoodRecord

/**
 * 添加 / 编辑美食记录
 * @param pickedLat 地图选点返回的纬度
 * @param pickedLng 地图选点返回的经度
 * @param pickedAddress 地图选点返回的地址
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    pickedLat: Double?,
    pickedLng: Double?,
    pickedAddress: String?,
    onPickLocation: (lat: Double?, lng: Double?) -> Unit,
    onSaved: () -> Unit
) {
    val app = LocalContext.current.applicationContext as BillApp
    val viewModel: FoodViewModel = viewModel(factory = FoodViewModelFactory(app.foodRepository))

    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lng by remember { mutableStateOf<Double?>(null) }
    var address by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    // 当地图选点返回结果时，更新本地状态
    LaunchedEffect(pickedLat, pickedLng, pickedAddress) {
        if (pickedLat != null && pickedLng != null) {
            lat = pickedLat
            lng = pickedLng
            if (!pickedAddress.isNullOrEmpty()) address = pickedAddress
        }
    }

    val canSave = name.isNotBlank() && lat != null && lng != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("随手记") },
                navigationIcon = {
                    IconButton(onClick = onSaved) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (canSave) {
                                viewModel.insert(
                                    FoodRecord(
                                        name = name.trim(),
                                        latitude = lat!!,
                                        longitude = lng!!,
                                        address = address.trim(),
                                        remark = remark.trim()
                                    )
                                )
                                onSaved()
                            }
                        },
                        enabled = canSave
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("店名") },
                placeholder = { Text("例如：老王川菜馆") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Text(
                    "位置",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = if (lat != null && lng != null) {
                        "${"%.6f".format(lat)}, ${"%.6f".format(lng)}"
                    } else {
                        ""
                    },
                    onValueChange = {},
                    label = { Text("经纬度") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { onPickLocation(lat, lng) }) {
                            Icon(Icons.Default.EditLocation, contentDescription = "在地图上选点")
                        }
                    }
                )
                if (lat == null) {
                    Text(
                        "点击右侧图标在地图上标记位置",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (address.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "地址：$address",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                label = { Text("备注") },
                placeholder = { Text("例如：水煮鱼、毛血旺很好吃") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { onPickLocation(lat, lng) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.EditLocation, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(if (lat == null) "在地图上标记位置" else "重新选择位置")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
