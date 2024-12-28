# Flip 2 DND

<p align="center">
  <img src="./metadata/en-US/images/icon.png" alt="App Icon" width="150"/>
</p>

## Overview

Flip 2 DND is a modern Android application that intelligently manages your Do Not Disturb (DND) mode based on your phone's orientation. Just flip your phone face down to activate DND and face up to turn it off—it's that simple!

## 📥 Download

<p align="center">
  <a href="https://apt.izzysoft.de/fdroid/index/apk/dev.robin.flip_2_dnd">
    <img src="./assets/images/getItIzzyOnDroid.png" alt="IzzyOnDroid" width="150"/>
  </a>
  <a href="https://github.com/robinsrk/Flip_2_DND/releases/">
    <img src="./assets/images/getItGithub.png" alt="GitHub" width="150"/>
  </a>
   </br>
   <a href="https://f-droid.org/en/packages/dev.robin.flip_2_dnd/">
   <img src="./assets/images/getItf-droid.png" alt="Obtainium" width="150"/>
   </a>
   <a href="https://www.openapk.net/flip-2-dnd/dev.robin.flip_2_dnd/">
    <img src="./assets/images/getItOpenapk.png" alt="OpenAPK" width="150"/>
   </a>
</p>

## 🚀 Features

- **Automatic DND Toggle**: Effortlessly switch DND on/off based on your phone's position.
- **Delay Mechanism**: A 2-second delay prevents accidental toggles.
- **Sleek UI**: Built with Jetpack Compose for a clean, modern look.
- **Material You Design**: Dynamic theming for a personalized experience.
- **Customizable Feedback**: Choose between vibration and sound notifications.
- **User-Friendly Settings**: Easily customize your DND preferences.

## 🏗️ Architecture

Built with modern Android practices:

- **UI Layer**: Jetpack Compose with Material 3
- **Architecture Pattern**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Concurrency**: Kotlin Coroutines & Flow
- **State Management**: StateFlow

## 📋 Requirements

- Android 6.0 (API level 23) or higher
- Accelerometer sensor
- Permission to modify Do Not Disturb settings

## Permissions

The app requires the following permissions:

- `ACCESS_NOTIFICATION_POLICY`: To modify DND settings
- `VIBRATE`: For vibration feedback
- `SENSOR`: To detect phone orientation
