# Heads Up! Android Game

A modern Android implementation of the popular party game "Heads Up!" where players have to guess words displayed on their phone screen based on their friends' clues while holding the phone to their forehead.

## Features

- Multiple themed decks (Pop Culture, Mime, Animals, Celebrities)
- Motion-based gameplay using device sensors
- Real-time video recording of gameplay sessions
- Interactive animations and particle effects
- Sound effects and countdown timers
- Score tracking and game statistics
- Dynamic word loading from JSONBin.io API
- Responsive UI with material design elements

## Technical Details

### Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK 33 or higher
- Java 11
- Device with:
  - Accelerometer sensor
  - Gyroscope sensor
  - Front-facing camera
  - Internet connection

### Dependencies

- AndroidX Libraries
- Material Design Components
- Retrofit2 for API calls
- CameraX for video recording
- GSON for JSON parsing

```implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
```

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/headsup.git
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Build and run the application

## Usage

1. Launch the app and select a deck category
2. Hold the phone in landscape orientation to your forehead
3. Wait for the countdown timer
4. Friends describe the word shown on screen
5. Tilt phone down for correct guess, up to pass
6. View your score and recorded video at the end

## Architecture

The app follows a modular architecture with distinct components:

### Managers
- `APIManager`: Handles API calls to fetch word lists
- `CameraManager`: Controls video recording functionality
- `GameSensorManager`: Manages device motion sensors
- `SoundManager`: Handles game sound effects

### Animations
- `CardFlipAnimator`: Manages card flip animations
- `CircularTimerView`: Custom view for countdown timer
- `ParticleSystem`: Handles particle effects
- `ShakeAnimator`: Controls shake animations

### Models
- `Deck`: Represents game categories
- `WordResult`: Stores word attempt results
- `CardResponse`: API response model

## Permissions Required

```xml
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```



