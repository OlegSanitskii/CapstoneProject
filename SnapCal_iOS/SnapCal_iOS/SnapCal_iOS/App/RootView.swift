import SwiftUI

struct RootView: View {
    @EnvironmentObject private var session: AppSession
    @State private var showSplash = true

    var body: some View {
        Group {
            if showSplash {
                SplashView {
                    withAnimation(.easeInOut(duration: 0.25)) {
                        showSplash = false
                    }
                }
            } else {
                NavigationStack {
                    if session.isLoggedIn {
                        DashboardView()
                    } else {
                        switch session.authScreen {
                        case .login:
                            LoginView()
                        case .signUp:
                            SignUpView()
                        }
                    }
                }
            }
        }
    }
}

#Preview {
    RootView()
        .environmentObject(AppSession())
}
