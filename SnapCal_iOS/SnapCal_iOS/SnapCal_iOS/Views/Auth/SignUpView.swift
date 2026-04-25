import SwiftUI

struct SignUpView: View {
    @EnvironmentObject private var session: AppSession

    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var errorMessage = ""

    var body: some View {
        ZStack {
            SnapCalTheme.background
                .ignoresSafeArea()

            VStack(spacing: 0) {
                Spacer(minLength: 95)

                Text("Create account")
                    .font(.system(size: 28, weight: .regular))
                    .foregroundStyle(SnapCalTheme.textPrimary)

                VStack(spacing: 16) {
                    SnapTextField(title: "Name (optional)", text: $name)
                    SnapTextField(title: "Email", text: $email)
                    SnapTextField(title: "Password", text: $password, isSecure: true)
                    SnapTextField(title: "Confirm password", text: $confirmPassword, isSecure: true)
                }
                .padding(.top, 42)

                if !errorMessage.isEmpty {
                    Text(errorMessage)
                        .font(.system(size: 14))
                        .foregroundStyle(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, 10)
                }

                PrimaryButton(title: "Create account") {
                    handleSignUp()
                }
                .padding(.top, 22)

                HStack(spacing: 4) {
                    Text("Already have an account?")
                        .font(.system(size: 13))
                        .foregroundStyle(SnapCalTheme.textSecondary)

                    Button("Login") {
                        session.showLogin()
                    }
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(SnapCalTheme.primary)
                }
                .padding(.top, 14)

                Spacer()
            }
            .padding(.horizontal, SnapCalTheme.screenHorizontalPadding)
        }
        .navigationBarBackButtonHidden(true)
    }

    private func handleSignUp() {
        let trimmedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedPassword = password.trimmingCharacters(in: .whitespacesAndNewlines)

        guard trimmedEmail.contains("@") else {
            errorMessage = "Please enter a valid email."
            return
        }

        guard trimmedPassword.count >= 4 else {
            errorMessage = "Password must be at least 4 characters."
            return
        }

        guard trimmedPassword == confirmPassword else {
            errorMessage = "Passwords do not match."
            return
        }

        errorMessage = ""
        session.signIn(email: trimmedEmail, remember: true)
    }
}

#Preview {
    NavigationStack {
        SignUpView()
            .environmentObject(AppSession())
    }
}
