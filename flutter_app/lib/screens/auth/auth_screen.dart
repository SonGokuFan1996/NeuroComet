import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/m3e_design_system.dart';
import '../../services/device_authorization_service.dart';
import '../../services/supabase_service.dart';
import '../../l10n/app_localizations.dart';
import '../../utils/responsive.dart';
import '../../widgets/brand/brand_mark.dart';
import '../../widgets/brand/brand_pill.dart';
import '../settings/dev_options_screen.dart';

/// Shared form key for auth form validation
final _authFormKey = GlobalKey<FormState>();

/// Authentication screen for user sign-in and sign-up.
/// Features neurodivergent-themed design with calming colors
/// and animated infinity symbol.
class AuthScreen extends ConsumerStatefulWidget {
  const AuthScreen({super.key});

  @override
  ConsumerState<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends ConsumerState<AuthScreen>
    with TickerProviderStateMixin {
  final _formKey = _authFormKey;
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();

  bool _isSignIn = true;
  bool _isLoading = false;
  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;
  bool _canSkipAuth = false;
  String? _error;

  late AnimationController _gradientController;

  static const _supabaseConfigMessage =
      'Supabase is not configured for this Flutter build. Start the app with SUPABASE_URL and SUPABASE_ANON_KEY dart-defines.';

  @override
  void initState() {
    super.initState();
    _gradientController = AnimationController(
      duration: const Duration(seconds: 8),
      vsync: this,
    )..repeat(reverse: true);
    _loadSkipAccess();
  }

  Future<void> _loadSkipAccess() async {
    final canSkipAuth = await DeviceAuthorizationService.canSkipAuth();
    if (!mounted) return;
    setState(() => _canSkipAuth = canSkipAuth);
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    _gradientController.dispose();
    super.dispose();
  }

  Future<bool> _handleAccountStateAfterSignIn() async {
    final status = await SupabaseService.getCurrentAccountStatus();
    if (!mounted || status == null) return true;

    if (status.detoxUntil != null && !status.isDetoxActive) {
      await SupabaseService.endDetoxMode();
      return true;
    }

    if (status.hasDeletionScheduled) {
      return _showPendingDeletionDialog(status);
    }

    if (status.isDetoxActive) {
      return _showDetoxDialog(status);
    }

    return true;
  }

  /// Dev-only: simulated biometric challenge dialog shown during sign-in.
  /// Returns true if the user passes the simulated biometric check.
  Future<bool> _showBiometricChallenge() async {
    final result = await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (ctx) => AlertDialog(
        icon: const Icon(Icons.fingerprint, size: 48, color: Colors.deepPurple),
        title: const Text('Biometric Verification'),
        content: const Text(
          'Place your finger on the sensor to authenticate.\n\n(Dev simulation — tap Authenticate to proceed)',
          style: TextStyle(fontSize: 13),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancel')),
          FilledButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Authenticate')),
        ],
      ),
    );
    return result == true;
  }

  /// Dev-only: simulated age verification dialog shown during sign-up.
  /// Returns true if the user passes the age check.
  Future<bool> _showAgeVerification() async {
    final result = await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (ctx) {
        int? selectedAge;
        return StatefulBuilder(
          builder: (ctx, setDialogState) => AlertDialog(
            title: const Text('Age Verification'),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Text(
                  'Please confirm your age to create an account.',
                  style: TextStyle(fontSize: 13),
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<int>(
                  value: selectedAge,
                  decoration: const InputDecoration(
                    labelText: 'Your Age',
                    border: OutlineInputBorder(),
                  ),
                  items: List.generate(100, (i) => i + 1)
                      .map((age) => DropdownMenuItem(value: age, child: Text('$age')))
                      .toList(),
                  onChanged: (v) => setDialogState(() => selectedAge = v),
                ),
                if (selectedAge != null && selectedAge! < 13) ...[
                  const SizedBox(height: 12),
                  const Text(
                    'You must be at least 13 years old to use NeuroComet.',
                    style: TextStyle(color: Colors.red, fontSize: 12),
                  ),
                ],
              ],
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(ctx, false),
                child: const Text('Cancel'),
              ),
              TextButton(
                onPressed: selectedAge != null && selectedAge! >= 13
                    ? () => Navigator.pop(ctx, true)
                    : null,
                child: const Text('Confirm'),
              ),
            ],
          ),
        );
      },
    );
    return result == true;
  }

  /// Dev-only: simulated 2FA challenge dialog
  Future<bool> _show2FAChallenge() async {
    final codeController = TextEditingController();
    final result = await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (ctx) => AlertDialog(
        title: const Text('Two-Factor Authentication'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Enter the 6-digit code from your authenticator app.\n\n(Dev flag: any code works)', style: TextStyle(fontSize: 13)),
            const SizedBox(height: 16),
            TextField(
              controller: codeController,
              keyboardType: TextInputType.number,
              maxLength: 6,
              decoration: const InputDecoration(labelText: '2FA Code', border: OutlineInputBorder()),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancel')),
          TextButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Verify')),
        ],
      ),
    );
    codeController.dispose();
    return result == true;
  }

  Future<bool> _showPendingDeletionDialog(AccountStatus status) async {
    final action = await showDialog<String>(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        title: const Text('Account deletion is scheduled'),
        content: Text(
          'This account is set to be deleted after the 14-day grace period. Cancel it to keep using NeuroComet, or stay signed out and let deletion continue.\n\nScheduled for: ${status.deletionScheduledAt?.toLocal() ?? 'soon'}',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, 'keep'),
            child: const Text('Keep scheduled'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, 'cancel'),
            child: const Text('Cancel deletion'),
          ),
        ],
      ),
    );

    if (action == 'cancel') {
      final result = await SupabaseService.cancelAccountDeletion();
      if (!mounted) return false;
      if (result['success'] == true) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(result['message']?.toString() ?? 'Account deletion cancelled')),
        );
        return true;
      }
      setState(() => _error = result['message']?.toString() ?? 'Failed to cancel deletion');
      return false;
    }

    await SupabaseService.signOut();
    if (!mounted) return false;
    setState(() {
      _error = 'Your account is still scheduled for deletion. Sign in again if you want to cancel it.';
    });
    return false;
  }

  Future<bool> _showDetoxDialog(AccountStatus status) async {
    final action = await showDialog<String>(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        title: const Text('Detox mode is active'),
        content: Text(
          'Your account is taking a break until ${status.detoxUntil?.toLocal() ?? 'your scheduled return'}. End detox early if you’re ready to come back, or stay signed out and protect your break.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, 'stay'),
            child: const Text('Stay on break'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, 'end'),
            child: const Text('End detox'),
          ),
        ],
      ),
    );

    if (action == 'end') {
      final result = await SupabaseService.endDetoxMode();
      if (!mounted) return false;
      if (result['success'] == true) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(result['message']?.toString() ?? 'Detox mode ended')),
        );
        return true;
      }
      setState(() => _error = result['message']?.toString() ?? 'Failed to end detox');
      return false;
    }

    await SupabaseService.signOut();
    if (!mounted) return false;
    setState(() {
      _error = 'Detox mode is still active. Sign in again when you’re ready to come back.';
    });
    return false;
  }

  Future<void> _handleAuth() async {
    final l10n = context.l10n;
    if (!_formKey.currentState!.validate()) return;

    if (!SupabaseService.isInitialized) {
      setState(() {
        _error = _supabaseConfigMessage;
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final supabase = Supabase.instance.client;

      if (_isSignIn) {
        await supabase.auth.signInWithPassword(
          email: _emailController.text.trim(),
          password: _passwordController.text,
        );
      } else {
        if (_passwordController.text != _confirmPasswordController.text) {
          setState(() {
            _error = l10n.authPasswordsNotMatch;
            _isLoading = false;
          });
          return;
        }

        // Feature flag: age verification gate (skipped when bypassAgeVerification is on)
        if (mounted) {
          final devOpts = ref.read(devOptionsProvider);
          if (!devOpts.bypassAgeVerification) {
            final passed = await _showAgeVerification();
            if (!passed || !mounted) {
              setState(() => _isLoading = false);
              return;
            }
          } else {
            debugPrint('[DevFlag] bypassAgeVerification active – skipping age gate');
          }
        }

        await supabase.auth.signUp(
          email: _emailController.text.trim(),
          password: _passwordController.text,
          emailRedirectTo: AppConstants.supabaseCallbackUrl,
        );
      }

      if (_isSignIn) {
        final canContinue = await _handleAccountStateAfterSignIn();
        if (!canContinue) return;
      }

      // Feature flag: force 2FA challenge after sign-in
      if (_isSignIn && mounted) {
        final devOpts = ref.read(devOptionsProvider);
        if (devOpts.force2FA) {
          final passed = await _show2FAChallenge();
          if (!passed || !mounted) return;
        }
      }

      // Feature flag: simulated biometric check after sign-in
      // When bypassBiometric is ON, this check is skipped entirely.
      if (_isSignIn && mounted) {
        final devOpts = ref.read(devOptionsProvider);
        if (!devOpts.bypassBiometric) {
          // Simulate a biometric prompt (in a real app this would call local_auth)
          final passed = await _showBiometricChallenge();
          if (!passed || !mounted) return;
        } else {
          debugPrint('[DevFlag] bypassBiometric active – skipping biometric check');
        }
      }

      if (mounted) {
        context.go('/');
      }
    } on AuthException catch (e) {
      setState(() {
        _error = e.message;
      });
    } catch (e) {
      setState(() {
        _error = l10n.error;
      });
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  void _showForgotPasswordDialog(BuildContext context) {
    final l10n = context.l10n;
    final resetEmailController = TextEditingController(text: _emailController.text);
    showDialog(
      context: context,
      builder: (dialogContext) {
        bool isSending = false;
        String? resultMessage;
        return StatefulBuilder(
          builder: (context, setDialogState) => AlertDialog(
            title: Text(l10n.get('resetPassword')),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  l10n.get('resetPasswordDescription'),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: resetEmailController,
                  keyboardType: TextInputType.emailAddress,
                  decoration: InputDecoration(
                    labelText: l10n.email,
                    prefixIcon: const Icon(Icons.email_outlined),
                  ),
                ),
                if (resultMessage != null) ...[
                  const SizedBox(height: 12),
                  Text(
                    resultMessage!,
                    style: TextStyle(
                      color: resultMessage!.startsWith('Error')
                          ? Theme.of(context).colorScheme.error
                          : Theme.of(context).colorScheme.primary,
                      fontSize: 13,
                    ),
                  ),
                ],
              ],
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(dialogContext),
                child: Text(l10n.cancel),
              ),
              FilledButton(
                onPressed: isSending
                    ? null
                    : () async {
                        final email = resetEmailController.text.trim();
                        if (email.isEmpty) {
                          setDialogState(() => resultMessage = l10n.get('enterEmail'));
                          return;
                        }
                        setDialogState(() {
                          isSending = true;
                          resultMessage = null;
                        });
                        try {
                          if (!SupabaseService.isInitialized) {
                            setDialogState(() {
                              isSending = false;
                              resultMessage = 'Error: $_supabaseConfigMessage';
                            });
                            return;
                          }

                          await SupabaseService.client.auth.resetPasswordForEmail(
                            email,
                            redirectTo: AppConstants.supabaseCallbackUrl,
                          );
                          setDialogState(() {
                            isSending = false;
                            resultMessage = l10n.get('resetLinkSent');
                          });
                        } catch (e) {
                          setDialogState(() {
                            isSending = false;
                            resultMessage = 'Error: ${e.toString()}';
                          });
                        }
                      },
                child: isSending
                    ? const SizedBox(
                        width: 16, height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : Text(l10n.get('sendResetLink')),
              ),
            ],
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final responsive = context.responsive;
    final isDark = theme.brightness == Brightness.dark;

    final contentMaxWidth = switch (responsive.authLayout) {
      AuthLayout.stacked => 520.0,
      AuthLayout.balanced => 640.0,
      AuthLayout.split => 1040.0,
    };

    final isWide = responsive.authLayout == AuthLayout.split;

    return Scaffold(
      body: Stack(
        children: [
          // Background
          Positioned.fill(
            child: _AuthAmbientBackground(
              isDark: isDark,
              controller: _gradientController,
            ),
          ),
          SafeArea(
            child: Center(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                child: ConstrainedBox(
                  constraints: BoxConstraints(maxWidth: contentMaxWidth),
                  child: isWide
                      ? Row(
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            Expanded(
                              flex: 1,
                              child: _AuthFormCard(
                                isSignIn: _isSignIn,
                                emailController: _emailController,
                                passwordController: _passwordController,
                                confirmPasswordController: _confirmPasswordController,
                                isLoading: _isLoading,
                                error: _error,
                                obscurePassword: _obscurePassword,
                                obscureConfirmPassword: _obscureConfirmPassword,
                                canSkipAuth: _canSkipAuth,
                                onToggleMode: () => setState(() => _isSignIn = !_isSignIn),
                                onToggleObscurePassword: () => setState(() => _obscurePassword = !_obscurePassword),
                                onToggleObscureConfirmPassword: () => setState(() => _obscureConfirmPassword = !_obscureConfirmPassword),
                                onAuth: _handleAuth,
                                onForgotPassword: () => _showForgotPasswordDialog(context),
                              ),
                            ),
                            const SizedBox(width: 48),
                            Expanded(
                              flex: 1,
                              child: _AuthHeroPanel(isDark: isDark),
                            ),
                          ],
                        )
                      : Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            _AuthHeroPanel(isDark: isDark),
                            const SizedBox(height: 24),
                            _AuthFormCard(
                              isSignIn: _isSignIn,
                              emailController: _emailController,
                              passwordController: _passwordController,
                              confirmPasswordController: _confirmPasswordController,
                              isLoading: _isLoading,
                              error: _error,
                              obscurePassword: _obscurePassword,
                              obscureConfirmPassword: _obscureConfirmPassword,
                              canSkipAuth: _canSkipAuth,
                              onToggleMode: () => setState(() => _isSignIn = !_isSignIn),
                              onToggleObscurePassword: () => setState(() => _obscurePassword = !_obscurePassword),
                              onToggleObscureConfirmPassword: () => setState(() => _obscureConfirmPassword = !_obscureConfirmPassword),
                              onAuth: _handleAuth,
                              onForgotPassword: () => _showForgotPasswordDialog(context),
                            ),
                          ],
                        ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _AuthAmbientBackground extends StatelessWidget {
  final bool isDark;
  final AnimationController controller;

  const _AuthAmbientBackground({required this.isDark, required this.controller});

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, child) {
        final t = controller.value;
        return Stack(
          children: [
            // Primary gradient layer
            Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: isDark
                      ? [
                          Color.lerp(const Color(0xFF0D0D1A), const Color(0xFF1A1035), t)!,
                          Color.lerp(const Color(0xFF0A1929), const Color(0xFF0D0D1A), t)!,
                        ]
                      : [
                          Color.lerp(const Color(0xFFF3EEFF), const Color(0xFFE8F5F0), t)!,
                          Color.lerp(const Color(0xFFEDE7F6), const Color(0xFFE3F2FD), t)!,
                        ],
                ),
              ),
            ),
            // Secondary radial glow — top right
            Positioned(
              top: -80,
              right: -60,
              child: Container(
                width: 300,
                height: 300,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      (isDark ? AppColors.primaryPurple : AppColors.calmLavender).withValues(alpha: isDark ? 0.12 : 0.18),
                      Colors.transparent,
                    ],
                  ),
                ),
              ),
            ),
            // Secondary radial glow — bottom left
            Positioned(
              bottom: -100,
              left: -80,
              child: Container(
                width: 350,
                height: 350,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      (isDark ? AppColors.secondaryTeal : AppColors.calmBlue).withValues(alpha: isDark ? 0.10 : 0.15),
                      Colors.transparent,
                    ],
                  ),
                ),
              ),
            ),
          ],
        );
      },
    );
  }
}

class _AuthHeroPanel extends StatelessWidget {
  final bool isDark;

  const _AuthHeroPanel({required this.isDark});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = context.l10n;

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const BrandPill(text: 'BETA'),
        const SizedBox(height: 12),
        NeuroCometBrandMark(
          size: 120,
          haloColor: theme.colorScheme.primary,
          accentColor: theme.colorScheme.secondary,
          motionEnabled: true,
        ),
        const SizedBox(height: 16),
        Text(
          l10n.authWelcomeTitle,
          style: theme.textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.bold),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 8),
        Text(
          l10n.authAppTagline,
          style: theme.textTheme.titleMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 12),
        Text(
          l10n.authWelcomeBody,
          style: theme.textTheme.bodyLarge?.copyWith(color: theme.colorScheme.onSurfaceVariant),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 24),
        Card(
          shape: RoundedRectangleBorder(borderRadius: M3EDesignSystem.shapeBubblyCard),
          color: theme.colorScheme.surface.withValues(alpha: 0.88),
          elevation: 2,
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Column(
              children: [
                _AuthFeatureRow(
                  icon: Icons.check_circle,
                  title: l10n.authFeatureAccessibleTitle,
                  body: l10n.authFeatureAccessibleBody,
                ),
                const SizedBox(height: 10),
                _AuthFeatureRow(
                  icon: Icons.lock,
                  title: l10n.authFeaturePrivateTitle,
                  body: l10n.authFeaturePrivateBody,
                ),
                const SizedBox(height: 10),
                _AuthFeatureRow(
                  icon: Icons.palette,
                  title: l10n.authFeatureFlexibleTitle,
                  body: l10n.authFeatureFlexibleBody,
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _AuthFeatureRow extends StatelessWidget {
  final IconData icon;
  final String title;
  final String body;

  const _AuthFeatureRow({required this.icon, required this.title, required this.body});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: theme.colorScheme.primary.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(icon, color: theme.colorScheme.primary, size: 18),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
              const SizedBox(height: 2),
              Text(body, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
            ],
          ),
        ),
      ],
    );
  }
}

class _AuthFormCard extends StatelessWidget {
  final bool isSignIn;
  final TextEditingController emailController;
  final TextEditingController passwordController;
  final TextEditingController confirmPasswordController;
  final bool isLoading;
  final String? error;
  final bool obscurePassword;
  final bool obscureConfirmPassword;
  final bool canSkipAuth;
  final VoidCallback onToggleMode;
  final VoidCallback onToggleObscurePassword;
  final VoidCallback onToggleObscureConfirmPassword;
  final VoidCallback onAuth;
  final VoidCallback onForgotPassword;

  const _AuthFormCard({
    required this.isSignIn,
    required this.emailController,
    required this.passwordController,
    required this.confirmPasswordController,
    required this.isLoading,
    this.error,
    required this.obscurePassword,
    required this.obscureConfirmPassword,
    required this.canSkipAuth,
    required this.onToggleMode,
    required this.onToggleObscurePassword,
    required this.onToggleObscureConfirmPassword,
    required this.onAuth,
    required this.onForgotPassword,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = context.l10n;

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      color: theme.colorScheme.surfaceContainerHigh.withValues(alpha: 0.96),
      elevation: 4,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Form(
          key: _authFormKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _AuthModeSwitch(isSignIn: isSignIn, onToggle: onToggleMode),
              const SizedBox(height: 16),
              Text(
                isSignIn ? l10n.authSignIn : l10n.authCreateAccount,
                style: theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              Text(
                isSignIn ? l10n.authSignInSubtitle : l10n.authSignUpSubtitle,
                style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant),
              ),
              if (error != null) ...[
                const SizedBox(height: 16),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.errorContainer,
                    borderRadius: BorderRadius.circular(18),
                  ),
                  child: Text(
                    error!,
                    style: TextStyle(color: theme.colorScheme.onErrorContainer),
                  ),
                ),
              ],
              const SizedBox(height: 20),
              TextFormField(
                controller: emailController,
                keyboardType: TextInputType.emailAddress,
                validator: (value) {
                  if (value == null || value.trim().isEmpty) return l10n.get('enterEmail');
                  if (!value.contains('@')) return l10n.get('invalidEmail');
                  return null;
                },
                decoration: InputDecoration(
                  labelText: l10n.authEmailLabel,
                  hintText: l10n.authEmailPlaceholder,
                  prefixIcon: const Icon(Icons.person),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(20)),
                ),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: passwordController,
                obscureText: obscurePassword,
                validator: (value) {
                  if (value == null || value.isEmpty) return l10n.get('enterPassword');
                  if (value.length < 6) return l10n.authPasswordRequirements;
                  return null;
                },
                decoration: InputDecoration(
                  labelText: l10n.authPasswordLabel,
                  prefixIcon: const Icon(Icons.lock),
                  suffixIcon: IconButton(
                    icon: Icon(obscurePassword ? Icons.visibility : Icons.visibility_off),
                    onPressed: onToggleObscurePassword,
                  ),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(20)),
                ),
              ),
              if (!isSignIn) ...[
                const SizedBox(height: 12),
                TextFormField(
                  controller: confirmPasswordController,
                  obscureText: obscureConfirmPassword,
                  validator: (value) {
                    if (value != passwordController.text) return l10n.authPasswordsNotMatch;
                    return null;
                  },
                  decoration: InputDecoration(
                    labelText: l10n.authConfirmPasswordLabel,
                    prefixIcon: const Icon(Icons.lock),
                    suffixIcon: IconButton(
                      icon: Icon(obscureConfirmPassword ? Icons.visibility : Icons.visibility_off),
                      onPressed: onToggleObscureConfirmPassword,
                    ),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(20)),
                  ),
                ),
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.primary.withValues(alpha: 0.08),
                    borderRadius: BorderRadius.circular(18),
                  ),
                  child: Text(
                    l10n.authPasswordRequirements,
                    style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                  ),
                ),
              ],
              const SizedBox(height: 20),
              SizedBox(
                width: double.infinity,
                height: 56,
                child: FilledButton(
                  onPressed: isLoading ? null : onAuth,
                  style: FilledButton.styleFrom(shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20))),
                  child: isLoading
                      ? const SizedBox(width: 24, height: 24, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                      : Text(isSignIn ? l10n.authSignIn : l10n.authCreateAccount, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                ),
              ),
              const SizedBox(height: 12),
              Center(
                child: Column(
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.lock, size: 14, color: theme.colorScheme.onSurfaceVariant),
                        const SizedBox(width: 4),
                        Text(l10n.authDataSecure, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                      ],
                    ),
                    if (isSignIn)
                      TextButton(onPressed: onForgotPassword, child: Text(l10n.get('forgotPassword'))),
                    if (canSkipAuth)
                      TextButton(
                        onPressed: () {
                          SupabaseService.devModeSkipAuth = true;
                          GoRouter.of(context).go('/');
                        },
                        child: Text(l10n.authSkipDev, style: TextStyle(color: theme.colorScheme.onSurface.withValues(alpha: 0.5))),
                      ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _AuthModeSwitch extends StatelessWidget {
  final bool isSignIn;
  final VoidCallback onToggle;

  const _AuthModeSwitch({required this.isSignIn, required this.onToggle});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = context.l10n;

    return Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceVariant.withValues(alpha: 0.7),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          Expanded(
            child: GestureDetector(
              onTap: isSignIn ? null : onToggle,
              child: Container(
                padding: const EdgeInsets.symmetric(vertical: 12),
                decoration: BoxDecoration(
                  color: isSignIn ? theme.colorScheme.surface : Colors.transparent,
                  borderRadius: BorderRadius.circular(16),
                ),
                alignment: Alignment.center,
                child: Text(
                  l10n.authSignIn,
                  style: TextStyle(
                    fontWeight: isSignIn ? FontWeight.bold : FontWeight.normal,
                    color: isSignIn ? theme.colorScheme.onSurface : theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ),
            ),
          ),
          Expanded(
            child: GestureDetector(
              onTap: !isSignIn ? null : onToggle,
              child: Container(
                padding: const EdgeInsets.symmetric(vertical: 12),
                decoration: BoxDecoration(
                  color: !isSignIn ? theme.colorScheme.surface : Colors.transparent,
                  borderRadius: BorderRadius.circular(16),
                ),
                alignment: Alignment.center,
                child: Text(
                  l10n.authSignUp,
                  style: TextStyle(
                    fontWeight: !isSignIn ? FontWeight.bold : FontWeight.normal,
                    color: !isSignIn ? theme.colorScheme.onSurface : theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

