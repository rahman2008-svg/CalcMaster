package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // BASIC, SCIENTIFIC, GPA, BMI, AGE, EMI, UNIT, CURRENCY
    val input: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val notes: String = ""
)
