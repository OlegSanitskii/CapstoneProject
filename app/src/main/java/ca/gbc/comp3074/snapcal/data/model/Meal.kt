package ca.gbc.comp3074.snapcal.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "meals",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["mealType"])
    ]
)
data class Meal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,


    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,


    val mealType: String,       // "breakfast" | "lunch" | "dinner" | "snack"
    val createdAt: Long,

    val portionGrams: Float? = null,
    val photoPath: String? = null,
    val notes: String? = null
)
