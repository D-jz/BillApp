package com.billapp.data

import kotlinx.coroutines.flow.Flow

class FoodRepository(private val dao: FoodRecordDao) {

    fun getAllRecords(): Flow<List<FoodRecord>> = dao.getAll()

    suspend fun getRecord(id: Long): FoodRecord? = dao.getById(id)

    suspend fun insert(record: FoodRecord): Long = dao.insert(record)

    suspend fun update(record: FoodRecord) = dao.update(record)

    suspend fun delete(record: FoodRecord) = dao.delete(record)
}
