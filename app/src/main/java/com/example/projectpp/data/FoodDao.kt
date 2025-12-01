package com.example.projectpp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: FoodItem): Long

    @Update
    suspend fun update(food: FoodItem)

    @Delete
    suspend fun delete(food: FoodItem)

    @Query("DELETE FROM food_table")
    suspend fun clearAll()

    @Query("SELECT * FROM food_table ORDER BY expirationDate ASC")
    fun getAllFoodFlow(): kotlinx.coroutines.flow.Flow<List<FoodItem>>

    @Query("SELECT * FROM food_table")
    suspend fun getAllNow(): List<FoodItem>

    @Query("DELETE FROM food_table WHERE quantity > 0")
    suspend fun clearAllProducts()

    @Query("SELECT * FROM food_table WHERE quantity > 0")
    suspend fun getAllRealProductsNow(): List<FoodItem>

    @Query("UPDATE food_table SET category = :newName WHERE category = :oldName")
    suspend fun renameCategory(oldName: String, newName: String)

    @Query("DELETE FROM food_table WHERE category = :categoryName")
    suspend fun deleteByCategory(categoryName: String)
}