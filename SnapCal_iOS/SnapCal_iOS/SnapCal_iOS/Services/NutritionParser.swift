import Foundation

struct ParsedNutritionLabel {
    var productName: String
    var calories: Int
    var protein: Double
    var carbs: Double
    var fat: Double
    var portionGrams: Double?
    var rawText: String
}

enum NutritionParser {
    static func parse(text: String) -> ParsedNutritionLabel {
        let cleaned = normalize(text)

        let lines = cleaned
            .components(separatedBy: .newlines)
            .map { cleanLine($0) }
            .filter { !$0.isEmpty }

        let productName = extractProductName(from: lines) ?? "Scanned Item"
        let calories = extractCalories(from: lines) ?? 0
        let protein = extractProtein(from: lines) ?? 0
        let carbs = extractCarbs(from: lines) ?? 0
        let fat = extractFat(from: lines) ?? 0
        let portionGrams = extractServingSize(from: lines)

        return ParsedNutritionLabel(
            productName: productName,
            calories: calories,
            protein: protein,
            carbs: carbs,
            fat: fat,
            portionGrams: portionGrams,
            rawText: text
        )
    }

    private static func normalize(_ text: String) -> String {
        text
            .replacingOccurrences(of: "\r\n", with: "\n")
            .replacingOccurrences(of: "\r", with: "\n")
            .replacingOccurrences(of: "\t", with: " ")
            .replacingOccurrences(of: "ﬁ", with: "fi")
            .replacingOccurrences(of: "ﬂ", with: "fl")
    }

    private static func cleanLine(_ line: String) -> String {
        line
            .replacingOccurrences(of: "  ", with: " ")
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private static func extractProductName(from lines: [String]) -> String? {
        let ignoredFragments = [
            "nutrition facts",
            "serving size",
            "servings per container",
            "amount per serving",
            "calories",
            "calories from fat",
            "total fat",
            "saturated fat",
            "trans fat",
            "cholesterol",
            "sodium",
            "total carbohydrate",
            "dietary fiber",
            "sugars",
            "protein",
            "vitamin",
            "calcium",
            "iron",
            "percent daily values",
            "daily value",
            "made in",
            "ingredients"
        ]

        var collected: [String] = []

        for line in lines.prefix(8) {
            let lowered = line.lowercased()

            if ignoredFragments.contains(where: { lowered.contains($0) }) {
                continue
            }

            if digitsCount(in: line) > 3 {
                continue
            }

            if line.count < 3 {
                continue
            }

            collected.append(line)

            if collected.count == 2 {
                break
            }
        }

        if collected.isEmpty {
            return nil
        }

        return collected.joined(separator: " ")
    }

    private static func extractCalories(from lines: [String]) -> Int? {
        for (index, line) in lines.enumerated() {
            let lowered = line.lowercased()

            if lowered.contains("calories from fat") {
                continue
            }

            if lowered.contains("calories"),
               let value = numberAfterKeyword(in: line, keyword: "calories") {
                return Int(value.rounded())
            }

            if lowered == "calories", let next = safeLine(lines, index + 1),
               let value = firstNumber(in: next) {
                return Int(value.rounded())
            }

            if lowered.contains("amount per serving"),
               let next = safeLine(lines, index + 1),
               next.lowercased().contains("calories"),
               let value = numberAfterKeyword(in: next, keyword: "calories") {
                return Int(value.rounded())
            }
        }

        return nil
    }

    private static func extractProtein(from lines: [String]) -> Double? {
        extractValue(
            from: lines,
            preferredKeywords: ["protein"],
            fallbackKeywords: []
        )
    }

    private static func extractCarbs(from lines: [String]) -> Double? {
        extractValue(
            from: lines,
            preferredKeywords: ["total carbohydrate", "carbohydrate", "carbs", "carb"],
            fallbackKeywords: []
        )
    }

    private static func extractFat(from lines: [String]) -> Double? {
        extractValue(
            from: lines,
            preferredKeywords: ["total fat"],
            fallbackKeywords: ["fat"]
        )
    }

    private static func extractServingSize(from lines: [String]) -> Double? {
        for line in lines {
            let lowered = line.lowercased()

            if lowered.contains("serving size") {
                if let valueInParentheses = firstNumber(in: line, pattern: #"\((\d+(?:[.,]\d+)?)\s*g\)"#) {
                    return valueInParentheses
                }

                if let valueInline = firstNumber(in: line, pattern: #"serving size.*?(\d+(?:[.,]\d+)?)\s*g"#) {
                    return valueInline
                }
            }
        }

        return nil
    }

    private static func extractValue(
        from lines: [String],
        preferredKeywords: [String],
        fallbackKeywords: [String]
    ) -> Double? {
        for keyword in preferredKeywords {
            if let value = extractValueForKeyword(from: lines, keyword: keyword) {
                return value
            }
        }

        for keyword in fallbackKeywords {
            if let value = extractValueForKeyword(from: lines, keyword: keyword) {
                return value
            }
        }

        return nil
    }

    private static func extractValueForKeyword(from lines: [String], keyword: String) -> Double? {
        for (index, line) in lines.enumerated() {
            let lowered = line.lowercased()

            guard lowered.contains(keyword) else { continue }

            if keyword == "fat", lowered.contains("calories from fat") {
                continue
            }

            if let value = nutrientValueFromSameLine(line, keyword: keyword) {
                return value
            }

            if let next = safeLine(lines, index + 1),
               let value = firstNumber(in: next) {
                return value
            }
        }

        return nil
    }

    private static func nutrientValueFromSameLine(_ line: String, keyword: String) -> Double? {
        let escapedKeyword = NSRegularExpression.escapedPattern(for: keyword)

        let patterns = [
            #"\#(escapedKeyword)\s*(\d+(?:[.,]\d+)?)\s*g"#,
            #"\#(escapedKeyword)\s*[:\-]?\s*(\d+(?:[.,]\d+)?)"#,
            #"\#(escapedKeyword).*?(\d+(?:[.,]\d+)?)\s*g"#
        ]

        for pattern in patterns {
            if let value = firstNumber(in: line, pattern: pattern) {
                return value
            }
        }

        return nil
    }

    private static func numberAfterKeyword(in line: String, keyword: String) -> Double? {
        let escapedKeyword = NSRegularExpression.escapedPattern(for: keyword)

        let patterns = [
            #"\#(escapedKeyword)\s*[:\-]?\s*(\d+(?:[.,]\d+)?)"#,
            #"\#(escapedKeyword).*?(\d+(?:[.,]\d+)?)"#
        ]

        for pattern in patterns {
            if let value = firstNumber(in: line, pattern: pattern) {
                return value
            }
        }

        return nil
    }

    private static func firstNumber(in text: String, pattern: String = #"(\d+(?:[.,]\d+)?)"#) -> Double? {
        guard let regex = try? NSRegularExpression(pattern: pattern, options: [.caseInsensitive]) else {
            return nil
        }

        let range = NSRange(text.startIndex..., in: text)

        guard let match = regex.firstMatch(in: text, range: range),
              match.numberOfRanges > 1,
              let valueRange = Range(match.range(at: 1), in: text) else {
            return nil
        }

        let value = String(text[valueRange]).replacingOccurrences(of: ",", with: ".")
        return Double(value)
    }

    private static func safeLine(_ lines: [String], _ index: Int) -> String? {
        guard lines.indices.contains(index) else { return nil }
        return lines[index]
    }

    private static func digitsCount(in text: String) -> Int {
        text.filter { $0.isNumber }.count
    }
}
