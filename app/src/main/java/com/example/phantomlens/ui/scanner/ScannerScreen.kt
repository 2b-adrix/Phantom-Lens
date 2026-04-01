package com.example.phantomlens.ui.scanner

import android.app.Activity
import android.net.Uri
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.phantomlens.R
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(UnstableApi::class)
@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {
    val isScareTriggered by viewModel.isScareTriggered.collectAsState()
    val showAprilFoolMessage by viewModel.showAprilFoolMessage.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val isGlitching by viewModel.isGlitching.collectAsState()
    
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.scare_video}")
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        viewModel.onScareSoundComplete()
                    }
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose { 
            exoPlayer.release()
        }
    }

    // High-impact spring for the jumpscare
    val scareScale by animateFloatAsState(
        targetValue = if (isScareTriggered) 2.0f else 0.1f,
        animationSpec = spring(
            dampingRatio = 0.15f, 
            stiffness = Spring.StiffnessHigh
        ), label = "scareScale"
    )

    // Intense Strobe Effect
    val infiniteTransition = rememberInfiniteTransition(label = "strobe")
    val strobeColor by infiniteTransition.animateColor(
        initialValue = Color.Black,
        targetValue = Color.Red.copy(alpha = 0.6f),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 35, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "strobeColor"
    )

    // Fixed Glitch Shiver Effect (Fixes the Iterations crash)
    val glitchOffset by animateDpAsState(
        targetValue = if (isGlitching) (-15..15).random().dp else 0.dp,
        animationSpec = if (isGlitching) {
            repeatable(
                iterations = Int.MAX_VALUE,
                animation = tween(durationMillis = 40)
            )
        } else {
            spring()
        }, label = "glitchOffset"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Fullscreen Camera View
        CameraPreview(modifier = Modifier.fillMaxSize())

        // Technical Overlay (The Hook)
        if (!showAprilFoolMessage && !isScareTriggered) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .offset(x = glitchOffset, y = glitchOffset),
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

        // THE MEGA JUMPSCARE VIDEO
        if (isScareTriggered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (Random.nextBoolean()) strobeColor else Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Removed jitter offset Box
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scareScale)
                )
                
                // Static Noise Overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (i in 0..150) {
                        drawCircle(
                            color = Color.White.copy(alpha = Random.nextFloat() * 0.4f),
                            radius = Random.nextFloat() * 6f,
                            center = androidx.compose.ui.geometry.Offset(
                                x = Random.nextFloat() * size.width,
                                y = Random.nextFloat() * size.height
                            )
                        )
                    }
                }
            }
            
            // Start video playback
            LaunchedEffect(Unit) {
                exoPlayer.play()
            }
        }

        // Final Reveal Popup
        if (showAprilFoolMessage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF6200EE)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Gotcha! 😂", 
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "HAPPY APRIL FOOL'S DAY!",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your heart rate just hit 250 BPM. Mission Accomplished.",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(56.dp))
                    Button(
                        onClick = { (context as? Activity)?.finish() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.height(64.dp).fillMaxWidth(0.8f)
                    ) {
                        Text("I'M LITERALLY DEAD", color = Color(0xFF6200EE), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
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
