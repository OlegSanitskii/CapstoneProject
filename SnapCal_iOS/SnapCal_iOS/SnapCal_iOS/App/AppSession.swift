import Foundation
import SwiftUI
import Combine

@MainActor
final class AppSession: ObservableObject {
    enum AuthScreen {
        case login
        case signUp
    }

    private enum Keys {
        static let isLoggedIn = "isLoggedIn"
        static let userEmail = "userEmail"
        static let rememberMe = "rememberMe"
        static let isGuest = "isGuest"
    }

    @Published var isLoggedIn: Bool
    @Published var userEmail: String
    @Published var rememberMe: Bool
    @Published var isGuest: Bool
    @Published var authScreen: AuthScreen = .login

    init() {
        let defaults = UserDefaults.standard

        self.isLoggedIn = defaults.bool(forKey: Keys.isLoggedIn)
        self.userEmail = defaults.string(forKey: Keys.userEmail) ?? ""
        self.rememberMe = defaults.bool(forKey: Keys.rememberMe)
        self.isGuest = defaults.bool(forKey: Keys.isGuest)
    }

    func showLogin() {
        authScreen = .login
    }

    func showSignUp() {
        authScreen = .signUp
    }

    func signIn(email: String, remember: Bool) {
        userEmail = email
        rememberMe = remember
        isGuest = false
        isLoggedIn = true

        let defaults = UserDefaults.standard
        defaults.set(true, forKey: Keys.isLoggedIn)
        defaults.set(email, forKey: Keys.userEmail)
        defaults.set(remember, forKey: Keys.rememberMe)
        defaults.set(false, forKey: Keys.isGuest)
    }

    func continueAsGuest() {
        userEmail = "Guest"
        rememberMe = false
        isGuest = true
        isLoggedIn = true

        let defaults = UserDefaults.standard
        defaults.set(true, forKey: Keys.isLoggedIn)
        defaults.set("Guest", forKey: Keys.userEmail)
        defaults.set(false, forKey: Keys.rememberMe)
        defaults.set(true, forKey: Keys.isGuest)
    }

    func signOut() {
        userEmail = ""
        rememberMe = false
        isGuest = false
        isLoggedIn = false
        authScreen = .login

        let defaults = UserDefaults.standard
        defaults.set(false, forKey: Keys.isLoggedIn)
        defaults.set("", forKey: Keys.userEmail)
        defaults.set(false, forKey: Keys.rememberMe)
        defaults.set(false, forKey: Keys.isGuest)
    }
}
