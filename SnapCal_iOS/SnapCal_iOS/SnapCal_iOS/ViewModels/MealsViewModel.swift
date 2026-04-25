import Foundation
import Combine

@MainActor
final class MealsViewModel: ObservableObject {
    @Published var foodName = ""
    @Published var mealType = "Meal"
    @Published var portion = ""
    @Published var calories = ""
    @Published var protein = ""
    @Published var carbs = ""
    @Published var fat = ""
    @Published var notes = ""
    @Published var errorMessage = ""

    func clear() {
        foodName = ""
        mealType = "Meal"
        portion = ""
        calories = ""
        protein = ""
        carbs = ""
        fat = ""
        notes = ""
        errorMessage = ""
    }
}
