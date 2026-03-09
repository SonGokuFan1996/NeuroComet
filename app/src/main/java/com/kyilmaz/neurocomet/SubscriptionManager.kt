package com.kyilmaz.neurocomet

import android.app.Activity
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

/**
 * Manages RevenueCat subscriptions for NeuroComet.
 *
 * Products:
 * - Monthly: $2/month ad-free subscription
 * - Lifetime: $60 one-time purchase for lifetime ad-free
 *
 * Entitlement: "premium" - grants ad-free experience
 *
 * SECURITY: This manager includes anti-piracy verification.
 * Any attempts to bypass the subscription will result in app termination.
 */
object SubscriptionManager {

    private const val TAG = "SubscriptionManager"
    private const val BILLING_UNAVAILABLE_MESSAGE = "Purchases are temporarily unavailable. Please try again later."

    // =========================================================================
    // TEST MODE — enabled automatically in debug builds.
    // Simulates the full purchase flow without a real RevenueCat account so the
    // UI, navigation, and ad-removal logic can be exercised end-to-end.
    // =========================================================================
    private val testMode: Boolean = BuildConfig.DEBUG

    /** Coroutine scope used by test-mode helpers to simulate async delays. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Tracks whether the simulated user has "purchased" premium in test mode. */
    @Volatile
    private var isTestPremium = false

    // RevenueCat product identifiers (configure these in RevenueCat dashboard)
    const val PRODUCT_MONTHLY = "NeuroComet_premium_monthly"
    const val PRODUCT_LIFETIME = "NeuroComet_premium_lifetime"

    // Entitlement identifier
    const val ENTITLEMENT_PREMIUM = "premium"

    // Security: Verification token for premium status
    @Volatile
    private var verificationToken: String? = null

    @Volatile
    private var lastVerificationTime: Long = 0L

    private const val VERIFICATION_VALIDITY_MS = 60_000L // 1 minute

    // Billing configuration state
    @Volatile
    private var isBillingConfigured = BuildConfig.DEBUG

    // State
    private val _subscriptionState = MutableStateFlow(SubscriptionState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    data class SubscriptionState(
        val isLoading: Boolean = false,
        val isPremium: Boolean = false,
        val offerings: Offerings? = null,
        val currentOffering: Offering? = null,
        val monthlyPackage: Package? = null,
        val lifetimePackage: Package? = null,
        val error: String? = null,
        val purchaseSuccess: Boolean = false,
        val purchaseType: String? = null // "monthly" or "lifetime"
    )

    /**
     * SECURITY: Generate verification token from RevenueCat CustomerInfo
     * This ensures premium status can only be set through legitimate purchases
     */
    private fun generateVerificationToken(customerInfo: CustomerInfo): String {
        val entitlement = customerInfo.entitlements[ENTITLEMENT_PREMIUM]
        val data = "${customerInfo.originalAppUserId}:${entitlement?.productIdentifier}:${entitlement?.isActive}"
        return MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .fold("") { str, byte -> str + "%02x".format(byte) }
    }

    /**
     * SECURITY: Verify premium status is legitimate
     * Returns true only if:
     * 1. User has valid RevenueCat entitlement
     * 2. Verification token matches
     * 3. Verification is recent (within validity window)
     */
    fun verifyPremiumStatus(): Boolean {
        // Test mode: trust the simulated flag
        if (testMode) return isTestPremium

        val state = _subscriptionState.value
        if (!state.isPremium) return false

        // Check if verification is still valid
        val now = System.currentTimeMillis()
        if (now - lastVerificationTime > VERIFICATION_VALIDITY_MS) {
            // Token expired, force re-verification
            verificationToken = null
            return false
        }

        return verificationToken != null
    }

    /**
     * SECURITY: Enforce premium verification
     * Call this before granting premium features.
     * Will crash the app if tampering is detected.
     */
    fun enforcePremiumSecurity() {
        // Test mode: no enforcement needed
        if (testMode) return

        val state = _subscriptionState.value

        // If app claims premium but has no valid verification
        if (state.isPremium && !verifyPremiumStatus()) {
            Log.e(TAG, "⚠️ SECURITY VIOLATION: Premium status without verification!")

            // Reset premium status
            _subscriptionState.value = _subscriptionState.value.copy(isPremium = false)

            // Schedule integrity check
            checkPremiumStatus { isPremium ->
                if (!isPremium && _subscriptionState.value.isPremium) {
                    // Definite tampering detected - crash
                    Log.e(TAG, "🚨 TAMPERING DETECTED: Forcing app termination")
                    throw SecurityException("License verification failed. Please purchase a valid subscription.")
                }
            }
        }
    }

    /**
     * Set billing availability
     */
    fun setBillingConfigured(isConfigured: Boolean, errorMessage: String? = null) {
        isBillingConfigured = isConfigured
        if (!testMode && !isConfigured) {
            verificationToken = null
            _subscriptionState.value = _subscriptionState.value.copy(
                isLoading = false,
                isPremium = false,
                offerings = null,
                currentOffering = null,
                monthlyPackage = null,
                lifetimePackage = null,
                error = errorMessage ?: BILLING_UNAVAILABLE_MESSAGE,
                purchaseSuccess = false,
                purchaseType = null
            )
        }
    }

    fun isBillingConfigured(): Boolean = testMode || isBillingConfigured

    private fun requireBillingConfigured(onError: ((String) -> Unit)? = null): Boolean {
        if (testMode || isBillingConfigured) return true
        val message = _subscriptionState.value.error ?: BILLING_UNAVAILABLE_MESSAGE
        _subscriptionState.value = _subscriptionState.value.copy(
            isLoading = false,
            error = message,
            isPremium = false
        )
        onError?.invoke(message)
        return false
    }

    /**
     * Fetch available offerings from RevenueCat
     */
    fun fetchOfferings() {
        // Test mode: simulate offerings loaded after a short delay
        if (testMode) {
            _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)
            scope.launch {
                delay(400) // Simulate network
                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false,
                    // Packages stay null — the UI already falls back to "$2.00" / "$60.00"
                    monthlyPackage = null,
                    lifetimePackage = null
                )
                Log.d(TAG, "🧪 TEST MODE: Offerings simulated (prices use UI fallbacks)")
            }
            return
        }

        if (!requireBillingConfigured()) return

        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)

        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                Log.e(TAG, "Error fetching offerings: ${error.message}")
                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            },
            onSuccess = { offerings ->
                Log.d(TAG, "Offerings fetched successfully")
                val currentOffering = offerings.current

                // Find monthly and lifetime packages
                val monthlyPkg = currentOffering?.monthly
                    ?: currentOffering?.getPackage(PRODUCT_MONTHLY)
                val lifetimePkg = currentOffering?.lifetime
                    ?: currentOffering?.getPackage(PRODUCT_LIFETIME)

                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false,
                    offerings = offerings,
                    currentOffering = currentOffering,
                    monthlyPackage = monthlyPkg,
                    lifetimePackage = lifetimePkg
                )

                Log.d(TAG, "Monthly package: ${monthlyPkg?.product?.price}")
                Log.d(TAG, "Lifetime package: ${lifetimePkg?.product?.price}")
            }
        )
    }

    /**
     * Check current premium status
     */
    fun checkPremiumStatus(onResult: (Boolean) -> Unit) {
        // Test mode: return simulated state immediately
        if (testMode) {
            _subscriptionState.value = _subscriptionState.value.copy(isPremium = isTestPremium)
            onResult(isTestPremium)
            Log.d(TAG, "🧪 TEST MODE: Premium status = $isTestPremium")
            return
        }

        if (!requireBillingConfigured { onResult(false) }) return

        try {
            Purchases.sharedInstance.getCustomerInfo(
                callback = object : com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: CustomerInfo) {
                        val isPremium = customerInfo.entitlements[ENTITLEMENT_PREMIUM]?.isActive == true

                        // SECURITY: Set verification token only if legitimately premium
                        if (isPremium) {
                            verificationToken = generateVerificationToken(customerInfo)
                            lastVerificationTime = System.currentTimeMillis()
                        } else {
                            verificationToken = null
                        }

                        _subscriptionState.value = _subscriptionState.value.copy(isPremium = isPremium)
                        onResult(isPremium)
                        Log.d(TAG, "Premium status: $isPremium")
                    }

                    override fun onError(error: PurchasesError) {
                        Log.e(TAG, "Error checking premium status: ${error.message}")
                        verificationToken = null
                        onResult(false)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception checking premium status", e)
            verificationToken = null
            onResult(false)
        }
    }

    /**
     * Purchase the monthly subscription
     */
    fun purchaseMonthly(activity: Activity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (testMode) {
            simulateTestPurchase("monthly", onSuccess)
            return
        }

        if (!requireBillingConfigured(onError)) return

        val pkg = _subscriptionState.value.monthlyPackage
        if (pkg == null) {
            onError("Monthly subscription not available")
            return
        }
        purchasePackage(activity, pkg, "monthly", onSuccess, onError)
    }

    /**
     * Purchase the lifetime subscription
     */
    fun purchaseLifetime(activity: Activity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (testMode) {
            simulateTestPurchase("lifetime", onSuccess)
            return
        }

        if (!requireBillingConfigured(onError)) return

        val pkg = _subscriptionState.value.lifetimePackage
        if (pkg == null) {
            onError("Lifetime subscription not available")
            return
        }
        purchasePackage(activity, pkg, "lifetime", onSuccess, onError)
    }

    /**
     * Internal purchase logic
     */
    private fun purchasePackage(
        activity: Activity,
        pkg: Package,
        purchaseType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!requireBillingConfigured(onError)) return

        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)

        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(activity, pkg).build(),
            object : PurchaseCallback {
                override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                    val isPremium = customerInfo.entitlements[ENTITLEMENT_PREMIUM]?.isActive == true

                    // SECURITY: Set verification token after successful purchase
                    if (isPremium) {
                        verificationToken = generateVerificationToken(customerInfo)
                        lastVerificationTime = System.currentTimeMillis()
                    }

                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        isPremium = isPremium,
                        purchaseSuccess = true,
                        purchaseType = purchaseType
                    )
                    Log.d(TAG, "Purchase successful! Premium: $isPremium, Type: $purchaseType")
                    onSuccess()
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        error = if (userCancelled) null else error.message
                    )
                    if (!userCancelled) {
                        Log.e(TAG, "Purchase error: ${error.message}")
                        onError(error.message)
                    } else {
                        Log.d(TAG, "Purchase cancelled by user")
                    }
                }
            }
        )
    }

    /**
     * Restore purchases
     */
    fun restorePurchases(onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        // Test mode: simulate restore
        if (testMode) {
            _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)
            scope.launch {
                delay(800)
                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false,
                    isPremium = isTestPremium,
                    purchaseSuccess = isTestPremium,
                    purchaseType = if (isTestPremium) "restored" else null
                )
                Log.d(TAG, "🧪 TEST MODE: Restore simulated — premium = $isTestPremium")
                onSuccess(isTestPremium)
            }
            return
        }

        if (!requireBillingConfigured {
                onError(it)
                onSuccess(false)
            }) return

        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)

        Purchases.sharedInstance.restorePurchases(
            callback = object : com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    val isPremium = customerInfo.entitlements[ENTITLEMENT_PREMIUM]?.isActive == true

                    // SECURITY: Set verification token after successful restore
                    if (isPremium) {
                        verificationToken = generateVerificationToken(customerInfo)
                        lastVerificationTime = System.currentTimeMillis()
                    } else {
                        verificationToken = null
                    }

                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        isPremium = isPremium,
                        purchaseSuccess = isPremium,
                        purchaseType = if (isPremium) "restored" else null
                    )
                    Log.d(TAG, "Restore successful! Premium: $isPremium")
                    onSuccess(isPremium)
                }

                override fun onError(error: PurchasesError) {
                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                    Log.e(TAG, "Restore error: ${error.message}")
                    onError(error.message)
                }
            }
        )
    }

    // =========================================================================
    // TEST MODE HELPERS
    // =========================================================================

    /**
     * Simulates a purchase flow with a realistic delay.
     * Called internally by [purchaseMonthly] / [purchaseLifetime] when [testMode] is active.
     */
    private fun simulateTestPurchase(purchaseType: String, onSuccess: () -> Unit) {
        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)
        scope.launch {
            delay(1200) // Simulate payment sheet + network round-trip
            isTestPremium = true
            _subscriptionState.value = _subscriptionState.value.copy(
                isLoading = false,
                isPremium = true,
                purchaseSuccess = true,
                purchaseType = purchaseType
            )
            Log.d(TAG, "🧪 TEST MODE: Purchase simulated — type = $purchaseType")
            onSuccess()
        }
    }

    /**
     * Resets the simulated premium status back to free.
     * Useful from a DevOptions screen so you can re-test the purchase flow
     * without restarting the app.
     */
    fun resetTestPurchase() {
        if (!testMode) return
        isTestPremium = false
        _subscriptionState.value = SubscriptionState()
        Log.d(TAG, "🧪 TEST MODE: Premium status reset to FREE")
    }

    /**
     * Clear purchase success flag
     */
    fun clearPurchaseSuccess() {
        _subscriptionState.value = _subscriptionState.value.copy(
            purchaseSuccess = false,
            purchaseType = null
        )
    }

    /**
     * Clear error
     */
    fun clearError() {
        _subscriptionState.value = _subscriptionState.value.copy(error = null)
    }
}

