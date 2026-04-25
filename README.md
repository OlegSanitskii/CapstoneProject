
<h1 align="center">📱 SnapCal</h1>

<p align="center">
  <b>Cross-Platform Fitness & Nutrition Tracker</b><br>
  Native Android and iOS implementations of one product idea.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-Kotlin-brightgreen" />
  <img src="https://img.shields.io/badge/iOS-Swift-orange" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue" />
  <img src="https://img.shields.io/badge/UI-SwiftUI-purple" />
  <img src="https://img.shields.io/badge/Status-Completed-brightgreen" />
</p>

---

## 📖 Description

**SnapCal** is a cross-platform fitness and nutrition tracking application built with two native implementations:

* 🤖 **Android:** Kotlin + Jetpack Compose
* 🍎 **iOS:** Swift + SwiftUI

Both versions follow the same product idea: help users track meals, nutrition labels, calories, progress, and daily balance through a clean mobile interface.

---

## 🚀 Platforms

| Platform | Technology               | Location                                                                                                             |
| -------- | ------------------------ | -------------------------------------------------------------------------------------------------------------------- |
| Android  | Kotlin + Jetpack Compose | `/SnapCalAndroid`                                                                                                    |
| iOS      | Swift + SwiftUI          | `/SnapCal_iOS`|

---

## 🔥 Key Features

* Nutrition tracking with manual entry
* Nutrition label scanning with OCR
* Macro tracking: calories, protein, carbohydrates, and fat
* Portion-based nutrition adjustment
* Dashboard with daily summary
* Progress tracking
* Reports and analytics
* Multi-user support
* Native UI on both platforms

---

## 📦 Live Demo — Android

Try the Android version directly in the browser:

https://appetize.io/app/b_3qojszfwd2rvbvcxzlxxevq5f4

---

## 🧠 Core Philosophy

SnapCal focuses on **accuracy over fake estimation**.

* Real health data is preferred where available
* Missing burned-calorie data is treated as **0**
* The app avoids inventing default calorie burn values
* Native platform features are used instead of generic cross-platform shortcuts

---

## 🛠️ Tech Stack

| Area         | Android                 | iOS                                    |
| ------------ | ----------------------- | -------------------------------------- |
| Language     | Kotlin                  | Swift                                  |
| UI           | Jetpack Compose         | SwiftUI                                |
| Database     | Room                    | SwiftData                              |
| OCR          | ML Kit Text Recognition | Vision Framework                       |
| Architecture | MVVM + Repository       | MVVM-style                             |
| Health Data  | Health Connect          | Mock Health Service / future HealthKit |

---

## 🧱 Architecture

Both implementations follow similar architectural principles:

* MVVM-style structure
* Repository layer
* Local persistence
* OCR service layer
* Nutrition parsing layer
* Dashboard and progress presentation layer

---

## 📂 Repository Structure

```bash
SnapCal/
│
├── SnapCalAndroid/      # Native Android implementation
│
└── SnapCal_iOS/         # Native iOS implementation
│
└── README.md            # Root project overview
```



---

## 🎯 Project Goals

* Build a realistic fitness and nutrition tracker
* Maintain feature parity across Android and iOS
* Use native mobile technologies
* Demonstrate mobile app architecture and real-world integrations
* Provide a clean user experience for nutrition tracking

---

## 📌 Future Improvements

* Cloud sync between devices
* Advanced analytics
* Real Apple HealthKit integration for iOS
* Expanded Garmin integration if API access is available
* Barcode scanning
* AI-assisted nutrition parsing

---

## 👨‍💻 Author

**Oleg Sanitskii**
Software Developer
Toronto, Canada
