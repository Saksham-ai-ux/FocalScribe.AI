package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ScriptViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.theme.*

@Composable
fun PreviewScreen(
    viewModel: ScriptViewModel,
    scriptId: Int?,
    isNew: Boolean,
    modifier: Modifier = Modifier
) {
    val activeScript by viewModel.activeScript.collectAsState()
    val scrollState = rememberScrollState()

    var isEditingText by remember { mutableStateOf(false) }
    var draftText by remember(activeScript) { mutableStateOf(activeScript?.fullText ?: "") }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("preview_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isNew) "Your Crafted Script" else "View Script",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                // --- Bookmark / Save toggle ---
                val isSaved = activeScript?.id != 0
                IconButton(
                    onClick = {
                        if (!isSaved) {
                            viewModel.saveActiveScript()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Status",
                        tint = if (isSaved) CyberTeal else Color.White
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            val script = activeScript

            if (script == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyberTeal)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // --- Meta Row ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BadgeLabel(
                            text = script.platform,
                            containerColor = CyberIndigo.copy(alpha = 0.2f),
                            textColor = CyberTeal
                        )
                        BadgeLabel(
                            text = script.tone,
                            containerColor = Color.White.copy(alpha = 0.05f),
                            textColor = TextSecondary
                        )
                        BadgeLabel(
                            text = "${script.durationSeconds}s max",
                            containerColor = Color.White.copy(alpha = 0.05f),
                            textColor = TextSecondary
                        )
                    }

                    val wordCount = viewModel.getWordCount(draftText)
                    val estDuration = viewModel.getEstimatedDurationSeconds(draftText)
                    val isOverrun = estDuration > script.durationSeconds

                    // --- Smart Pacing & Estimation Indicator ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOverrun) RedError.copy(alpha = 0.08f) else GreenSuccess.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚡ SCRIPT PACING INDICATOR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverrun) RedError else CyberTeal,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Est. Duration: ~${estDuration}s ($wordCount words)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isOverrun) {
                                        "⚠️ Overruns limit by ${estDuration - script.durationSeconds}s. Consider trimming or speaking faster."
                                    } else {
                                        "✅ Perfect length! Fits nicely within your ${script.durationSeconds}s limit."
                                    },
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // --- AI Hook Score Engine ---
                    HookScoreEngineCard(viewModel, script)

                    // --- Script Text Editor Container ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberSurface)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SCRIPT CONTENT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTeal,
                                    letterSpacing = 1.sp
                                )

                                IconButton(
                                    onClick = {
                                        if (isEditingText) {
                                            viewModel.updateActiveScriptText(draftText)
                                        }
                                        isEditingText = !isEditingText
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Toggle edit mode",
                                        tint = if (isEditingText) CyberTeal else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (isEditingText) {
                                OutlinedTextField(
                                    value = draftText,
                                    onValueChange = { draftText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 250.dp)
                                        .testTag("script_editor_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = CyberTeal,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                        focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                        unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            } else {
                                Text(
                                    text = draftText,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = Color.White,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Tip Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberPink.copy(alpha = 0.08f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "💡 Bracketed prompts like [Cut to close up] are indicators for your video presentation flow. They will be highlighted in the teleprompter to sync physical gestures with words.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = CyberPink
                        )
                    }

                    // --- Tone Shifter (Script Re-writer) ---
                    val isShiftingTone by viewModel.isShiftingTone.collectAsState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberSurface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🎭 TONE SHIFTER (AI RE-WRITER)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTeal,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Instantly rewrite this script into a different vibe with one click.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        
                        if (isShiftingTone) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = CyberTeal, modifier = Modifier.size(24.dp))
                            }
                        } else {
                            val tones = listOf("Hype", "Educational", "Controversial", "Storytelling")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tones.forEach { t ->
                                    val isCurrent = script.tone.equals(t, ignoreCase = true)
                                    FilterChip(
                                        selected = isCurrent,
                                        onClick = {
                                            if (!isCurrent) {
                                                viewModel.shiftActiveScriptTone(t)
                                            }
                                        },
                                        label = { Text(t, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyberIndigo,
                                            selectedLabelColor = Color.White,
                                            containerColor = CyberSurfaceVariant,
                                            labelColor = TextSecondary
                                        ),
                                        border = null
                                    )
                                }
                            }
                        }
                    }

                    // --- Multi-Hook Variant Generator ---
                    MultiHookVariantGeneratorCard(viewModel)

                    // --- Viral SEO Distribution Pack ---
                    ViralSEODistributionPackCard(viewModel)

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // --- Bottom Action Buttons ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isSaved = script.id != 0

                    if (!isSaved) {
                        Button(
                            onClick = { viewModel.saveActiveScript() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberIndigo,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Script to Library", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            // If user is editing text, save first!
                            if (isEditingText) {
                                viewModel.updateActiveScriptText(draftText)
                                isEditingText = false
                            }
                            // Save to database automatically if it's a new script, so it can be loaded in the teleprompter safely
                            if (script.id == 0) {
                                viewModel.saveActiveScript()
                            }
                            // Navigate to teleprompter
                            viewModel.selectScriptForTeleprompter(viewModel.activeScript.value?.id ?: script.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("launch_teleprompter_btn"),
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
                            Icon(imageVector = Icons.Default.VideoCameraFront, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch Pro Teleprompter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HookScoreEngineCard(
    viewModel: ScriptViewModel,
    script: com.example.data.database.ScriptEntity
) {
    val hookAnalysis by viewModel.hookAnalysis.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingHook.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = CyberTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🏆 AI HOOK SCORE ENGINE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTeal,
                    letterSpacing = 1.sp
                )
            }
            
            BadgeLabel(
                text = "PREMIUM",
                containerColor = CyberPink.copy(alpha = 0.2f),
                textColor = CyberPink
            )
        }

        Text(
            text = "Viral hooks dictate 85% of short-form success. Analyze and optimize your opening 3 seconds to keep viewers hooked.",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 17.sp
        )

        if (isAnalyzing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(color = CyberTeal, modifier = Modifier.size(36.dp))
                Text(
                    text = "Calculating scroll-stopping potential...",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (hookAnalysis == null) {
            Button(
                onClick = { viewModel.analyzeActiveScriptHook() },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Analyze Hook Performance", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        } else {
            val analysis = hookAnalysis!!
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(35.dp))
                        .background(
                            when {
                                analysis.overallScore >= 90 -> GreenSuccess.copy(alpha = 0.15f)
                                analysis.overallScore >= 80 -> CyberTeal.copy(alpha = 0.15f)
                                else -> CyberPink.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${analysis.overallScore}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                analysis.overallScore >= 90 -> GreenSuccess
                                analysis.overallScore >= 80 -> CyberTeal
                                else -> CyberPink
                            }
                        )
                        Text(
                            text = "/100",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            analysis.overallScore >= 90 -> "🔥 VIRAL LEVEL HOOK"
                            analysis.overallScore >= 80 -> "⚡ STRONG HOOK"
                            else -> "⚠️ WEAK HOOK STYLE"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            analysis.overallScore >= 90 -> GreenSuccess
                            analysis.overallScore >= 80 -> CyberTeal
                            else -> CyberPink
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = analysis.explanation,
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 16.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ENGAGEMENT METRICS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                MetricBar("Hook Strength", analysis.hookStrength, CyberTeal)
                MetricBar("Curiosity Score", analysis.curiosityScore, CyberIndigo)
                MetricBar("Scroll-Stopping Potential", analysis.scrollStoppingPotential, CyberPink)
                MetricBar("Emotional Impact", analysis.emotionalImpact, CyberTeal)
                MetricBar("Clarity", analysis.clarity, GreenSuccess)
            }

            Divider(color = Color.White.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "⚠️ WEAK AREAS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedError
                    )
                    analysis.weakAreas.forEach { area ->
                        Text(
                            text = "• $area",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "🚀 VALUE FIXES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenSuccess
                    )
                    analysis.suggestions.forEach { suggestion ->
                        Text(
                            text = "👉 $suggestion",
                            fontSize = 11.sp,
                            color = Color.White,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = { viewModel.analyzeActiveScriptHook() },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTeal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Refresh Score Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MetricBar(label: String, value: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Text(text = "$value/100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value / 100f)
                    .background(color)
            )
        }
    }
}

@Composable
fun MultiHookVariantGeneratorCard(
    viewModel: ScriptViewModel
) {
    val variants by viewModel.fiveHookVariants.collectAsState()
    val isLoading by viewModel.isLoadingFiveHookVariants.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyberTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🎯 MULTI-HOOK VARIANT GENERATOR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTeal,
                    letterSpacing = 1.sp
                )
            }

            BadgeLabel(
                text = "5 FRAMEWORKS",
                containerColor = CyberIndigo.copy(alpha = 0.2f),
                textColor = CyberTeal
            )
        }

        Text(
            text = "Generate 5 hooks targeting curiosity, contradiction, fear, story, and status. Swapping is instant and automatically triggers scoring.",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 17.sp
        )

        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(color = CyberTeal, modifier = Modifier.size(36.dp))
                Text(
                    text = "Synthesizing psychological viral angles...",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (variants.isEmpty()) {
            Button(
                onClick = { viewModel.generateFiveHookVariants() },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Generate 5 Viral Frameworks", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                variants.forEach { variant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CyberIndigo)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = variant.framework.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    
                                    Text(
                                        text = "${variant.predictedScore}% lift",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenSuccess
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = GreenSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = variant.hookText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = variant.shortExplanation,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.applyHookVariant(variant.hookText) },
                                    modifier = Modifier.height(28.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = CyberTeal
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberTeal.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Apply & Recalculate", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { viewModel.generateFiveHookVariants() },
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Regenerate Framework Variants", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ViralSEODistributionPackCard(
    viewModel: ScriptViewModel
) {
    val pack by viewModel.viralSEOPack.collectAsState()
    val isLoading by viewModel.isLoadingViralSEOPack.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var selectedPlatformIndex by remember { mutableStateOf(0) }
    val platforms = listOf("Instagram Reels", "YouTube Shorts", "TikTok", "LinkedIn")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = CyberTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🌐 VIRAL SEO DISTRIBUTION PACK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTeal,
                    letterSpacing = 1.sp
                )
            }

            BadgeLabel(
                text = "SEO SUITE",
                containerColor = CyberIndigo.copy(alpha = 0.2f),
                textColor = CyberTeal
            )
        }

        Text(
            text = "Generate optimized publishing materials customized for specific platform algorithms to secure higher search visibility and SEO discoverability.",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 17.sp
        )

        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(color = CyberTeal, modifier = Modifier.size(36.dp))
                Text(
                    text = "Generating cross-platform metadata suites...",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (pack == null) {
            Button(
                onClick = { viewModel.generateViralSEODistributionPack() },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Generate Cross-Platform Pack", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                platforms.forEachIndexed { idx, platform ->
                    val isSelected = selectedPlatformIndex == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CyberIndigo else CyberSurfaceVariant)
                            .clickable { selectedPlatformIndex = idx }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = platform.substringBefore(" "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            val currentPlatformName = platforms[selectedPlatformIndex]
            val platformPack = pack?.platforms?.find { 
                it.platform.lowercase().contains(currentPlatformName.substringBefore(" ").lowercase()) 
            } ?: pack?.platforms?.getOrNull(selectedPlatformIndex)

            if (platformPack != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberSurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📈 HIGH-CTR TITLE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTeal
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(platformPack.highCtrTitle))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy title",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = platformPack.highCtrTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberSurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💬 SHORT-FORM CAPTION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberPink
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(platformPack.shortCaption))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy short caption",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = platformPack.shortCaption,
                            fontSize = 13.sp,
                            color = Color.White,
                            lineHeight = 17.sp
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberSurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📝 IN-DEPTH DESCRIPTION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberIndigo
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(platformPack.longCaption))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy long caption",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = platformPack.longCaption,
                            fontSize = 12.sp,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(CyberSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🏷️ TOP SEO HASHTAGS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTeal
                            )
                            
                            val tagsText = platformPack.hashtags.joinToString(" ")
                            Text(
                                text = tagsText,
                                fontSize = 11.sp,
                                color = CyberTeal,
                                lineHeight = 14.sp
                            )

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(tagsText))
                                },
                                modifier = Modifier.fillMaxWidth().height(24.dp),
                                contentPadding = PaddingValues(0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Copy Tags", fontSize = 9.sp, color = Color.White)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(CyberSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🔍 SEARCH KEYWORDS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            
                            val keywordText = platformPack.searchKeywords.joinToString(", ")
                            Text(
                                text = keywordText,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(keywordText))
                                },
                                modifier = Modifier.fillMaxWidth().height(24.dp),
                                contentPadding = PaddingValues(0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Copy Keywords", fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }

                    if (platformPack.ctaSuggestions.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberPink.copy(alpha = 0.08f))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "🔥 CONVERSION CALL-TO-ACTION (CTA)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberPink
                                )
                                Text(
                                    text = platformPack.ctaSuggestions.firstOrNull() ?: "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = { viewModel.generateViralSEODistributionPack() },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTeal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Regenerate Cross-Platform Pack", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
