# Share Quote App 📱✨

<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_quotable.png" width="150" alt="App Logo">
</p>

A modern, minimalist Android application built with **Jetpack Compose** that allows users to discover, save, and share inspiring quotes with ease. Whether you need daily motivation or want to share wisdom with friends, this app provides a seamless experience.

## 🚀 Features

- **Daily Motivation:** Fetches real-time quotes from the [ZenQuotes API](https://zenquotes.io/).
- **User Authentication:** Secure login and signup powered by **Firebase Auth**.
- **Favorite Quotes:** Save your favorite quotes to your personal collection via **Cloud Firestore**.
- **Quote Sharing:** Easily share quotes as text or beautifully formatted messages to any social platform.
- **Modern UI:** Built entirely with **Jetpack Compose**, featuring glassmorphism, smooth animations, and a responsive layout.
- **Dark Mode Support:** A sleek, premium dark theme for night-time reading.

## 🛠️ Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Networking:** [Ktor Client](https://ktor.io/)
- **Backend/Database:** [Firebase Auth](https://firebase.google.com/docs/auth) & [Cloud Firestore](https://firebase.google.com/docs/firestore)
- **JSON Parsing:** [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) & [Gson](https://github.com/google/gson)
- **Dependency:** [Firebase BOM](https://firebase.google.com/docs/android/learn-more#bom)

## 👥 Team 3 Members

We are a passionate team of developers working on the **20CYS404 - Android Application Development** course project:

- **Agil Prasanna P**
- **Charan K**
- **Deepak Kumar S**
- **Ramraj S**

## 📂 Project Structure

```text
app/
├── src/main/java/com/example/share_quote_app/
│   ├── ui/             # UI Components & NavGraph
│   ├── viewModel/      # Architecture components
│   ├── data/           # Data models (Quote.kt)
│   ├── utils/          # Helper classes
│   └── QuoteManager.kt # Networking logic with Ktor
└── build.gradle.kts    # Project configuration
```

## ⚙️ Setup & Installation

1. Clone this repository (Fork first if you are a contributor).
2. Open the project in **Android Studio (Koala or later)**.
3. Ensure you have a `google-services.json` file in the `app/` directory (for Firebase setup).
4. Sync the Gradle files.
5. Run the app on an emulator or physical device.

---
*Created as part of the 20CYS404 - Android Application Development Course.*
