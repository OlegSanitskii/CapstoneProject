<h1 align="center">🤖 SnapCal Android</h1>

<p align="center">
  <b>Native Android Fitness & Nutrition Tracking App</b><br>
  Built with Kotlin, Jetpack Compose, Room, Health Connect, and ML Kit OCR.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen" />
  <img src="https://img.shields.io/badge/Language-Kotlin-purple" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue" />
  <img src="https://img.shields.io/badge/Database-Room-orange" />
  <img src="https://img.shields.io/badge/OCR-ML%20Kit-green" />
  <img src="https://img.shields.io/badge/Health-Health%20Connect-red" />
</p>

---

## 📖 Description

**SnapCal Android** is the native Android implementation of SnapCal — a fitness and nutrition tracking app focused on meal logging, OCR-based nutrition label scanning, burned-calorie tracking, reports, and multi-user support.

The app uses real data where possible and avoids fake default calorie-burn estimates.

---

## 🚀 Features

### 🔍 OCR Nutrition Label Scanning

* Scan nutrition labels
* Extract text using OCR
* Parse nutrition information from recognized text
* Detect calories, protein, carbohydrates, fat, and serving size

---

### 🥗 Meal Tracking

* Manual meal entry
* OCR-based meal creation
* Editable nutrition values
* Local meal storage
* Daily meal summary

---

### 🔥 Health Connect Integration

* Reads health/activity data from Health Connect
* Supports Garmin data when synced into Health Connect
* Tracks burned calories and steps
* Uses **0** when no burned-calorie data is available

---

### 📊 Reports

* Daily calorie balance
* Monthly report generation
* CSV export
* PDF export
* Background report scheduling

---

### 👤 User System

* Login and sign up
* Multi-user support
* Session-based authentication
* User-specific meal data

---

## 📦 Live Demo

Try the Android version directly in browser:

https://appetize.io/app/b_3qojszfwd2rvbvcxzlxxevq5f4

---

## 🛠️ Tech Stack

| Layer            | Technology                     |
| ---------------- | ------------------------------ |
| Language         | Kotlin                         |
| UI               | Jetpack Compose + Material 3   |
| Database         | Room                           |
| Local Settings   | DataStore Preferences          |
| OCR              | Google ML Kit Text Recognition |
| Health Data      | Health Connect API             |
| Background Tasks | WorkManager                    |
| Images           | Coil                           |
| Async            | Coroutines                     |
| Navigation       | Navigation Compose             |
| Build Tools      | Gradle + KSP                   |
| Java Version     | Java 17                        |

---

## 🧱 Architecture

SnapCal Android follows an **MVVM + Repository** structure.

```text
Compose Screens
      ↓
ViewModels
      ↓
Repositories
      ↓
Room Database / Health Connect / OCR / Reports
```

---

## 🔄 Main Data Flow

### OCR Flow

```text
User scans label
        ↓
OcrRecognizer
        ↓
NutritionParser
        ↓
Meal object
        ↓
MealsRepository
        ↓
Room Database
        ↓
MealsViewModel
        ↓
Compose UI
```

### Health Data Flow

```text
Garmin / Health Provider
        ↓
Health Connect
        ↓
HealthConnectManager
        ↓
Dashboard / Reports
```

### Report Flow

```text
Meals + Health Connect data
        ↓
MonthlyReportService
        ↓
ReportModels
        ↓
CsvReportWriter / PdfReportWriter
        ↓
Generated monthly report
```

---

## 📂 Project Structure

```bash
SnapCalAndroid/
│
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   │
│   └── src/main/
│       ├── AndroidManifest.xml
│       │
│       ├── java/ca/gbc/comp3074/snapcal/
│       │   │
│       │   ├── MainActivity.kt
│       │   ├── SnapCalApp.kt
│       │   │
│       │   ├── data/
│       │   │   ├── auth/
│       │   │   │   ├── AuthRepository.kt
│       │   │   │   └── SessionStore.kt
│       │   │   │
│       │   │   ├── db/
│       │   │   │   ├── DBProvider.kt
│       │   │   │   ├── MealDao.kt
│       │   │   │   └── SnapCalDatabase.kt
│       │   │   │
│       │   │   ├── model/
│       │   │   │   └── Meal.kt
│       │   │   │
│       │   │   ├── repo/
│       │   │   │   └── MealsRepository.kt
│       │   │   │
│       │   │   ├── settings/
│       │   │   │   └── ReportSettingsStore.kt
│       │   │   │
│       │   │   └── user/
│       │   │       ├── User.kt
│       │   │       ├── UserDao.kt
│       │   │       └── UserRepository.kt
│       │   │
│       │   ├── health/
│       │   │   └── HealthConnectManager.kt
│       │   │
│       │   ├── navigation/
│       │   │   ├── AppNav.kt
│       │   │   └── Screen.kt
│       │   │
│       │   ├── nutrition/
│       │   │   └── NutritionParser.kt
│       │   │
│       │   ├── ocr/
│       │   │   └── OcrRecognizer.kt
│       │   │
│       │   ├── reports/
│       │   │   ├── CsvReportWriter.kt
│       │   │   ├── MonthlyReportService.kt
│       │   │   ├── PdfReportWriter.kt
│       │   │   ├── ReportModels.kt
│       │   │   └── ReportScheduler.kt
│       │   │
│       │   ├── screens/
│       │   │   ├── DashboardScreen.kt
│       │   │   ├── GarminScreen.kt
│       │   │   ├── LoginScreen.kt
│       │   │   ├── ManualMealScreen.kt
│       │   │   ├── ProgressScreen.kt
│       │   │   ├── ScanScreen.kt
│       │   │   ├── SignUpScreen.kt
│       │   │   └── SplashScreen.kt
│       │   │
│       │   ├── ui/
│       │   │   ├── components/
│       │   │   ├── state/
│       │   │   └── theme/
│       │   │
│       │   ├── util/
│       │   │   └── TimeUtils.kt
│       │   │
│       │   └── workers/
│       │       └── MonthlyReportWorker.kt
│       │
│       └── res/
│
├── gradle/
│   └── libs.versions.toml
│
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

---

## ⚙️ Setup & Run

### 1. Open Project

Open the `SnapCalAndroid` folder in Android Studio.

---

### 2. Run App

Use Android Studio:

```text
Run → app
```

---

### 3. Build APK

```text
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔐 Health Connect Notes

The app uses Health Connect for health and activity data.

Health Connect availability depends on the device, emulator, Android version, and connected providers.

If no burned-calorie data is available, SnapCal uses:

```text
0 calories burned
```

The app does not create artificial calorie-burn values.

---

## 🔗 Related Project

iOS version built with Swift + SwiftUI:

https://github.com/OlegSanitskii/COMP3097Mobile-Application-Development2/tree/OlegSanitskii

---

## 👨‍💻 Author

**Oleg Sanitskii**
Software Developer
Toronto, Canada
