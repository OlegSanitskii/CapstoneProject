import SwiftUI
import SwiftData

struct DashboardView: View {
    @EnvironmentObject private var session: AppSession
    @Query(sort: \Meal.createdAt, order: .reverse) private var meals: [Meal]

    @State private var selectedTab: SnapTab = .home
    @State private var showManualMeal = false
    @State private var showScanMeal = false
    @State private var mealToEdit: Meal?

    @State private var todaySteps = 0
    @State private var calorieOut = 0

    private let calorieGoal = 2300
    private let stepGoal = 8000
    private let healthService = HealthService()

    var body: some View {
        ZStack {
            SnapCalTheme.background
                .ignoresSafeArea()

            VStack(spacing: 0) {
                content

                BottomTabBar(selected: selectedTab) { tab in
                    selectedTab = tab
                }
            }
        }
        .navigationBarBackButtonHidden(true)
        .sheet(isPresented: $showManualMeal) {
            ManualMealView()
        }
        .sheet(isPresented: $showScanMeal) {
            ScanMealView()
        }
        .sheet(item: $mealToEdit) { meal in
            EditMealSheet(meal: meal)
        }
        .task {
            await loadMockHealthData()
        }
    }

    @ViewBuilder
    private var content: some View {
        switch selectedTab {
        case .home:
            homeContent
        case .log:
            ManualMealView()
        case .progress:
            ProgressView()
        case .settings:
            SettingsView()
        }
    }

    private var homeContent: some View {
        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 28) {
                headerSection
                caloriesSection
                statsSection
                quickActionsSection
                recentMealsSection
                accountSection
            }
            .padding(.horizontal, SnapCalTheme.screenHorizontalPadding)
            .padding(.top, 28)
            .padding(.bottom, 24)
        }
    }

    private var headerSection: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Dashboard")
                .font(.system(size: 24, weight: .bold))
                .foregroundStyle(SnapCalTheme.textPrimary)

            Text(session.isGuest ? "Welcome, Guest" : session.userEmail)
                .font(.system(size: 15))
                .foregroundStyle(SnapCalTheme.textSecondary)
        }
    }

    private var caloriesSection: some View {
        VStack(alignment: .leading, spacing: 18) {
            Text("Calories (in vs out)")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(SnapCalTheme.textPrimary)

            HStack(alignment: .center, spacing: 18) {
                RingChartView(progress: calorieProgress)
                    .frame(width: 108, height: 108)

                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 12) {
                        Text("In: \(todayCalories) kcal")
                            .font(.system(size: 14))
                            .foregroundStyle(SnapCalTheme.textPrimary)

                        Text("Out: \(calorieOut) kcal")
                            .font(.system(size: 14))
                            .foregroundStyle(SnapCalTheme.textPrimary)
                    }

                    Text("Balance: \(todayCalories - calorieOut) kcal")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(SnapCalTheme.textPrimary)

                    HStack(spacing: 16) {
                        legendItem(color: SnapCalTheme.chartBlue, text: "Consumed")
                        legendItem(color: SnapCalTheme.chartGray, text: "Remaining")
                    }
                    .padding(.top, 4)
                }

                Spacer()
            }
        }
    }

    private var statsSection: some View {
        HStack(alignment: .top, spacing: 16) {
            MacroCard(
                title: "Steps",
                value: "\(todaySteps)",
                subtitle: "Goal: \(stepGoal)"
            )

            MacroCard(
                title: "Meals",
                value: "\(todaysMeals.count)",
                subtitle: meals.first?.name ?? "No meals yet"
            )
        }
    }

    private var quickActionsSection: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Quick Actions")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(SnapCalTheme.textPrimary)

            HStack(spacing: 12) {
                quickActionFilled(title: "Scan\nLabel") {
                    showScanMeal = true
                }

                quickActionOutlined(title: "Log\nMeal") {
                    showManualMeal = true
                }

                quickActionOutlined(title: "Progres\ns") {
                    selectedTab = .progress
                }
            }
        }
    }

    private var recentMealsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Recent Meals")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(SnapCalTheme.textPrimary)

            if meals.isEmpty {
                SnapCard {
                    Text("No meals logged yet. Use Log Meal or Scan Label to get started.")
                        .foregroundStyle(SnapCalTheme.textSecondary)
                }
            } else {
                ForEach(Array(meals.prefix(3))) { meal in
                    MealRow(meal: meal) {
                        mealToEdit = meal
                    } onDelete: {
                        deleteMeal(meal)
                    }
                }
            }
        }
    }

    private var accountSection: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Account")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(SnapCalTheme.textPrimary)

            Button {
                session.signOut()
            } label: {
                Text("Sign out")
                    .font(.system(size: 17, weight: .semibold))
                    .frame(maxWidth: .infinity)
                    .frame(height: 54)
                    .background(SnapCalTheme.danger)
                    .foregroundStyle(.white)
                    .clipShape(Capsule())
            }
        }
    }

    private var todaysMeals: [Meal] {
        meals.filter { Calendar.current.isDateInToday($0.createdAt) }
    }

    private var todayCalories: Int {
        todaysMeals.reduce(0) { $0 + $1.calories }
    }

    private var calorieProgress: Double {
        min(max(Double(todayCalories) / Double(calorieGoal), 0), 1)
    }

    private func deleteMeal(_ meal: Meal) {
        if let context = meal.modelContext {
            context.delete(meal)
        }
    }

    private func legendItem(color: Color, text: String) -> some View {
        HStack(spacing: 6) {
            RoundedRectangle(cornerRadius: 2)
                .fill(color)
                .frame(width: 12, height: 12)

            Text(text)
                .font(.system(size: 13))
                .foregroundStyle(SnapCalTheme.textSecondary)
        }
    }

    private func quickActionFilled(title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 16, weight: .medium))
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                .frame(height: 56)
                .background(SnapCalTheme.primary)
                .foregroundStyle(.white)
                .clipShape(Capsule())
        }
    }

    private func quickActionOutlined(title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 16, weight: .medium))
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                .frame(height: 56)
                .background(Color.clear)
                .foregroundStyle(SnapCalTheme.primary)
                .overlay(
                    Capsule()
                        .stroke(SnapCalTheme.border, lineWidth: 1)
                )
        }
    }

    private func loadMockHealthData() async {
        let steps = await healthService.fetchTodaySteps()
        let activeCalories = await healthService.fetchTodayActiveEnergy()

        await MainActor.run {
            todaySteps = steps
            calorieOut = activeCalories
        }
    }
}

private struct RingChartView: View {
    let progress: Double

    var body: some View {
        ZStack {
            Circle()
                .stroke(SnapCalTheme.chartGray, lineWidth: 12)

            Circle()
                .trim(from: 0, to: progress)
                .stroke(
                    SnapCalTheme.chartBlue,
                    style: StrokeStyle(lineWidth: 12, lineCap: .butt)
                )
                .rotationEffect(.degrees(-90))
        }
    }
}

#Preview {
    NavigationStack {
        DashboardView()
            .environmentObject({
                let session = AppSession()
                session.signIn(email: "oleg@example.com", remember: true)
                return session
            }())
            .modelContainer(for: Meal.self, inMemory: true)
    }
}
