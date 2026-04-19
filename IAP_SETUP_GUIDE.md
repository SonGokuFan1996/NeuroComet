# In-App Purchases & Ads Setup Guide for NeuroComet

Your app already has a complete RevenueCat and Google AdMob integration with enhanced security! Here's how to make it work in production.

## Current Implementation Status ✅

Your app already includes:
- `SubscriptionManager.kt` - Handles all purchase logic with security verification
- `SubscriptionScreen.kt` - Beautiful premium subscription UI
- `GoogleAdsManager.kt` - Neurodivergent-friendly ad management
- `SecurityUtils.kt` - **ENHANCED SECURITY**: All API keys and Ad IDs are now obfuscated in the binary
- RevenueCat SDK integration
- Two products: Monthly ($2/month) and Lifetime ($60 one-time)

---

## Step 1: Set Up Credentials in `local.properties`

For security, ALL sensitive keys must be placed in `local.properties`. This file is ignored by Git, preventing your keys from being exposed in your repository.

Add these lines to your `C:/Users/bkyil/AndroidStudioProjects/NeuroComet/local.properties`:

```properties
# RevenueCat
REVENUECAT_API_KEY=goog_tqbgqglcFYAbwdfvHDhNLgZtXWb

# Google AdMob
ADMOB_APP_ID=ca-app-pub-XXXXXXXXXX~XXXXXXXXXX
ADMOB_BANNER_ID=ca-app-pub-XXXXXXXXXX/XXXXXXXXXX
ADMOB_INTERSTITIAL_ID=ca-app-pub-XXXXXXXXXX/XXXXXXXXXX
ADMOB_REWARDED_ID=ca-app-pub-XXXXXXXXXX/XXXXXXXXXX

# Gemini AI (for Practice Calls)
GEMINI_API_KEY=your_gemini_api_key_here

# Supabase (Backend)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_KEY=your-supabase-anon-key
```

---

## Step 2: How Security Works (Auto-implemented)

The app now uses a multi-layer security approach:
1.  **Source Security**: Keys are stored in `local.properties` (never committed to Git).
2.  **Build Security**: `build.gradle.kts` reads these keys and **obfuscates** them before injecting them into `BuildConfig`.
3.  **Binary Security**: `SecurityUtils.kt` decrypts the keys at runtime. This prevents "bad actors" from finding your plain-text API keys by simply running `strings` on your APK or decompiling it.

**No additional code changes are needed!** The app is already configured to use `SecurityUtils.decrypt()` for all sensitive configurations.

---

## Step 3: Set Up RevenueCat Account

1. **Create a RevenueCat account**: https://app.revenuecat.com
2. **Create a new project** for NeuroComet
3. **Get your API key** from Project Settings → API Keys and add it to `local.properties`.

---

## Step 4: Set Up Google Play Console

### A. Create Your Products

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app → **Monetization** → **Products**

#### For Monthly Subscription:
- Product ID: `NeuroComet_premium_monthly`
- Product Type: **Subscription**
- Price: $2.00/month

#### For Lifetime Purchase:
- Product ID: `NeuroComet_premium_lifetime`  
- Product Type: **In-app product**
- Price: $60.00

### B. Connect Google Play to RevenueCat

1. In Google Play Console → **Monetization setup**
2. Create a Service Account with Play Developer API access
3. Download the JSON credentials and upload to RevenueCat Dashboard.

---

## Step 5: Google AdMob Setup

### A. Create AdMob Account

1. Go to [Google AdMob](https://admob.google.com/)
2. Create a new app for NeuroComet (Android)
3. Get your **App ID** and **Ad Unit IDs**.

### B. Update `local.properties`

Add the production IDs to your `local.properties` as shown in Step 1. The `build.gradle.kts` will automatically pick these up, obfuscate them, and `GoogleAdsManager` will use them for production builds.

---

## Step 6: Security Features (Already Implemented!)

Your `SubscriptionManager.kt` and `SecurityUtils.kt` include:
- ✅ **API Key Obfuscation**: Prevents string-scraping attacks.
- ✅ **Anti-Piracy Tokens**: Ensures purchases are legitimate.
- ✅ **Tamper Detection**: Verifies premium status hasn't been modified locally.
- ✅ **Native Keystore**: Sensitive user data is encrypted with hardware-backed keys.

---

## Step 7: Production Checklist

- [ ] Add all production keys to `local.properties`.
- [ ] Create products in Google Play Console.
- [ ] Configure products and entitlements in RevenueCat.
- [ ] Test purchases with license testers.
- [ ] Verify restore purchases works.
- [ ] Set `BuildConfig.DEBUG` to `false` for release (handled by Gradle).
- [ ] Submit app for review.

---

## Support

- RevenueCat Docs: https://docs.revenuecat.com
- Google AdMob Docs: https://developers.google.com/admob/android/quick-start
- NeuroComet Security: See `SecurityUtils.kt` and `CredentialStorage.kt`
