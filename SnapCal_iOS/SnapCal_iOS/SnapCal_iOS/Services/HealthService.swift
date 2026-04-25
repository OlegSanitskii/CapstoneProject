import Foundation

struct MockHealthData {
    let todaySteps: Int
    let todayActiveCalories: Int
    let restingHeartRate: Int
    let averageWorkoutHeartRate: Int
    let latestWorkoutMaxHeartRate: Int
}

final class HealthService {
    func fetchMockHealthData() async -> MockHealthData {
        return MockHealthData(
            todaySteps: 8426,
            todayActiveCalories: 615,
            restingHeartRate: 58,
            averageWorkoutHeartRate: 136,
            latestWorkoutMaxHeartRate: 172
        )
    }

    func fetchTodaySteps() async -> Int {
        return 8426
    }

    func fetchTodayActiveEnergy() async -> Int {
        return 615
    }

    func fetchRestingHeartRate() async -> Int {
        return 58
    }

    func fetchAverageWorkoutHeartRate() async -> Int {
        return 136
    }

    func fetchLatestWorkoutMaxHeartRate() async -> Int {
        return 172
    }
}
