import SwiftUI
import SwiftData

struct EditMealSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Bindable var meal: Meal

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    SnapTextField(title: "Food name", text: $meal.name)
                    SnapTextField(title: "Meal type", text: $meal.mealType)
                    SnapTextField(title: "Portion size (g)", text: portionBinding)
                    SnapTextField(title: "Calories", text: caloriesBinding)

                    HStack {
                        SnapTextField(title: "Protein", text: proteinBinding)
                        SnapTextField(title: "Carbs", text: carbsBinding)
                        SnapTextField(title: "Fat", text: fatBinding)
                    }

                    HStack(spacing: 12) {
                        PrimaryButton(title: "Done") {
                            dismiss()
                        }

                        SecondaryButton(title: "Cancel") {
                            dismiss()
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Edit meal")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private var portionBinding: Binding<String> {
        Binding(
            get: { meal.portionGrams.map { format($0) } ?? "" },
            set: { meal.portionGrams = Double($0.replacingOccurrences(of: ",", with: ".")) }
        )
    }

    private var caloriesBinding: Binding<String> {
        Binding(
            get: { String(meal.calories) },
            set: { meal.calories = Int($0) ?? 0 }
        )
    }

    private var proteinBinding: Binding<String> {
        Binding(
            get: { format(meal.protein) },
            set: { meal.protein = Double($0.replacingOccurrences(of: ",", with: ".")) ?? 0 }
        )
    }

    private var carbsBinding: Binding<String> {
        Binding(
            get: { format(meal.carbs) },
            set: { meal.carbs = Double($0.replacingOccurrences(of: ",", with: ".")) ?? 0 }
        )
    }

    private var fatBinding: Binding<String> {
        Binding(
            get: { format(meal.fat) },
            set: { meal.fat = Double($0.replacingOccurrences(of: ",", with: ".")) ?? 0 }
        )
    }

    private func format(_ value: Double) -> String {
        if value.rounded() == value {
            return String(Int(value))
        }
        return String(format: "%.1f", value)
    }
}

#Preview {
    let meal = Meal(name: "Protein Shake", calories: 120, protein: 24, carbs: 3, fat: 1, mealType: "Snack")
    return EditMealSheet(meal: meal)
}
