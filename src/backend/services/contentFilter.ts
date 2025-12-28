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
 * Sample list of vulgar/inappropriate words to filter
 * In production, this should be maintained in a separate configuration or database
 */
const VULGAR_WORDS: string[] = [
  'damn',
  'hell',
  'crap',
  'stupid',
  'idiot',
  'dumb',
  'ass',
  'bitch',
  'bastard',
  'fuck',
  'shit',
  'piss',
];

/**
 * Creates a regex pattern to match vulgar words (case-insensitive, word boundaries)
 */
function createVulgarWordsPattern(): RegExp {
  const pattern = VULGAR_WORDS.map(word => `\\b${word}\\b`).join('|');
  return new RegExp(pattern, 'gi');
}

/**
 * Checks if text contains vulgar words
 */
function containsVulgarWords(text: string): boolean {
  const pattern = createVulgarWordsPattern();
  return pattern.test(text);
}

/**
 * Sanitizes text by masking vulgar words with asterisks
 */
function sanitizeVulgarWords(text: string): string {
  const pattern = createVulgarWordsPattern();
  return text.replace(pattern, (match) => '*'.repeat(match.length));
}

/**
 * Filters content based on user's age group
 * 
 * @param text - The content text to filter
 * @param ageGroup - The user's age group
 * @returns Result indicating if content is allowed and any sanitized version
 * 
 * Behavior:
 * - UNDER_13: Block content if vulgar words are present
 * - TEEN_13_17: Sanitize vulgar words (mask with ****)
 * - ADULT_18_PLUS: Allow all content
 * - UNKNOWN: Treat as UNDER_13 for safety
 */
export function filterContentForAge(
  text: string,
  ageGroup: AgeGroup
): ContentFilterResult {
  if (!text) {
    return { allowed: true, sanitized: text };
  }

  // Adults get unrestricted access
  if (ageGroup === 'ADULT_18_PLUS') {
    return { allowed: true, sanitized: text };
  }

  const hasVulgarContent = containsVulgarWords(text);

  // Under 13 or unknown users: block if vulgar content present
  if (ageGroup === 'UNDER_13' || ageGroup === 'UNKNOWN') {
    if (hasVulgarContent) {
      return {
        allowed: false,
        reason: 'Content contains inappropriate language for your age group',
      };
    }
    return { allowed: true, sanitized: text };
  }

  // Teens: sanitize vulgar words
  if (ageGroup === 'TEEN_13_17') {
    if (hasVulgarContent) {
      return {
        allowed: true,
        sanitized: sanitizeVulgarWords(text),
      };
    }
    return { allowed: true, sanitized: text };
  }

  // Default: allow
  return { allowed: true, sanitized: text };
}
