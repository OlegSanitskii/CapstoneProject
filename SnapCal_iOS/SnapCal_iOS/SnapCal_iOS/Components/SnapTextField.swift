import SwiftUI

struct SnapTextField: View {
    let title: String
    @Binding var text: String
    var isSecure: Bool = false

    var body: some View {
        Group {
            if isSecure {
                SecureField(title, text: $text)
            } else {
                TextField(title, text: $text)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
            }
        }
        .font(.system(size: 17))
        .padding(.horizontal, 16)
        .frame(height: 54)
        .background(Color.clear)
        .overlay(
            RoundedRectangle(cornerRadius: SnapCalTheme.fieldRadius)
                .stroke(SnapCalTheme.border, lineWidth: 1)
        )
    }
}
