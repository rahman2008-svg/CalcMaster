package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    val allCalculations: Flow<List<CalculationRecord>> = dao.getAllCalculations()
    val favoriteCalculations: Flow<List<CalculationRecord>> = dao.getFavoriteCalculations()

    suspend fun insert(record: CalculationRecord): Long {
        return dao.insertCalculation(record)
    }

    suspend fun update(record: CalculationRecord) {
        dao.updateCalculation(record)
    }

    suspend fun delete(record: CalculationRecord) {
        dao.deleteCalculation(record)
    }

    suspend fun clearAll() {
        dao.clearAllCalculations()
    }

    suspend fun clearHistoryOnly() {
        dao.clearHistoryOnly()
    }
}
