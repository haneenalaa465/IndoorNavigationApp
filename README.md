# Door Navigation Assistant for Visually Impaired

This mobile application is designed to assist visually impaired individuals in navigating through a room and exiting via a door. It uses real-time object detection to identify doors and provides audio instructions to guide the user.

## Features
- Real-time door detection using YOLO11.
- Audio instructions to guide the user (e.g., "Go Right", "Go Left", "Go ahead", "Stop").
- User-friendly interface with start/stop functionality.

## Technologies Used
- **Programming Language**: Kotlin
- **Framework**: Android Studio
- **Object Detection**: YOLO11 (ONNX format)
- **Audio**: Pre-defined voice commands stored in `res/raw`
- **Dataset**: [Door Detection Dataset](https://www.kaggle.com/datasets/sayedmohamed1/doors-detection/data)

## Setup Instructions
1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/door-navigation-assistant.git
   ```

2. **Open in Android Studio**
   - Launch Android Studio and select "Open an existing project".
   - Navigate to the cloned repository and select it.

3. **Add Dependencies**
   - Ensure the following dependencies are included in `build.gradle`:
     ```gradle
     implementation 'androidx.camera:camera-core:1.3.0'
     implementation 'androidx.camera:camera-camera2:1.3.0'
     implementation 'androidx.camera:camera-lifecycle:1.3.0'
     implementation 'androidx.camera:camera-view:1.3.0'
     implementation 'com.microsoft.onnxruntime:onnxruntime-android:1.11.0'
     ```

4. **Add Permissions**
   - Include the following permission in `AndroidManifest.xml`:
     ```xml
     <uses-permission android:name="android.permission.CAMERA" />
     ```

5. **Add Audio Files**
   - Place the audio files (`go_left.mp3`, `go_right.mp3`, `go_ahead.mp3`, `stop.mp3`, `searching.mp3`) in the `res/raw` directory.

6. **Add Model File**
   - Place the fine-tuned YOLO11 model (`yolo11_door_detection.onnx`) in the `assets` folder.

## Usage
1. **Launch the Application**
   - Build and run the application on an Android device.

2. **Start Assistance**
   - Press the "Start Assistance" button to begin navigation guidance.

3. **Follow Audio Instructions**
   - Listen to the audio instructions to navigate towards the door.

4. **Stop Assistance**
   - Press the "Stop Assistance" button to halt the guidance.

## Contributing
Contributions are welcome! Please fork the repository and submit a pull request with your changes.
