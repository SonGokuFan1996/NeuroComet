package com.kyilmaz.neurocomet

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium subscription screen for NeuroComet.
 * Offers:
 * - $2/month ad-free subscription
 * - $60 one-time lifetime purchase
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    onPurchaseSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val subscriptionState by SubscriptionManager.subscriptionState.collectAsState()
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan>(SubscriptionPlan.MONTHLY) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }

    // Fetch offerings when screen opens
    LaunchedEffect(Unit) {
        SubscriptionManager.fetchOfferings()
        SubscriptionManager.checkPremiumStatus { }
    }

    // Handle purchase success
    LaunchedEffect(subscriptionState.purchaseSuccess) {
        if (subscriptionState.purchaseSuccess) {
            showSuccessDialog = true
            SubscriptionManager.clearPurchaseSuccess()
        }
    }

    // Handle errors
    LaunchedEffect(subscriptionState.error) {
        if (subscriptionState.error != null) {
            showErrorSnackbar = true
        }
    }

    // Already premium - show thank you screen
    if (subscriptionState.isPremium && !showSuccessDialog) {
        PremiumActiveScreen(onBack = onBack)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Go Premium", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Premium icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFD4AF37),
                                    Color(0xFFE8C547)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color(0xFF1A1A1A)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "NeuroComet Premium",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Enjoy an ad-free, distraction-free experience",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                // Benefits list
                PremiumBenefitsList()

                Spacer(Modifier.height(32.dp))

                // Subscription options
                Text(
                    "Choose Your Plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))

                // Monthly plan
                SubscriptionPlanCard(
                    plan = SubscriptionPlan.MONTHLY,
                    title = "Monthly",
                    price = subscriptionState.monthlyPackage?.product?.price?.formatted ?: "$2.00",
                    period = "/month",
                    description = "Billed monthly, cancel anytime",
                    isSelected = selectedPlan == SubscriptionPlan.MONTHLY,
                    onSelect = { selectedPlan = SubscriptionPlan.MONTHLY }
                )

                Spacer(Modifier.height(12.dp))

                // Lifetime plan
                SubscriptionPlanCard(
                    plan = SubscriptionPlan.LIFETIME,
                    title = "Lifetime",
                    price = subscriptionState.lifetimePackage?.product?.price?.formatted ?: "$60.00",
                    period = " one-time",
                    description = "Pay once, premium forever!",
                    badge = "BEST VALUE",
                    isSelected = selectedPlan == SubscriptionPlan.LIFETIME,
                    onSelect = { selectedPlan = SubscriptionPlan.LIFETIME },
                    savings = "Save 60%+ vs monthly"
                )

                Spacer(Modifier.height(32.dp))

                // Subscribe button
                Button(
                    onClick = {
                        if (activity == null) return@Button

                        when (selectedPlan) {
                            SubscriptionPlan.MONTHLY -> {
                                SubscriptionManager.purchaseMonthly(
                                    activity = activity,
                                    onSuccess = { onPurchaseSuccess() },
                                    onError = { }
                                )
                            }
                            SubscriptionPlan.LIFETIME -> {
                                SubscriptionManager.purchaseLifetime(
                                    activity = activity,
                                    onSuccess = { onPurchaseSuccess() },
                                    onError = { }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !subscriptionState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (subscriptionState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            when (selectedPlan) {
                                SubscriptionPlan.MONTHLY -> "Subscribe Monthly"
                                SubscriptionPlan.LIFETIME -> "Get Lifetime Access"
                            },
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Restore purchases
                TextButton(
                    onClick = {
                        SubscriptionManager.restorePurchases(
                            onSuccess = { isPremium ->
                                if (isPremium) {
                                    showSuccessDialog = true
                                }
                            },
                            onError = { }
                        )
                    }
                ) {
                    Text(
                        "Restore Purchases",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Terms and privacy
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Terms of Service",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { /* Open terms */ }
                    )
                    Text(
                        " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Privacy Policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { /* Open privacy */ }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Subscription info
                Text(
                    "Subscriptions auto-renew unless cancelled at least 24 hours before the end of the current period. " +
                    "Manage subscriptions in your Google Play account settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Success dialog with enhanced congratulations
    if (showSuccessDialog) {
        val purchaseType = subscriptionState.purchaseType

        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onPurchaseSuccess()
                onBack()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFD4AF37),
                                    Color(0xFFE8C547)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(48.dp)
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "🎉 Congratulations! 🎉",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when (purchaseType) {
                            "lifetime" -> "You're a Lifetime Member!"
                            "monthly" -> "You're Now Premium!"
                            "restored" -> "Welcome Back, Premium Member!"
                            else -> "Welcome to Premium!"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when (purchaseType) {
                            "lifetime" -> "Thank you for your incredible support! You now have lifetime access to all premium features. No more ads, ever! 🚀"
                            "monthly" -> "Thank you for subscribing! Enjoy your ad-free experience and all premium features. Your support means everything to us! 💜"
                            "restored" -> "Your premium subscription has been restored! Welcome back to the ad-free experience. 🌟"
                            else -> "Thank you for supporting NeuroComet! Enjoy your ad-free experience."
                        },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(16.dp))

                    // Benefits reminder
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            "Your Premium Benefits:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "✅ Completely ad-free experience",
                            "✅ Priority content delivery",
                            "✅ Exclusive premium themes",
                            "✅ Supporting neurodiversity"
                        ).forEach { benefit ->
                            Text(
                                benefit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPurchaseSuccess()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700)
                    )
                ) {
                    Text(
                        "Let's Go! 🚀",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // Error snackbar
    if (showErrorSnackbar && subscriptionState.error != null) {
        LaunchedEffect(showErrorSnackbar) {
            kotlinx.coroutines.delay(3000)
            showErrorSnackbar = false
            SubscriptionManager.clearError()
        }
    }
}

@Composable
private fun PremiumBenefitsList() {
    val benefits = listOf(
        BenefitItem(Icons.Filled.Block, "No Ads", "Completely ad-free experience"),
        BenefitItem(Icons.Filled.Speed, "Faster Loading", "Priority content delivery"),
        BenefitItem(Icons.Filled.Palette, "Exclusive Themes", "Special premium themes"),
        BenefitItem(Icons.Filled.Favorite, "Support Development", "Help us build more features")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        benefits.forEach { benefit ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        benefit.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        benefit.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        benefit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    title: String,
    price: String,
    period: String,
    description: String,
    badge: String? = null,
    savings: String? = null,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "scale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            Color(0xFFFFD700)
        else
            MaterialTheme.colorScheme.outlineVariant,
        label = "border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio button
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFFD4AF37)
                )
            )

            Spacer(Modifier.width(8.dp))

            // Title, description, savings column
            Column(modifier = Modifier.weight(1f)) {
                // Title row with inline badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (badge != null) {
                        Surface(
                            color = Color(0xFFD4AF37),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                badge,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (savings != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        savings,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Price column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    price,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    period,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Screen shown when user already has premium
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumActiveScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium Active", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFFA500)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "You're Premium! ⭐",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Thank you for supporting NeuroComet!\nEnjoy your ad-free experience.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            OutlinedButton(onClick = onBack) {
                Text("Back to App")
            }
        }
    }
}

private data class BenefitItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

enum class SubscriptionPlan {
    MONTHLY,
    LIFETIME
}

