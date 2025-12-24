package com.example.incometracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val date: String, // ISO format recommended
    val category: String,
    val description: String,
    val type: String // "income" or "expense"
)
