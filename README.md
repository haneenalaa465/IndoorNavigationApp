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


## Usage
1. **Clone the Repository**
   ```bash
   git clone https://github.com/haneenalaa465/IndoorNavigationApp.git
   ```

2. **Open in Android Studio**
   - Launch Android Studio and select "Open an existing project".
     
3. **Launch the Application**
   - Build and run the application on an Android device.

4. **Start Assistance**
   - Press the "Start Assistance" button to begin navigation guidance.

5. **Follow Audio Instructions**
   - Listen to the audio instructions to navigate towards the door.

6. **Stop Assistance**
   - Press the "Stop Assistance" button to halt the guidance.

## Contributing
Contributions are welcome! Please fork the repository and submit a pull request with your changes.
