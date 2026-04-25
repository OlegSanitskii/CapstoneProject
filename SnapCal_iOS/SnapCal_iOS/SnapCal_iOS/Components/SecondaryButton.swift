import SwiftUI

struct SecondaryButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 16, weight: .medium))
                .frame(maxWidth: .infinity)
                .frame(height: 52)
                .background(Color.clear)
                .foregroundStyle(SnapCalTheme.primary)
                .overlay(
                    Capsule()
                        .stroke(SnapCalTheme.border, lineWidth: 1)
                )
        }
    }
}
