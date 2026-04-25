"""

<h1 align="center">🍎 SnapCal iOS</h1>

<p align="center">
  <b>COMP 3097 Project — Native iOS Nutrition Tracking App</b><br>
  Built with Swift, SwiftUI, SwiftData, Vision OCR, and MVVM-style architecture.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-iOS-blue" />
  <img src="https://img.shields.io/badge/Language-Swift-orange" />
  <img src="https://img.shields.io/badge/UI-SwiftUI-purple" />
  <img src="https://img.shields.io/badge/Database-SwiftData-red" />
  <img src="https://img.shields.io/badge/OCR-Vision-green" />
  <img src="https://img.shields.io/badge/Status-Completed-brightgreen" />
</p>

---

## 📖 Description

**SnapCal iOS** is the native iOS implementation of SnapCal — a mobile nutrition tracking application built with **SwiftUI**.

The app helps users log meals, scan nutrition labels, extract macro-nutrient values, adjust portions, and view daily progress through a clean iOS interface.

---

## 🚀 Features

### 🔍 OCR-based Food Scanning

* Select a nutrition label image from the gallery
* Extract text using Apple’s **Vision OCR**
* Automatically detect:

  * Calories
  * Protein
  * Carbohydrates
  * Fat
  * Serving size

---

### 🧠 Smart Nutrition Parser

* Custom parsing logic using regex and line analysis
* Handles noisy OCR text
* Converts unstructured label text into structured nutrition data

---

### ⚖️ Portion Scaling

* Enter actual consumed grams
* Automatically recalculates:

  * Calories
  * Protein
  * Carbs
  * Fat
* Uses a dynamic multiplier based on detected serving size

---

### ✍️ Manual Editing

* OCR results can be corrected manually
* Editable fields give the user full control
* Supports manual meal creation without OCR

---

### 📊 Dashboard

* Calories in vs calories out
* Step count using mock health data
* Recent meals
* Daily summary

---

### 📈 Progress Tracking

* 7-day calorie visualization
* Daily meal breakdown
* Progress screen inside the Dashboard flow

---

### ⚙️ Settings & Health Data

* Mock Garmin / Health-style integration
* Displays sample:

  * Steps
  * Active calories
  * Heart rate data

---

### 👤 User System

* Login screen
* Sign up screen
* Session-based app flow
* Local user model and repository

---

## 🛠️ Tech Stack

| Layer        | Technology                |
| ------------ | ------------------------- |
| Language     | Swift                     |
| UI           | SwiftUI                   |
| Persistence  | SwiftData                 |
| OCR          | Vision Framework          |
| Image Input  | PhotosPicker              |
| Parsing      | Regex-based custom parser |
| Architecture | MVVM-style                |
| Data Access  | Repository pattern        |

---

## 🧱 Architecture

```text
SwiftUI Views
      ↓
ViewModels
      ↓
Repositories
      ↓
SwiftData / Services
```

Main service layers:

```text
OCRService
NutritionParser
HealthService
```

---

## 📂 Project Structure

```bash
SnapCal_iOS/
│
├── App/
│   ├── AppSession.swift
│   ├── RootView.swift
│   └── SnapCallApp.swift
│
├── Components/
│   ├── BottomTabBar.swift
│   ├── MacroCard.swift
│   ├── MealRow.swift
│   ├── PrimaryButton.swift
│   ├── SecondaryButton.swift
│   ├── SnapCalTheme.swift
│   ├── SnapCard.swift
│   └── SnapTextField.swift
│
├── Models/
│   ├── Meal.swift
│   └── User.swift
│
├── Persistence/
│   └── ModelContainerFactory.swift
│
├── Repositories/
│   ├── AuthRepository.swift
│   └── MealRepository.swift
│
├── Services/
│   ├── HealthService.swift
│   ├── NutritionParser.swift
│   └── OCRService.swift
│
├── ViewModels/
│   ├── AuthViewModel.swift
│   └── MealsViewModel.swift
│
└── Views/
    ├── Auth/
    │   ├── LoginView.swift
    │   └── SignUpView.swift
    │
    ├── Dashboard/
    │   ├── DashboardView.swift
    │   └── Progress/
    │       └── ProgressView.swift
    │
    ├── Meals/
    │   ├── EditMealSheet.swift
    │   ├── ManualMealView.swift
    │   └── ScanMealView.swift
    │
    ├── Settings/
    │   └── SettingsView.swift
    │
    └── Splash/
        └── SplashView.swift
```

---

## 🔄 Main Data Flow

### OCR Flow

```text
User selects label image
        ↓
OCRService
        ↓
NutritionParser
        ↓
Editable nutrition fields
        ↓
MealRepository
        ↓
SwiftData
        ↓
Dashboard / Progress views
```

### Manual Meal Flow

```text
User enters meal values
        ↓
MealsViewModel
        ↓
MealRepository
        ↓
SwiftData
        ↓
Dashboard / Recent Meals
```

---

## ⚠️ Limitations

* Real Garmin / Apple Health integration is not available in the current simulator-based version
* Mock data is used for:

  * Steps
  * Calories burned
  * Heart rate
* Future real-device integration can be added through HealthKit

---

## 🧪 Challenges

* OCR text can be noisy and inconsistent
* Nutrition labels vary widely in format
* Serving size detection requires flexible parsing
* Portion scaling must preserve original label values while recalculating consumed amounts
* UI consistency needed to match the Android version

---

## 📦 Future Improvements

* Real Apple HealthKit integration
* Garmin API integration if available
* AI-based nutrition parsing
* Barcode scanning
* Meal history analytics
* Cloud sync between Android and iOS

---

## 🔗 Related Project

Android version built with Kotlin + Jetpack Compose:

https://appetize.io/app/b_3qojszfwd2rvbvcxzlxxevq5f4

---

## 👨‍💻 Author

**Oleg Sanitskii**
Software Developer
Toronto, Canada
"""
