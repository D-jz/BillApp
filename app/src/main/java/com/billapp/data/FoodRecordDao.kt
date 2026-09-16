package com.billapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodRecordDao {

    @Query("SELECT * FROM food_records ORDER BY createdAt DESC")
    fun getAll(): Flow<List<FoodRecord>>

    @Query("SELECT * FROM food_records WHERE id = :id")
    suspend fun getById(id: Long): FoodRecord?

    @Insert
    suspend fun insert(record: FoodRecord): Long

    @Update
    suspend fun update(record: FoodRecord)

    @Delete
    suspend fun delete(record: FoodRecord)
}
