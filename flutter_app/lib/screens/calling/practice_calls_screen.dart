import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:speech_to_text/speech_to_text.dart' as stt;
import 'package:flutter_tts/flutter_tts.dart';
import 'package:permission_handler/permission_handler.dart';
import '../../core/theme/app_colors.dart';
import '../../services/gemini_practice_call_service.dart';
import '../../core/config/gemini_env.dart';
import '../../core/utils/security_utils.dart';
import '../../core/utils/app_utils.dart';

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
  
  GeminiPracticeCallService? _geminiService;
  final List<Map<String, String>> _transcript = [];
  final TextEditingController _textController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  bool _isAIResponding = false;

  // Voice features
  late stt.SpeechToText _speech;
  late FlutterTts _flutterTts;
  bool _isListening = false;
  String _currentWords = '';

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat(reverse: true);

    _initVoice();
    _initGemini();

    // Start call after a brief delay
    Future.delayed(const Duration(seconds: 1), () {
      if (mounted) {
        setState(() => _isCallActive = true);
        _startTimer();
      }
    });
  }

  Future<void> _initVoice() async {
    _speech = stt.SpeechToText();
    _flutterTts = FlutterTts();

    // Check permissions
    final micStatus = await Permission.microphone.request();
    if (micStatus != PermissionStatus.granted) {
      debugPrint('Microphone permission denied.');
    }

    // Initialize TTS
    await _flutterTts.setLanguage("en-US");
    await _flutterTts.setSpeechRate(0.5); // Slightly slower for clarity
    await _flutterTts.setVolume(1.0);
    await _flutterTts.setPitch(1.0);
  }

  Future<void> _speakText(String text) async {
    if (_isCallActive && !_isMuted) {
      await _flutterTts.speak(text);
    }
  }

  void _startListening() async {
    if (_isMuted || _isAIResponding) return;

    final available = await _speech.initialize(
      onStatus: (status) {
        if (status == 'done' || status == 'notListening') {
          setState(() => _isListening = false);
          _submitSpokenText();
        }
      },
      onError: (error) => debugPrint('STT Error: $error'),
    );

    if (available) {
      setState(() => _isListening = true);
      _speech.listen(
        onResult: (result) {
          setState(() {
            _currentWords = result.recognizedWords;
          });
        },
      );
    }
  }

  void _stopListening() {
    if (_isListening) {
      _speech.stop();
      setState(() => _isListening = false);
    }
  }

  void _submitSpokenText() {
    if (_currentWords.trim().isNotEmpty) {
      _sendMessage(_currentWords);
      setState(() {
        _currentWords = '';
      });
    }
  }

  void _initGemini() {
    // 1) Try compile-time dart-define value first
    final envKey = const String.fromEnvironment('GEMINI_API_KEY', defaultValue: '');
    
    // 2) De-obfuscate if it looks like a hex string, otherwise use as-is
    var apiKey = SecurityUtils.decrypt(envKey).ifEmpty(() => envKey).trim();
    
    // 3) Fall back to the built-in obfuscated config (matches Android native build)
    if (apiKey.isEmpty || apiKey == 'your-gemini-key') {
      apiKey = GeminiEnv.apiKey.trim();
    }

    if (apiKey.isNotEmpty) {
      final prompt = 'You are playing the role of ${widget.persona.name}, a ${widget.persona.role}. '
          '${widget.persona.description}. '
          'The user is practicing a phone call with you. Keep your responses short, natural, and conversational, '
          'as if you are speaking on the phone. Ask engaging questions where appropriate to keep the conversation going.';
      _geminiService = GeminiPracticeCallService(apiKey, systemPrompt: prompt);

      // Initial greeting
      Future.delayed(const Duration(seconds: 3), () async {
        if (!mounted || !_isCallActive) return;
        setState(() {
          _isAIResponding = true;
        });
        
        try {
          final response = await _geminiService!.sendMessage('Hello?');
          if (mounted) {
            setState(() {
              _isAIResponding = false;
              if (response != null) {
                _transcript.add({
                  'sender': widget.persona.name,
                  'text': response,
                });
                _speakText(response); // AI speaks its response
              } else {
                _transcript.add({
                  'sender': 'System',
                  'text': 'Error: Failed to get initial response from AI.',
                });
              }
            });
            _scrollToBottom();
          }
        } catch (e) {
          if (mounted) {
            setState(() {
              _isAIResponding = false;
              _transcript.add({
                'sender': 'System',
                'text': 'Error: $e',
              });
            });
          }
        }
      });
    } else {
      _transcript.add({
        'sender': 'System',
        'text': 'GEMINI_API_KEY is not set. Simulation mode only.\n\nTo use AI, start the app with:\n--dart-define=GEMINI_API_KEY=your_key_here',
      });
    }
  }

  void _sendMessage([String? textArg]) async {
    final text = textArg ?? _textController.text.trim();
    if (text.isEmpty || _geminiService == null) return;

    if (_isListening) _stopListening();
    _flutterTts.stop(); // Stop TTS if user interrupts

    setState(() {
      _transcript.add({'sender': 'You', 'text': text});
      _isAIResponding = true;
    });
    _textController.clear();
    _scrollToBottom();

    final response = await _geminiService!.sendMessage(text);
    
    if (mounted) {
      setState(() {
        _isAIResponding = false;
        if (response != null) {
          _transcript.add({'sender': widget.persona.name, 'text': response});
          _speakText(response); // AI speaks its response
        } else {
          _transcript.add({
            'sender': 'System',
            'text': 'Error: Failed to get response from AI. Please try again.',
          });
        }
      });
      _scrollToBottom();
    }
  }

  void _scrollToBottom() {
    Future.delayed(const Duration(milliseconds: 100), () {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
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
    _speech.cancel();
    _flutterTts.stop();
    _pulseController.dispose();
    _textController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _endCall() {
    HapticFeedback.mediumImpact();
    setState(() => _isCallActive = false);
    _speech.stop();
    _flutterTts.stop();

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
                _transcript.clear();
                _currentWords = '';
              });
              _initGemini();
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
      resizeToAvoidBottomInset: true,
      body: SafeArea(
        child: Column(
          children: [
            // Top section: Avatar and Info
            Padding(
              padding: const EdgeInsets.only(top: 24, bottom: 16),
              child: Column(
                children: [
                  AnimatedBuilder(
                    animation: _pulseController,
                    builder: (context, child) {
                      return Transform.scale(
                        scale: 1 + (_pulseController.value * 0.05),
                        child: Container(
                          width: 80,
                          height: 80,
                          decoration: BoxDecoration(
                            color: widget.persona.color.withAlpha(100),
                            shape: BoxShape.circle,
                            boxShadow: _isCallActive && (_isAIResponding || _isListening)
                                ? [
                                    BoxShadow(
                                      color: _isListening ? AppColors.success.withAlpha(100) : widget.persona.color.withAlpha(100),
                                      blurRadius: 20,
                                      spreadRadius: 5,
                                    ),
                                  ]
                                : null,
                          ),
                          child: Center(
                            child: Text(
                              widget.persona.emoji,
                              style: const TextStyle(fontSize: 36),
                            ),
                          ),
                        ),
                      );
                    },
                  ),
                  const SizedBox(height: 12),
                  Text(
                    widget.persona.name,
                    style: theme.textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    _isCallActive ? _formatDuration(_callDuration) : 'Connecting...',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: _isCallActive ? AppColors.success : Colors.grey,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  if (_isListening)
                    Text(
                      'Listening...',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: AppColors.success,
                        fontStyle: FontStyle.italic,
                      ),
                    ),
                ],
              ),
            ),

            // Middle section: Live Transcript / Chat
            Expanded(
              child: Container(
                margin: const EdgeInsets.symmetric(horizontal: 16),
                decoration: BoxDecoration(
                  color: theme.colorScheme.surface,
                  borderRadius: BorderRadius.circular(16),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withAlpha(10),
                      blurRadius: 10,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(16),
                  child: Column(
                    children: [
                      Expanded(
                        child: ListView.builder(
                          controller: _scrollController,
                          padding: const EdgeInsets.all(16),
                          itemCount: _transcript.length + (_isAIResponding ? 1 : 0) + (_currentWords.isNotEmpty ? 1 : 0),
                          itemBuilder: (context, index) {
                            if (index == _transcript.length && _isAIResponding) {
                              return Align(
                                alignment: Alignment.centerLeft,
                                child: Container(
                                  margin: const EdgeInsets.only(top: 8),
                                  padding: const EdgeInsets.all(12),
                                  decoration: BoxDecoration(
                                    color: theme.colorScheme.surfaceContainerHighest,
                                    borderRadius: BorderRadius.circular(16).copyWith(
                                      bottomLeft: Radius.zero,
                                    ),
                                  ),
                                  child: const SizedBox(
                                    width: 40,
                                    child: LinearProgressIndicator(),
                                  ),
                                ),
                              );
                            }

                            if (index == _transcript.length + (_isAIResponding ? 1 : 0) && _currentWords.isNotEmpty) {
                              return Align(
                                alignment: Alignment.centerRight,
                                child: Container(
                                  margin: const EdgeInsets.only(top: 8),
                                  padding: const EdgeInsets.all(12),
                                  decoration: BoxDecoration(
                                    color: AppColors.primaryPurple.withAlpha(150),
                                    borderRadius: BorderRadius.circular(16).copyWith(
                                      bottomRight: Radius.zero,
                                    ),
                                  ),
                                  child: Text(
                                    '$_currentWords...',
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontStyle: FontStyle.italic,
                                    ),
                                  ),
                                ),
                              );
                            }

                            final msg = _transcript[index];
                            final isMe = msg['sender'] == 'You';
                            final isSystem = msg['sender'] == 'System';

                            if (isSystem) {
                              return Center(
                                child: Padding(
                                  padding: const EdgeInsets.symmetric(vertical: 8),
                                  child: Text(
                                    msg['text']!,
                                    style: TextStyle(
                                      color: theme.colorScheme.error,
                                      fontSize: 12,
                                    ),
                                  ),
                                ),
                              );
                            }

                            return Align(
                              alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
                              child: Container(
                                margin: const EdgeInsets.only(top: 8),
                                padding: const EdgeInsets.all(12),
                                decoration: BoxDecoration(
                                  color: isMe
                                      ? AppColors.primaryPurple
                                      : theme.colorScheme.surfaceContainerHighest,
                                  borderRadius: BorderRadius.circular(16).copyWith(
                                    bottomRight: isMe ? Radius.zero : const Radius.circular(16),
                                    bottomLeft: isMe ? const Radius.circular(16) : Radius.zero,
                                  ),
                                ),
                                child: Text(
                                  msg['text']!,
                                  style: TextStyle(
                                    color: isMe ? Colors.white : theme.colorScheme.onSurface,
                                  ),
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                      
                      // Input Area
                      if (_isCallActive)
                        Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: theme.colorScheme.surfaceContainerHighest.withAlpha(100),
                            border: Border(
                              top: BorderSide(color: theme.dividerColor),
                            ),
                          ),
                          child: Row(
                            children: [
                              IconButton(
                                icon: Icon(
                                  _isListening ? Icons.mic : Icons.mic_none,
                                  color: _isListening ? AppColors.error : AppColors.primaryPurple,
                                ),
                                onPressed: _isListening ? _stopListening : _startListening,
                              ),
                              Expanded(
                                child: TextField(
                                  controller: _textController,
                                  decoration: const InputDecoration(
                                    hintText: 'Type or speak...',
                                    border: InputBorder.none,
                                    contentPadding: EdgeInsets.symmetric(horizontal: 16),
                                  ),
                                  onSubmitted: (_) => _sendMessage(),
                                ),
                              ),
                              IconButton(
                                icon: const Icon(Icons.send, color: AppColors.primaryPurple),
                                onPressed: () => _sendMessage(),
                              ),
                            ],
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Call controls
            Padding(
              padding: const EdgeInsets.only(bottom: 24),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  _buildControlButton(
                    icon: _isMuted ? Icons.mic_off : Icons.mic,
                    label: _isMuted ? 'Unmute' : 'Mute',
                    color: _isMuted ? Colors.red.withAlpha(200) : Colors.white.withAlpha(200),
                    iconColor: _isMuted ? Colors.white : Colors.black,
                    onTap: () {
                      HapticFeedback.selectionClick();
                      setState(() {
                         _isMuted = !_isMuted;
                         if (_isMuted) _stopListening();
                      });
                    },
                  ),
                  _buildEndCallButton(),
                  _buildControlButton(
                    icon: _isSpeakerOn ? Icons.volume_up : Icons.volume_down,
                    label: 'Speaker',
                    color: Colors.white.withAlpha(200),
                    iconColor: Colors.black,
                    onTap: () {
                      HapticFeedback.selectionClick();
                      setState(() => _isSpeakerOn = !_isSpeakerOn);
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildControlButton({
    required IconData icon,
    required String label,
    Color color = const Color(0xCCFFFFFF),
    Color iconColor = Colors.black,
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
              color: color,
              shape: BoxShape.circle,
            ),
            child: Icon(icon, size: 28, color: iconColor),
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

