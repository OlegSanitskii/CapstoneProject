import SwiftUI
import SwiftData

@main
struct SnapCalApp: App {
    @StateObject private var session = AppSession()
    private let container: ModelContainer

    init() {
        do {
            container = try ModelContainer(for: Meal.self, User.self)
        } catch {
            fatalError("Failed to create ModelContainer: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(session)
        }
        .modelContainer(container)
    }
}
