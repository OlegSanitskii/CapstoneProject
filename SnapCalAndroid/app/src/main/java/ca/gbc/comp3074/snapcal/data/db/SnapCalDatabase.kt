package ca.gbc.comp3074.snapcal.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ca.gbc.comp3074.snapcal.data.model.Meal
import ca.gbc.comp3074.snapcal.data.user.User
import ca.gbc.comp3074.snapcal.data.user.UserDao

@Database(
    entities = [Meal::class, User::class],
    version = 7,
    exportSchema = true
)
abstract class SnapCalDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun userDao(): UserDao
}