import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Size
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.microsoft.onnxruntime.OrtEnvironment
import com.microsoft.onnxruntime.OrtSession
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var ortEnvironment: OrtEnvironment
    private lateinit var ortSession: OrtSession
    private var mediaPlayer: MediaPlayer? = null
    private var isAssistanceActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        } else {
            startCamera()
        }

        ortEnvironment = OrtEnvironment.getEnvironment()
        val modelBytes = assets.open("yolo11_door_detection.onnx").readBytes()
        ortSession = ortEnvironment.createSession(modelBytes, OrtSession.SessionOptions())

        findViewById<Button>(R.id.start_stop_button).setOnClickListener {
            toggleAssistance()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.preview_view).surfaceProvider)
            }
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 640))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), DoorDetectionAnalyzer())
                }
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleAssistance() {
        isAssistanceActive = !isAssistanceActive
        val button = findViewById<Button>(R.id.start_stop_button)
        button.text = if (isAssistanceActive) "Stop Assistance" else "Start Assistance"
        if (!isAssistanceActive) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    private inner class DoorDetectionAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            if (!isAssistanceActive) {
                image.close()
                return
            }

            val inputTensor = preprocessImage(image)
            val output = ortSession.run(mapOf("images" to inputTensor))
            val door = parseOutput(output)

            door?.let {
                val instruction = determineInstruction(it)
                playInstruction(instruction)
            } ?: run {
                playInstruction("Searching")
            }

            image.close()
        }

        private fun preprocessImage(image: ImageProxy): Any {
            val bitmap = image.toBitmap()
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
            val buffer = ByteBuffer.allocateDirect(1 * 3 * 640 * 640 * 4)
            buffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(640 * 640)
            resizedBitmap.getPixels(intValues, 0, 640, 0, 0, 640, 640)
            for (pixel in intValues) {
                buffer.putFloat(((pixel shr 16 and 0xFF) / 255.0f)) // R
                buffer.putFloat(((pixel shr 8 and 0xFF) / 255.0f))  // G
                buffer.putFloat(((pixel and 0xFF) / 255.0f))       // B
            }
            return OrtSession.TensorInfo("images", buffer.asFloatBuffer(), longArrayOf(1, 3, 640, 640))
        }

        private fun parseOutput(output: OrtSession.Result): BoundingBox? {
            val detections = output.get(0).value as Array<FloatArray>
            for (detection in detections) {
                val confidence = detection[4]
                if (confidence > 0.5f && detection[5].toInt() == 0) { // Assuming class 0 is "door"
                    return BoundingBox(
                        x = detection[0] - detection[2] / 2,
                        y = detection[1] - detection[3] / 2,
                        width = detection[2],
                        height = detection[3]
                    )
                }
            }
            return null
        }

        private fun determineInstruction(box: BoundingBox): String {
            val frameCenterX = 320f
            val boxCenterX = box.x + box.width / 2
            val boxArea = box.width * box.height

            return when {
                boxArea > 150000 -> "Stop" // Close to door
                boxCenterX < frameCenterX - 50 -> "Go Left"
                boxCenterX > frameCenterX + 50 -> "Go Right"
                else -> "Go ahead"
            }
        }
    }

    private fun playInstruction(instruction: String) {
        if (mediaPlayer?.isPlaying == true) return
        mediaPlayer?.release()
        val resId = when (instruction) {
            "Go Left" -> R.raw.go_left
            "Go Right" -> R.raw.go_right
            "Go ahead" -> R.raw.go_ahead
            "Stop" -> R.raw.stop
            "Searching" -> R.raw.searching
            else -> return
        }
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.start()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        ortSession.close()
        ortEnvironment.close()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

data class BoundingBox(val x: Float, val y: Float, val width: Float, val height: Float)