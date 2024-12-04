# Flip 2 DND

A modern Android application that automatically toggles Do Not Disturb mode based on phone orientation. Simply flip your phone face down to enable DND mode and face up to disable it.

## Features

- 🔄 Automatic DND toggle based on phone orientation
- ⏱️ 2-second delay before enabling DND to prevent accidental triggers
- 📱 Clean, modern UI built with Jetpack Compose
- 🎨 Material You design with dynamic theming
- ⚡ Efficient sensor monitoring
- 🔔 Configurable feedback options:
  - Vibration feedback
  - Sound notifications (when DND is disabled)
- ⚙️ Settings screen for customization

## Architecture

The app is built using Clean Architecture principles and modern Android development practices:

- **UI Layer**: Jetpack Compose with Material 3
- **Architecture Pattern**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Concurrency**: Kotlin Coroutines & Flow
- **State Management**: StateFlow

## Requirements

- Android 6.0 (API level 23) or higher
- Accelerometer sensor
- Permission to modify Do Not Disturb settings

## Setup

1. Clone the repository:
```bash
git clone https://github.com/robinsrk/Flip_2_DND.git
```

2. Open the project in Android Studio

3. Build and run the app

## Permissions

The app requires the following permissions:

- `ACCESS_NOTIFICATION_POLICY`: To modify DND settings
- `VIBRATE`: For vibration feedback
- `SENSOR`: To detect phone orientation

## Contributing

Feel free to submit issues, fork the repository, and create pull requests for any improvements.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Material 3 design system
- Android sensor framework
