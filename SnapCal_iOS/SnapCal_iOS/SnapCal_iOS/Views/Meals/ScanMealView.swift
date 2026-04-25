import SwiftUI
import SwiftData
import PhotosUI
import UIKit

struct ScanMealView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext

    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var selectedImage: UIImage?

    @State private var recognizedText = ""
    @State private var isProcessing = false
    @State private var statusMessage = ""
    @State private var showError = false

    @State private var productName = ""
    @State private var calories = ""
    @State private var protein = ""
    @State private var carbs = ""
    @State private var fat = ""
    @State private var portionGrams = ""
    @State private var consumedGrams = ""
    @State private var mealType = "Scanned"
    @State private var notes = ""

    @State private var baseCalories: Double = 0
    @State private var baseProtein: Double = 0
    @State private var baseCarbs: Double = 0
    @State private var baseFat: Double = 0
    @State private var basePortionGrams: Double = 0

    private let ocrService = OCRService()

    var body: some View {
        NavigationStack {
            ZStack {
                SnapCalTheme.background
                    .ignoresSafeArea()

                ScrollView {
                    VStack(alignment: .leading, spacing: 22) {
                        titleSection
                        imageSection
                        pickerSection
                        statusSection
                        parsedFieldsSection
                        recognizedTextSection
                        saveSection
                    }
                    .padding(.horizontal, SnapCalTheme.screenHorizontalPadding)
                    .padding(.vertical, 20)
                }
            }
            .navigationTitle("Scan")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
            .onChange(of: selectedPhotoItem) { _, newItem in
                guard let newItem else { return }

                Task {
                    await loadSelectedPhoto(from: newItem)
                }
            }
            .onChange(of: consumedGrams) { _, _ in
                recalculateFromConsumedAmount()
            }
            .onChange(of: portionGrams) { _, _ in
                syncBasePortionFromFieldIfNeeded()
                recalculateFromConsumedAmount()
            }
            .alert("Scan Error", isPresented: $showError) {
                Button("OK", role: .cancel) { }
            } message: {
                Text(statusMessage)
            }
        }
    }

    private var titleSection: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Scan Product / Receipt")
                .font(.title2)
                .fontWeight(.semibold)

            Text("Choose a nutrition label photo from your gallery. OCR text will appear below, and you can correct any values manually.")
                .font(.system(size: 14))
                .foregroundStyle(SnapCalTheme.textSecondary)
        }
    }

    private var imageSection: some View {
        Group {
            if let selectedImage {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Selected Image")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(SnapCalTheme.textPrimary)

                    Image(uiImage: selectedImage)
                        .resizable()
                        .scaledToFit()
                        .frame(maxWidth: .infinity)
                        .frame(height: 230)
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                        .overlay(
                            RoundedRectangle(cornerRadius: 18)
                                .stroke(SnapCalTheme.border, lineWidth: 1)
                        )
                }
            } else {
                ZStack {
                    RoundedRectangle(cornerRadius: 20)
                        .fill(Color.black.opacity(0.92))
                        .frame(height: 220)

                    VStack(spacing: 10) {
                        Text("Choose a photo from gallery")
                            .font(.system(size: 20, weight: .medium))
                            .foregroundStyle(.white.opacity(0.9))

                        Text("Use a clear image of the nutrition label")
                            .font(.system(size: 15))
                            .foregroundStyle(.white.opacity(0.75))

                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.white.opacity(0.92))
                            .frame(width: 150, height: 8)
                            .padding(.top, 10)
                    }
                }
            }
        }
    }

    private var pickerSection: some View {
        VStack(spacing: 12) {
            PhotosPicker(
                selection: $selectedPhotoItem,
                matching: .images,
                photoLibrary: .shared()
            ) {
                HStack(spacing: 8) {
                    Image(systemName: "photo.on.rectangle")
                    Text(selectedImage == nil ? "Choose Photo" : "Choose Another Photo")
                        .fontWeight(.semibold)
                }
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .background(SnapCalTheme.primary)
                .foregroundStyle(.white)
                .clipShape(Capsule())
            }

            if selectedImage != nil {
                SecondaryButton(title: "Run OCR Again") {
                    Task {
                        await processCurrentImage()
                    }
                }
            }
        }
    }

    private var statusSection: some View {
        Group {
            if isProcessing {
                HStack(spacing: 10) {
                    ProgressView()
                    Text("Processing image...")
                        .foregroundStyle(SnapCalTheme.textSecondary)
                }
            } else if !statusMessage.isEmpty {
                Text(statusMessage)
                    .font(.system(size: 14))
                    .foregroundStyle(SnapCalTheme.textSecondary)
            }
        }
    }

    private var parsedFieldsSection: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Detected Item")
                .font(.system(size: 17, weight: .semibold))
                .foregroundStyle(SnapCalTheme.textPrimary)

            SnapTextField(title: "Product name", text: $productName)
            SnapTextField(title: "Meal type", text: $mealType)

            SnapTextField(title: "Label portion (g)", text: $portionGrams)
            SnapTextField(title: "Consumed amount (g)", text: $consumedGrams)

            multiplierInfoView

            SnapTextField(title: "Calories (kcal)", text: $calories)

            HStack(spacing: 10) {
                SnapTextField(title: "Protein (g)", text: $protein)
                SnapTextField(title: "Carbs (g)", text: $carbs)
                SnapTextField(title: "Fat (g)", text: $fat)
            }

            SnapTextField(title: "Notes (optional)", text: $notes)
        }
    }

    private var multiplierInfoView: some View {
        VStack(alignment: .leading, spacing: 6) {
            let labelValue = parseDouble(portionGrams)
            let consumedValue = parseDouble(consumedGrams)

            if labelValue > 0, consumedValue > 0 {
                let multiplier = consumedValue / labelValue

                Text("Multiplier: \(doubleString(multiplier))x")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(SnapCalTheme.primary)

                Text("Values below are automatically recalculated for the consumed amount.")
                    .font(.system(size: 13))
                    .foregroundStyle(SnapCalTheme.textSecondary)
            } else {
                Text("Enter consumed grams to auto-recalculate calories and macros.")
                    .font(.system(size: 13))
                    .foregroundStyle(SnapCalTheme.textSecondary)
            }
        }
    }

    private var recognizedTextSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Recognized OCR Text")
                .font(.system(size: 17, weight: .semibold))
                .foregroundStyle(SnapCalTheme.textPrimary)

            ZStack(alignment: .topLeading) {
                RoundedRectangle(cornerRadius: 14)
                    .stroke(SnapCalTheme.border, lineWidth: 1)
                    .background(
                        RoundedRectangle(cornerRadius: 14)
                            .fill(Color.clear)
                    )
                    .frame(minHeight: 180)

                TextEditor(text: $recognizedText)
                    .scrollContentBackground(.hidden)
                    .padding(8)
                    .frame(minHeight: 180)
                    .font(.system(size: 15))
            }

            Text("You can manually correct the OCR text here if recognition was imperfect.")
                .font(.system(size: 13))
                .foregroundStyle(SnapCalTheme.textSecondary)

            SecondaryButton(title: "Parse Text Again") {
                reparseRecognizedText()
            }
        }
    }

    private var saveSection: some View {
        VStack(spacing: 12) {
            PrimaryButton(title: "Save") {
                saveMeal()
            }

            SecondaryButton(title: "Clear") {
                clearAll()
            }
        }
    }

    private func loadSelectedPhoto(from item: PhotosPickerItem) async {
        isProcessing = true
        statusMessage = ""

        do {
            guard let data = try await item.loadTransferable(type: Data.self),
                  let image = UIImage(data: data) else {
                throw NSError(domain: "ScanMealView", code: 1, userInfo: [
                    NSLocalizedDescriptionKey: "Unable to load selected image."
                ])
            }

            await MainActor.run {
                selectedImage = image
            }

            await processCurrentImage()
        } catch {
            await MainActor.run {
                isProcessing = false
                statusMessage = error.localizedDescription
                showError = true
            }
        }
    }

    private func processCurrentImage() async {
        guard let selectedImage else {
            await MainActor.run {
                isProcessing = false
                statusMessage = "Please choose an image first."
            }
            return
        }

        isProcessing = true
        statusMessage = "Running OCR..."

        do {
            let text = try await ocrService.recognizeText(from: selectedImage)
            let parsed = NutritionParser.parse(text: text)

            await MainActor.run {
                recognizedText = text
                applyParsedLabel(parsed)
                isProcessing = false
                statusMessage = "OCR completed."
            }
        } catch {
            await MainActor.run {
                isProcessing = false
                statusMessage = error.localizedDescription
                showError = true
            }
        }
    }

    private func reparseRecognizedText() {
        let parsed = NutritionParser.parse(text: recognizedText)
        applyParsedLabel(parsed)
        statusMessage = "Parsed fields updated from OCR text."
    }

    private func applyParsedLabel(_ parsed: ParsedNutritionLabel) {
        productName = parsed.productName

        baseCalories = Double(parsed.calories)
        baseProtein = parsed.protein
        baseCarbs = parsed.carbs
        baseFat = parsed.fat
        basePortionGrams = parsed.portionGrams ?? 0

        portionGrams = parsed.portionGrams != nil ? doubleString(parsed.portionGrams ?? 0) : ""
        consumedGrams = parsed.portionGrams != nil ? doubleString(parsed.portionGrams ?? 0) : ""

        if mealType.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            mealType = "Scanned"
        }

        recalculateFromConsumedAmount()
    }

    private func recalculateFromConsumedAmount() {
        let labelPortion = basePortionGrams > 0 ? basePortionGrams : parseDouble(portionGrams)
        let consumed = parseDouble(consumedGrams)

        guard labelPortion > 0 else {
            calories = baseCalories > 0 ? String(Int(baseCalories.rounded())) : ""
            protein = baseProtein > 0 ? doubleString(baseProtein) : ""
            carbs = baseCarbs > 0 ? doubleString(baseCarbs) : ""
            fat = baseFat > 0 ? doubleString(baseFat) : ""
            return
        }

        guard consumed > 0 else {
            calories = baseCalories > 0 ? String(Int(baseCalories.rounded())) : ""
            protein = baseProtein > 0 ? doubleString(baseProtein) : ""
            carbs = baseCarbs > 0 ? doubleString(baseCarbs) : ""
            fat = baseFat > 0 ? doubleString(baseFat) : ""
            return
        }

        let multiplier = consumed / labelPortion

        let recalculatedCalories = baseCalories * multiplier
        let recalculatedProtein = baseProtein * multiplier
        let recalculatedCarbs = baseCarbs * multiplier
        let recalculatedFat = baseFat * multiplier

        calories = String(Int(recalculatedCalories.rounded()))
        protein = doubleString(recalculatedProtein)
        carbs = doubleString(recalculatedCarbs)
        fat = doubleString(recalculatedFat)
    }

    private func syncBasePortionFromFieldIfNeeded() {
        let fieldValue = parseDouble(portionGrams)
        if fieldValue > 0 {
            basePortionGrams = fieldValue
        }
    }

    private func saveMeal() {
        let trimmedName = productName.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedMealType = mealType.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !trimmedName.isEmpty else {
            statusMessage = "Please enter a product name before saving."
            showError = true
            return
        }

        guard let caloriesValue = Int(calories), caloriesValue >= 0 else {
            statusMessage = "Calories must be a valid number."
            showError = true
            return
        }

        let proteinValue = parseDouble(protein)
        let carbsValue = parseDouble(carbs)
        let fatValue = parseDouble(fat)

        let savedPortion = consumedGrams.isEmpty ? parseDouble(portionGrams) : parseDouble(consumedGrams)
        let portionValue = savedPortion > 0 ? savedPortion : nil

        let meal = Meal(
            name: trimmedName,
            calories: caloriesValue,
            protein: proteinValue,
            carbs: carbsValue,
            fat: fatValue,
            mealType: trimmedMealType.isEmpty ? "Scanned" : trimmedMealType,
            portionGrams: portionValue,
            notes: notes.isEmpty ? nil : notes
        )

        modelContext.insert(meal)
        dismiss()
    }

    private func clearAll() {
        selectedPhotoItem = nil
        selectedImage = nil
        recognizedText = ""
        isProcessing = false
        statusMessage = ""

        productName = ""
        calories = ""
        protein = ""
        carbs = ""
        fat = ""
        portionGrams = ""
        consumedGrams = ""
        mealType = "Scanned"
        notes = ""

        baseCalories = 0
        baseProtein = 0
        baseCarbs = 0
        baseFat = 0
        basePortionGrams = 0
    }

    private func parseDouble(_ text: String) -> Double {
        Double(text.replacingOccurrences(of: ",", with: ".")) ?? 0
    }

    private func doubleString(_ value: Double) -> String {
        if value.rounded() == value {
            return String(Int(value))
        }
        return String(format: "%.1f", value)
    }
}

#Preview {
    ScanMealView()
        .modelContainer(for: Meal.self, inMemory: true)
}
