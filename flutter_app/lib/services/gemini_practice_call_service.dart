import 'package:google_generative_ai/google_generative_ai.dart';

class GeminiPracticeCallService {
  late final GenerativeModel _model;
  late final ChatSession _chat;

  GeminiPracticeCallService(String apiKey, {String? systemPrompt}) {
    final prompt = systemPrompt ??
        'You are a professional language practice assistant. Keep your responses short, natural, and helpful for a user practicing conversation.';

    _model = GenerativeModel(
      model: 'gemini-2.0-flash-lite-preview-02-05',
      apiKey: apiKey,
      systemInstruction: Content.system(prompt),
    );

    _chat = _model.startChat();
  }

  Future<String?> sendMessage(String message) async {
    try {
      final response = await _chat.sendMessage(Content.text(message));
      return response.text;
    } catch (e) {
      print('Error communicating with Gemini: $e');
      return null;
    }
  }
}

