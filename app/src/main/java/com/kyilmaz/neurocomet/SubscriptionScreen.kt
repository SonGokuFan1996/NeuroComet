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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.purchases.PackageType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay as kdelay
import kotlinx.coroutines.launch

/**
 * Possible outcomes shown on the transaction banking-card.
 */
private enum class TransactionResult { SUCCESS, DECLINED, TIMED_OUT }

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
    var showAllOfferingsDialog by remember { mutableStateOf(false) }

    // ── Transaction status card state ──
    var transactionResult by remember { mutableStateOf<TransactionResult?>(null) }
    var purchaseInFlight by remember { mutableStateOf(false) }
    val timeoutScope = rememberCoroutineScope()
    var timeoutJob by remember { mutableStateOf<Job?>(null) }

    // Start a 30-second timeout whenever a purchase goes in-flight
    LaunchedEffect(purchaseInFlight) {
        if (purchaseInFlight) {
            timeoutJob?.cancel()
            timeoutJob = timeoutScope.launch {
                kdelay(30_000L)
                // If still loading after 30 s, show timed-out card
                if (purchaseInFlight) {
                    purchaseInFlight = false
                    transactionResult = TransactionResult.TIMED_OUT
                }
            }
        }
    }

    // Fetch offerings when screen opens
    LaunchedEffect(Unit) {
        SubscriptionManager.fetchOfferings()
        SubscriptionManager.checkPremiumStatus { }
    }

    // Handle purchase success
    LaunchedEffect(subscriptionState.purchaseSuccess) {
        if (subscriptionState.purchaseSuccess) {
            purchaseInFlight = false
            timeoutJob?.cancel()
            transactionResult = TransactionResult.SUCCESS
            showSuccessDialog = true
            SubscriptionManager.clearPurchaseSuccess()
        }
    }

    // Handle errors → show declined card
    LaunchedEffect(subscriptionState.error) {
        if (subscriptionState.error != null && purchaseInFlight) {
            purchaseInFlight = false
            timeoutJob?.cancel()
            transactionResult = TransactionResult.DECLINED
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
                title = { Text(stringResource(R.string.sub_go_premium), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
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
                    stringResource(R.string.sub_premium_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    stringResource(R.string.sub_premium_tagline),
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
                    stringResource(R.string.sub_choose_plan),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))

                // Monthly plan
                SubscriptionPlanCard(
                    plan = SubscriptionPlan.MONTHLY,
                    title = stringResource(R.string.sub_plan_monthly),
                    price = subscriptionState.monthlyPackage?.product?.price?.formatted ?: "$2.00",
                    period = stringResource(R.string.sub_period_month),
                    description = stringResource(R.string.sub_monthly_desc),
                    isSelected = selectedPlan == SubscriptionPlan.MONTHLY,
                    onSelect = { if (!purchaseInFlight) selectedPlan = SubscriptionPlan.MONTHLY }
                )

                Spacer(Modifier.height(12.dp))

                // Lifetime plan
                val lifetimePrice = subscriptionState.lifetimePackage?.product?.price?.formatted
                    ?: subscriptionState.availableProducts.find { p -> 
                        SubscriptionManager.ENTITLEMENT_PREMIUM.let { _ -> // Just a scope
                            p.id.contains("lifetime", ignoreCase = true) 
                        }
                    }?.price?.formatted 
                    ?: "$59.99"

                SubscriptionPlanCard(
                    plan = SubscriptionPlan.LIFETIME,
                    title = stringResource(R.string.sub_plan_lifetime),
                    price = lifetimePrice,
                    period = stringResource(R.string.sub_period_one_time),
                    description = stringResource(R.string.sub_lifetime_desc),
                    badge = stringResource(R.string.sub_best_value),
                    isSelected = selectedPlan == SubscriptionPlan.LIFETIME,
                    onSelect = { if (!purchaseInFlight) selectedPlan = SubscriptionPlan.LIFETIME },
                    savings = stringResource(R.string.sub_savings)
                )

                // Fallback: Show all fetched offerings if specific ones aren't found
                if ((subscriptionState.monthlyPackage == null || subscriptionState.lifetimePackage == null) && !subscriptionState.isLoading) {
                    val offerings = subscriptionState.offerings
                    if (offerings != null && offerings.all.isNotEmpty()) {
                        val allPackages = offerings.all.values.flatMap { it.availablePackages }
                        
                        // Find any package that isn't already assigned to a main card
                        val extraPackages = allPackages.filter { pkg ->
                            pkg.identifier != subscriptionState.monthlyPackage?.identifier &&
                            pkg.identifier != subscriptionState.lifetimePackage?.identifier
                        }

                        if (extraPackages.isNotEmpty()) {
                            Text(
                                "Other Available Plans",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                            extraPackages.forEach { pkg ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            if (activity != null) {
                                                SubscriptionManager.purchasePackage(
                                                    activity,
                                                    pkg,
                                                    if (pkg.packageType == PackageType.LIFETIME) "lifetime" else "other",
                                                    { onPurchaseSuccess() },
                                                    { }
                                                )
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            val displayName = when(pkg.packageType) {
                                                PackageType.LIFETIME -> "Lifetime Access"
                                                PackageType.ANNUAL -> "Annual Plan"
                                                PackageType.MONTHLY -> "Monthly Plan"
                                                else -> pkg.identifier
                                            }
                                            Text(displayName, fontWeight = FontWeight.Bold)
                                            Text(pkg.product.id, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(pkg.product.price.formatted, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "No packages found.",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { SubscriptionManager.fetchOfferings() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Retry Fetch Offerings")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Subscribe button
                Button(
                    onClick = {
                        if (activity == null || purchaseInFlight) return@Button
                        transactionResult = null  // reset any previous result
                        purchaseInFlight = true

                        when (selectedPlan) {
                            SubscriptionPlan.MONTHLY -> {
                                SubscriptionManager.purchaseMonthly(
                                    activity = activity,
                                    onSuccess = { onPurchaseSuccess() },
                                    onError = { purchaseInFlight = false }
                                )
                            }
                            SubscriptionPlan.LIFETIME -> {
                                SubscriptionManager.purchaseLifetime(
                                    activity = activity,
                                    onSuccess = { onPurchaseSuccess() },
                                    onError = { purchaseInFlight = false }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !subscriptionState.isLoading && !purchaseInFlight,
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
                                SubscriptionPlan.MONTHLY -> stringResource(R.string.sub_subscribe_monthly)
                                SubscriptionPlan.LIFETIME -> stringResource(R.string.sub_get_lifetime)
                            },
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Dev: Show All Offerings
                if (SubscriptionManager.testMode || BuildConfig.DEBUG || DeviceAuthority.isAuthorizedDevice(context)) {
                    TextButton(
                        onClick = { showAllOfferingsDialog = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Dev: Inspect All Offerings", style = MaterialTheme.typography.labelSmall)
                    }
                }

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
                        stringResource(R.string.sub_restore_purchases),
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
                        stringResource(R.string.sub_terms),
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
                        stringResource(R.string.sub_privacy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { /* Open privacy */ }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Subscription info
                Text(
                    stringResource(R.string.sub_legal_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(24.dp))

                // ── Developer test payment dialogs ──
                if (SubscriptionManager.testMode) {
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.txn_test_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.txn_test_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { transactionResult = TransactionResult.SUCCESS },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF2E7D32)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                stringResource(R.string.txn_test_success),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                        OutlinedButton(
                            onClick = { transactionResult = TransactionResult.DECLINED },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                stringResource(R.string.txn_test_declined),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                        OutlinedButton(
                            onClick = { transactionResult = TransactionResult.TIMED_OUT },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE65100)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE65100)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                stringResource(R.string.txn_test_timed_out),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            transactionResult = null
                            SubscriptionManager.resetTestPurchase()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.txn_test_reset))
                    }

                    Spacer(Modifier.height(16.dp))
                }

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
                        "🎉 Woohoo! Payment successful.",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when (purchaseType) {
                            "lifetime" -> stringResource(R.string.premium_lifetime_member)
                            "monthly" -> stringResource(R.string.premium_now_premium)
                            "restored" -> stringResource(R.string.premium_welcome_back)
                            else -> stringResource(R.string.premium_welcome)
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
                        "The dopamine hit has been deposited, and your premium powers are now fully unlocked.",
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
                            stringResource(R.string.sub_your_benefits),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            stringResource(R.string.sub_benefit_ad_free),
                            stringResource(R.string.sub_benefit_priority),
                            stringResource(R.string.sub_benefit_themes),
                            stringResource(R.string.sub_benefit_support)
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
                        stringResource(R.string.sub_lets_go),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // ── Payment error/timeout popups ──
    if (transactionResult != null && transactionResult != TransactionResult.SUCCESS) {
        val (popupTitle, popupMessage) = when (transactionResult) {
            TransactionResult.DECLINED -> "🚫 Payment declined." to "Sometimes the executive function to find the right credit card just isn't there today."
            TransactionResult.TIMED_OUT -> "⏳ Timed-Out!" to "The server got distracted by a shiny side quest and forgot to finish your transaction. Let's try again!"
            else -> "" to ""
        }

        AlertDialog(
            onDismissRequest = { transactionResult = null },
            title = { Text(popupTitle) },
            text = { Text(popupMessage) },
            confirmButton = {
                TextButton(onClick = { transactionResult = null }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        transactionResult = null
                        if (activity != null) {
                            purchaseInFlight = true
                            if (selectedPlan == SubscriptionPlan.MONTHLY) {
                                SubscriptionManager.purchaseMonthly(activity, { purchaseInFlight = false }, { purchaseInFlight = false })
                            } else {
                                SubscriptionManager.purchaseLifetime(activity, { purchaseInFlight = false }, { purchaseInFlight = false })
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_retry))
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

    // --- Dev Inspection Dialog ---
    if (showAllOfferingsDialog) {
        AlertDialog(
            onDismissRequest = { showAllOfferingsDialog = false },
            title = { Text("RevenueCat Offerings Info") },
            text = {
                val offerings = subscriptionState.offerings
                if (offerings == null) {
                    Text("Offerings: NULL (SDK not initialized or network error)")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Current Offering: ${offerings.current?.identifier ?: "NONE"}", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        
                        offerings.all.forEach { (offId, off) ->
                            Text("Offering: $offId", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            off.availablePackages.forEach { pkg ->
                                Card(
                                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(Modifier.padding(8.dp)) {
                                        Text("Package ID: ${pkg.identifier}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Product ID: ${pkg.product.id}", fontSize = 11.sp)
                                        Text("Type: ${pkg.packageType}", fontSize = 10.sp)
                                    }
                                }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllOfferingsDialog = false }) { Text(stringResource(R.string.cd_close)) }
            }
        )
    }
}

@Composable
private fun PremiumBenefitsList() {
    val benefits = listOf(
        BenefitItem(Icons.Filled.Block, stringResource(R.string.sub_benefit_no_ads), stringResource(R.string.sub_benefit_no_ads_desc)),
        BenefitItem(Icons.Filled.Speed, stringResource(R.string.sub_benefit_faster), stringResource(R.string.sub_benefit_faster_desc)),
        BenefitItem(Icons.Filled.Palette, stringResource(R.string.sub_benefit_exclusive_themes), stringResource(R.string.sub_benefit_exclusive_themes_desc)),
        BenefitItem(Icons.Filled.Favorite, stringResource(R.string.sub_benefit_dev_support), stringResource(R.string.sub_benefit_dev_support_desc))
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
                title = { Text(stringResource(R.string.sub_premium_active), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
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
                stringResource(R.string.sub_youre_premium),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                stringResource(R.string.sub_thanks_support),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            OutlinedButton(onClick = onBack) {
                Text(stringResource(R.string.sub_back_to_app))
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

// Old TransactionStatusCard removed

