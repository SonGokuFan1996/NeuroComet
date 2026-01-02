# In-App Purchases Setup Guide for NeuroComet

Your app already has a complete RevenueCat integration! Here's how to make it work in production.

## Current Implementation Status ✅

Your app already includes:
- `SubscriptionManager.kt` - Handles all purchase logic with security verification
- `SubscriptionScreen.kt` - Beautiful premium subscription UI
- RevenueCat SDK integration
- Two products: Monthly ($2/month) and Lifetime ($60 one-time)

---

## Step 1: Set Up RevenueCat Account

1. **Create a RevenueCat account**: https://app.revenuecat.com
2. **Create a new project** for NeuroComet
3. **Get your API key** from Project Settings → API Keys

### Update your API key in `MainActivity.kt`:
```kotlin
// Find this line around line 885:
Purchases.configure(PurchasesConfiguration.Builder(this, "test_ghfalVJOgCZfjWpsJdiyCbHARmz").build())

// Replace with your actual RevenueCat public API key:
Purchases.configure(PurchasesConfiguration.Builder(this, "YOUR_REVENUECAT_API_KEY").build())
```

---

## Step 2: Set Up Google Play Console

### A. Create Your Products

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app → **Monetization** → **Products**

#### For Monthly Subscription:
- Product ID: `NeuroComet_premium_monthly`
- Product Type: **Subscription**
- Name: "NeuroComet Premium Monthly"
- Price: $2.00/month
- Benefits:
  - Ad-free experience
  - Premium features
  - Priority support

#### For Lifetime Purchase:
- Product ID: `NeuroComet_premium_lifetime`  
- Product Type: **In-app product** (one-time purchase)
- Name: "NeuroComet Premium Lifetime"
- Price: $60.00
- Benefits:
  - Permanent ad-free access
  - All premium features forever

### B. Connect Google Play to RevenueCat

1. In Google Play Console → **Monetization setup**
2. Create a Service Account with Play Developer API access
3. Download the JSON credentials
4. In RevenueCat → **Apps** → Your Android app → **Configuration**
5. Upload the service account credentials

---

## Step 3: Configure RevenueCat Products

### A. Create Products in RevenueCat

1. Go to RevenueCat Dashboard → **Products**
2. **Add Product** for each:

| Identifier | Store Product ID | Type |
|------------|-----------------|------|
| `NeuroComet_premium_monthly` | `NeuroComet_premium_monthly` | Subscription |
| `NeuroComet_premium_lifetime` | `NeuroComet_premium_lifetime` | Non-consumable |

### B. Create Entitlement

1. Go to **Entitlements**
2. Create entitlement: `premium`
3. Add both products to this entitlement

### C. Create Offering

1. Go to **Offerings**
2. Create offering: `default`
3. Add packages:
   - `$rc_monthly` → `NeuroComet_premium_monthly`
   - `$rc_lifetime` → `NeuroComet_premium_lifetime`

---

## Step 4: Testing

### A. Add Test Users

1. In Google Play Console → **Settings** → **License testing**
2. Add your test Gmail accounts
3. Test purchases won't charge your card

### B. Test in App

1. Build and run the app
2. Go to **Settings** → **Go Premium**
3. Select a plan and complete the test purchase
4. Check RevenueCat dashboard for the transaction

---

## Step 5: Security Features (Already Implemented!)

Your `SubscriptionManager.kt` includes anti-piracy measures:

```kotlin
// Verification token ensures purchases are legitimate
private fun generateVerificationToken(customerInfo: CustomerInfo): String

// Verifies premium status hasn't been tampered with
fun verifyPremiumStatus(): Boolean

// Crashes app if tampering detected
fun enforcePremiumSecurity()
```

---

## Step 6: Using Premium Status in Your App

### Check if user is premium:
```kotlin
val subscriptionState by SubscriptionManager.subscriptionState.collectAsState()
val isPremium = subscriptionState.isPremium

if (isPremium) {
    // Show premium features
} else {
    // Show ads or lock features
}
```

### Example: Hide ads for premium users:
```kotlin
@Composable
fun AdBanner() {
    val isPremium by SubscriptionManager.subscriptionState.collectAsState()
    
    if (!isPremium.isPremium) {
        // Show ad banner
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    // Set up ad
                }
            }
        )
    }
}
```

### Example: Lock premium features:
```kotlin
@Composable
fun PremiumFeatureCard(
    onUpgradeClick: () -> Unit
) {
    val isPremium by SubscriptionManager.subscriptionState.collectAsState()
    
    if (isPremium.isPremium) {
        // Show feature content
        Text("Premium Feature Available!")
    } else {
        // Show upgrade prompt
        Card {
            Column {
                Text("🌟 Premium Feature")
                Text("Upgrade to access this feature")
                Button(onClick = onUpgradeClick) {
                    Text("Go Premium")
                }
            }
        }
    }
}
```

---

## Step 7: Production Checklist

- [ ] Replace test API key with production RevenueCat key
- [ ] Create products in Google Play Console
- [ ] Configure products in RevenueCat
- [ ] Test purchases with license testers
- [ ] Verify restore purchases works
- [ ] Add Terms of Service and Privacy Policy links
- [ ] Submit app for review

---

## Files to Update for Production

### 1. `local.properties` (add):
```properties
REVENUECAT_API_KEY=your_production_api_key_here
```

### 2. `app/build.gradle.kts` (add BuildConfig field):
```kotlin
val revenueCatKey = localProperties.getProperty("REVENUECAT_API_KEY") ?: ""
buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenueCatKey\"")
```

### 3. `MainActivity.kt` (use BuildConfig):
```kotlin
Purchases.configure(
    PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY).build()
)
```

---

## Revenue Split

- **Google Play**: Takes 15-30% of revenue
- **RevenueCat**: Free for first $2,500/month, then 1%
- **You**: Keep the rest!

---

## Support

- RevenueCat Docs: https://docs.revenuecat.com
- Google Play Billing: https://developer.android.com/google/play/billing
- RevenueCat Discord: https://discord.gg/revenuecat

