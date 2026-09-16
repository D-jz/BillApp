package com.billapp.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.billapp.data.FoodRecord
import com.billapp.data.FoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {

    val records: StateFlow<List<FoodRecord>> = repository.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(record: FoodRecord, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insert(record)
            onDone()
        }
    }

    fun update(record: FoodRecord, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.update(record)
            onDone()
        }
    }

    fun delete(record: FoodRecord) {
        viewModelScope.launch { repository.delete(record) }
    }

    suspend fun get(id: Long): FoodRecord? = repository.getRecord(id)
}

class FoodViewModelFactory(
    private val repository: FoodRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            return FoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
