package ca.gbc.comp3074.snapcal.data.repo

import ca.gbc.comp3074.snapcal.data.db.MealDao
import ca.gbc.comp3074.snapcal.data.model.Meal
import kotlinx.coroutines.flow.Flow

class MealsRepository(private val mealDao: MealDao) {

    suspend fun save(meal: Meal): Long = mealDao.upsert(meal)

    suspend fun saveAll(meals: List<Meal>) = mealDao.upsertAll(meals)

    suspend fun delete(meal: Meal) = mealDao.delete(meal)

    suspend fun deleteById(id: Long, userId: Int) = mealDao.deleteById(id, userId)

    suspend fun getById(id: Long, userId: Int) = mealDao.getById(id, userId)

    fun observeAll(userId: Int): Flow<List<Meal>> =
        mealDao.observeAll(userId)

    fun observeInRange(userId: Int, fromMillis: Long, toMillis: Long): Flow<List<Meal>> =
        mealDao.observeInRange(userId, fromMillis, toMillis)

    suspend fun getInRange(userId: Int, fromMillis: Long, toMillis: Long): List<Meal> =
        mealDao.getInRange(userId, fromMillis, toMillis)

    fun observeByDay(userId: Int, dayStartMillis: Long): Flow<List<Meal>> =
        mealDao.observeByDay(userId, dayStartMillis)

    fun observeTotalCalories(userId: Int, fromMillis: Long, toMillis: Long): Flow<Int> =
        mealDao.observeTotalCalories(userId, fromMillis, toMillis)

    fun observeTotalProtein(userId: Int, fromMillis: Long, toMillis: Long): Flow<Float> =
        mealDao.observeTotalProtein(userId, fromMillis, toMillis)

    fun observeTotalCarbs(userId: Int, fromMillis: Long, toMillis: Long): Flow<Float> =
        mealDao.observeTotalCarbs(userId, fromMillis, toMillis)

    fun observeTotalFat(userId: Int, fromMillis: Long, toMillis: Long): Flow<Float> =
        mealDao.observeTotalFat(userId, fromMillis, toMillis)
}