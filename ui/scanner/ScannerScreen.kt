package com.example.phantomlens.ui.scanner

import android.app.Activity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.phantomlens.util.AudioPlayer
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {
    val isScareTriggered by viewModel.isScareTriggered.collectAsState()
    val showAprilFoolMessage by viewModel.showAprilFoolMessage.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val isGlitching by viewModel.isGlitching.collectAsState()
    
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val audioPlayer = remember { AudioPlayer(context) }

    DisposableEffect(Unit) {
        onDispose { audioPlayer.release() }
    }

    val scareScale by animateFloatAsState(
        targetValue = if (isScareTriggered) 8f else 0.1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = "scareScale"
    )

    // Glitch Shiver Effect with explicit Random
    var glitchX by remember { mutableStateOf(0.dp) }
    var glitchY by remember { mutableStateOf(0.dp) }
    
    LaunchedEffect(isGlitching) {
        if (isGlitching) {
            while(true) {
                glitchX = Random.nextInt(-10, 10).dp
                glitchY = Random.nextInt(-10, 10).dp
                delay(50)
            }
        } else {
            glitchX = 0.dp
            glitchY = 0.dp
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        CameraPreview(modifier = Modifier.fillMaxSize())

        if (!showAprilFoolMessage && !isScareTriggered) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .offset(x = glitchX, y = glitchY),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statusText,
                    color = if (isGlitching) Color.Red else Color(0xFF00FF41),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { loadingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = if (isGlitching) Color.Red else Color(0xFF00FF41),
                    trackColor = Color.DarkGray,
                )
            }
        }

        if (isScareTriggered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👹", 
                    fontSize = 180.sp, 
                    modifier = Modifier.scale(scareScale)
                )
            }
            
            LaunchedEffect(Unit) {
                audioPlayer.playScreech()
                while(true) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    delay(50)
                }
            }
        }

        if (showAprilFoolMessage) {
            AlertDialog(
                onDismissRequest = {},
                title = { 
                    Text(
                        "Gotcha! 😂", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    ) 
                },
                text = { 
                    Text(
                        "Happy April Fool's Day! Your heart rate just hit 200 BPM. Prank complete.",
                        style = MaterialTheme.typography.bodyLarge
                    ) 
                },
                confirmButton = {
                    Button(
                        onClick = { (context as? Activity)?.finish() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Text("I'm not even mad", color = Color.White)
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startPrank()
    }
}

@Composable
fun CameraPreview(modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview
                    )
                } catch (e: Exception) {}
            }, ContextCompat.getMainExecutor(context))
            previewView
        },
        modifier = modifier
    )
}
