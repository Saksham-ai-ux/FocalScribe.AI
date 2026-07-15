package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ScriptViewModel
import com.example.ui.theme.*

@Composable
fun PaywallScreen(
    viewModel: ScriptViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedPlanIndex by remember { mutableStateOf(0) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val spotsRemaining = 82

    val plans = listOf(
        PlanOption(
            name = "Founding Creator",
            price = "₹99",
            duration = "/mo",
            desc = "Lifetime price lock • First 100 creators only",
            isPopular = true,
            badge = "Most Popular",
            features = listOf(
                "Lifetime price lock (lock ₹99/mo forever)",
                "Unlimited script generations",
                "Unlimited hook variants",
                "SEO title generator & captions",
                "Hashtag packs & Tone shifter",
                "Smart pacing & Interactive delivery coach",
                "Priority feature access"
            )
        ),
        PlanOption(
            name = "Creator Pro",
            price = "₹199",
            duration = "/mo",
            desc = "Standard pricing • Full advanced capabilities",
            isPopular = false,
            badge = "Future Default",
            features = listOf(
                "Everything in Founding Creator",
                "Priority support",
                "Advanced creator tools",
                "Viral score engine",
                "Future analytics dashboard",
                "Priority feature access"
            )
        )
    )

    // Log Upgrade Viewed event when screen is entered
    LaunchedEffect(Unit) {
        com.example.analytics.FocalScribeAnalytics.logUpgradeViewed(context, "Direct Paywall Navigation")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Top Dismiss Navigation Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini Brand Identifier
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = CyberTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FOCALSCRIBE PREMIUM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTeal,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("paywall_dismiss_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = TextSecondary
                    )
                }
            }

            // --- Scarcity Indicator ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE65100).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$spotsRemaining / 100 Founding Creator Spots Remaining",
                        color = Color(0xFFFFB74D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- Hero Section ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Lock Founding Pricing Forever",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Acquire early adopters. Build trust. Boost creator success.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // --- Plan Selection Cards (Stacked) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                plans.forEachIndexed { index, plan ->
                    val isSelected = selectedPlanIndex == index
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) CyberSurfaceVariant else CyberSurface)
                            .border(
                                width = 2.dp,
                                color = if (isSelected) {
                                    if (plan.isPopular) CyberTeal else CyberPink
                                } else Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedPlanIndex = index }
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = plan.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (plan.badge != null) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (plan.isPopular) CyberTeal.copy(alpha = 0.15f)
                                                        else CyberPink.copy(alpha = 0.15f)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = plan.badge,
                                                    color = if (plan.isPopular) CyberTeal else CyberPink,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = plan.desc,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = plan.price,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = plan.duration,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Features Checklist for the Selected Plan ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberSurface)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val activePlan = plans[selectedPlanIndex]
                Text(
                    text = "What's included in ${activePlan.name}:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activePlan.isPopular) CyberTeal else CyberPink
                )

                activePlan.features.forEach { feature ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (activePlan.isPopular) CyberTeal else CyberPink,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // --- Checkout CTA ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val plan = plans[selectedPlanIndex]
                Button(
                    onClick = {
                        isProcessingPayment = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("paywall_purchase_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (plan.isPopular) CyberTeal else CyberPink,
                        contentColor = if (plan.isPopular) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isProcessingPayment
                ) {
                    if (isProcessingPayment) {
                        CircularProgressIndicator(
                            color = if (plan.isPopular) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Get Instant Access (${plan.price}${plan.duration})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Secure payment processing powered by Google Play & Stripe",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Simulating purchase processing
        if (isProcessingPayment) {
            val plan = plans[selectedPlanIndex]
            LaunchedEffect(Unit) {
                com.example.analytics.FocalScribeAnalytics.logUpgradePlanClicked(context, plan.name, plan.price)
                kotlinx.coroutines.delay(2000)
                isProcessingPayment = false
                viewModel.unlockPro(planName = plan.name, price = plan.price)
                showSuccessDialog = true
            }
        }

        if (showSuccessDialog) {
            val plan = plans[selectedPlanIndex]
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    viewModel.navigateBack()
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            viewModel.navigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (plan.isPopular) CyberTeal else CyberPink,
                            contentColor = if (plan.isPopular) Color.Black else Color.White
                        )
                    ) {
                        Text("Get Started", fontWeight = FontWeight.Bold)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (plan.isPopular) CyberTeal else CyberPink,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "FocalScribe Pro Unlocked!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "You have unlocked all Premium features on the ${plan.name} Tier! Thank you for backing our build-in-public journey.",
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

data class PlanOption(
    val name: String,
    val price: String,
    val duration: String,
    val desc: String,
    val isPopular: Boolean,
    val badge: String?,
    val features: List<String>
)
