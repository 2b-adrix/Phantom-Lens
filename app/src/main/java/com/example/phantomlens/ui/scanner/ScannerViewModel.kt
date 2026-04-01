package com.example.phantomlens.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel : ViewModel() {

    private val _isScareTriggered = MutableStateFlow(false)
    val isScareTriggered = _isScareTriggered.asStateFlow()

    private val _showAprilFoolMessage = MutableStateFlow(false)
    val showAprilFoolMessage = _showAprilFoolMessage.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress = _loadingProgress.asStateFlow()

    private val _statusText = MutableStateFlow("INITIALIZING SENSORS...")
    val statusText = _statusText.asStateFlow()

    private val _isGlitching = MutableStateFlow(false)
    val isGlitching = _isGlitching.asStateFlow()

    fun startPrank() {
        viewModelScope.launch {
            // Stage 1: Slow, "Authentic" Calibration to build trust (Total ~8 seconds)
            val stages = listOf(
                Triple("CONNECTING TO CORE...", 0.15f, 1000L),
                Triple("CALIBRATING MAGNETOMETER...", 0.45f, 1000L),
                Triple("SCANNING FOR IONIZING RADIATION...", 0.75f, 500L),
                Triple("ENTITY DETECTED: ANALYZING...", 1.0f, 1000L)
            )

            for (stage in stages) {
                _statusText.value = stage.first
                _loadingProgress.value = stage.second
                delay(stage.third)
            }

            // Stage 2: The Glitch - Build DREAD (3 seconds)
            _isGlitching.value = true
            _statusText.value = "FATAL ERROR: OVERLOAD DETECTED"
            delay(1800)
            _statusText.value = "SYSTEM KERNEL PANIC - REBOOTING..."
            delay(1200)

            // Stage 2.5: The "Lean In" Moment
            _isGlitching.value = false 
            _statusText.value = ""
            delay(700)

            // Stage 3: THE SCARE triggers
            _isScareTriggered.value = true
        }
    }

    fun onScareSoundComplete() {
        // Stop the scare immediately when the video ends
        _isScareTriggered.value = false
        _isGlitching.value = false
        _showAprilFoolMessage.value = true
    }
}
