package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculations WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteCalculations(): Flow<List<CalculationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(record: CalculationRecord): Long

    @Update
    suspend fun updateCalculation(record: CalculationRecord)

    @Delete
    suspend fun deleteCalculation(record: CalculationRecord)

    @Query("DELETE FROM calculations")
    suspend fun clearAllCalculations()

    @Query("DELETE FROM calculations WHERE isFavorite = 0")
    suspend fun clearHistoryOnly()
}
