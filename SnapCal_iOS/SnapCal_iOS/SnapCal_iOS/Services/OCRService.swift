import Foundation
import UIKit
import Vision

enum OCRServiceError: LocalizedError {
    case unableToReadImage
    case noTextFound

    var errorDescription: String? {
        switch self {
        case .unableToReadImage:
            return "Unable to read image."
        case .noTextFound:
            return "No text was recognized in the image."
        }
    }
}

final class OCRService {
    func recognizeText(from image: UIImage) async throws -> String {
        guard let cgImage = image.cgImage else {
            throw OCRServiceError.unableToReadImage
        }

        return try await withCheckedThrowingContinuation { continuation in
            let request = VNRecognizeTextRequest { request, error in
                if let error {
                    continuation.resume(throwing: error)
                    return
                }

                guard let observations = request.results as? [VNRecognizedTextObservation] else {
                    continuation.resume(throwing: OCRServiceError.noTextFound)
                    return
                }

                let recognizedLines = observations.compactMap { observation in
                    observation.topCandidates(1).first?.string
                }

                let fullText = recognizedLines.joined(separator: "\n").trimmingCharacters(in: .whitespacesAndNewlines)

                if fullText.isEmpty {
                    continuation.resume(throwing: OCRServiceError.noTextFound)
                } else {
                    continuation.resume(returning: fullText)
                }
            }

            request.recognitionLevel = .accurate
            request.usesLanguageCorrection = true
            request.minimumTextHeight = 0.02
            request.recognitionLanguages = ["en-US"]

            let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])

            do {
                try handler.perform([request])
            } catch {
                continuation.resume(throwing: error)
            }
        }
    }

    func recognizeAndParse(from image: UIImage) async throws -> ParsedNutritionLabel {
        let text = try await recognizeText(from: image)
        return NutritionParser.parse(text: text)
    }

    func parseMockText(_ text: String) -> ParsedNutritionLabel {
        NutritionParser.parse(text: text)
    }
}
