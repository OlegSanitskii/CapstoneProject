
<h1 align="center">📱 SnapCal iOS</h1>

<p align="center">
  <b>COMP 3097 Project — iOS Nutrition Tracking App</b><br>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-iOS-blue" />
  <img src="https://img.shields.io/badge/Language-Swift-orange" />
  <img src="https://img.shields.io/badge/UI-SwiftUI-purple" />
  <img src="https://img.shields.io/badge/OCR-Vision-green" />
  <img src="https://img.shields.io/badge/Status-Completed-brightgreen" />
</p>

---

## 📖 Description

**SnapCal** is a mobile nutrition tracking application built for iOS using **SwiftUI**.  
The app simplifies food logging by allowing users to **scan nutrition labels**, automatically extract macro-nutrient data, and adjust values based on actual consumed portions.

---

## 🚀 Features

### 🔍 OCR-based Food Scanning
- Select a photo of a nutrition label from the gallery
- Extract text using Apple’s **Vision OCR**
- Automatically detect:
  - Calories
  - Protein
  - Carbohydrates
  - Fat
  - Serving size

---

### 🧠 Smart Nutrition Parser
- Custom parsing logic using regex + line analysis
- Handles noisy OCR text
- Extracts structured nutrition data from real-world labels

---

### ⚖️ Portion Scaling
- Input actual consumed grams
- Automatically recalculates:
  - Calories
  - Protein
  - Carbs
  - Fat
- Uses a dynamic multiplier based on label serving size

---

### ✍️ Manual Editing
- All OCR results can be corrected manually
- Editable fields for full user control

---

### 📊 Dashboard
- Calories in vs out
- Step count (mock data)
- Recent meals
- Daily summary

---

### 📈 Progress Tracking
- 7-day calorie visualization
- Daily meal breakdown

---

### ⚙️ Settings & Health Data
- Mock Garmin/Health integration
- Displays:
  - Steps
  - Active calories
  - Heart rate data (mock)

---

## 🛠️ Tech Stack

| Layer | Technology |
|------|----------|
| UI | SwiftUI |
| Persistence | SwiftData |
| OCR | Vision Framework |
| Image Input | PhotosPicker |
| Parsing | Regex-based custom parser |
| Architecture | MVVM-style |

---

## ⚠️ Limitations

- Real Garmin / Apple Health integration is not available in iOS Simulator
- Mock data is used instead for steps, calories burned, and heart rate

---

## 🧪 Challenges

- OCR produces noisy and inconsistent text
- Nutrition labels vary in format
- Required building a flexible parsing system
- Handling portion scaling without breaking original values

---

## 📦 Future Improvements

- Real Apple HealthKit integration
- Garmin API integration
- AI-based nutrition parsing
- Barcode scanning
- Meal history analytics


---

## 📂 Project Structure
```bash
SnapCal_iOS/
│
├── App/
├── Views/
│ ├── Dashboard/
│ ├── Meals/
│ ├── Progress/
│ └── Settings/
│
├── Components/
├── Services/
│ ├── OCRService.swift
│ ├── NutritionParser.swift
│ └── HealthService.swift
│
├── Models/
├── Persistence/
└── ViewModels/
```
---