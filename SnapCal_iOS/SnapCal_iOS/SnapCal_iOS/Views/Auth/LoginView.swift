import SwiftUI

struct LoginView: View {
    @EnvironmentObject private var session: AppSession

    @State private var email = ""
    @State private var password = ""
    @State private var rememberMe = true
    @State private var errorMessage = ""

    var body: some View {
        ZStack {
            SnapCalTheme.background
                .ignoresSafeArea()

            VStack(spacing: 0) {
                Spacer(minLength: 70)

                Text("SnapCal")
                    .font(.system(size: 34, weight: .bold))
                    .foregroundStyle(SnapCalTheme.textPrimary)

                Text("Log in to continue")
                    .font(.system(size: 17))
                    .foregroundStyle(SnapCalTheme.textSecondary)
                    .padding(.top, 8)

                VStack(spacing: 16) {
                    SnapTextField(title: "Email", text: $email)
                    SnapTextField(title: "Password", text: $password, isSecure: true)
                }
                .padding(.top, 40)

                HStack(spacing: 12) {
                    Toggle("", isOn: $rememberMe)
                        .labelsHidden()
                        .tint(SnapCalTheme.primary)

                    Text("Remember me")
                        .font(.system(size: 17))
                        .foregroundStyle(SnapCalTheme.textPrimary)

                    Spacer()
                }
                .padding(.top, 18)

                if !errorMessage.isEmpty {
                    Text(errorMessage)
                        .font(.system(size: 14))
                        .foregroundStyle(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, 10)
                }

                PrimaryButton(title: "Login") {
                    handleLogin()
                }
                .padding(.top, 24)

                Button("Continue as guest") {
                    session.continueAsGuest()
                }
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(SnapCalTheme.primary)
                .padding(.top, 18)

                Button("Create an account") {
                    session.showSignUp()
                }
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(SnapCalTheme.primary)
                .padding(.top, 22)

                Spacer()
            }
            .padding(.horizontal, SnapCalTheme.screenHorizontalPadding)
        }
        .navigationBarBackButtonHidden(true)
    }

    private func handleLogin() {
        let trimmedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedPassword = password.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !trimmedEmail.isEmpty else {
            errorMessage = "Please enter your email."
            return
        }

        guard trimmedEmail.contains("@") else {
            errorMessage = "Please enter a valid email."
            return
        }

        guard !trimmedPassword.isEmpty else {
            errorMessage = "Please enter your password."
            return
        }

        errorMessage = ""
        session.signIn(email: trimmedEmail, remember: rememberMe)
    }
}

#Preview {
    NavigationStack {
        LoginView()
            .environmentObject(AppSession())
    }
}
