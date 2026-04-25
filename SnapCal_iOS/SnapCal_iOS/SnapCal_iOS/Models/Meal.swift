import Foundation
import SwiftData

@Model
final class Meal {
    var id: UUID
    var name: String
    var calories: Int
    var protein: Double
    var carbs: Double
    var fat: Double
    var mealType: String
    var createdAt: Date
    var portionGrams: Double?
    var photoPath: String?
    var notes: String?

    init(
        id: UUID = UUID(),
        name: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        mealType: String,
        createdAt: Date = .now,
        portionGrams: Double? = nil,
        photoPath: String? = nil,
        notes: String? = nil
    ) {
        self.id = id
        self.name = name
        self.calories = calories
        self.protein = protein
        self.carbs = carbs
        self.fat = fat
        self.mealType = mealType
        self.createdAt = createdAt
        self.portionGrams = portionGrams
        self.photoPath = photoPath
        self.notes = notes
    }
}
