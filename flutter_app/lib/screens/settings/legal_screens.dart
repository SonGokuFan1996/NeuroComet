import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../core/constants/app_constants.dart';

/// Privacy Policy Screen for app store compliance
class PrivacyPolicyScreen extends StatelessWidget {
  const PrivacyPolicyScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Privacy Policy'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Privacy Policy',
              style: theme.textTheme.headlineMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Last updated: January 8, 2026',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.outline,
              ),
            ),
            const SizedBox(height: 24),
            _buildSection(
              theme,
              'Introduction',
              'Welcome to NeuroComet. We are committed to protecting your privacy and ensuring a safe experience for all our users, especially those in the neurodivergent community. This Privacy Policy explains how we collect, use, and protect your information.',
            ),
            _buildSection(
              theme,
              'Information We Collect',
              '''We collect the following types of information:

• Account Information: Email address, display name, and profile information you provide.
• Content: Posts, messages, and other content you create.
• Usage Data: How you interact with our app, including features used and time spent.
• Device Information: Device type, operating system, and app version for optimization.
• Location: Only with your explicit consent for relevant features.''',
            ),
            _buildSection(
              theme,
              'How We Use Your Information',
              '''We use your information to:

• Provide and improve our services
• Personalize your experience
• Ensure community safety through content moderation
• Send important notifications about your account
• Comply with legal requirements''',
            ),
            _buildSection(
              theme,
              'Data Protection',
              '''We implement strong security measures:

• End-to-end encryption for private messages
• Secure data storage with encryption at rest
• Regular security audits and updates
• Strict access controls for our team''',
            ),
            _buildSection(
              theme,
              'Your Rights',
              '''You have the right to:

• Access your personal data
• Request data correction or deletion
• Export your data
• Opt out of non-essential communications
• Delete your account at any time''',
            ),
            _buildSection(
              theme,
              'Children\'s Privacy',
              'NeuroComet is designed for users aged 13 and older. We provide enhanced privacy protections for users under 18, including parental controls and restricted content access.',
            ),
            _buildSection(
              theme,
              'Contact Us',
              'If you have questions about this Privacy Policy or your data, please contact us at privacy@getneurocomet.com',
            ),
            const SizedBox(height: 32),
            Center(
              child: TextButton(
                onPressed: () => _launchUrl('https://getneurocomet.com/privacy'),
                child: const Text('View Full Privacy Policy Online'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSection(ThemeData theme, String title, String content) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            content,
            style: theme.textTheme.bodyMedium?.copyWith(
              height: 1.6,
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _launchUrl(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }
}

/// Terms of Service Screen for app store compliance
class TermsOfServiceScreen extends StatelessWidget {
  const TermsOfServiceScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Terms of Service'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Terms of Service',
              style: theme.textTheme.headlineMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Last updated: January 8, 2026',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.outline,
              ),
            ),
            const SizedBox(height: 24),
            _buildSection(
              theme,
              'Acceptance of Terms',
              'By using NeuroComet, you agree to these Terms of Service. If you do not agree, please do not use our services. These terms apply to all users of the platform.',
            ),
            _buildSection(
              theme,
              'Community Guidelines',
              '''NeuroComet is a safe space. We expect all users to:

• Treat others with respect and kindness
• Not engage in harassment, bullying, or hate speech
• Not share content that is harmful, illegal, or inappropriate
• Respect others' privacy and boundaries
• Report concerning behavior to our moderation team''',
            ),
            _buildSection(
              theme,
              'User Content',
              '''When you post content on NeuroComet:

• You retain ownership of your content
• You grant us a license to display and distribute your content
• You are responsible for the content you share
• We may remove content that violates our guidelines''',
            ),
            _buildSection(
              theme,
              'Account Responsibilities',
              '''You are responsible for:

• Maintaining the security of your account
• All activity that occurs under your account
• Providing accurate information
• Keeping your contact information updated''',
            ),
            _buildSection(
              theme,
              'Intellectual Property',
              'NeuroComet and its original content, features, and functionality are owned by NeuroComet and are protected by international copyright, trademark, and other intellectual property laws.',
            ),
            _buildSection(
              theme,
              'Termination',
              'We reserve the right to terminate or suspend access to our service immediately, without prior notice, for conduct that we believe violates these Terms or is harmful to other users, us, or third parties.',
            ),
            _buildSection(
              theme,
              'Disclaimer',
              'NeuroComet is provided "as is" without warranties of any kind. We do not guarantee that the service will be uninterrupted, secure, or error-free.',
            ),
            _buildSection(
              theme,
              'Contact',
              'Questions about these Terms should be sent to legal@getneurocomet.com',
            ),
            const SizedBox(height: 32),
            Center(
              child: TextButton(
                onPressed: () => _launchUrl('https://getneurocomet.com/terms'),
                child: const Text('View Full Terms Online'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSection(ThemeData theme, String title, String content) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            content,
            style: theme.textTheme.bodyMedium?.copyWith(
              height: 1.6,
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _launchUrl(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }
}

/// About Screen showing app information
class AboutScreen extends StatelessWidget {
  const AboutScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('About'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            const SizedBox(height: 20),
            // App icon
            Container(
              width: 100,
              height: 100,
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    theme.colorScheme.primary,
                    theme.colorScheme.secondary,
                  ],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(24),
              ),
              child: const Icon(
                Icons.psychology,
                size: 50,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 20),
            Text(
              'NeuroComet',
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              'Version ${AppConstants.appVersion}',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.outline,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              'A safe space for neurodivergent minds',
              style: theme.textTheme.bodyLarge,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 32),
            const Divider(),
            ListTile(
              leading: const Icon(Icons.privacy_tip_outlined),
              title: const Text('Privacy Policy'),
              trailing: const Icon(Icons.chevron_right),
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const PrivacyPolicyScreen()),
              ),
            ),
            ListTile(
              leading: const Icon(Icons.description_outlined),
              title: const Text('Terms of Service'),
              trailing: const Icon(Icons.chevron_right),
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const TermsOfServiceScreen()),
              ),
            ),
            ListTile(
              leading: const Icon(Icons.article_outlined),
              title: const Text('Open Source Licenses'),
              trailing: const Icon(Icons.chevron_right),
              onTap: () => showLicensePage(
                context: context,
                applicationName: 'NeuroComet',
                applicationVersion: AppConstants.appVersion,
              ),
            ),
            const Divider(),
            const SizedBox(height: 20),
            Text(
              '© 2026 NeuroComet. All rights reserved.',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.outline,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Made with ❤️ for the neurodivergent community',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.outline,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

