import SwiftUI
import SwiftData

struct ManualMealView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Meal.createdAt, order: .reverse) private var meals: [Meal]

    @State private var name = ""
    @State private var mealType = "Meal"
    @State private var portion = ""
    @State private var calories = ""
    @State private var protein = ""
    @State private var carbs = ""
    @State private var fat = ""
    @State private var notes = ""
    @State private var validationMessage = ""
    @State private var mealToEdit: Meal?

    var body: some View {
        NavigationStack {
            ZStack {
                SnapCalTheme.background
                    .ignoresSafeArea()

                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        Text("Add Meal Manually")
                            .font(.title2)
                            .fontWeight(.semibold)

                        SnapTextField(title: "Food name", text: $name)
                        SnapTextField(title: "Meal type", text: $mealType)
                        SnapTextField(title: "Portion size (g)", text: $portion)
                        SnapTextField(title: "Calories (kcal)", text: $calories)

                        HStack {
                            SnapTextField(title: "Protein (g)", text: $protein)
                            SnapTextField(title: "Carbs (g)", text: $carbs)
                            SnapTextField(title: "Fat (g)", text: $fat)
                        }

                        SnapTextField(title: "Notes (optional)", text: $notes)

                        if !validationMessage.isEmpty {
                            Text(validationMessage)
                                .font(.system(size: 14))
                                .foregroundStyle(.red)
                        }

                        PrimaryButton(title: "Save Meal") {
                            saveMeal()
                        }

                        Divider()

                        recentMeals
                    }
                    .padding(.horizontal, SnapCalTheme.screenHorizontalPadding)
                    .padding(.vertical, 20)
                }
            }
            .navigationTitle("Manual Log")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
            .sheet(item: $mealToEdit) { meal in
                EditMealSheet(meal: meal)
            }
        }
    }

    private var recentMeals: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Recent meals")
                .fontWeight(.semibold)

            if meals.isEmpty {
                SnapCard {
                    Text("No meals yet. Add your first meal above.")
                        .foregroundStyle(SnapCalTheme.textSecondary)
                }
            } else {
                ForEach(Array(meals.prefix(5))) { meal in
                    MealRow(meal: meal) {
                        mealToEdit = meal
                    } onDelete: {
                        delete(meal)
                    }
                }
            }
        }
    }

    private func saveMeal() {
        let trimmedName = name.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedType = mealType.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !trimmedName.isEmpty else {
            validationMessage = "Please enter a food name."
            return
        }

        guard let caloriesValue = Int(calories), caloriesValue >= 0 else {
            validationMessage = "Calories must be a valid number."
            return
        }

        let proteinValue = parseDouble(protein)
        let carbsValue = parseDouble(carbs)
        let fatValue = parseDouble(fat)
        let portionValue = portion.isEmpty ? nil : parseDouble(portion)

        let meal = Meal(
            name: trimmedName,
            calories: caloriesValue,
            protein: proteinValue,
            carbs: carbsValue,
            fat: fatValue,
            mealType: trimmedType.isEmpty ? "Meal" : trimmedType,
            portionGrams: portionValue,
            notes: notes.isEmpty ? nil : notes
        )

        modelContext.insert(meal)
        validationMessage = ""
        clearForm()
    }

    private func delete(_ meal: Meal) {
        modelContext.delete(meal)
    }

    private func clearForm() {
        name = ""
        mealType = "Meal"
        portion = ""
        calories = ""
        protein = ""
        carbs = ""
        fat = ""
        notes = ""
    }

    private func parseDouble(_ text: String) -> Double {
        Double(text.replacingOccurrences(of: ",", with: ".")) ?? 0
    }
}

#Preview {
    ManualMealView()
        .modelContainer(for: Meal.self, inMemory: true)
}
