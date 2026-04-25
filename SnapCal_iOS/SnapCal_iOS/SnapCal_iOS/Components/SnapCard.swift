import SwiftUI

struct SnapCard<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            content
        }
        .padding(16)
        .background(SnapCalTheme.surface)
        .clipShape(RoundedRectangle(cornerRadius: SnapCalTheme.cardRadius))
    }
}
