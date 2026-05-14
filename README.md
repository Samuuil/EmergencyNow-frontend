# EmergencyNow Android

EmergencyNow is a real-time emergency-response platform that coordinates patients, dispatchers, ambulance drivers and hospitals through structured data exchange instead of voice calls. This repository is the **Android client**: a single Compose app that adapts its UI to the signed-in user's role (caller, dispatcher, driver, or doctor), streams a call's location and route over WebSockets, and receives loud Firebase Cloud Messaging pushes so drivers don't miss calls when the app is closed.


## Install the app

A signed APK is published on the **[Releases page](https://github.com/Samuuil/EmergencyNow-frontend/releases)**. On the phone:

1. Open the latest release and download the `.apk` asset.
2. Allow installs from unknown sources for the browser doing the download (Android prompts you the first time).
3. Open the downloaded APK and tap **Install**.
4. Launch the app and log in with your EGN.


## Build from source

### Prerequisites

- JDK 21
- Android Studio **or** the Android SDK + Gradle CLI
- A Firebase project with an Android app registered under the package name `com.example.emergencynow`, and a generated `google-services.json`
- A Google Maps Platform API key with the **Maps SDK for Android** enabled
- A running EmergencyNow backend (local or hosted)

### Setup

1. **Clone and open in Android Studio:**

   ```
   git clone https://github.com/Samuuil/EmergencyNow-frontend.git
   ```

2. **Drop your Firebase config** into `app/google-services.json`. Download it from **Firebase Console → Project settings → Your apps → Android app → google-services.json**. The package name of the registered Android app must be `com.example.emergencynow`. This file is gitignored.

3. **Set your Google Maps API key** in `app/src/main/res/values/google_maps_api.xml`:

   ```xml
   <resources>
       <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
           YOUR_GOOGLE_MAPS_API_KEY
       </string>
   </resources>
   ```

   This file is gitignored. **In Google Cloud Console**, restrict the key to your app's package name and SHA-1 signing-certificate fingerprint so it can't be reused elsewhere — without this restriction, anyone with the APK can extract the key.

4. **Point the app at a backend** by creating `local.properties` at the repo root (gitignored) and adding:

   ```
   backend.url=https://your-backend.example.com/
   ```

   If omitted, the build defaults to `https://emergencynow.samuil.me/`.

5. **Run** from Android Studio onto an emulator or a real device, or from the command line:

   ```
   ./gradlew installDebug
   ```
