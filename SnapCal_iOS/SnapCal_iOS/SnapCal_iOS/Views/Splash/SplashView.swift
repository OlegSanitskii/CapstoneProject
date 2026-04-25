import SwiftUI

struct SplashView: View {
    var onFinished: (() -> Void)? = nil

    @State private var animate = false

    var body: some View {
        ZStack {
            backgroundGradient
                .ignoresSafeArea()

            ZStack {
                flameShape
                    .frame(width: 230, height: 250)
                    .scaleEffect(animate ? 1.04 : 0.96)
                    .opacity(animate ? 1.0 : 0.85)
                    .animation(
                        .easeInOut(duration: 1.0).repeatForever(autoreverses: true),
                        value: animate
                    )

                logoMark
                    .scaleEffect(animate ? 1.06 : 0.98)
                    .animation(
                        .easeInOut(duration: 0.9).repeatForever(autoreverses: true),
                        value: animate
                    )
            }
        }
        .onAppear {
            animate = true

            DispatchQueue.main.asyncAfter(deadline: .now() + 7.0) {
                onFinished?()
            }
        }
    }

    private var backgroundGradient: some View {
        LinearGradient(
            colors: [
                Color(hex: "#FFD600"),
                Color(hex: "#FF9800"),
                Color(hex: "#FF5A5F"),
                Color(hex: "#6F53D9")
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }

    private var flameShape: some View {
        ZStack {
            FlameBlob()
                .fill(
                    LinearGradient(
                        colors: [
                            Color(hex: "#FF6A00"),
                            Color(hex: "#FFC400")
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .rotationEffect(.degrees(-8))
                .offset(x: 12, y: 10)

            Circle()
                .fill(Color(hex: "#FF6A00").opacity(0.85))
                .frame(width: 40, height: 40)
                .offset(x: 18, y: -58)
        }
    }

    private var logoMark: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(Color(hex: "#6F53D9"))
                .frame(width: 96, height: 92)

            Rectangle()
                .fill(Color.white)
                .frame(width: 18, height: 10)
                .offset(x: -10, y: -19)

            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .fill(Color.white)
                .frame(width: 64, height: 42)
                .offset(y: 8)

            Circle()
                .stroke(Color(hex: "#6F53D9"), lineWidth: 5)
                .frame(width: 26, height: 26)
                .offset(x: 0, y: 11)
        }
        .shadow(color: .black.opacity(0.06), radius: 8, x: 0, y: 4)
    }
}

private struct FlameBlob: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()

        let w = rect.width
        let h = rect.height

        path.move(to: CGPoint(x: w * 0.52, y: h * 0.02))

        path.addCurve(
            to: CGPoint(x: w * 0.82, y: h * 0.32),
            control1: CGPoint(x: w * 0.72, y: h * 0.10),
            control2: CGPoint(x: w * 0.90, y: h * 0.18)
        )

        path.addCurve(
            to: CGPoint(x: w * 0.66, y: h * 0.92),
            control1: CGPoint(x: w * 0.86, y: h * 0.58),
            control2: CGPoint(x: w * 0.82, y: h * 0.82)
        )

        path.addCurve(
            to: CGPoint(x: w * 0.28, y: h * 0.86),
            control1: CGPoint(x: w * 0.56, y: h * 0.98),
            control2: CGPoint(x: w * 0.38, y: h * 0.96)
        )

        path.addCurve(
            to: CGPoint(x: w * 0.18, y: h * 0.42),
            control1: CGPoint(x: w * 0.08, y: h * 0.74),
            control2: CGPoint(x: w * 0.08, y: h * 0.56)
        )

        path.addCurve(
            to: CGPoint(x: w * 0.52, y: h * 0.02),
            control1: CGPoint(x: w * 0.28, y: h * 0.18),
            control2: CGPoint(x: w * 0.38, y: h * 0.06)
        )

        path.closeSubpath()
        return path
    }
}

private extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)

        let a, r, g, b: UInt64
        switch hex.count {
        case 8:
            (a, r, g, b) = (
                (int >> 24) & 0xff,
                (int >> 16) & 0xff,
                (int >> 8) & 0xff,
                int & 0xff
            )
        default:
            (a, r, g, b) = (
                255,
                (int >> 16) & 0xff,
                (int >> 8) & 0xff,
                int & 0xff
            )
        }

        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

#Preview {
    SplashView()
}
