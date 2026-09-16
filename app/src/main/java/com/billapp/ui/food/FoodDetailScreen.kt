package com.billapp.ui.food

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.billapp.BillApp
import com.billapp.data.FoodRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    recordId: Long,
    onEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as BillApp
    val viewModel: FoodViewModel = viewModel(factory = FoodViewModelFactory(app.foodRepository))

    var record by remember { mutableStateOf<FoodRecord?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(recordId) {
        record = viewModel.get(recordId)
    }

    if (showDeleteConfirm && record != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除记录") },
            text = { Text("确定删除「${record!!.name}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(record!!)
                    onNavigateBack()
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(record?.name ?: "详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { padding ->
        val r = record
        if (r == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("加载中…", color = MaterialTheme.colorScheme.outline)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(r.name, style = MaterialTheme.typography.titleLarge)

            if (r.address.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(r.address, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Text(
                "经纬度：${"%.6f".format(r.latitude)}, ${"%.6f".format(r.longitude)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (r.remark.isNotEmpty()) {
                Column {
                    Text(
                        "备注",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(r.remark, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Text(
                "记录时间：${formatDate(r.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(8.dp))

            // 打开高德地图导航
            Button(
                onClick = { openAmapNavigation(context, r.latitude, r.longitude, r.name) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Directions, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("打开高德地图导航")
            }

            // 在高德地图中查看位置
            TextButton(
                onClick = { openAmapMap(context, r.latitude, r.longitude, r.name) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("在高德地图中查看")
            }
        }
    }
}

/** 打开高德地图导航（驾车） */
private fun openAmapNavigation(context: android.content.Context, lat: Double, lng: Double, name: String) {
    val uri = Uri.parse(
        "androidamap://navi?sourceApplication=BillApp" +
            "&lat=$lat&lon=$lng&dev=0&style=2&poiname=${Uri.encode(name)}"
    )
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.autonavi.minimap")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
        .onFailure { openAmapInMarket(context) }
}

/** 在高德地图中标记查看 */
private fun openAmapMap(context: android.content.Context, lat: Double, lng: Double, name: String) {
    val uri = Uri.parse(
        "androidamap://viewMap?sourceApplication=BillApp" +
            "&lat=$lat&lon=$lng&dev=0&poiname=${Uri.encode(name)}"
    )
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.autonavi.minimap")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
        .onFailure { openAmapInMarket(context) }
}

/** 未安装高德地图时，提示去应用市场下载 */
private fun openAmapInMarket(context: android.content.Context) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=com.autonavi.minimap")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
