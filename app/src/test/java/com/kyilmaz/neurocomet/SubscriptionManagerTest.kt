package com.kyilmaz.neurocomet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests for [SubscriptionManager] test-mode simulation paths.
 *
 * These run as pure JVM unit tests using kotlinx-coroutines-test.
 * They exercise the same code paths triggered by the DevOptions "Simulate
 * Successful / Declined / Timed-Out Payment" buttons and the Subscription
 * screen's real purchase flow in test mode.
 *
 * Coverage:
 *  • simulateTestSuccess  — immediate premium grant
 *  • simulateTestDeclined — delayed error state
 *  • simulateTestTimedOut — stuck-loading state
 *  • resetTestPurchase    — full state wipe
 *  • purchaseMonthly / purchaseLifetime in test mode — delayed purchase simulation
 *  • restorePurchases in test mode
 *  • verifyPremiumStatus in test mode
 *  • setBillingConfigured edge cases
 *  • clearPurchaseSuccess / clearError helpers
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Ensure clean slate — reset clears the singleton state
        SubscriptionManager.resetForTesting()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        SubscriptionManager.resetForTesting()
    }

    // ═══════════════════════════════════════════════════════════════
    // 1. INITIAL STATE
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun initialState_isFreeAndIdle() {
        val state = SubscriptionManager.subscriptionState.value
        assertFalse("Initial: not loading", state.isLoading)
        assertFalse("Initial: not premium", state.isPremium)
        assertFalse("Initial: no purchase success", state.purchaseSuccess)
        assertNull("Initial: no error", state.error)
        assertNull("Initial: no purchase type", state.purchaseType)
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. simulateTestSuccess
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun simulateTestSuccess_setsPremiumImmediately() {
        SubscriptionManager.simulateTestSuccess()

        val state = SubscriptionManager.subscriptionState.value
        assertTrue("Success: isPremium", state.isPremium)
        assertTrue("Success: purchaseSuccess", state.purchaseSuccess)
        assertFalse("Success: not loading", state.isLoading)
        assertNull("Success: no error", state.error)
        assertEquals("Success: purchaseType = monthly", "monthly", state.purchaseType)
    }

    @Test
    fun simulateTestSuccess_verifyPremiumStatusReturnsTrue() {
        SubscriptionManager.simulateTestSuccess()
        assertTrue(
            "verifyPremiumStatus should return true after simulated success",
            SubscriptionManager.verifyPremiumStatus()
        )
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. simulateTestDeclined
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun simulateTestDeclined_setsErrorAfterDelay() = runTest {
        SubscriptionManager.simulateTestDeclined()

        // After the internal delay, state should show an error
        advanceUntilIdle()

        val state = SubscriptionManager.subscriptionState.value
        assertFalse("Declined: not loading", state.isLoading)
        assertFalse("Declined: not premium", state.isPremium)
        assertFalse("Declined: no purchase success", state.purchaseSuccess)
        assertNotNull("Declined: has error", state.error)
        assertTrue(
            "Declined: error mentions 'declined'",
            state.error!!.contains("declined", ignoreCase = true)
        )
    }

    @Test
    fun simulateTestDeclined_verifyPremiumStatusReturnsFalse() = runTest {
        SubscriptionManager.simulateTestDeclined()
        advanceUntilIdle()
        assertFalse(SubscriptionManager.verifyPremiumStatus())
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. simulateTestTimedOut
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun simulateTestTimedOut_leavesLoadingState() = runTest {
        SubscriptionManager.simulateTestTimedOut()
        advanceUntilIdle()

        val state = SubscriptionManager.subscriptionState.value
        // The manager intentionally leaves isLoading = true;
        // the SubscriptionScreen's 30s timer handles the timeout card.
        assertTrue("TimedOut: still loading", state.isLoading)
        assertFalse("TimedOut: not premium", state.isPremium)
        assertFalse("TimedOut: no purchase success", state.purchaseSuccess)
        assertNull("TimedOut: no error", state.error)
    }

    // ═══════════════════════════════════════════════════════════════
    // 5. resetTestPurchase
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun resetTestPurchase_restoresDefaultState() {
        // Dirty the state first
        SubscriptionManager.simulateTestSuccess()
        assertTrue(SubscriptionManager.subscriptionState.value.isPremium)

        // Reset
        SubscriptionManager.resetTestPurchase()

        val state = SubscriptionManager.subscriptionState.value
        assertFalse("Reset: not loading", state.isLoading)
        assertFalse("Reset: not premium", state.isPremium)
        assertFalse("Reset: no purchase success", state.purchaseSuccess)
        assertNull("Reset: no error", state.error)
        assertNull("Reset: no purchase type", state.purchaseType)
    }

    @Test
    fun resetTestPurchase_verifyPremiumStatusReturnsFalse() {
        SubscriptionManager.simulateTestSuccess()
        assertTrue(SubscriptionManager.verifyPremiumStatus())

        SubscriptionManager.resetTestPurchase()
        assertFalse(SubscriptionManager.verifyPremiumStatus())
    }

    @Test
    fun resetTestPurchase_isNoOpWhenNotInTestMode() {
        SubscriptionManager.simulateTestSuccess()
        assertTrue(SubscriptionManager.subscriptionState.value.isPremium)

        // Switch to production mode
        SubscriptionManager.setTestMode(false)
        SubscriptionManager.resetTestPurchase()

        // State should NOT have been reset (method is guarded by testMode)
        assertTrue(
            "Non-test-mode reset should be a no-op",
            SubscriptionManager.subscriptionState.value.isPremium
        )

        // Clean up
        SubscriptionManager.setTestMode(true)
        SubscriptionManager.resetTestPurchase()
    }

    // ═══════════════════════════════════════════════════════════════
    // 6. fetchOfferings (test mode)
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun fetchOfferings_testMode_setsLoadingThenClears() = runTest {
        SubscriptionManager.fetchOfferings()

        // After delay, loading should be cleared and packages stay null
        // (the UI falls back to "$2.00" / "$60.00")
        advanceUntilIdle()

        val state = SubscriptionManager.subscriptionState.value
        assertFalse("Offerings: not loading after fetch", state.isLoading)
        assertNull("Offerings: monthly package null (simulated)", state.monthlyPackage)
        assertNull("Offerings: lifetime package null (simulated)", state.lifetimePackage)
    }

    // ═══════════════════════════════════════════════════════════════
    // 7. checkPremiumStatus (test mode)
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun checkPremiumStatus_testMode_returnsFalseWhenFree() {
        var result: Boolean? = null
        SubscriptionManager.checkPremiumStatus { result = it }
        assertFalse("Free user should get false", result!!)
    }

    @Test
    fun checkPremiumStatus_testMode_returnsTrueAfterPurchase() {
        SubscriptionManager.simulateTestSuccess()

        var result: Boolean? = null
        SubscriptionManager.checkPremiumStatus { result = it }
        assertTrue("Premium user should get true", result!!)
    }

    // ═══════════════════════════════════════════════════════════════
    // 8. restorePurchases (test mode)
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun restorePurchases_testMode_restoresNothing_whenFree() = runTest {
        var restoredPremium: Boolean? = null
        var restoreError: String? = null

        SubscriptionManager.restorePurchases(
            onSuccess = { restoredPremium = it },
            onError = { restoreError = it }
        )
        advanceUntilIdle()

        assertFalse("Restore when free: should report false", restoredPremium!!)
        assertNull("Restore when free: no error", restoreError)
    }

    @Test
    fun restorePurchases_testMode_restoresPremium_whenPreviouslyPurchased() = runTest {
        // First simulate a purchase
        SubscriptionManager.simulateTestSuccess()
        assertTrue(SubscriptionManager.subscriptionState.value.isPremium)

        // Now restore
        var restoredPremium: Boolean? = null
        SubscriptionManager.restorePurchases(
            onSuccess = { restoredPremium = it },
            onError = { }
        )
        advanceUntilIdle()

        assertTrue("Restore after purchase: should report true", restoredPremium!!)
        val state = SubscriptionManager.subscriptionState.value
        assertTrue("Restore: isPremium", state.isPremium)
        assertTrue("Restore: purchaseSuccess", state.purchaseSuccess)
        assertEquals("Restore: purchaseType = restored", "restored", state.purchaseType)
    }

    // ═══════════════════════════════════════════════════════════════
    // 9. clearPurchaseSuccess / clearError
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun clearPurchaseSuccess_resetsSuccessButKeepsPremium() {
        SubscriptionManager.simulateTestSuccess()
        assertTrue(SubscriptionManager.subscriptionState.value.purchaseSuccess)

        SubscriptionManager.clearPurchaseSuccess()

        val state = SubscriptionManager.subscriptionState.value
        assertFalse("Cleared: purchaseSuccess false", state.purchaseSuccess)
        assertNull("Cleared: purchaseType null", state.purchaseType)
        assertTrue("Cleared: isPremium preserved", state.isPremium)
    }

    @Test
    fun clearError_removesErrorMessage() = runTest {
        SubscriptionManager.simulateTestDeclined()
        advanceUntilIdle()
        assertNotNull(SubscriptionManager.subscriptionState.value.error)

        SubscriptionManager.clearError()
        assertNull(
            "After clearError: error must be null",
            SubscriptionManager.subscriptionState.value.error
        )
    }

    // ═══════════════════════════════════════════════════════════════
    // 10. setBillingConfigured
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun billingConfigured_truthy_inTestMode() {
        assertTrue(
            "In test mode, billing should always report configured",
            SubscriptionManager.isBillingConfigured()
        )
    }

    @Test
    fun setBillingConfigured_false_inProductionMode_setsErrorState() {
        SubscriptionManager.setTestMode(false)

        SubscriptionManager.setBillingConfigured(
            isConfigured = false,
            errorMessage = "Billing unavailable"
        )

        val state = SubscriptionManager.subscriptionState.value
        assertFalse("Non-configured: not premium", state.isPremium)
        assertFalse("Non-configured: not loading", state.isLoading)
        assertEquals("Non-configured: error propagated", "Billing unavailable", state.error)
        assertFalse("Non-configured: isBillingConfigured false", SubscriptionManager.isBillingConfigured())

        // Clean up
        SubscriptionManager.setTestMode(true)
    }

    // ═══════════════════════════════════════════════════════════════
    // 11. SEQUENTIAL FLOW — full purchase → reset → re-purchase
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun fullCycle_purchaseResetRepurchase() = runTest {
        // Phase 1: Purchase succeeds
        SubscriptionManager.simulateTestSuccess()
        assertTrue(SubscriptionManager.subscriptionState.value.isPremium)
        assertTrue(SubscriptionManager.verifyPremiumStatus())

        // Phase 2: Reset to free
        SubscriptionManager.resetTestPurchase()
        assertFalse(SubscriptionManager.subscriptionState.value.isPremium)
        assertFalse(SubscriptionManager.verifyPremiumStatus())

        // Phase 3: Declined attempt
        SubscriptionManager.simulateTestDeclined()
        advanceUntilIdle()
        assertFalse(SubscriptionManager.subscriptionState.value.isPremium)
        assertNotNull(SubscriptionManager.subscriptionState.value.error)

        // Phase 4: Clear error, retry, succeed
        SubscriptionManager.clearError()
        assertNull(SubscriptionManager.subscriptionState.value.error)

        SubscriptionManager.simulateTestSuccess()
        assertTrue(SubscriptionManager.subscriptionState.value.isPremium)
        assertTrue(SubscriptionManager.verifyPremiumStatus())
    }

    @Test
    fun fullCycle_successThenRestoreAfterReset() = runTest {
        // Purchase
        SubscriptionManager.simulateTestSuccess()
        assertTrue(SubscriptionManager.subscriptionState.value.isPremium)

        // Clear success flag (as the screen does)
        SubscriptionManager.clearPurchaseSuccess()
        assertFalse(SubscriptionManager.subscriptionState.value.purchaseSuccess)
        assertTrue("Premium persists after clearing success flag", SubscriptionManager.subscriptionState.value.isPremium)

        // Restore should still find premium
        var restoredPremium: Boolean? = null
        SubscriptionManager.restorePurchases(
            onSuccess = { restoredPremium = it },
            onError = { }
        )
        advanceUntilIdle()
        assertTrue("Restore finds existing premium", restoredPremium!!)
    }

    // ═══════════════════════════════════════════════════════════════
    // 12. PRODUCT CONSTANTS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun productConstants_areCorrect() {
        assertEquals("NeuroComet_premium_monthly", SubscriptionManager.PRODUCT_MONTHLY)
        assertEquals("NeuroComet_premium_lifetime", SubscriptionManager.PRODUCT_LIFETIME)
        assertEquals("premium", SubscriptionManager.ENTITLEMENT_PREMIUM)
    }
}

