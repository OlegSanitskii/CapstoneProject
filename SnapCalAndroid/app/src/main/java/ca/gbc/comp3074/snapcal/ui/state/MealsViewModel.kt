package ca.gbc.comp3074.snapcal.ui.state

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.snapcal.data.auth.SessionStore
import ca.gbc.comp3074.snapcal.data.db.DBProvider
import ca.gbc.comp3074.snapcal.data.model.Meal
import ca.gbc.comp3074.snapcal.data.repo.MealsRepository
import ca.gbc.comp3074.snapcal.util.endOfDayMillisExclusive
import ca.gbc.comp3074.snapcal.util.startOfDayMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MealsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: MealsRepository
    private val session = SessionStore(app)

    init {
        val db = DBProvider.get(app)
        repo = MealsRepository(db.mealDao())
    }

    private val currentUserId: StateFlow<Int> =
        session.userId
            .map { it ?: 0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = 0
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val meals: StateFlow<List<Meal>> =
        currentUserId
            .flatMapLatest { userId ->
                repo.observeAll(userId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addMeal(input: Meal) {
        viewModelScope.launch {
            val userId = currentUserId.value

            val toSave = input.copy(
                userId = userId,
                createdAt = if (input.createdAt == 0L) {
                    System.currentTimeMillis()
                } else {
                    input.createdAt
                }
            )

            repo.save(toSave)
        }
    }

    fun editMeal(meal: Meal) = viewModelScope.launch {
        repo.save(
            meal.copy(
                userId = currentUserId.value
            )
        )
    }

    fun deleteMeal(meal: Meal) = viewModelScope.launch {
        repo.deleteById(
            id = meal.id,
            userId = currentUserId.value
        )
    }

    fun deleteById(id: Long) = viewModelScope.launch {
        repo.deleteById(
            id = id,
            userId = currentUserId.value
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeMealsByDay(dayStartMillis: Long): Flow<List<Meal>> =
        currentUserId.flatMapLatest { userId ->
            repo.observeByDay(
                userId = userId,
                dayStartMillis = dayStartMillis
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeInRange(fromMillis: Long, toMillis: Long): Flow<List<Meal>> =
        currentUserId.flatMapLatest { userId ->
            repo.observeInRange(
                userId = userId,
                fromMillis = fromMillis,
                toMillis = toMillis
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTotalCalories(fromMillis: Long, toMillis: Long): Flow<Int> =
        currentUserId.flatMapLatest { userId ->
            repo.observeTotalCalories(
                userId = userId,
                fromMillis = fromMillis,
                toMillis = toMillis
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTotalProtein(fromMillis: Long, toMillis: Long): Flow<Float> =
        currentUserId.flatMapLatest { userId ->
            repo.observeTotalProtein(
                userId = userId,
                fromMillis = fromMillis,
                toMillis = toMillis
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTotalCarbs(fromMillis: Long, toMillis: Long): Flow<Float> =
        currentUserId.flatMapLatest { userId ->
            repo.observeTotalCarbs(
                userId = userId,
                fromMillis = fromMillis,
                toMillis = toMillis
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTotalFat(fromMillis: Long, toMillis: Long): Flow<Float> =
        currentUserId.flatMapLatest { userId ->
            repo.observeTotalFat(
                userId = userId,
                fromMillis = fromMillis,
                toMillis = toMillis
            )
        }

    @RequiresApi(Build.VERSION_CODES.O)
    fun todayBounds(now: Long = System.currentTimeMillis()): Pair<Long, Long> =
        startOfDayMillis(now) to endOfDayMillisExclusive(now)

    data class IntakeDay(
        val dayStart: Long,
        val kcal: Int
    )

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeDailyIntake(lastDays: Int): Flow<List<IntakeDay>> {
        val todayStart = startOfDayMillis()
        val oneDayMs = 86_400_000L

        val dayStarts = (lastDays - 1 downTo 0).map { daysBack ->
            todayStart - daysBack * oneDayMs
        }

        return currentUserId.flatMapLatest { userId ->
            val flows: List<Flow<Int>> = dayStarts.map { dayStart ->
                repo.observeTotalCalories(
                    userId = userId,
                    fromMillis = dayStart,
                    toMillis = dayStart + oneDayMs
                )
            }

            combine(flows) { dailyKcals ->
                dailyKcals.mapIndexed { index, kcal ->
                    IntakeDay(
                        dayStart = dayStarts[index],
                        kcal = kcal
                    )
                }
            }
        }
    }
}