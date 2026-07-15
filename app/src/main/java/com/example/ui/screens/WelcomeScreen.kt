package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScriptViewModel

@Composable
fun WelcomeScreen(
    viewModel: ScriptViewModel,
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 4

    // Pricing details
    val spotsRemaining = 82
    val foundingPrice = "₹99"
    val proPrice = "₹199"

    var isProcessingPayment by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var selectedOfferPlan by remember { mutableStateOf("Founding Creator") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyberBg,
                        Color(0xFF0F0B1E),
                        CyberBg
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Top Step Indicator & Skip Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stepper dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..totalSteps) {
                        Box(
                            modifier = Modifier
                                .size(width = if (currentStep == i) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(if (currentStep == i) CyberTeal else Color.White.copy(alpha = 0.3f))
                        )
                    }
                }

                // Skip button (Only show on screen 1-3)
                if (currentStep < totalSteps) {
                    TextButton(
                        onClick = { currentStep = totalSteps },
                        modifier = Modifier.testTag("onboarding_skip_btn")
                    ) {
                        Text(
                            text = "Skip Offer",
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // --- Main Content Box with Animated Transitions ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = currentStep, label = "onboarding_step_fade") { step ->
                    when (step) {
                        1 -> OnboardingStepOne()
                        2 -> OnboardingStepTwo()
                        3 -> OnboardingStepThree()
                        4 -> OnboardingStepFour(
                            spotsRemaining = spotsRemaining,
                            foundingPrice = foundingPrice,
                            proPrice = proPrice,
                            selectedPlan = selectedOfferPlan,
                            onSelectPlan = { selectedOfferPlan = it },
                            onUpgradeClick = {
                                isProcessingPayment = true
                            },
                            onFreeClick = {
                                com.example.analytics.FocalScribeAnalytics.logEvent(context, "onboarding_free_selected")
                                onGetStarted()
                            }
                        )
                    }
                }
            }

            // --- Footer Navigation Controls (Next/Unlock Buttons) ---
            if (currentStep < totalSteps) {
                Button(
                    onClick = { currentStep++ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_next_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberTeal,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStep == 3) "See Special Offer" else "Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowForward,
                            contentDescription = "Next"
                        )
                    }
                }
            }
        }

        // Simulating billing conversion flow
        if (isProcessingPayment) {
            LaunchedEffect(Unit) {
                com.example.analytics.FocalScribeAnalytics.logUpgradePlanClicked(
                    context,
                    selectedOfferPlan,
                    if (selectedOfferPlan == "Founding Creator") foundingPrice else proPrice
                )
                kotlinx.coroutines.delay(2000)
                isProcessingPayment = false
                viewModel.unlockPro(
                    planName = "$selectedOfferPlan Plan (Onboarding)",
                    price = if (selectedOfferPlan == "Founding Creator") foundingPrice else proPrice
                )
                showSuccessDialog = true
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    onGetStarted()
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onGetStarted()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = Color.Black)
                    ) {
                        Text("Let's Create!", fontWeight = FontWeight.Bold)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CyberTeal,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Price Locked Forever! ⭐",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Welcome to FocalScribe Pro! You are officially one of our Founding Creators. Let's record some scroll-stopping videos!",
                        textAlign = TextAlign.Center,
                        color = TextSecondary
                    )
                },
                containerColor = CyberSurface,
                titleContentColor = Color.White,
                textContentColor = TextSecondary,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

// --- Screen 1 Composable ---
@Composable
fun OnboardingStepOne() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Aesthetic Vector Dashboard Mock
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CyberSurfaceBrush())
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw decorative dashboard outlines representing speed and video
                val barBrush = Brush.linearGradient(listOf(CyberTeal, CyberIndigo))
                drawRoundRect(
                    brush = barBrush,
                    size = androidx.compose.ui.geometry.Size(size.width * 0.8f, 16f),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.6f, 12f),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 60f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.5f, 12f),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 90f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // Draw video player mock inside
                drawCircle(
                    color = CyberPink.copy(alpha = 0.2f),
                    radius = 45f,
                    center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f + 30f)
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = CyberPink,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Create better videos faster.",
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "An all-in-one suite designed specifically for solo-creators to draft viral video outlines, calibrate hooks, and record scripts confidently.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// --- Screen 2 Composable ---
@Composable
fun OnboardingStepTwo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Modern AI output mockup
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CyberSurfaceBrush())
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Platform chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberPink.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("YouTube Shorts", color = CyberPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Glowing generated content lines
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Viral Hook Formulation", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(8.dp)
                ) {
                    Text(
                        "\"I built an entire business in exactly 48 hours, and here is how...\"",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                // Hashtags & SEO indicator
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("#solopreneur", color = CyberTeal, fontSize = 10.sp)
                    Text("#buildinpublic", color = CyberTeal, fontSize = 10.sp)
                    Text("#indiehackers", color = CyberTeal, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Generate hooks, scripts and SEO content.",
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Skip writers block. Unlock high-CTR captions, tailored frameworks, contrarian hooks, and complete optimized hashtag platforms instantly.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// --- Screen 3 Composable ---
@Composable
fun OnboardingStepThree() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Teleprompter eye guide simulation
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CyberSurfaceBrush())
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Outlined top / bottom text
                Text("REMAIN CALM", color = Color.White.copy(alpha = 0.2f), fontSize = 12.sp)
                
                // Active eye line zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberTeal.copy(alpha = 0.1f))
                        .border(1.dp, CyberTeal, RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "LOOK DIRECTLY HERE",
                            color = CyberTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Keep perfect camera eye-contact",
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }

                Text("AND DELIVER SECURELY", color = Color.White.copy(alpha = 0.2f), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Record confidently using the smart teleprompter.",
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Maintain direct eye-contact with the lens using our interactive focal zones, customizable auto-scroll sliders, and mirrored layouts.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// --- Screen 4 Composable ---
@Composable
fun OnboardingStepFour(
    spotsRemaining: Int,
    foundingPrice: String = "₹99",
    proPrice: String = "₹199",
    selectedPlan: String,
    onSelectPlan: (String) -> Unit,
    onUpgradeClick: () -> Unit,
    onFreeClick: () -> Unit
) {
    val pricingText = if (selectedPlan == "Founding Creator") "₹99/mo" else "₹199/mo"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Scarcity alert banner
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE65100).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$spotsRemaining / 100 Founding Creator Spots Remaining",
                    color = Color(0xFFFFB74D),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Catchy launch header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Limited Founder Launch",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Become one of our first 100 creators and lock your price forever.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Two Plan Choice Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Founding Creator Card
            val isFoundingSelected = selectedPlan == "Founding Creator"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isFoundingSelected) CyberSurfaceVariant else CyberSurface)
                    .border(
                        width = 2.dp,
                        color = if (isFoundingSelected) CyberTeal else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectPlan("Founding Creator") }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Founding Creator", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberTeal.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PROMO LOCK", color = CyberTeal, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Lifetime price lock, unlimited script exports, smart pacing.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹99", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("/month", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }

            // Creator Pro Card
            val isProSelected = selectedPlan == "Creator Pro"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isProSelected) CyberSurfaceVariant else CyberSurface)
                    .border(
                        width = 2.dp,
                        color = if (isProSelected) CyberPink else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectPlan("Creator Pro") }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Creator Pro", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberPink.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LATER DEFAULT", color = CyberPink, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Everything in Founding, future premium features, viral score engine.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹199", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("/month", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        }

        // Lock offer and Start free CTAs
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_pricing_pro_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPlan == "Founding Creator") CyberTeal else CyberPink,
                    contentColor = if (selectedPlan == "Founding Creator") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Lock In $selectedPlan ($pricingText)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Text(
                text = "Skip & start with Starter Free Plan (5 scripts/day)",
                color = TextSecondary,
                fontSize = 13.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { onFreeClick() }
                    .testTag("onboarding_pricing_free_btn")
            )
        }
    }
}
