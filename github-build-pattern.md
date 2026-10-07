# GitHub-based Android Build Pattern Used by top3

## Summary
This document explains how the current Android app project in the sibling repo called top3 is structured so it can be built on GitHub and produce an Android App Bundle (AAB) or APK.

The important point is that the project is a standard Gradle-based Android app, and GitHub can build it by checking out the repo, installing Java, and running Gradle commands.

## What was observed in the repo

### Project type
The project is a standard Android application using Gradle.

The key config files include:
- build.gradle at the root
- settings.gradle
- gradle.properties
- app/build.gradle
- gradlew (Gradle wrapper)

### Root Gradle setup
The root build.gradle file uses the Android Gradle Plugin:

- com.android.application version 8.2.1

This is the normal setup for Android builds in GitHub Actions.

### Module setup
The project includes an app module:

- app/
  - build.gradle
  - src/

This means the build command is generally run against the app module.

### Android configuration
The app module is configured with:
- applicationId
- compileSdk 34
- targetSdk 34
- Java 17
- viewBinding enabled
- buildConfig enabled

This is important because GitHub CI must use a matching Java version and Android build tools environment.

## Build commands used for Android packaging
For a standard Android project, the release bundle is usually produced with:

- ./gradlew bundleRelease

or for APK:

- ./gradlew assembleRelease

These commands generate output under:

- app/build/outputs/bundle/release/
- app/build/outputs/apk/release/

The generated AAB file is what is normally uploaded for Play Store release.

## GitHub build flow pattern
Even though the current workspace does not show a .github/workflows folder, the intended pattern for GitHub-hosted builds is:

1. Checkout repository
2. Set up Java 17
3. Grant execute permission to gradlew
4. Run Gradle bundleRelease or assembleRelease
5. Upload the generated AAB/APK as a workflow artifact
6. Optional: sign the app with a keystore before uploading to Play Console or release storage

## Example GitHub Actions workflow
This is the basic pattern to use for a simple app like Colors:

```yaml
name: Android Build

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - name: Build release bundle
        run: ./gradlew bundleRelease

      - name: Upload AAB artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-bundle
          path: app/build/outputs/bundle/release/*.aab
```

## What this means for the new Colors app
The Colors app should follow the same GitHub-friendly build pattern:

- use a standard Android Gradle project
- keep app code under an app module
- use Java or Kotlin with Java 17
- build with bundleRelease or assembleRelease
- upload the bundle as an artifact in GitHub Actions

## Recommended release pattern for future Android apps
For this kind of app, the best GitHub build pipeline is:

1. Build on every push to main
2. Upload artifact for testing
3. Optionally sign release builds with a keystore
4. Publish the AAB to Play Console or internal testing

## Conclusion
The top3 app is a standard Android build that can be automated through GitHub using Gradle and Java 17. The key idea is:

- GitHub checks out the code
- Gradle builds the app
- release APK/AAB is produced
- generated artifacts are uploaded for release or testing

This is the pattern the Colors repo should copy.

## Action item for the next AI
When building the Colors app, set up the repository so it can be built by GitHub Actions with:

- Java 17
- Android Gradle Plugin
- Gradle wrapper
- a release build command
- optional keystore signing only if release signing is needed later
