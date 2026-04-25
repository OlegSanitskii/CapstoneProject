import Foundation
import SwiftData

struct MealRepository {
    let context: ModelContext

    func addMeal(
        name: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        mealType: String,
        portionGrams: Double? = nil,
        notes: String? = nil
    ) {
        let meal = Meal(
            name: name,
            calories: calories,
            protein: protein,
            carbs: carbs,
            fat: fat,
            mealType: mealType,
            portionGrams: portionGrams,
            notes: notes
        )
        context.insert(meal)
    }

    func deleteMeal(_ meal: Meal) {
        context.delete(meal)
    }
}
