package com.billapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.billapp.ui.food.AddFoodScreen
import com.billapp.ui.food.FoodDetailScreen
import com.billapp.ui.food.FoodScreen
import com.billapp.ui.food.MapPickerScreen
import com.billapp.ui.placeholder.PlaceholderScreen

/** 底部导航栏的 Tab 定义 */
sealed class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Food : BottomTab("food", "美食", Icons.Default.Restaurant)
    data object Bills : BottomTab("bills", "记账", Icons.AutoMirrored.Filled.ReceiptLong)
    data object Profile : BottomTab("profile", "我的", Icons.Default.AccountCircle)
}

/** 美食模块内部的子路由 */
object FoodRoutes {
    const val LIST = "food/list"
    const val ADD = "food/add"
    const val DETAIL = "food/detail/{recordId}"
    const val MAP_PICKER = "food/mapPicker?lat={lat}&lng={lng}"

    fun detail(id: Long) = "food/detail/$id"
    fun mapPicker(lat: Double? = null, lng: Double? = null): String {
        val la = lat?.toString() ?: ""
        val ln = lng?.toString() ?: ""
        return "food/mapPicker?lat=$la&lng=$ln"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // 判断当前是否处于底部 Tab 的根路由
    val isTopLevel = currentRoute in listOf(
        BottomTab.Food.route, BottomTab.Bills.route, BottomTab.Profile.route
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BillApp") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    val tabs = listOf(BottomTab.Food, BottomTab.Bills, BottomTab.Profile)
                    tabs.forEach { tab ->
                        val selected = backStackEntry?.destination?.hierarchy?.any {
                            it.route == tab.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Food.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 美食模块列表（Tab 根）
            composable(BottomTab.Food.route) {
                FoodScreen(
                    onAddClick = { navController.navigate(FoodRoutes.ADD) },
                    onItemClick = { id -> navController.navigate(FoodRoutes.detail(id)) }
                )
            }

            // 美食：添加 / 编辑
            composable(FoodRoutes.ADD) { entry ->
                // 观察地图选点返回的结果
                val pickedLat by entry.savedStateHandle
                    .getStateFlow<Double?>("pickedLat", null)
                    .collectAsStateWithLifecycle()
                val pickedLng by entry.savedStateHandle
                    .getStateFlow<Double?>("pickedLng", null)
                    .collectAsStateWithLifecycle()
                val pickedAddress by entry.savedStateHandle
                    .getStateFlow<String?>("pickedAddress", null)
                    .collectAsStateWithLifecycle()

                AddFoodScreen(
                    pickedLat = pickedLat,
                    pickedLng = pickedLng,
                    pickedAddress = pickedAddress,
                    onPickLocation = { lat, lng ->
                        navController.navigate(FoodRoutes.mapPicker(lat, lng))
                    },
                    onSaved = { navController.popBackStack() }
                )
            }

            // 美食：详情
            composable(
                route = FoodRoutes.DETAIL,
                arguments = listOf(navArgument("recordId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: return@composable
                FoodDetailScreen(
                    recordId = recordId,
                    onEdit = { navController.navigate(FoodRoutes.ADD) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 美食：地图选点
            composable(
                route = FoodRoutes.MAP_PICKER,
                arguments = listOf(
                    navArgument("lat") { nullable = true },
                    navArgument("lng") { nullable = true }
                )
            ) { backStackEntry ->
                val latStr = backStackEntry.arguments?.getString("lat")
                val lngStr = backStackEntry.arguments?.getString("lng")
                val lat = latStr?.toDoubleOrNull()
                val lng = lngStr?.toDoubleOrNull()
                MapPickerScreen(
                    initialLat = lat,
                    initialLng = lng,
                    onConfirm = { la, ln, addr ->
                        // 把结果写回上一个页面（添加页）的 savedStateHandle
                        val prev = navController.previousBackStackEntry
                        prev?.savedStateHandle?.set("pickedLat", la)
                        prev?.savedStateHandle?.set("pickedLng", ln)
                        prev?.savedStateHandle?.set("pickedAddress", addr)
                        navController.popBackStack()
                    },
                    onDismiss = { navController.popBackStack() }
                )
            }

            // 占位模块
            composable(BottomTab.Bills.route) {
                PlaceholderScreen(title = "记账", message = "记账模块开发中…")
            }
            composable(BottomTab.Profile.route) {
                PlaceholderScreen(title = "我的", message = "个人中心开发中…")
            }
        }
    }
}
