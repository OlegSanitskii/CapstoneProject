import SwiftUI

struct MealRow: View {
    let meal: Meal
    var onEdit: () -> Void
    var onDelete: () -> Void

    var body: some View {
        SnapCard {
            HStack(alignment: .top, spacing: 12) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(meal.name)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(SnapCalTheme.textPrimary)

                    Text(summaryText)
                        .font(.system(size: 13))
                        .foregroundStyle(SnapCalTheme.textSecondary)

                    Text(meal.mealType)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(SnapCalTheme.primary)
                }

                Spacer()

                VStack(spacing: 10) {
                    Button(action: onEdit) {
                        Image(systemName: "pencil")
                    }

                    Button(role: .destructive, action: onDelete) {
                        Image(systemName: "trash")
                    }
                }
                .foregroundStyle(SnapCalTheme.textPrimary)
            }
        }
    }

    private var summaryText: String {
        "\(meal.calories) kcal • P \(format(meal.protein)) • C \(format(meal.carbs)) • F \(format(meal.fat))"
    }

    private func format(_ value: Double) -> String {
        if value.rounded() == value {
            return String(Int(value))
        }
        return String(format: "%.1f", value)
    }
}
