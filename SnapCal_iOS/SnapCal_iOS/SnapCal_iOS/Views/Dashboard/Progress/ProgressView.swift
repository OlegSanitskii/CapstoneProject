import SwiftUI
import SwiftData

struct ProgressView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Meal.createdAt, order: .reverse) private var meals: [Meal]
    @State private var mealToEdit: Meal?

    var body: some View {
        ZStack {
            SnapCalTheme.background
                .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    Text("Progress")
                        .font(.title2)
                        .fontWeight(.semibold)

                    summaryCards
                    weeklyChart
                    todayMeals
                }
                .padding(.horizontal, SnapCalTheme.screenHorizontalPadding)
                .padding(.vertical, 20)
            }
        }
        .sheet(item: $mealToEdit) { meal in
            EditMealSheet(meal: meal)
        }
    }

    private var summaryCards: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                card("Calories", "\(todayCalories) kcal")
                card("Protein", gramsText(todayProtein))
                card("Carbs", gramsText(todayCarbs))
                card("Fat", gramsText(todayFat))
            }
        }
    }

    private func card(_ title: String, _ value: String) -> some View {
        SnapCard {
            VStack(alignment: .leading, spacing: 8) {
                Text(title)
                    .font(.caption)
                    .foregroundStyle(SnapCalTheme.textSecondary)
                Text(value)
                    .fontWeight(.bold)
                    .foregroundStyle(SnapCalTheme.textPrimary)
            }
            .frame(width: 120, alignment: .leading)
        }
    }

    private var weeklyChart: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Calories In (last 7 days)")
                .fontWeight(.semibold)

            SnapCard {
                HStack(alignment: .bottom, spacing: 10) {
                    ForEach(weeklyEntries) { entry in
                        VStack(spacing: 8) {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(SnapCalTheme.primary)
                                .frame(width: 28, height: max(12, entry.barHeight(maxCalories: maxWeeklyCalories)))

                            Text(entry.shortLabel)
                                .font(.caption2)
                                .foregroundStyle(SnapCalTheme.textSecondary)
                        }
                        .frame(maxWidth: .infinity)
                    }
                }
                .frame(height: 180, alignment: .bottom)
            }
        }
    }

    private var todayMeals: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Today's Meals")
                .fontWeight(.semibold)

            if todaysMeals.isEmpty {
                SnapCard {
                    Text("No meals logged today.")
                        .foregroundStyle(SnapCalTheme.textSecondary)
                }
            } else {
                ForEach(todaysMeals) { meal in
                    MealRow(meal: meal) {
                        mealToEdit = meal
                    } onDelete: {
                        modelContext.delete(meal)
                    }
                }
            }
        }
    }

    private var todaysMeals: [Meal] {
        meals.filter { Calendar.current.isDateInToday($0.createdAt) }
    }

    private var todayCalories: Int {
        todaysMeals.reduce(0) { $0 + $1.calories }
    }

    private var todayProtein: Double {
        todaysMeals.reduce(0) { $0 + $1.protein }
    }

    private var todayCarbs: Double {
        todaysMeals.reduce(0) { $0 + $1.carbs }
    }

    private var todayFat: Double {
        todaysMeals.reduce(0) { $0 + $1.fat }
    }

    private var weeklyEntries: [DailyCaloriesEntry] {
        let calendar = Calendar.current
        return (0..<7).reversed().compactMap { offset in
            guard let date = calendar.date(byAdding: .day, value: -offset, to: .now) else { return nil }
            let total = meals
                .filter { calendar.isDate($0.createdAt, inSameDayAs: date) }
                .reduce(0) { $0 + $1.calories }
            return DailyCaloriesEntry(date: date, calories: total)
        }
    }

    private var maxWeeklyCalories: Int {
        max(weeklyEntries.map(\.calories).max() ?? 0, 100)
    }

    private func gramsText(_ value: Double) -> String {
        if value.rounded() == value {
            return "\(Int(value)) g"
        }
        return String(format: "%.1f g", value)
    }
}

private struct DailyCaloriesEntry: Identifiable {
    let id = UUID()
    let date: Date
    let calories: Int

    var shortLabel: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "E"
        return String(formatter.string(from: date).prefix(1))
    }

    func barHeight(maxCalories: Int) -> CGFloat {
        guard maxCalories > 0 else { return 12 }
        return CGFloat(calories) / CGFloat(maxCalories) * 120
    }
}

#Preview {
    ProgressView()
        .modelContainer(for: Meal.self, inMemory: true)
}
