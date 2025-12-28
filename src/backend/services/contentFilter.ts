import { AgeGroup } from '../middleware/ageGate';

export interface ContentFilterResult {
  allowed: boolean;
  reason?: string;
  sanitized?: string;
}

// Sample list of vulgar words - in production, this should be more comprehensive
const VULGAR_WORDS = [
  'damn',
  'hell',
  'crap',
  'ass',
  'bastard',
  'bitch',
  'shit',
  'fuck',
  'asshole',
  'dick',
  'pussy',
  'cock',
  'cum',
  'slut',
  'whore',
  'fag',
  'retard',
];

/**
 * Checks if the text contains any vulgar words.
 * @param text - The text to check
 * @returns true if vulgar words are found, false otherwise
 */
function containsVulgarWords(text: string): boolean {
  const lowerText = text.toLowerCase();
  return VULGAR_WORDS.some(word => {
    const regex = new RegExp(`\\b${word}\\b`, 'i');
    return regex.test(lowerText);
  });
}

/**
 * Sanitizes vulgar words in the text by replacing them with asterisks.
 * @param text - The text to sanitize
 * @returns The sanitized text
 */
function sanitizeVulgarWords(text: string): string {
  let sanitized = text;
  
  VULGAR_WORDS.forEach(word => {
    const regex = new RegExp(`\\b${word}\\b`, 'gi');
    const replacement = '*'.repeat(word.length);
    sanitized = sanitized.replace(regex, replacement);
  });
  
  return sanitized;
}

/**
 * Filters content based on the user's age group.
 * 
 * Behavior:
 * - UNDER_13: Block content containing vulgar words
 * - TEEN_13_17: Sanitize vulgar words (mask with ****)
 * - ADULT_18_PLUS: Allow all content
 * - UNKNOWN: Treat as UNDER_13 (most restrictive)
 * 
 * @param text - The text content to filter
 * @param ageGroup - The user's age group
 * @returns ContentFilterResult with allowed status and sanitized content if applicable
 */
export function filterContentForAge(text: string, ageGroup: AgeGroup): ContentFilterResult {
  if (!text) {
    return { allowed: true, sanitized: text };
  }

  const hasVulgarContent = containsVulgarWords(text);

  switch (ageGroup) {
    case 'UNDER_13':
    case 'UNKNOWN':
      if (hasVulgarContent) {
        return {
          allowed: false,
          reason: 'Content contains inappropriate language for users under 13',
        };
      }
      return { allowed: true, sanitized: text };

    case 'TEEN_13_17':
      if (hasVulgarContent) {
        const sanitized = sanitizeVulgarWords(text);
        return {
          allowed: true,
          sanitized,
        };
      }
      return { allowed: true, sanitized: text };

    case 'ADULT_18_PLUS':
      return { allowed: true, sanitized: text };

    default:
      // Default to most restrictive for unknown age groups
      if (hasVulgarContent) {
        return {
          allowed: false,
          reason: 'Content contains inappropriate language',
        };
      }
      return { allowed: true, sanitized: text };
  }
}
