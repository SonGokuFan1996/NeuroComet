import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../screens/settings/dev_options_screen.dart';
import '../services/supabase_service.dart';
import 'profile_provider.dart';

/// Auth state provider
final authStateProvider = StreamProvider<AuthState>((ref) {
  if (!SupabaseService.isInitialized) {
    return Stream.value(
      AuthState(AuthChangeEvent.initialSession, null),
    );
  }

  return Supabase.instance.client.auth.onAuthStateChange;
});

/// Current user provider — reacts to auth state changes
final currentUserProvider = Provider<User?>((ref) {
  // Watch auth state so this provider re-evaluates on login/logout
  ref.watch(authStateProvider);
  return SupabaseService.currentUser;
});


/// Is authenticated provider
final isAuthenticatedProvider = Provider<bool>((ref) {
  // Feature flag: force logged out state
  if (!kReleaseMode) {
    final devOpts = ref.watch(devOptionsProvider);
    if (devOpts.forceLoggedOut) return false;
  }

  final user = ref.watch(currentUserProvider);
  return user != null || SupabaseService.devModeSkipAuth;
});

/// Auth controller provider
final authControllerProvider = NotifierProvider<AuthController, AsyncValue<void>>(
  AuthController.new,
);

/// Auth controller for authentication operations
class AuthController extends Notifier<AsyncValue<void>> {
  @override
  AsyncValue<void> build() => const AsyncValue.data(null);

  Future<void> signIn({
    required String email,
    required String password,
  }) async {
    state = const AsyncValue.loading();
    try {
      await SupabaseService.signInWithEmail(
        email: email,
        password: password,
      );
      // Ensure a users-table row exists for this account
      await SupabaseService.ensureUserProfile();
      // Refresh the profile providers so they pick up real data
      ref.invalidate(currentUserProfileProvider);
      state = const AsyncValue.data(null);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> signUp({
    required String email,
    required String password,
    String? displayName,
  }) async {
    state = const AsyncValue.loading();
    try {
      await SupabaseService.signUpWithEmail(
        email: email,
        password: password,
        displayName: displayName,
      );
      // Create a users-table row for the new account
      await SupabaseService.ensureUserProfile();
      ref.invalidate(currentUserProfileProvider);
      state = const AsyncValue.data(null);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> signOut() async {
    state = const AsyncValue.loading();
    try {
      await SupabaseService.signOut();
      ref.invalidate(currentUserProfileProvider);
      state = const AsyncValue.data(null);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> resetPassword(String email) async {
    state = const AsyncValue.loading();
    try {
      await SupabaseService.resetPassword(email);
      state = const AsyncValue.data(null);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> signInWithGoogle() async {
    state = const AsyncValue.loading();
    try {
      await SupabaseService.signInWithGoogle();
      state = const AsyncValue.data(null);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> signInWithApple() async {
    state = const AsyncValue.loading();
    try {
      await SupabaseService.signInWithApple();
      state = const AsyncValue.data(null);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }
}


