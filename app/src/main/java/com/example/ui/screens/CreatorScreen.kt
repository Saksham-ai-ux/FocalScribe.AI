package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ScriptViewModel
import com.example.ui.viewmodel.GenerationState
import com.example.ui.theme.*

@Composable
fun CreatorScreen(
    viewModel: ScriptViewModel,
    modifier: Modifier = Modifier
) {
    val topic by viewModel.topic.collectAsState()
    val platform by viewModel.selectedPlatform.collectAsState()
    val tone by viewModel.selectedTone.collectAsState()
    val hookStyle by viewModel.selectedHookStyle.collectAsState()
    val duration by viewModel.selectedDuration.collectAsState()
    val customPrompt by viewModel.customPrompt.collectAsState()
    val generationState by viewModel.generationState.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // --- Custom Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("creator_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back to Dashboard",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Script Engine",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // --- Form Inputs ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Intro Text
                Text(
                    text = "Customize your script parameters below. The Gemini model will optimize structural pacing.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                // --- Topic Input ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "What is your video about?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { viewModel.topic.value = it },
                        placeholder = { Text("e.g. 3 simple morning habits to double productivity", fontSize = 14.sp, color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("script_topic_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = CyberSurface,
                            unfocusedContainerColor = CyberSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        maxLines = 3
                    )
                }

                // --- Platform Selection ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Target Platform",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    val platforms = listOf("Instagram Reels", "YouTube Shorts", "TikTok", "LinkedIn Video")
                    SelectChipsRow(
                        options = platforms,
                        selectedOption = platform,
                        onSelected = { viewModel.selectedPlatform.value = it },
                        activeColor = CyberTeal,
                        inactiveColor = CyberSurface
                    )
                }

                // --- Vibe & Tone ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Vibe & Tone",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    val tones = listOf("Hype", "Controversial", "Educational", "Storytelling", "Inspiring")
                    SelectChipsRow(
                        options = tones,
                        selectedOption = tone,
                        onSelected = { viewModel.selectedTone.value = it },
                        activeColor = CyberIndigo,
                        inactiveColor = CyberSurface
                    )
                }

                // --- Hook Framework ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hook Framework",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Hook info",
                            tint = CyberTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    val hookStyles = listOf("Alex Hormozi Style", "Ali Abdaal Style", "The Pattern Interrupt", "Negative Threats")
                    SelectChipsRow(
                        options = hookStyles,
                        selectedOption = hookStyle,
                        onSelected = { viewModel.selectedHookStyle.value = it },
                        activeColor = CyberPink,
                        inactiveColor = CyberSurface
                    )
                }

                // --- Duration ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Max Video Duration",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$duration seconds",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTeal
                        )
                    }
                    val durations = listOf(15, 30, 60, 90)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        durations.forEach { seconds ->
                            val isSelected = duration == seconds
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) CyberTeal else CyberSurface)
                                    .clickable { viewModel.selectedDuration.value = seconds }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${seconds}s",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else TextSecondary
                                )
                            }
                        }
                    }
                }

                // --- Custom Instruction Prompt ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Custom Guidelines (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    OutlinedTextField(
                        value = customPrompt,
                        onValueChange = { viewModel.customPrompt.value = it },
                        placeholder = { Text("e.g. Include a statistic about cortisol levels, end with a mention of my free eBook", fontSize = 14.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = CyberSurface,
                            unfocusedContainerColor = CyberSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- Generate Action Button ---
                Button(
                    onClick = { viewModel.generateAIScript() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("generate_script_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberTeal,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Craft AI Script",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- Full Screen Loading Overlay ---
        AnimatedVisibility(
            visible = generationState is GenerationState.Loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CreatorLoadingOverlay()
        }
    }
}

@Composable
fun SelectChipsRow(
    options: List<String>,
    selectedOption: String,
    onSelected: (String) -> Unit,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    val rowScrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rowScrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) activeColor else inactiveColor)
                    .clickable { onSelected(option) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = option,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        if (activeColor == CyberTeal) Color.Black else Color.White
                    } else TextSecondary
                )
            }
        }
    }
}

@Composable
fun CreatorLoadingOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Cycle through visual tips
    val tips = listOf(
        "A strong hook makes people stop scrolling instantly.",
        "Your script should sound natural when spoken. Short sentences work best.",
        "Maintain eye contact with the lens, not with your face on screen.",
        "Include pauses. Let the visual bracket guides prompt your gestures.",
        "Call to Action is key: tell the user exactly what to do next."
    )
    var currentTipIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            currentTipIndex = (currentTipIndex + 1) % tips.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .rotate(rotationAngle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Loading",
                    tint = CyberTeal,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Crafting Viral Script...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Consulting content frameworks and formatting delivery prompts.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- Creator Tip Card ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CREATOR TIP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberPink,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tips[currentTipIndex],
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
