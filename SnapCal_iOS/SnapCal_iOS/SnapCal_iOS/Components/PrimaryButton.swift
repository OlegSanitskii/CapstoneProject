import SwiftUI

struct PrimaryButton: View {
    let title: String
    let action: () -> Void
    var isEnabled: Bool = true

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 17, weight: .semibold))
                .frame(maxWidth: .infinity)
                .frame(height: 52)
                .background(isEnabled ? SnapCalTheme.primary : SnapCalTheme.surface)
                .foregroundStyle(isEnabled ? Color.white : SnapCalTheme.textSecondary)
                .clipShape(Capsule())
        }
        .disabled(!isEnabled)
    }
}
