import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../services/supabase_service.dart';

/// Auth state provider
final authStateProvider = StreamProvider<AuthState>((ref) {
  if (!SupabaseService.isInitialized) {
    return Stream.value(
      AuthState(AuthChangeEvent.initialSession, null),
    );
  }

  return Supabase.instance.client.auth.onAuthStateChange;
});

/// Current user provider
final currentUserProvider = Provider<User?>((ref) {
  return SupabaseService.currentUser;
});


/// Is authenticated provider
final isAuthenticatedProvider = Provider<bool>((ref) {
  final user = ref.watch(currentUserProvider);
  return user != null;
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
      state = const AsyncValue.data(null);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> signOut() async {
    state = const AsyncValue.loading();
    try {
      await SupabaseService.signOut();
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


