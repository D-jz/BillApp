package com.billapp

import android.app.Application
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer
import com.billapp.data.AppDatabase
import com.billapp.data.FoodRepository

class BillApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val foodRepository by lazy { FoodRepository(database.foodRecordDao()) }

    override fun onCreate() {
        super.onCreate()
        // 高德 SDK 隐私合规：必须在使用相关能力前调用
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)
    }
}
