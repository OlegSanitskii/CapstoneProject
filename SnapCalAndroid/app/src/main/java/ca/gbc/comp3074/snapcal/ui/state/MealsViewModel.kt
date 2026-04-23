package ca.gbc.comp3074.snapcal.ui.state

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.snapcal.data.db.DBProvider
import ca.gbc.comp3074.snapcal.data.model.Meal
import ca.gbc.comp3074.snapcal.data.repo.MealsRepository
import ca.gbc.comp3074.snapcal.util.startOfDayMillis
import ca.gbc.comp3074.snapcal.util.endOfDayMillisExclusive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MealsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: MealsRepository

    init {
        val db = DBProvider.get(app)
        repo = MealsRepository(db.mealDao())
    }
    
    val meals: StateFlow<List<Meal>> =
        repo.observeAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addMeal(input: Meal) {
        val toSave = input.copy(
            createdAt = if (input.createdAt == 0L) System.currentTimeMillis() else input.createdAt
        )
        viewModelScope.launch { repo.save(toSave) }
    }

    fun editMeal(meal: Meal) = viewModelScope.launch { repo.save(meal) }
    fun deleteMeal(meal: Meal) = viewModelScope.launch { repo.delete(meal) }
    fun deleteById(id: Long) = viewModelScope.launch { repo.deleteById(id) }


    fun observeMealsByDay(dayStartMillis: Long): Flow<List<Meal>> =
        repo.observeByDay(dayStartMillis)

    fun observeInRange(fromMillis: Long, toMillis: Long): Flow<List<Meal>> =
        repo.observeInRange(fromMillis, toMillis)

    fun observeTotalCalories(fromMillis: Long, toMillis: Long) =
        repo.observeTotalCalories(fromMillis, toMillis)

    fun observeTotalProtein(fromMillis: Long, toMillis: Long) =
        repo.observeTotalProtein(fromMillis, toMillis)

    fun observeTotalCarbs(fromMillis: Long, toMillis: Long) =
        repo.observeTotalCarbs(fromMillis, toMillis)

    fun observeTotalFat(fromMillis: Long, toMillis: Long) =
        repo.observeTotalFat(fromMillis, toMillis)

    @RequiresApi(Build.VERSION_CODES.O)
    fun todayBounds(now: Long = System.currentTimeMillis()): Pair<Long, Long> =
        startOfDayMillis(now) to endOfDayMillisExclusive(now)

    data class IntakeDay(val dayStart: Long, val kcal: Int)

    @RequiresApi(Build.VERSION_CODES.O)
    fun observeDailyIntake(lastDays: Int): Flow<List<IntakeDay>> {
        val todayStart = startOfDayMillis()
        val oneDayMs = 86_400_000L


        val dayStarts = (lastDays - 1 downTo 0).map { d -> todayStart - d * oneDayMs }


        val flows: List<Flow<Int>> = dayStarts.map { ds ->
            repo.observeTotalCalories(ds, ds + oneDayMs)
        }

        return combine(flows) { dailyKcals ->
            dailyKcals.mapIndexed { idx, kcal -> IntakeDay(dayStarts[idx], kcal) }
        }
    }
}
