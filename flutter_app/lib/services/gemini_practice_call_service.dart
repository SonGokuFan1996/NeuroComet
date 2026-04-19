import 'package:google_generative_ai/google_generative_ai.dart';

class GeminiPracticeCallService {
  late final GenerativeModel _model;
  late final ChatSession _chat;

  GeminiPracticeCallService(String apiKey, {String? systemPrompt}) {
    final prompt = systemPrompt ??
        'You are a professional language practice assistant. Keep your responses short, natural, and helpful for a user practicing conversation.';

    _model = GenerativeModel(
      model: 'gemini-2.0-flash',
      apiKey: apiKey,
      systemInstruction: Content.system(prompt),
    );

    _chat = _model.startChat();
  }

  Future<String?> sendMessage(String message) async {
    try {
      final response = await _chat.sendMessage(Content.text(message));
      if (response.text == null || response.text!.isEmpty) {
        return 'Error: AI returned an empty response.';
      }
      return response.text;
    } catch (e) {
      return 'Error: $e';
    }
  }
}

