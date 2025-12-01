package com.example.projectpp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_table")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val quantity: Int,
    val expirationDate: Long,
    val note: String = ""
)

