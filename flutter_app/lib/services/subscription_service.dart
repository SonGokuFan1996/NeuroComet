import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:in_app_purchase/in_app_purchase.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Manages subscription state for NeuroComet.
///
/// Products:
/// - Monthly: $2/month ad-free subscription
/// - Lifetime: $60 one-time purchase for lifetime ad-free
///
/// Entitlement: "premium" — grants ad-free experience
///
/// Uses the `in_app_purchase` package for real billing on Play Store / App Store.
/// Falls back to SharedPreferences caching when billing is unavailable.
class SubscriptionService {
  SubscriptionService._();
  static final SubscriptionService instance = SubscriptionService._();

  static const String _tag = 'SubscriptionService';

  /// Product IDs configured in Play Console / App Store Connect
  static const String monthlyProductId = 'monthly_subscription';
  static const String lifetimeProductId = 'lifetime_purchase';
  static const Set<String> _productIds = {monthlyProductId, lifetimeProductId};

  /// Whether we are in debug / test mode.
  static final bool testMode = kDebugMode;

  // ── IAP ────────────────────────────────────────────────────────
  final InAppPurchase _iap = InAppPurchase.instance;
  StreamSubscription<List<PurchaseDetails>>? _purchaseSubscription;
  bool _iapAvailable = false;

  /// Resolved product details from the store
  ProductDetails? _monthlyProduct;
  ProductDetails? _lifetimeProduct;

  // Pending callbacks stored during purchase flow
  VoidCallback? _pendingOnSuccess;
  ValueChanged<String>? _pendingOnError;

  // ── State ──────────────────────────────────────────────────────
  final ValueNotifier<SubscriptionState> _stateNotifier =
      ValueNotifier(const SubscriptionState());

  ValueNotifier<SubscriptionState> get stateNotifier => _stateNotifier;
  SubscriptionState get state => _stateNotifier.value;

  /// Simulated premium flag used in test mode.
  bool _isTestPremium = false;

  // ── Initialization ────────────────────────────────────────────

  /// Initialize IAP listeners. Call once at app startup.
  Future<void> init() async {
    if (testMode) {
      debugPrint('$_tag 🧪 TEST MODE: Skipping IAP init');
      return;
    }

    _iapAvailable = await _iap.isAvailable();
    if (!_iapAvailable) {
      debugPrint('$_tag ⚠️ In-app purchases not available on this device');
      return;
    }

    _purchaseSubscription = _iap.purchaseStream.listen(
      _handlePurchaseUpdates,
      onDone: () => _purchaseSubscription?.cancel(),
      onError: (error) {
        debugPrint('$_tag Purchase stream error: $error');
      },
    );

    debugPrint('$_tag IAP initialized and listening for purchases');
  }

  /// Dispose IAP subscription. Call on app shutdown.
  void dispose() {
    _purchaseSubscription?.cancel();
    _purchaseSubscription = null;
  }

  // ── Public API ─────────────────────────────────────────────────

  /// Fetch offerings from the store.
  Future<void> fetchOfferings() async {
    _update(state.copyWith(isLoading: true, error: null));
    if (testMode) {
      await Future.delayed(const Duration(milliseconds: 400));
      _update(state.copyWith(isLoading: false));
      debugPrint('$_tag 🧪 TEST MODE: Offerings simulated');
      return;
    }

    if (!_iapAvailable) {
      _update(state.copyWith(
        isLoading: false,
        error: 'Purchases are temporarily unavailable. Please try again later.',
      ));
      return;
    }

    try {
      final response = await _iap.queryProductDetails(_productIds);

      if (response.notFoundIDs.isNotEmpty) {
        debugPrint('$_tag Products not found: ${response.notFoundIDs}');
      }

      if (response.error != null) {
        _update(state.copyWith(
          isLoading: false,
          error: response.error!.message,
        ));
        return;
      }

      for (final product in response.productDetails) {
        if (product.id == monthlyProductId) {
          _monthlyProduct = product;
        } else if (product.id == lifetimeProductId) {
          _lifetimeProduct = product;
        }
      }

      _update(state.copyWith(
        isLoading: false,
        monthlyPackage: _monthlyProduct,
        lifetimePackage: _lifetimeProduct,
      ));
      debugPrint('$_tag Fetched ${response.productDetails.length} products');
    } catch (e) {
      _update(state.copyWith(isLoading: false, error: e.toString()));
      debugPrint('$_tag Error fetching offerings: $e');
    }
  }

  /// Check current premium status. 
  /// Reads cached SharedPreferences flag first, then optionally verifies with the store.
  Future<bool> checkPremiumStatus({bool forceStoreCheck = false}) async {
    if (testMode) {
      _update(state.copyWith(isPremium: _isTestPremium));
      debugPrint('$_tag 🧪 TEST MODE: Premium = $_isTestPremium');
      return _isTestPremium;
    }

    final prefs = await SharedPreferences.getInstance();
    var isPremium = prefs.getBool('is_premium') ?? false;

    // If not premium locally, or if forced, try to check with the store (restore logic)
    if (!isPremium || forceStoreCheck) {
      debugPrint('$_tag Local premium false or forced. Checking store for past purchases...');
      if (_iapAvailable) {
        try {
          // This is a lightweight way to check for active purchases without a full restore UI
          await _iap.restorePurchases();
          // The results will be handled by _handlePurchaseUpdates and update SharedPreferences
          // We wait a bit for the stream to process (though it's async)
          await Future.delayed(const Duration(milliseconds: 500));
          isPremium = prefs.getBool('is_premium') ?? false;
        } catch (e) {
          debugPrint('$_tag Error auto-checking store: $e');
        }
      }
    }

    _update(state.copyWith(isPremium: isPremium));
    return isPremium;
  }

  /// Purchase the monthly subscription.
  Future<void> purchaseMonthly({
    VoidCallback? onSuccess,
    ValueChanged<String>? onError,
  }) async {
    if (testMode) {
      await _simulateTestPurchase('monthly', onSuccess);
      return;
    }
    await _purchasePackage('monthly',
        product: _monthlyProduct, onSuccess: onSuccess, onError: onError);
  }

  /// Purchase the lifetime subscription.
  Future<void> purchaseLifetime({
    VoidCallback? onSuccess,
    ValueChanged<String>? onError,
  }) async {
    if (testMode) {
      await _simulateTestPurchase('lifetime', onSuccess);
      return;
    }
    await _purchasePackage('lifetime',
        product: _lifetimeProduct, onSuccess: onSuccess, onError: onError);
  }

  /// Restore purchases.
  Future<void> restorePurchases({
    ValueChanged<bool>? onSuccess,
    ValueChanged<String>? onError,
  }) async {
    _update(state.copyWith(isLoading: true, error: null));
    if (testMode) {
      await Future.delayed(const Duration(milliseconds: 800));
      _update(state.copyWith(
        isLoading: false,
        isPremium: _isTestPremium,
        purchaseSuccess: _isTestPremium,
        purchaseType: _isTestPremium ? 'restored' : null,
      ));
      debugPrint('$_tag 🧪 TEST MODE: Restore — premium = $_isTestPremium');
      onSuccess?.call(_isTestPremium);
      return;
    }

    if (!_iapAvailable) {
      _update(state.copyWith(isLoading: false, error: 'Store not available'));
      onError?.call('Store not available');
      return;
    }

    try {
      // Store callbacks for when purchase stream delivers results
      _pendingOnSuccess = onSuccess != null ? () => onSuccess(true) : null;
      _pendingOnError = onError;
      await _iap.restorePurchases();
      // Results arrive via _handlePurchaseUpdates
    } catch (e) {
      _update(state.copyWith(isLoading: false, error: e.toString()));
      onError?.call(e.toString());
    }
  }

  /// Clear the [purchaseSuccess] flag (call after the UI has shown the result).
  void clearPurchaseSuccess() {
    _update(state.copyWith(purchaseSuccess: false, purchaseType: null));
  }

  /// Clear the error message.
  void clearError() {
    _update(state.copyWith(error: null));
  }

  /// Reset simulated purchase (test mode only).
  void resetTestPurchase() {
    if (!testMode) return;
    _isTestPremium = false;
    _stateNotifier.value = const SubscriptionState();
    debugPrint('$_tag 🧪 TEST MODE: Premium status reset to FREE');
  }

  // ── Test-only helpers for triggering each transaction card state ──

  /// Simulate a successful purchase (test mode only).
  Future<void> simulateTestSuccess() async {
    if (!testMode) return;
    _update(state.copyWith(isLoading: true, error: null));
    await Future.delayed(const Duration(milliseconds: 600));
    _isTestPremium = true;
    _update(state.copyWith(
      isLoading: false,
      isPremium: true,
      purchaseSuccess: true,
      purchaseType: 'monthly',
    ));
    debugPrint('$_tag 🧪 TEST: Simulated SUCCESS');
  }

  /// Simulate a declined purchase (test mode only).
  Future<void> simulateTestDeclined() async {
    if (!testMode) return;
    _update(state.copyWith(isLoading: true, error: null));
    await Future.delayed(const Duration(milliseconds: 600));
    _update(state.copyWith(
      isLoading: false,
      error: 'Payment declined by card issuer.',
    ));
    debugPrint('$_tag 🧪 TEST: Simulated DECLINED');
  }

  /// Simulate a timed-out purchase (test mode only).
  Future<void> simulateTestTimedOut() async {
    if (!testMode) return;
    _update(state.copyWith(isLoading: true, error: null));
    await Future.delayed(const Duration(milliseconds: 600));
    debugPrint('$_tag 🧪 TEST: Simulated TIMED_OUT (no response)');
  }

  // ── Private helpers ────────────────────────────────────────────

  void _update(SubscriptionState next) {
    _stateNotifier.value = next;
  }

  Future<void> _purchasePackage(
    String purchaseType, {
    ProductDetails? product,
    VoidCallback? onSuccess,
    ValueChanged<String>? onError,
  }) async {
    _update(state.copyWith(isLoading: true, error: null));

    if (!_iapAvailable || product == null) {
      // Fallback: if store not available, show error
      const msg = 'Purchases are temporarily unavailable. Please try again later.';
      _update(state.copyWith(isLoading: false, error: msg));
      onError?.call(msg);
      return;
    }

    try {
      // Store callbacks for when purchase stream delivers results
      _pendingOnSuccess = onSuccess;
      _pendingOnError = onError;

      final purchaseParam = PurchaseParam(productDetails: product);

      // Lifetime is non-consumable, monthly is non-consumable (subscription)
      final success = await _iap.buyNonConsumable(purchaseParam: purchaseParam);
      if (!success) {
        _update(state.copyWith(isLoading: false, error: 'Purchase could not be initiated'));
        onError?.call('Purchase could not be initiated');
        _pendingOnSuccess = null;
        _pendingOnError = null;
      }
      // If successful, the purchase stream will deliver the result
    } catch (e) {
      _update(state.copyWith(isLoading: false, error: e.toString()));
      debugPrint('$_tag Purchase error: $e');
      onError?.call(e.toString());
      _pendingOnSuccess = null;
      _pendingOnError = null;
    }
  }

  /// Handle incoming purchase updates from the store.
  Future<void> _handlePurchaseUpdates(
      List<PurchaseDetails> purchaseDetailsList) async {
    for (final purchase in purchaseDetailsList) {
      switch (purchase.status) {
        case PurchaseStatus.pending:
          debugPrint('$_tag Purchase pending: ${purchase.productID}');
          _update(state.copyWith(isLoading: true));
          break;

        case PurchaseStatus.purchased:
        case PurchaseStatus.restored:
          debugPrint('$_tag Purchase ${purchase.status.name}: ${purchase.productID}');

          // Verify and grant entitlement
          await _grantPremium(purchase);

          final type = purchase.status == PurchaseStatus.restored
              ? 'restored'
              : (purchase.productID == monthlyProductId ? 'monthly' : 'lifetime');

          _update(state.copyWith(
            isLoading: false,
            isPremium: true,
            purchaseSuccess: true,
            purchaseType: type,
          ));
          _pendingOnSuccess?.call();
          _pendingOnSuccess = null;
          _pendingOnError = null;
          break;

        case PurchaseStatus.error:
          debugPrint('$_tag Purchase error: ${purchase.error?.message}');
          final errorMsg = purchase.error?.message ?? 'Purchase failed';
          _update(state.copyWith(isLoading: false, error: errorMsg));
          _pendingOnError?.call(errorMsg);
          _pendingOnSuccess = null;
          _pendingOnError = null;
          break;

        case PurchaseStatus.canceled:
          debugPrint('$_tag Purchase canceled: ${purchase.productID}');
          _update(state.copyWith(isLoading: false));
          _pendingOnSuccess = null;
          _pendingOnError = null;
          break;
      }

      // Always complete pending purchases
      if (purchase.pendingCompletePurchase) {
        await _iap.completePurchase(purchase);
      }
    }
  }

  /// Grant premium entitlement and persist locally.
  Future<void> _grantPremium(PurchaseDetails purchase) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('is_premium', true);
    await prefs.setString('subscription_date', DateTime.now().toIso8601String());
    await prefs.setString('subscription_product_id', purchase.productID);
    debugPrint('$_tag Premium granted for product: ${purchase.productID}');
  }

  Future<void> _simulateTestPurchase(
      String purchaseType, VoidCallback? onSuccess) async {
    _update(state.copyWith(isLoading: true, error: null));
    await Future.delayed(const Duration(milliseconds: 1200));
    _isTestPremium = true;
    _update(state.copyWith(
      isLoading: false,
      isPremium: true,
      purchaseSuccess: true,
      purchaseType: purchaseType,
    ));
    debugPrint('$_tag 🧪 TEST MODE: Purchase simulated — type = $purchaseType');
    onSuccess?.call();
  }
}

/// Immutable subscription state — mirrors the Android `SubscriptionManager.SubscriptionState`.
class SubscriptionState {
  final bool isLoading;
  final bool isPremium;
  final bool purchaseSuccess;
  final String? purchaseType; // "monthly", "lifetime", or "restored"
  final String? error;

  // Offerings placeholders (will be populated by IAP)
  final dynamic offerings;
  final dynamic monthlyPackage;
  final dynamic lifetimePackage;

  const SubscriptionState({
    this.isLoading = false,
    this.isPremium = false,
    this.purchaseSuccess = false,
    this.purchaseType,
    this.error,
    this.offerings,
    this.monthlyPackage,
    this.lifetimePackage,
  });

  SubscriptionState copyWith({
    bool? isLoading,
    bool? isPremium,
    bool? purchaseSuccess,
    String? purchaseType,
    String? error,
    dynamic offerings,
    dynamic monthlyPackage,
    dynamic lifetimePackage,
  }) {
    return SubscriptionState(
      isLoading: isLoading ?? this.isLoading,
      isPremium: isPremium ?? this.isPremium,
      purchaseSuccess: purchaseSuccess ?? this.purchaseSuccess,
      purchaseType: purchaseType ?? this.purchaseType,
      error: error ?? this.error,
      offerings: offerings ?? this.offerings,
      monthlyPackage: monthlyPackage ?? this.monthlyPackage,
      lifetimePackage: lifetimePackage ?? this.lifetimePackage,
    );
  }

  /// Returns a copy with specific nullable fields explicitly cleared.
  SubscriptionState copyWithClear({
    bool clearPurchaseType = false,
    bool clearError = false,
  }) {
    return SubscriptionState(
      isLoading: isLoading,
      isPremium: isPremium,
      purchaseSuccess: purchaseSuccess,
      purchaseType: clearPurchaseType ? null : purchaseType,
      error: clearError ? null : error,
      offerings: offerings,
      monthlyPackage: monthlyPackage,
      lifetimePackage: lifetimePackage,
    );
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is SubscriptionState &&
          runtimeType == other.runtimeType &&
          isLoading == other.isLoading &&
          isPremium == other.isPremium &&
          purchaseSuccess == other.purchaseSuccess &&
          purchaseType == other.purchaseType &&
          error == other.error &&
          offerings == other.offerings &&
          monthlyPackage == other.monthlyPackage &&
          lifetimePackage == other.lifetimePackage;

  @override
  int get hashCode =>
      isLoading.hashCode ^
      isPremium.hashCode ^
      purchaseSuccess.hashCode ^
      purchaseType.hashCode ^
      error.hashCode ^
      offerings.hashCode ^
      monthlyPackage.hashCode ^
      lifetimePackage.hashCode;

  @override
  String toString() =>
      'SubscriptionState(isLoading: $isLoading, isPremium: $isPremium, '
      'purchaseSuccess: $purchaseSuccess, purchaseType: $purchaseType, '
      'error: $error)';
}

