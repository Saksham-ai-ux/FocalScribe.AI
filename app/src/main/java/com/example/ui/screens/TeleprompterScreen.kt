package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ScriptViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TeleprompterScreen(
    viewModel: ScriptViewModel,
    scriptId: Int,
    modifier: Modifier = Modifier
) {
    val activeScript by viewModel.activeScript.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val speed by viewModel.scrollSpeed.collectAsState()
    val size by viewModel.fontSize.collectAsState()
    val isMirrored by viewModel.isMirrored.collectAsState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Smooth auto-scrolling side-effect loop
    LaunchedEffect(isPlaying, speed) {
        if (isPlaying) {
            while (isPlaying) {
                if (scrollState.value >= scrollState.maxValue) {
                    viewModel.isPlaying.value = false
                    break
                }
                // Adjust scroll increments for smooth 60fps pacing
                val increment = (speed / 15f)
                scrollState.scrollBy(increment)
                delay(16) // roughly 60fps updates
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black), // Force pure pitch black for maximal contrast
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.isPlaying.value = false
                        viewModel.navigateBack()
                    },
                    modifier = Modifier.testTag("teleprompter_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activeScript?.title ?: "Teleprompter",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                // Mirror mode toggle
                IconButton(
                    onClick = { viewModel.isMirrored.value = !isMirrored }
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = "Mirror text for reflection",
                        tint = if (isMirrored) CyberTeal else Color.White
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
        ) {
            val fullScriptText = activeScript?.fullText ?: "Loading script..."

            // Parse text to separate visual directions bracket from speaking text for highlight styling
            val parsedLines = remember(fullScriptText) { parseScriptText(fullScriptText) }

            // --- Interactive Delivery Coach Cue HUD ---
            val cues = remember(parsedLines) { parsedLines.filter { it.isDirection } }
            val scrollPercentage = if (scrollState.maxValue > 0) scrollState.value.toFloat() / scrollState.maxValue else 0f
            val activeCue = if (cues.isNotEmpty()) {
                val idx = (scrollPercentage * cues.size).toInt().coerceIn(0, cues.size - 1)
                cues[idx].text.removeSurrounding("[", "]")
            } else null

            activeCue?.let { cue ->
                val cueVibe = when {
                    cue.contains("smile", ignoreCase = true) -> Pair("😊", "SMILE COACH")
                    cue.contains("point", ignoreCase = true) || cue.contains("gesture", ignoreCase = true) -> Pair("👉", "GESTURE COACH")
                    cue.contains("pause", ignoreCase = true) || cue.contains("breathe", ignoreCase = true) -> Pair("⏱️", "PACING COACH")
                    cue.contains("energy", ignoreCase = true) || cue.contains("hype", ignoreCase = true) || cue.contains("serious", ignoreCase = true) -> Pair("🔥", "ENERGY COACH")
                    else -> Pair("🎬", "DELIVERY COACH")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberPink.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = cueVibe.first,
                            fontSize = 22.sp
                        )
                        Column {
                            Text(
                                text = cueVibe.second,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberPink,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = cue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // --- Main Scrollable Script View ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (isMirrored) {
                            rotationY = 180f
                        }
                    }
                    .verticalScroll(scrollState)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 180.dp, bottom = 300.dp) // Generous top/bottom margin to scroll off-screen
                ) {
                    parsedLines.forEach { part ->
                        if (part.isDirection) {
                            // Style bracketed cues (e.g. [Show Smile]) differently
                            Text(
                                text = part.text,
                                fontSize = (size - 4f).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyberPink,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = if (isMirrored) TextAlign.Right else TextAlign.Left
                            )
                        } else {
                            Text(
                                text = part.text,
                                fontSize = size.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = (size * 1.4f).sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = if (isMirrored) TextAlign.Right else TextAlign.Left
                            )
                        }
                    }
                }
            }

            // --- Eye-Tracking Alignment Guide Line ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .height(48.dp)
                    .background(CyberTeal.copy(alpha = 0.08f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .weight(1f)
                            .background(CyberTeal)
                    )
                    Text(
                        text = " 👀 LENS EYE-ALIGNMENT LINE (LOOK HERE) ",
                        color = CyberTeal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .weight(1f)
                            .background(CyberTeal)
                    )
                }
            }

            // --- Bottom Floating Control Center Panel ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(CyberSurface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Row 1: Play/Pause button + Resets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.isPlaying.value = false
                                coroutineScope.launch {
                                    scrollState.scrollTo(0)
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Scroll Position",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        FloatingActionButton(
                            onClick = { viewModel.isPlaying.value = !isPlaying },
                            containerColor = if (isPlaying) CyberPink else CyberTeal,
                            contentColor = Color.Black,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(56.dp)
                                .testTag("teleprompter_play_fab")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Complete manual button
                        IconButton(
                            onClick = {
                                viewModel.isPlaying.value = false
                                viewModel.navigateBack()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Complete Session",
                                tint = CyberTeal
                            )
                        }
                    }

                    // Divider
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Row 2: Sliders (Font Size and Scroll Speed)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Font Size Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Font: ${size.toInt()}sp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.width(80.dp)
                            )
                            Slider(
                                value = size,
                                onValueChange = { viewModel.fontSize.value = it },
                                valueRange = 18f..48f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberTeal,
                                    activeTrackColor = CyberTeal,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.08f)
                                )
                            )
                        }

                        // Scroll Speed Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Speed: ${speed.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.width(80.dp)
                            )
                            Slider(
                                value = speed,
                                onValueChange = { viewModel.scrollSpeed.value = it },
                                valueRange = 10f..80f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberTeal,
                                    activeTrackColor = CyberTeal,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.08f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// Simple parser helper
data class ScriptPart(val text: String, val isDirection: Boolean)

fun parseScriptText(text: String): List<ScriptPart> {
    val list = mutableListOf<ScriptPart>()
    val lines = text.split("\n")
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            list.add(ScriptPart(trimmed, true))
        } else {
            list.add(ScriptPart(trimmed, false))
        }
    }
    return list
}
