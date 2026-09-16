package com.billapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 美食随手记记录
 * @param name 店名
 * @param latitude 纬度
 * @param longitude 经度
 * @param address 地址文本（选点后逆地理编码结果，可选）
 * @param remark 备注（好吃的菜等）
 * @param createdAt 创建时间戳
 */
@Entity(tableName = "food_records")
data class FoodRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val remark: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
