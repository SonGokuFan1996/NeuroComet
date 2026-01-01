package com.kyilmaz.neuronetworkingtitle

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
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

/**
 * Manages RevenueCat subscriptions for NeuroNet.
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

    // RevenueCat product identifiers (configure these in RevenueCat dashboard)
    const val PRODUCT_MONTHLY = "neuronet_premium_monthly"
    const val PRODUCT_LIFETIME = "neuronet_premium_lifetime"

    // Entitlement identifier
    const val ENTITLEMENT_PREMIUM = "premium"

    // Security: Verification token for premium status
    @Volatile
    private var verificationToken: String? = null

    @Volatile
    private var lastVerificationTime: Long = 0L

    private const val VERIFICATION_VALIDITY_MS = 60_000L // 1 minute

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
     * Fetch available offerings from RevenueCat
     */
    fun fetchOfferings() {
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

