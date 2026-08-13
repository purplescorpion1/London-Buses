# London Buses Android Application

A modern, native Android application built with Jetpack Compose, Material 3, and Kotlin, designed to assist passengers traveling across the London Bus network. The app integrates directly with the live Transport for London (TfL) Unified API to provide passengers with reliable, real-time transport information.

---

## 🔑 Getting a TfL API Key

The application requires a TfL API key to fetch live bus routes, arrivals, disruptions, and journey suggestions. Follow these steps to obtain a key:

1. **Sign Up / Log In**: Visit the [TfL Developer Portal](https://api-portal.tfl.gov.uk/).
2. **Register**: Register a free account or log into your existing one.
3. **Products & Subscriptions**: Navigate to the "Products" tab and select "500 Requests per min".
4. **Subscribe**: Create a free subscription by entering a name and selecting subscript. Your subscription key (Primary Key) will be generated and viewable in your profile.
5. **Copy Subscription Key**: The primary subscription key found in your profile is your `app_key` used to authorize API requests.

---

## ⚙️ Configuring the API Key in the App

To enable all real-time features:
1. Open the **London Buses** application on your device or emulator.
2. Tap on the **Settings** tab in the bottom navigation bar.
3. Paste your TfL subscription key into the input field.
4. Tap **Save API Key**.

The app securely stores your API key locally in shared preferences, appending it as the authorized `app_key` parameter for all successive API requests.

---

## 🚀 Key Features

### 1. 🔍 Route Search Screen
* **Live Route Sequence**: Enter a bus route number (e.g., `72`, `14`, `220`) to immediately view the entire sequence of bus stops along its route.
* **Directional Filtering**: Effortlessly switch between Outbound and Inbound stop sequences using the direction tabs.
* **Smart Nearest Stop**: The app automatically compares the coordinates of all stops on the route against your current device GPS position to highlight the nearest stop with a labeled `NEAREST` badge and a distance measurement.
* **Real-time Stop Arrivals**: Displays live countdowns for the next three approaching buses directly in the stop list, or tap on any stop to open an Arrivals Overlay with comprehensive arrival details.

### 2. 📍 Nearby Stops Screen
* **Geographical Search**: Instantly lists all public bus, coach, and tram stops within 1000 meters of your exact coordinates.
* **Location Permission Request**: Upon first launching, the app requests standard runtime `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` permissions so that nearby stops load instantly.
* **Coordinates & GPS Refreshes**: Shows your current latitude and longitude and provides a one-click button to refresh your GPS location.
* **Built-in Location Simulator**: Perfect for testing or planning trips when outside London. Open the simulator to type in custom coordinates, or click preset presets (such as **Westminster** or **Kings Cross**) to instantly view stops in those areas.

### 3. ⚠️ Route Status & Active Disruptions
* **Service Badges**: When searching for a route, the app calls the TfL Line Status endpoint to display service health. Shows a green **Good Service** badge or orange/yellow warning badges for delays, diversions, or suspensions.
* **Disruption Details**: If a route is experiencing disruptions, a dedicated, colored info block expands to explain the cause and reason for the delays.

### 4. 🚧 Stop Closures & Warning Banners
* **Active Stop Warnings**: When you select a bus stop on either the Search or Nearby screen, the app requests active stop disruptions.
* **Alert System**: If a stop is closed, suspended, or affected by nearby roadworks, a high-visibility warning card displays the details at the top of the arrivals dialog.

### 5. 📅 Scheduled Timetable Fallback
* **Late Night/No-Service Fallback**: If there are no live buses currently running (such as late at night or during schedule gaps), the app doesn't leave you stranded with empty results.
* **Timetable Lookups**: It automatically falls back to fetching the official scheduled timetables for that route and stop, listing scheduled departure times so you can still plan your journey.

### 6. 🗺️ Journey Planner Screen
* **End-to-End Routing**: Planning a route from Point A to Point B is easier than ever. Enter an origin and destination (such as coordinates, UK postcodes, Naptan stop IDs, or names).
* **Multi-Leg Itineraries**: Renders a list of the best travel options, including exact durations, departure/arrival times, and step-by-step directions (with bus line connections and walking transfers).

---

## 🛠️ Build and Test

The project uses Gradle and standard Kotlin Android configuration.

### Run Unit Tests
```bash
./gradlew test
```

### Build Debug APK
```bash
./gradlew assembleDebug
```
