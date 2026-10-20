# TrendGlance 📈

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Glance](https://img.shields.io/badge/Widget-Jetpack_Glance_1.1-34A853?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose/glance)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

**TrendGlance** is a modern, responsive Android application and Home Screen App Widget built with **Jetpack Compose** and **Jetpack Glance**. It delivers real-time Google Trends search volumes, upward trend indicators, and news breakdown summaries directly to your Android Home Screen.

---

## 🔥 Key Features

- 📈 **Real-Time Google Trends RSS Feed**: Live search volumes and trending topics categorized by country & region (US, IN, GB, DE, FR, JP, CA, AU, etc.).
- 📱 **Google Widget UI Design**: Google Trends Material You aesthetics with rank bullets (`▲ #1`), search traffic badges, and trend breakdown summaries.
- 📐 **Responsive Glance AppWidget (2x4 Default)**: Adapts seamlessly to 2x2, 2x3, 2x4, and 4x4 grid sizes, defaulting to a **2x4** layout when pinned.
- ➕ **One-Tap Home Screen Pinning**: Pin widgets directly to your Home Screen from inside the app using `AppWidgetManager.requestPinAppWidget`.
- 🌐 **Automatic English Translation Engine**: Converts foreign-language trends across all global regions into clean, fluent English.
- 🎨 **Live Customization Dashboard**: Real-time live widget preview with customizable background hex colors, opacity slider, font sizes, text colors, and item count limits (Top 3 / Top 5).
- ⚡ **Background WorkManager Sync**: Configurable periodic background refresh intervals (30 min to 24 hours) with offline fallback resilience.
- 🔐 **Google Sign-In & MongoDB Atlas Integration**: Android Credential Manager authentication integrated with a Node.js/Express backend for user persistence in MongoDB Atlas.

---

## 🛠️ App Architecture & Tech Stack

TrendGlance follows **Clean Architecture** and **MVVM** design principles with unidirectional data flow (UDF).

```
com.trendglance/
├── data/
│   ├── local/          # DataStore Preferences & User Preferences Repository
│   ├── model/          # Domain models (TrendingTopic)
│   ├── remote/         # Retrofit RSS Parser, English Translator & Auth API
│   └── repository/     # TrendsRepository & TrendsRepositoryImpl
├── ui/
│   ├── auth/           # Welcome Screen, Google Auth Manager & AuthViewModel
│   ├── dashboard/      # Configuration Dashboard & Live Widget Preview
│   └── theme/          # Material 3 Color Schemes & Typography
├── widget/             # Jetpack Glance Widget UI, Receiver & Refresh Callbacks
└── worker/             # WorkManager Background Worker & Scheduler
```

### Libraries & Frameworks
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material 3
- **Home Screen Widget**: [Jetpack Glance AppWidget](https://developer.android.com/jetpack/compose/glance)
- **Background Processing**: [AndroidX WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Networking**: [Retrofit 2](https://github.com/square/retrofit) & [OkHttp 4](https://github.com/square/okhttp)
- **Local Persistence**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Authentication**: Android Credential Manager API & Google ID Library
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow`

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / 2024.2.1 or newer
- **JDK**: Java 17
- **Android SDK**: Min SDK 24 / Target SDK 35

### Installation & Build

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/shefeeqkk/TrendGlance.git
   cd TrendGlance
   ```

2. **Open in Android Studio**:
   Open Android Studio and select **Open** -> Navigate to the cloned `TrendGlance` directory.

3. **Build & Run**:
   Connect an Android device (or launch an Emulator) running API 24+ and click **Run 'app'** (`Shift + F10`).

---

## 🍃 Backend Setup (MongoDB Atlas Integration)

The app includes built-in Retrofit endpoints (`@POST("api/auth/google")`) for persisting user sign-in data to a **MongoDB Atlas** database.

Full server code and setup instructions are provided in [`backend_auth_snippet.md`](backend_auth_snippet.md).

### Quick Backend Setup:
1. Initialize a Node.js environment:
   ```bash
   npm install express mongoose google-auth-library dotenv
   ```
2. Set your environment variables in `.env`:
   ```env
   MONGODB_URI=mongodb+srv://<username>:<password>@cluster.mongodb.net/trendglance
   GOOGLE_WEB_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com
   ```
3. Run the backend server:
   ```bash
   node server.js
   ```

---

## 📜 License

```
MIT License

Copyright (c) 2025 Shefeeq KK

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```
