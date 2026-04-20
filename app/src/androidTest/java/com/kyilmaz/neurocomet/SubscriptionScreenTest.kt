package com.kyilmaz.neurocomet

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Compose UI tests for [SubscriptionScreen].
 *
 * These run on a real device / emulator and verify:
 *  • The paywall renders with both plan cards, subscribe button, and restore link
 *  • Debug test buttons appear in DEBUG builds
 *  • Simulated success transitions the UI to the "You're Premium" screen
 *  • Simulated decline shows the error dialog
 *  • Reset returns the UI to the paywall
 *
 * All tests run in test mode — no real billing or RevenueCat calls are made.
 */
@RunWith(AndroidJUnit4::class)
class SubscriptionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        SubscriptionManager.setTestMode(true)
        SubscriptionManager.resetTestPurchase()
    }

    @After
    fun tearDown() {
        SubscriptionManager.resetTestPurchase()
    }

    // ── helpers ──

    private fun setContent() {
        composeTestRule.setContent {
            SubscriptionScreen(
                onBack = {},
                onPurchaseSuccess = {}
            )
        }
        composeTestRule.waitForIdle()
    }

    /**
     * Waits until the test-mode offerings simulation finishes (the 400 ms
     * internal delay inside [SubscriptionManager.fetchOfferings]).
     * Once loading is done, the subscribe button text becomes visible.
     */
    private fun waitForOfferingsLoaded() {
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule
                .onAllNodesWithText("Subscribe", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * Scrolls the paywall column until the node matching [matcher] is visible.
     */
    private fun scrollTo(matcher: SemanticsMatcher) {
        composeTestRule
            .onNode(hasScrollAction())
            .performScrollToNode(matcher)
        composeTestRule.waitForIdle()
    }

    // ═══════════════════════════════════════════════════════════════
    // 1. PAYWALL RENDERS CORRECTLY
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun paywall_displaysGoTitle() {
        setContent()
        composeTestRule.onNodeWithText("Go Premium").assertIsDisplayed()
    }

    @Test
    fun paywall_showsSubscribeButton() {
        setContent()
        waitForOfferingsLoaded()
        // After loading finishes the button shows "Subscribe Monthly"
        composeTestRule.onNodeWithText("Subscribe Monthly").assertExists()
    }

    @Test
    fun paywall_showsRestorePurchasesLink() {
        setContent()
        waitForOfferingsLoaded()
        scrollTo(hasText("Restore Purchases"))
        composeTestRule.onNodeWithText("Restore Purchases").assertIsDisplayed()
    }

    @Test
    fun paywall_showsFallbackPrices_inTestMode() {
        setContent()
        // Test mode provides null packages, so the UI shows hard-coded fallbacks
        composeTestRule.onNodeWithText("$2.00").assertExists()
        composeTestRule.onNodeWithText("$60.00").assertExists()
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. DEBUG TEST BUTTONS (only in debug builds)
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun debugButtons_areVisible() {
        if (!BuildConfig.DEBUG) return

        setContent()
        waitForOfferingsLoaded()

        // Debug buttons are at the very bottom — scroll to "Reset State"
        scrollTo(hasText("Reset State", substring = true))
        composeTestRule.onNodeWithText("Reset State", substring = true).assertIsDisplayed()

        // The other buttons are in the same region
        composeTestRule.onNodeWithText("Success", substring = true).assertExists()
        composeTestRule.onNodeWithText("Declined", substring = true).assertExists()
        composeTestRule.onNodeWithText("Timed Out", substring = true).assertExists()
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. SIMULATE SUCCESS → SUCCESS DIALOG
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun simulateSuccess_showsSuccessDialog() {
        setContent()

        // Trigger simulated success directly from the manager
        SubscriptionManager.simulateTestSuccess()

        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule
                .onAllNodesWithText("Payment successful", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. SIMULATE DECLINED → ERROR DIALOG (via debug button)
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun simulateDeclined_showsDeclinedDialog() {
        if (!BuildConfig.DEBUG) return

        setContent()
        waitForOfferingsLoaded()

        // Scroll to and tap the "Declined" debug button
        scrollTo(hasText("Declined", substring = true))
        composeTestRule
            .onNodeWithText("Declined", substring = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule
                .onAllNodesWithText("Payment declined", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 5. SIMULATE TIMED OUT → TIMED OUT DIALOG (via debug button)
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun simulateTimedOut_showsTimedOutDialog() {
        if (!BuildConfig.DEBUG) return

        setContent()
        waitForOfferingsLoaded()

        // Scroll to and tap the "Timed Out" debug button
        scrollTo(hasText("Timed Out", substring = true))
        composeTestRule
            .onNodeWithText("Timed Out", substring = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule
                .onAllNodesWithText("Timed-Out", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 6. RESET → BACK TO PAYWALL
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun resetAfterSuccess_returnsToPaywall() {
        setContent()

        // Simulate success from the manager side
        SubscriptionManager.simulateTestSuccess()
        composeTestRule.waitForIdle()

        // Reset from the manager
        SubscriptionManager.resetTestPurchase()
        SubscriptionManager.clearPurchaseSuccess()
        composeTestRule.waitForIdle()

        // The paywall title should be visible again after recomposition
        composeTestRule.onNodeWithText("Go Premium").assertExists()
    }

    // ═══════════════════════════════════════════════════════════════
    // 7. PLAN SELECTION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun selectLifetimePlan_changesButtonText() {
        setContent()
        waitForOfferingsLoaded()

        // Tap the lifetime price to select that plan
        composeTestRule.onNodeWithText("$60.00").performClick()
        composeTestRule.waitForIdle()

        // The button should now mention "Lifetime"
        composeTestRule
            .onAllNodesWithText("Lifetime", substring = true)
            .fetchSemanticsNodes()
            .also { assert(it.isNotEmpty()) { "Expected lifetime plan UI" } }
    }
}
