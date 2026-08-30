package com.example.data.db

import androidx.room.*
import com.example.data.model.CustomRecipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY id ASC")
    fun getAllRecipes(): Flow<List<CustomRecipe>>

    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY id ASC")
    fun getRecipesByCategory(category: String): Flow<List<CustomRecipe>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoriteRecipes(): Flow<List<CustomRecipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: CustomRecipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<CustomRecipe>)

    @Update
    suspend fun updateRecipe(recipe: CustomRecipe)

    @Delete
    suspend fun deleteRecipe(recipe: CustomRecipe)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int
}
