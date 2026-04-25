import SwiftUI

enum SnapTab {
    case home
    case log
    case progress
    case settings
}

struct BottomTabBar: View {
    let selected: SnapTab
    let onSelect: (SnapTab) -> Void

    var body: some View {
        HStack {
            tabButton(.home, icon: "house.fill", title: "Home")
            Spacer()
            tabButton(.log, icon: "pencil", title: "Log")
            Spacer()
            tabButton(.progress, icon: "waveform.path.ecg", title: "Progress")
            Spacer()
            tabButton(.settings, icon: "gearshape.fill", title: "Settings")
        }
        .padding(.horizontal, 22)
        .padding(.top, 12)
        .padding(.bottom, 10)
        .background(SnapCalTheme.surfaceSoft)
    }

    private func tabButton(_ tab: SnapTab, icon: String, title: String) -> some View {
        let isSelected = selected == tab

        return Button {
            onSelect(tab)
        } label: {
            VStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 18, weight: .semibold))
                    .frame(width: 44, height: 28)
                    .background(isSelected ? SnapCalTheme.primaryLight : Color.clear)
                    .clipShape(Capsule())

                Text(title)
                    .font(.system(size: 12, weight: .medium))
            }
            .foregroundStyle(SnapCalTheme.textPrimary)
        }
    }
}
