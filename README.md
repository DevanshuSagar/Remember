Remember - A Spaced Repetition App

Remember is a native Android application built with Kotlin and Jetpack Compose, designed to help users learn and remember information more effectively using the Spaced Repetition System (SRS).

## About The Project

The core principle behind Remember is spaced repetition, a scientifically proven learning technique for long-term memory retention. The app allows users to create rich, media-enabled flashcards and reminds them to review the material at increasing intervals, optimizing the learning process.

This project was built from the ground up to showcase modern Android development practices, including a declarative UI, a reactive data layer, and a robust architecture.

## Key Features

    Rich-Media Cards: Create cards with a primary topic, detailed notes, and attach multiple images or PDF documents.

    Interactive Review: A full-screen, swipeable UI for daily review sessions. Images and PDFs are viewable in a zoomable, pannable interface.

    Customizable Intervals: Users can select from pre-defined interval sets (e.g., Standard, Quick, Academic) for each card.

    Smart Notifications: A reliable background system using WorkManager schedules a daily notification at the user's preferred time to remind them of cards due for review.

    Modern UI/UX: A clean, single-activity interface built entirely with Jetpack Compose and Material 3, supporting both light and dark themes.

    Full CRUD Functionality: Users can create, view, edit, and delete their revision cards.

## Tech Stack & Architecture

This project utilizes a modern, robust tech stack and follows the official Android architecture guidelines.

    UI: Jetpack Compose with Material 3 for a fully declarative and modern user interface.

    Architecture: Model-View-ViewModel (MVVM) to ensure a clean separation of concerns.

    Language: Kotlin (100%).

    Asynchronous Programming: Kotlin Coroutines & Flow for managing background threads and handling data streams reactively.

    Database: Room for robust, local persistence of all user data.

    Dependency Injection: Dagger Hilt to manage dependencies throughout the app.

    Background Processing: WorkManager for guaranteed, deferrable background tasks like notifications.

    Navigation: Jetpack Navigation for Compose to handle all in-app navigation.

    Image Loading: Coil for efficient loading and display of images.

    PDF Viewing: A native solution using Android's PdfRenderer.
		

## Setup

To build and run this project, you'll need Android Studio Giraffe or newer.

    Clone the repository: git clone https://github.com/devanshusagar/remember.git

Open the project in Android Studio.

Let Gradle sync the dependencies.

Run the app on an emulator or a physical device.
