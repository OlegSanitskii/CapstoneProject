import SwiftUI
import SwiftData

struct SettingsView: View {
    @EnvironmentObject private var session: AppSession
    @Query(sort: \Meal.createdAt, order: .reverse) private var meals: [Meal]

    @State private var garminConnected = true

    @State private var todaySteps = 0
    @State private var todayActiveCalories = 0
    @State private var restingHeartRate = 0
    @State private var averageWorkoutHeartRate = 0
    @State private var latestWorkoutMaxHeartRate = 0

    private let healthService = HealthService()

    var body: some View {
        ZStack {
            SnapCalTheme.background
                .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    Text("Settings")
                        .font(.title2)
                        .fontWeight(.semibold)

                    garminSection

                    Divider()

                    account

                    Divider()

                    nutritionActivity

                    Divider()

                    healthSummary
                }
                .padding(.horizontal, SnapCalTheme.screenHorizontalPadding)
                .padding(.vertical, 20)
            }
        }
        .task {
            await loadMockHealthData()
        }
    }

    private var garminSection: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Garmin / Health")
                .fontWeight(.semibold)

            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Mock Garmin Connected")
                        .fontWeight(.semibold)

                    Text("This is demo health data for the iOS simulator.")
                        .font(.caption)
                        .foregroundStyle(SnapCalTheme.textSecondary)
                }

                Spacer()

                Toggle("", isOn: $garminConnected)
                    .labelsHidden()
                    .tint(SnapCalTheme.primary)
                    .disabled(true)
            }
        }
    }

    private var account: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Account")
                .fontWeight(.semibold)

            Text(session.isGuest ? "Guest session" : session.userEmail)
                .font(.caption)
                .foregroundStyle(SnapCalTheme.textSecondary)

            Button("Sign out") {
                session.signOut()
            }
            .foregroundStyle(SnapCalTheme.primary)
        }
    }

    private var nutritionActivity: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Nutrition Activity")
                .fontWeight(.semibold)

            Text("Logged meals: \(meals.count)")
            Text("Meals today: \(meals.filter { Calendar.current.isDateInToday($0.createdAt) }.count)")
            Text("Last logged meal: \(meals.first?.name ?? "None")")
            Text("Mock active calories: \(todayActiveCalories) kcal")
            Text("Mock steps: \(todaySteps)")
        }
    }

    private var healthSummary: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Health Summary")
                .fontWeight(.semibold)

            Text("Resting HR: \(restingHeartRate)")
            Text("Average HR (workouts): \(averageWorkoutHeartRate)")
            Text("Max HR (last workout): \(latestWorkoutMaxHeartRate)")
            Text("This section currently uses mock Garmin/Health data.")
                .font(.caption)
                .foregroundStyle(SnapCalTheme.textSecondary)
                .padding(.top, 4)
        }
    }

    private func loadMockHealthData() async {
        let data = await healthService.fetchMockHealthData()

        await MainActor.run {
            todaySteps = data.todaySteps
            todayActiveCalories = data.todayActiveCalories
            restingHeartRate = data.restingHeartRate
            averageWorkoutHeartRate = data.averageWorkoutHeartRate
            latestWorkoutMaxHeartRate = data.latestWorkoutMaxHeartRate
        }
    }
}

#Preview {
    SettingsView()
        .environmentObject(AppSession())
        .modelContainer(for: Meal.self, inMemory: true)
}
