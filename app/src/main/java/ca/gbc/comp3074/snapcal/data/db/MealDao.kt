package ca.gbc.comp3074.snapcal.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ca.gbc.comp3074.snapcal.data.model.Meal
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    // ---- CRUD ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meal: Meal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(meals: List<Meal>)

    @Update
    suspend fun update(meal: Meal)

    @Delete
    suspend fun delete(meal: Meal)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteById(id: Long)


    @Query("SELECT * FROM meals ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Meal?

    @Query("""
        SELECT * FROM meals
        WHERE createdAt BETWEEN :fromMillis AND :toMillis
        ORDER BY createdAt DESC
    """)
    fun observeInRange(fromMillis: Long, toMillis: Long): Flow<List<Meal>>

    @Query("""
        SELECT * FROM meals
        WHERE date(createdAt/1000, 'unixepoch') = date(:dayStartMillis/1000, 'unixepoch')
        ORDER BY createdAt DESC
    """)
    fun observeByDay(dayStartMillis: Long): Flow<List<Meal>>


    @Query("""
        SELECT IFNULL(SUM(calories), 0) FROM meals
        WHERE createdAt BETWEEN :fromMillis AND :toMillis
    """)
    fun observeTotalCalories(fromMillis: Long, toMillis: Long): Flow<Int>

    @Query("""
        SELECT IFNULL(SUM(protein), 0) FROM meals
        WHERE createdAt BETWEEN :fromMillis AND :toMillis
    """)
    fun observeTotalProtein(fromMillis: Long, toMillis: Long): Flow<Float>

    @Query("""
        SELECT IFNULL(SUM(carbs), 0) FROM meals
        WHERE createdAt BETWEEN :fromMillis AND :toMillis
    """)
    fun observeTotalCarbs(fromMillis: Long, toMillis: Long): Flow<Float>

    @Query("""
        SELECT IFNULL(SUM(fat), 0) FROM meals
        WHERE createdAt BETWEEN :fromMillis AND :toMillis
    """)
    fun observeTotalFat(fromMillis: Long, toMillis: Long): Flow<Float>
}
