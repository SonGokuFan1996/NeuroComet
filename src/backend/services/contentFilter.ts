import { AgeGroup } from '../middleware/ageGate';

/**
 * Result of content filtering
 */
export interface ContentFilterResult {
  allowed: boolean;
  reason?: string;
  sanitized?: string;
}

/**
 * Sample list of vulgar/explicit words to filter
 * In production, use a more comprehensive list or external moderation API
 */
const VULGAR_WORDS = [
  'damn',
  'hell',
  'crap',
  'fuck',
  'shit',
  'ass',
  'bitch',
  'bastard',
  'dick',
  'cock',
  'pussy',
  'slut',
  'whore',
  // Add more words as needed for production
];

/**
 * Checks if text contains vulgar words (case-insensitive)
 * @param text - Text to check
 * @returns true if vulgar words are found
 */
function containsVulgarWords(text: string): boolean {
  const lowerText = text.toLowerCase();
  return VULGAR_WORDS.some(word => {
    const regex = new RegExp(`\\b${word}\\b`, 'i');
    return regex.test(lowerText);
  });
}

/**
 * Sanitizes text by replacing vulgar words with asterisks
 * @param text - Text to sanitize
 * @returns Sanitized text
 */
function sanitizeVulgarWords(text: string): string {
  let sanitized = text;
  VULGAR_WORDS.forEach(word => {
    const regex = new RegExp(`\\b${word}\\b`, 'gi');
    sanitized = sanitized.replace(regex, '****');
  });
  return sanitized;
}

/**
 * Filters content based on user's age group
 * 
 * Behavior:
 * - UNDER_13: Block content if vulgar words are present
 * - TEEN_13_17: Sanitize vulgar words (mask with ****)
 * - ADULT_18_PLUS: Allow all content
 * - UNKNOWN: Treat as UNDER_13 (most restrictive) for safety
 * 
 * @param text - Content text to filter
 * @param ageGroup - User's age group
 * @returns ContentFilterResult with allowed status and optional sanitized content
 */
export function filterContentForAge(
  text: string,
  ageGroup: AgeGroup
): ContentFilterResult {
  if (!text) {
    return { allowed: true };
  }

  const hasVulgarWords = containsVulgarWords(text);

  switch (ageGroup) {
    case 'UNDER_13':
    case 'UNKNOWN': // Default to most restrictive for safety
      if (hasVulgarWords) {
        return {
          allowed: false,
          reason: 'Content contains inappropriate language for users under 13',
        };
      }
      return { allowed: true };

    case 'TEEN_13_17':
      if (hasVulgarWords) {
        return {
          allowed: true,
          sanitized: sanitizeVulgarWords(text),
        };
      }
      return { allowed: true };

    case 'ADULT_18_PLUS':
      return { allowed: true };

    default:
      // Fallback to most restrictive
      if (hasVulgarWords) {
        return {
          allowed: false,
          reason: 'Content contains inappropriate language',
        };
      }
      return { allowed: true };
  }
}
