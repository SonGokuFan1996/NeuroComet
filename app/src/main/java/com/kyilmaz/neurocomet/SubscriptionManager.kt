package com.kyilmaz.neurocomet

import android.app.Activity
import android.content.Context
import android.util.Log
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.models.StoreTransaction
import com.kyilmaz.neurocomet.ads.GoogleAdsManager
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.put
import java.security.MessageDigest
import java.util.Properties

/**
 * Manages RevenueCat subscriptions and premium state for the application.
 * Provides security verification, offering fetching, and purchase flows.
 */
object SubscriptionManager {
    private const val TAG = "SubscriptionManager"
    private const val BILLING_UNAVAILABLE_MESSAGE = "Billing service is not initialized."

    /**
     * If true, uses simulated responses for purchases instead of real RevenueCat/Play calls.
     * Useful for UI testing and layout verification.
     */
    @Volatile
    var testMode: Boolean = false
        private set

    /** Coroutine scope used by test-mode helpers to simulate async delays. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Tracks whether the simulated user has "purchased" premium in test mode. */
    @Volatile
    private var isTestPremium = false

    // RevenueCat product identifiers (configured in RevenueCat dashboard)
    // We search for all case variations as provided in the dashboard JSON and labels
    const val PRODUCT_MONTHLY = "NeuroComet_premium_monthly"
    const val PRODUCT_LIFETIME = "NeuroComet_premium_lifetime"

    private val MONTHLY_IDS = listOf("neurocomet_monthly_pro", PRODUCT_MONTHLY, "neurocomet_premium_monthly", "\$rc_monthly", "\$monthly")
    private val LIFETIME_IDS = listOf("neurocomet_lifetime_pro", PRODUCT_LIFETIME, "neurocomet_premium_lifetime", "\$rc_lifetime", "\$lifetime")

    // Entitlement identifier
    const val ENTITLEMENT_PREMIUM = "premium"

    // Security: Verification token for premium status
    private const val SECURITY_SALT = "NeuroComet_Secure_Salt_v1"
    
    @Volatile
    private var verificationToken: String? = null

    @Volatile
    private var lastVerificationTime: Long = 0L
    private const val VERIFICATION_VALIDITY_MS = 3600_000L // 1 hour

    @Volatile
    private var lastCustomerInfo: CustomerInfo? = null

    @Volatile
    private var isBillingConfigured: Boolean = false

    private val _subscriptionState = MutableStateFlow(SubscriptionState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState

    data class SubscriptionState(
        val isLoading: Boolean = false,
        val isPremium: Boolean = false,
        val offerings: Offerings? = null,
        val currentOffering: Offering? = null,
        val monthlyPackage: Package? = null,
        val lifetimePackage: Package? = null,
        val availableProducts: List<com.revenuecat.purchases.models.StoreProduct> = emptyList(),
        val error: String? = null,
        val purchaseSuccess: Boolean = false,
        val purchaseType: String? = null // "monthly" or "lifetime"
    )

    private fun generateVerificationToken(info: CustomerInfo): String {
        val isActive = info.entitlements[ENTITLEMENT_PREMIUM]?.isActive == true
        val userId = info.originalAppUserId
        val raw = "NC_PREM_${userId}_${isActive}_$SECURITY_SALT"
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(raw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Checks if the user is authorized for premium features.
     * Re-verifies against the RevenueCat state if the token is expired.
     */
    fun verifyPremiumStatus(): Boolean {
        if (testMode) return isTestPremium
        
        val currentState = _subscriptionState.value
        if (!currentState.isPremium) return false

        val currentToken = verificationToken ?: return false
        val info = lastCustomerInfo ?: return currentState.isPremium // Fallback to state flow if info is lost

        // Verify the token matches the current info and hasn't been tampered with
        val expectedToken = generateVerificationToken(info)
        val matches = currentToken == expectedToken

        val now = System.currentTimeMillis()
        if (now - lastVerificationTime > VERIFICATION_VALIDITY_MS) {
            // Expired - in a production app we might trigger a background refresh here
            Log.d(TAG, "Verification token expired, relying on last known state")
        }
        
        if (!matches) {
            Log.e(TAG, "SECURITY ALERT: Premium verification token mismatch!")
        }

        return matches && currentState.isPremium
    }

    /**
     * Throws SecurityException if premium is not active.
     */
    fun enforcePremiumSecurity() {
        if (!verifyPremiumStatus()) {
            Log.e(TAG, "SECURITY ALERT: Unauthorized access to premium feature!")
            throw SecurityException("Feature requires active Premium subscription.")
        }
    }

    /**
     * Initialize RevenueCat SDK
     */
    fun initialize(debug: Boolean = false) {
        if (debug) {
            Purchases.logLevel = LogLevel.DEBUG
        }
        Log.d(TAG, "SubscriptionManager initialized (v182)")
    }

    /**
     * Updates the local state from RevenueCat's CustomerInfo.
     */
    private fun updateStateFromCustomerInfo(info: CustomerInfo) {
        lastCustomerInfo = info
        val activePremium = info.entitlements[ENTITLEMENT_PREMIUM]?.isActive == true
        
        _subscriptionState.value = _subscriptionState.value.copy(
            isPremium = activePremium,
            isLoading = false
        )
        
        verificationToken = generateVerificationToken(info)
        lastVerificationTime = System.currentTimeMillis()
        
        Log.d(TAG, "Premium status updated: $activePremium (User: ${info.originalAppUserId})")

        // SYNC TO SUPABASE: Update the server-side premium flag
        syncPremiumStatusToSupabase(activePremium)
    }

    private fun syncPremiumStatusToSupabase(isPremium: Boolean) {
        val client = AppSupabaseClient.client ?: return
        val userId = try { client.auth.currentUserOrNull()?.id } catch (_: Exception) { null } ?: return
        
        scope.launch(Dispatchers.IO) {
            try {
                val payload = kotlinx.serialization.json.buildJsonObject {
                    put("is_premium", isPremium)
                    put("updated_at", java.time.Instant.now().toString())
                }
                // Update both tables for consistency
                safeUpdate("users", payload, "id=eq.$userId")
                safeUpdate("profiles", payload, "id=eq.$userId")
                Log.d(TAG, "Successfully synced premium status ($isPremium) to Supabase")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync premium status to Supabase: ${e.message}")
            }
        }
    }

    fun setBillingConfigured(isConfigured: Boolean, errorMessage: String? = null) {
        this.isBillingConfigured = isConfigured
        if (errorMessage != null) {
            _subscriptionState.value = _subscriptionState.value.copy(error = errorMessage)
        }
    }

    fun isBillingConfigured(): Boolean = isBillingConfigured

    private fun requireBillingConfigured(onError: ((String) -> Unit)? = null): Boolean {
        if (!isBillingConfigured) {
            Log.e(TAG, BILLING_UNAVAILABLE_MESSAGE)
            onError?.invoke(BILLING_UNAVAILABLE_MESSAGE)
            return false
        }
        return true
    }

    /**
     * Fetches available products from RevenueCat
     */
    fun fetchOfferings() {
        if (!requireBillingConfigured()) return

        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)

        // 1. Fetch Offerings
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                Log.d(TAG, "Offerings fetched successfully. Current: ${offerings.current?.identifier}")
                
                val currentOffering = offerings.current
                val allPackages = offerings.all.values.flatMap { it.availablePackages }
                
                var monthlyPkg: Package? = offerings.current?.monthly
                    ?: allPackages.find { pkg -> 
                        MONTHLY_IDS.any { id -> 
                            pkg.identifier.equals(id, ignoreCase = true) || 
                            pkg.product.id.startsWith(id, ignoreCase = true) 
                        }
                    }

                var lifetimePkg: Package? = offerings.current?.lifetime
                    ?: allPackages.find { pkg -> 
                        LIFETIME_IDS.any { id -> 
                            pkg.identifier.equals(id, ignoreCase = true) || 
                            pkg.product.id.startsWith(id, ignoreCase = true) 
                        }
                    }
                
                if (lifetimePkg == null) {
                    lifetimePkg = allPackages.find { pkg ->
                        pkg.packageType == PackageType.LIFETIME || 
                        pkg.identifier.contains("lifetime", ignoreCase = true) ||
                        pkg.product.id.contains("lifetime", ignoreCase = true)
                    }
                }

                _subscriptionState.value = _subscriptionState.value.copy(
                    offerings = offerings,
                    currentOffering = currentOffering,
                    monthlyPackage = monthlyPkg,
                    lifetimePackage = lifetimePkg
                )

                // 2. Also fetch products directly as a backup
                fetchProductsDirectly()
            }

            override fun onError(error: PurchasesError) {
                val fullError = "${error.message} (Underlying: ${error.underlyingErrorMessage})"
                Log.e(TAG, "Error fetching offerings: $fullError")
                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false,
                    error = fullError
                )
                fetchProductsDirectly()
            }
        })
    }

    private fun fetchProductsDirectly() {
        val productIds = MONTHLY_IDS + LIFETIME_IDS
        
        // Fetch both subscriptions and in-app products to be safe
        val allProducts = mutableListOf<com.revenuecat.purchases.models.StoreProduct>()
        var pendingRequests = 2

        val callback = object : com.revenuecat.purchases.interfaces.GetStoreProductsCallback {
            override fun onReceived(storeProducts: List<com.revenuecat.purchases.models.StoreProduct>) {
                synchronized(allProducts) {
                    allProducts.addAll(storeProducts)
                    pendingRequests--
                    if (pendingRequests == 0) {
                        updateStateWithDirectProducts(allProducts)
                    }
                }
            }

            override fun onError(error: PurchasesError) {
                Log.w(TAG, "Error fetching products directly: ${error.message}")
                synchronized(allProducts) {
                    pendingRequests--
                    if (pendingRequests == 0) {
                        updateStateWithDirectProducts(allProducts)
                    }
                }
            }
        }

        Purchases.sharedInstance.getProducts(productIds, ProductType.SUBS, callback)
        Purchases.sharedInstance.getProducts(productIds, ProductType.INAPP, callback)
    }

    private fun updateStateWithDirectProducts(storeProducts: List<com.revenuecat.purchases.models.StoreProduct>) {
        Log.d(TAG, "All direct products fetched: ${storeProducts.map { "${it.id} (${it.type})" }}")
        
        _subscriptionState.value = _subscriptionState.value.copy(
            isLoading = false,
            availableProducts = storeProducts
        )
    }

    /**
     * Checks current premium status from RevenueCat
     */
    fun checkPremiumStatus(onResult: (Boolean) -> Unit) {
        if (testMode) {
            onResult(isTestPremium)
            return
        }

        if (!requireBillingConfigured()) {
            onResult(false)
            return
        }

        try {
        Purchases.sharedInstance.getCustomerInfo(
            object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    updateStateFromCustomerInfo(customerInfo)
                    onResult(customerInfo.entitlements[ENTITLEMENT_PREMIUM]?.isActive == true)
                }

                override fun onError(error: PurchasesError) {
                    val fullError = "${error.message} (Underlying: ${error.underlyingErrorMessage})"
                    Log.e(TAG, "Error checking premium status: $fullError")
                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        error = fullError
                    )
                    verificationToken = null
                    onResult(false)
                }
            }
        )
        } catch (e: Exception) {
            Log.e(TAG, "Exception checking premium status", e)
            _subscriptionState.value = _subscriptionState.value.copy(
                isLoading = false,
                error = e.message
            )
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
        val state = _subscriptionState.value
        val offerings = state.offerings
        
        Log.d(TAG, "LIFETIME PURCHASE ATTEMPT STARTED")

        if (testMode) {
            simulateTestPurchase("lifetime", onSuccess)
            return
        }

        if (!requireBillingConfigured(onError)) return

        var pkg = _subscriptionState.value.lifetimePackage
        
        // Final fallback: Case-insensitive search across everything for any known lifetime ID
        if (pkg == null && offerings != null) {
            pkg = offerings.all.values.flatMap { it.availablePackages }.find { p ->
                p.packageType == PackageType.LIFETIME ||
                LIFETIME_IDS.any { id -> p.identifier.equals(id, ignoreCase = true) || p.product.id.startsWith(id, ignoreCase = true) }
            }
        }

        if (pkg != null) {
            Log.d(TAG, "Proceeding with lifetime purchase (via package): ${pkg.product.id}")
            purchasePackage(activity, pkg, "lifetime", onSuccess, onError)
            return
        }

        // --- NEW FALLBACK: Purchase product directly if package is missing ---
        val prod = state.availableProducts.find { p ->
            LIFETIME_IDS.any { id -> p.id.startsWith(id, ignoreCase = true) }
        }

        if (prod != null) {
            Log.d(TAG, "Proceeding with lifetime purchase (via direct product): ${prod.id}")
            purchaseProduct(activity, prod, "lifetime", onSuccess, onError)
            return
        }

        val foundIds = (offerings?.all?.values?.flatMap { it.availablePackages }?.map { "${it.identifier}(${it.product.id})" } ?: emptyList()) +
                        state.availableProducts.map { it.id }
        
        val errorMsg = "Lifetime plan not found. Found: $foundIds. Please check RevenueCat dashboard offerings."
        Log.e(TAG, errorMsg)
        
        _subscriptionState.value = _subscriptionState.value.copy(
            isLoading = false,
            error = errorMsg
        )
        
        android.widget.Toast.makeText(activity, "Error: Lifetime plan not available. Check dashboard.", android.widget.Toast.LENGTH_LONG).show()
        onError(errorMsg)
    }

    /**
     * Internal direct product purchase logic
     */
    fun purchaseProduct(
        activity: Activity,
        product: com.revenuecat.purchases.models.StoreProduct,
        purchaseType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!requireBillingConfigured(onError)) return

        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)

        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(activity, product).build(),
            object : PurchaseCallback {
                override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        purchaseSuccess = true,
                        purchaseType = purchaseType
                    )
                    updateStateFromCustomerInfo(customerInfo)
                    onSuccess()
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    val fullError = "${error.message} (Underlying: ${error.underlyingErrorMessage})"
                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        error = if (userCancelled) null else fullError
                    )
                    
                    if (userCancelled) {
                        Log.d(TAG, "Purchase cancelled by user")
                        onError("USER_CANCELLED")
                    } else {
                        Log.e(TAG, "Purchase error: $fullError")
                        onError(fullError)
                    }
                }
            }
        )
    }

    /**
     * Internal purchase logic
     */
    fun purchasePackage(
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

                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        purchaseSuccess = true,
                        purchaseType = purchaseType
                    )
                    
                    updateStateFromCustomerInfo(customerInfo)
                    Log.d(TAG, "Purchase successful! Premium: $isPremium, Type: $purchaseType")
                    onSuccess()
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    val fullError = "${error.message} (Underlying: ${error.underlyingErrorMessage})"
                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        error = if (userCancelled) null else fullError
                    )
                    
                    if (userCancelled) {
                        Log.d(TAG, "Purchase cancelled by user")
                        onError("USER_CANCELLED")
                    } else {
                        Log.e(TAG, "Purchase error: $fullError")
                        onError(fullError)
                    }
                }
            }
        )
    }

    /**
     * Restore purchases
     */
    fun restorePurchases(onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        if (!requireBillingConfigured(onError)) return

        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)

        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                updateStateFromCustomerInfo(customerInfo)
                val isPremium = customerInfo.entitlements[ENTITLEMENT_PREMIUM]?.isActive == true
                
                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false,
                    purchaseSuccess = isPremium,
                    purchaseType = "restored"
                )
                
                Log.d(TAG, "Purchases restored. Premium: $isPremium")
                onSuccess(isPremium)
            }

            override fun onError(error: PurchasesError) {
                val fullError = "${error.message} (Underlying: ${error.underlyingErrorMessage})"
                Log.e(TAG, "Restore error: $fullError")
                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false,
                    error = fullError
                )
                onError(fullError)
            }
        })
    }

    // ── Test Mode Helpers ─────────────────────────────────────

    fun simulateTestSuccess(type: String = "debug") {
        simulateTestPurchase(type) {}
    }

    fun simulateTestPurchase(type: String, onDone: () -> Unit) {
        testMode = true
        isTestPremium = true
        _subscriptionState.value = _subscriptionState.value.copy(
            isLoading = false,
            isPremium = true,
            purchaseSuccess = true,
            purchaseType = type
        )
        Log.d(TAG, "🧪 TEST: Simulated SUCCESS ($type) (immediate premium applied)")
        
        // Refresh AdMob to hide ads immediately in UI
        try {
            GoogleAdsManager.devSetSimulatePremium(true)
        } catch (e: Exception) {
            Log.w(TAG, "Could not trigger GoogleAdsManager update after simulated purchase.")
        }
        
        onDone()
    }

    fun simulateTestDeclined() {
        testMode = true
        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)
        scope.launch {
            kotlinx.coroutines.delay(1500)
            _subscriptionState.value = _subscriptionState.value.copy(
                isLoading = false,
                error = "Simulated: Payment declined by test bank."
            )
            Log.d(TAG, "🧪 TEST: Simulated DECLINED")
        }
    }

    fun simulateTestTimedOut() {
        testMode = true
        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)
        scope.launch {
            kotlinx.coroutines.delay(3000)
            _subscriptionState.value = _subscriptionState.value.copy(isLoading = false)
            Log.d(TAG, "🧪 TEST: Simulated TIMED_OUT (no response)")
        }
    }

    fun resetTestPurchase() {
        isTestPremium = false
        _subscriptionState.value = _subscriptionState.value.copy(
            isPremium = false,
            purchaseSuccess = false,
            error = null
        )
        Log.d(TAG, "🧪 TEST: Simulated Premium RESET")
    }

    fun setTestMode(enabled: Boolean) {
        testMode = enabled
        if (!enabled) resetTestPurchase()
        Log.d(TAG, "🧪 TEST: Test mode ${if (enabled) "ENABLED" else "DISABLED"}")
    }

    fun clearPurchaseSuccess() {
        _subscriptionState.value = _subscriptionState.value.copy(purchaseSuccess = false)
    }

    fun clearError() {
        _subscriptionState.value = _subscriptionState.value.copy(error = null)
    }

    /**
     * Resets the manager to its initial state. For use in unit tests only.
     */
    fun resetForTesting() {
        testMode = false
        isTestPremium = false
        isBillingConfigured = false
        verificationToken = null
        lastVerificationTime = 0L
        lastCustomerInfo = null
        _subscriptionState.value = SubscriptionState()
    }
}
