# Mind Flutter App

This is the Flutter version of the Mind app, migrated from the original Kotlin project.

## Getting Started

1. Ensure you have Flutter installed: https://docs.flutter.dev/get-started/install
2. Open this folder in VS Code or your preferred IDE.
3. Run `flutter pub get` to fetch dependencies.
4. Use `flutter run` to launch the app on an emulator or device.

## Project Structure
- `lib/` — Dart source code
- `assets/` — Images, fonts, etc.
- `test/` — Unit and widget tests

## Publishing
- Update app icons and assets in `assets/`
- Configure `android/app/build.gradle` for signing
- Build release with `flutter build appbundle`
- Upload to Google Play Console
