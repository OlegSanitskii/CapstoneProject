package ca.gbc.comp3074.snapcal.data.repo

import ca.gbc.comp3074.snapcal.data.db.MealDao
import ca.gbc.comp3074.snapcal.data.model.Meal
import kotlinx.coroutines.flow.Flow

class MealsRepository(private val mealDao: MealDao) {

    suspend fun save(meal: Meal): Long = mealDao.upsert(meal)
    suspend fun saveAll(meals: List<Meal>) = mealDao.upsertAll(meals)
    suspend fun delete(meal: Meal) = mealDao.delete(meal)
    suspend fun deleteById(id: Long) = mealDao.deleteById(id)
    suspend fun getById(id: Long) = mealDao.getById(id)

    fun observeAll(): Flow<List<Meal>> = mealDao.observeAll()
    fun observeInRange(fromMillis: Long, toMillis: Long) =
        mealDao.observeInRange(fromMillis, toMillis)

    suspend fun getInRange(fromMillis: Long, toMillis: Long): List<Meal> =
        mealDao.getInRange(fromMillis, toMillis)

    fun observeByDay(dayStartMillis: Long) =
        mealDao.observeByDay(dayStartMillis)

    fun observeTotalCalories(fromMillis: Long, toMillis: Long) =
        mealDao.observeTotalCalories(fromMillis, toMillis)

    fun observeTotalProtein(fromMillis: Long, toMillis: Long) =
        mealDao.observeTotalProtein(fromMillis, toMillis)

    fun observeTotalCarbs(fromMillis: Long, toMillis: Long) =
        mealDao.observeTotalCarbs(fromMillis, toMillis)

    fun observeTotalFat(fromMillis: Long, toMillis: Long) =
        mealDao.observeTotalFat(fromMillis, toMillis)
}