import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_colors.dart';

/// Practice Calls Screen - Practice phone calls with AI personas
class PracticeCallsScreen extends ConsumerWidget {
  const PracticeCallsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {

    return Scaffold(
      appBar: AppBar(
        title: const Text('Practice Calls'),
        actions: [
          IconButton(
            icon: const Icon(Icons.info_outline),
            onPressed: () => _showInfo(context),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Introduction card
          _buildIntroCard(context),
          const SizedBox(height: 24),

          // Personas section
          _buildSectionHeader(context, 'Choose a Persona'),
          const SizedBox(height: 12),

          ..._personas.map((persona) => _PersonaCard(
            persona: persona,
            onTap: () => _startPracticeCall(context, persona),
          )),

          const SizedBox(height: 24),

          // Tips section
          _buildSectionHeader(context, 'Tips for Practice'),
          const SizedBox(height: 12),
          _buildTipsCard(context),
          const SizedBox(height: 32),
        ],
      ),
    );
  }

  Widget _buildIntroCard(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.calmBlue.withAlpha(50),
            AppColors.calmLavender.withAlpha(50),
          ],
        ),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.phone_in_talk, size: 32, color: AppColors.primaryPurple),
              const SizedBox(width: 12),
              Text(
                'Build Confidence',
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            'Practice phone conversations with AI personas in a safe, judgment-free environment. Great for building communication skills!',
            style: theme.textTheme.bodyMedium?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Text(
      title,
      style: Theme.of(context).textTheme.titleMedium?.copyWith(
        fontWeight: FontWeight.bold,
      ),
    );
  }

  Widget _buildTipsCard(BuildContext context) {

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildTip(context, '🎯', 'Start Simple', 'Begin with casual conversations before trying complex scenarios.'),
            const SizedBox(height: 12),
            _buildTip(context, '🔄', 'Repeat as Needed', 'Practice the same scenario multiple times to build confidence.'),
            const SizedBox(height: 12),
            _buildTip(context, '⏸️', 'Take Breaks', 'It\'s okay to pause or end a call anytime.'),
            const SizedBox(height: 12),
            _buildTip(context, '💜', 'Be Kind to Yourself', 'There\'s no wrong way to practice. Every attempt helps!'),
          ],
        ),
      ),
    );
  }

  Widget _buildTip(BuildContext context, String emoji, String title, String description) {
    final theme = Theme.of(context);

    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(emoji, style: const TextStyle(fontSize: 20)),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
              ),
              Text(
                description,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  void _showInfo(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('About Practice Calls'),
        content: const Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Practice Calls let you rehearse phone conversations with AI personas who understand neurodivergent experiences.'),
            SizedBox(height: 12),
            Text('• Conversations are private and not recorded'),
            SizedBox(height: 4),
            Text('• AI responses are understanding and patient'),
            SizedBox(height: 4),
            Text('• You can end the call at any time'),
            SizedBox(height: 4),
            Text('• Practice as many times as you want'),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Got it!'),
          ),
        ],
      ),
    );
  }

  void _startPracticeCall(BuildContext context, PracticePersona persona) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => PracticeCallScreen(persona: persona),
      ),
    );
  }
}

final List<PracticePersona> _personas = [
  PracticePersona(
    id: 'friendly_neighbor',
    name: 'Jordan',
    role: 'Friendly Neighbor',
    description: 'Practice casual small talk and everyday conversations',
    emoji: '👋',
    color: AppColors.calmGreen,
    scenarios: ['Saying hello', 'Asking about their day', 'Discussing weather'],
    difficulty: 'Easy',
  ),
  PracticePersona(
    id: 'doctor_receptionist',
    name: 'Alex',
    role: 'Doctor\'s Receptionist',
    description: 'Practice scheduling appointments and asking about procedures',
    emoji: '🏥',
    color: AppColors.calmBlue,
    scenarios: ['Scheduling appointment', 'Rescheduling', 'Asking questions'],
    difficulty: 'Medium',
  ),
  PracticePersona(
    id: 'job_interviewer',
    name: 'Morgan',
    role: 'Job Interviewer',
    description: 'Practice answering interview questions confidently',
    emoji: '💼',
    color: AppColors.accentOrange,
    scenarios: ['Introduction', 'Strengths/weaknesses', 'Experience'],
    difficulty: 'Hard',
  ),
  PracticePersona(
    id: 'customer_service',
    name: 'Sam',
    role: 'Customer Service Rep',
    description: 'Practice making complaints or asking for help politely',
    emoji: '📞',
    color: AppColors.secondaryTeal,
    scenarios: ['Product issue', 'Refund request', 'General inquiry'],
    difficulty: 'Medium',
  ),
  PracticePersona(
    id: 'pizza_order',
    name: 'Jamie',
    role: 'Pizza Place Employee',
    description: 'Practice ordering food over the phone',
    emoji: '🍕',
    color: AppColors.categoryADHD,
    scenarios: ['Placing order', 'Special requests', 'Asking about menu'],
    difficulty: 'Easy',
  ),
];

class PracticePersona {
  final String id;
  final String name;
  final String role;
  final String description;
  final String emoji;
  final Color color;
  final List<String> scenarios;
  final String difficulty;

  const PracticePersona({
    required this.id,
    required this.name,
    required this.role,
    required this.description,
    required this.emoji,
    required this.color,
    required this.scenarios,
    required this.difficulty,
  });
}

class _PersonaCard extends StatelessWidget {
  final PracticePersona persona;
  final VoidCallback onTap;

  const _PersonaCard({required this.persona, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              // Avatar
              Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  color: persona.color.withAlpha(50),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Center(
                  child: Text(
                    persona.emoji,
                    style: const TextStyle(fontSize: 28),
                  ),
                ),
              ),
              const SizedBox(width: 16),

              // Info
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Text(
                          persona.name,
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                          decoration: BoxDecoration(
                            color: _getDifficultyColor(persona.difficulty).withAlpha(30),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            persona.difficulty,
                            style: theme.textTheme.labelSmall?.copyWith(
                              color: _getDifficultyColor(persona.difficulty),
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                      ],
                    ),
                    Text(
                      persona.role,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: persona.color,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      persona.description,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),

              // Arrow
              Icon(
                Icons.arrow_forward_ios,
                size: 16,
                color: theme.colorScheme.outline,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Color _getDifficultyColor(String difficulty) {
    switch (difficulty) {
      case 'Easy':
        return AppColors.success;
      case 'Medium':
        return AppColors.warning;
      case 'Hard':
        return AppColors.error;
      default:
        return AppColors.categoryGeneral;
    }
  }
}

/// Active practice call screen
class PracticeCallScreen extends StatefulWidget {
  final PracticePersona persona;

  const PracticeCallScreen({super.key, required this.persona});

  @override
  State<PracticeCallScreen> createState() => _PracticeCallScreenState();
}

class _PracticeCallScreenState extends State<PracticeCallScreen>
    with TickerProviderStateMixin {
  bool _isCallActive = false;
  bool _isMuted = false;
  bool _isSpeakerOn = false;
  int _callDuration = 0;
  late AnimationController _pulseController;

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat(reverse: true);

    // Start call after a brief delay
    Future.delayed(const Duration(seconds: 1), () {
      if (mounted) {
        setState(() => _isCallActive = true);
        _startTimer();
      }
    });
  }

  void _startTimer() {
    Future.doWhile(() async {
      await Future.delayed(const Duration(seconds: 1));
      if (mounted && _isCallActive) {
        setState(() => _callDuration++);
        return true;
      }
      return false;
    });
  }

  @override
  void dispose() {
    _pulseController.dispose();
    super.dispose();
  }

  void _endCall() {
    HapticFeedback.mediumImpact();
    setState(() => _isCallActive = false);

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        title: const Text('Call Ended'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Duration: ${_formatDuration(_callDuration)}'),
            const SizedBox(height: 16),
            const Text('How did that feel?'),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _buildFeedbackButton('😟', 'Tough'),
                _buildFeedbackButton('😐', 'Okay'),
                _buildFeedbackButton('😊', 'Good'),
                _buildFeedbackButton('🎉', 'Great'),
              ],
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              Navigator.pop(context);
            },
            child: const Text('Done'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(context);
              setState(() {
                _isCallActive = true;
                _callDuration = 0;
              });
              _startTimer();
            },
            child: const Text('Practice Again'),
          ),
        ],
      ),
    );
  }

  Widget _buildFeedbackButton(String emoji, String label) {
    return GestureDetector(
      onTap: () => HapticFeedback.selectionClick(),
      child: Column(
        children: [
          Text(emoji, style: const TextStyle(fontSize: 28)),
          Text(label, style: const TextStyle(fontSize: 12)),
        ],
      ),
    );
  }

  String _formatDuration(int seconds) {
    final minutes = seconds ~/ 60;
    final secs = seconds % 60;
    return '${minutes.toString().padLeft(2, '0')}:${secs.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: widget.persona.color.withAlpha(30),
      body: SafeArea(
        child: Column(
          children: [
            const Spacer(),

            // Persona info
            AnimatedBuilder(
              animation: _pulseController,
              builder: (context, child) {
                return Transform.scale(
                  scale: 1 + (_pulseController.value * 0.05),
                  child: Container(
                    width: 120,
                    height: 120,
                    decoration: BoxDecoration(
                      color: widget.persona.color.withAlpha(100),
                      shape: BoxShape.circle,
                      boxShadow: _isCallActive
                          ? [
                              BoxShadow(
                                color: widget.persona.color.withAlpha(100),
                                blurRadius: 30,
                                spreadRadius: 10,
                              ),
                            ]
                          : null,
                    ),
                    child: Center(
                      child: Text(
                        widget.persona.emoji,
                        style: const TextStyle(fontSize: 56),
                      ),
                    ),
                  ),
                );
              },
            ),
            const SizedBox(height: 24),

            Text(
              widget.persona.name,
              style: theme.textTheme.headlineMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            Text(
              widget.persona.role,
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 8),

            // Call status
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: _isCallActive ? AppColors.success.withAlpha(30) : Colors.grey.withAlpha(30),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                _isCallActive ? _formatDuration(_callDuration) : 'Connecting...',
                style: theme.textTheme.titleMedium?.copyWith(
                  color: _isCallActive ? AppColors.success : Colors.grey,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),

            const Spacer(),

            // Hint text
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 32),
              child: Text(
                'This is a practice call with an AI. Speak naturally - there are no wrong answers!',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                textAlign: TextAlign.center,
              ),
            ),
            const SizedBox(height: 24),

            // Call controls
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _buildControlButton(
                  icon: _isMuted ? Icons.mic_off : Icons.mic,
                  label: _isMuted ? 'Unmute' : 'Mute',
                  onTap: () {
                    HapticFeedback.selectionClick();
                    setState(() => _isMuted = !_isMuted);
                  },
                ),
                _buildControlButton(
                  icon: _isSpeakerOn ? Icons.volume_up : Icons.volume_down,
                  label: 'Speaker',
                  onTap: () {
                    HapticFeedback.selectionClick();
                    setState(() => _isSpeakerOn = !_isSpeakerOn);
                  },
                ),
                _buildEndCallButton(),
              ],
            ),
            const SizedBox(height: 48),
          ],
        ),
      ),
    );
  }

  Widget _buildControlButton({
    required IconData icon,
    required String label,
    required VoidCallback onTap,
  }) {
    return Column(
      children: [
        GestureDetector(
          onTap: onTap,
          child: Container(
            width: 56,
            height: 56,
            decoration: BoxDecoration(
              color: Colors.white.withAlpha(200),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, size: 28),
          ),
        ),
        const SizedBox(height: 8),
        Text(label, style: const TextStyle(fontSize: 12)),
      ],
    );
  }

  Widget _buildEndCallButton() {
    return Column(
      children: [
        GestureDetector(
          onTap: _endCall,
          child: Container(
            width: 64,
            height: 64,
            decoration: const BoxDecoration(
              color: AppColors.error,
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.call_end,
              size: 32,
              color: Colors.white,
            ),
          ),
        ),
        const SizedBox(height: 8),
        const Text('End', style: TextStyle(fontSize: 12)),
      ],
    );
  }
}

