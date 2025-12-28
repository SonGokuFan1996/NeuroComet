import { AgeGroup } from '../middleware/ageGate';

/**
 * Configurable list of vulgar words to filter
 * Note: This is a basic example. In production, use a comprehensive profanity library.
 */
export const VULGAR_WORDS = [
  'damn',
  'hell',
  'crap',
  'stupid',
  // Add more words as needed
];

/**
 * Normalize text for comparison (lowercase, trim)
 */
function normalizeText(text: string): string {
  return text.toLowerCase().trim();
}

/**
 * Check if text contains any vulgar words
 */
export function containsVulgarWords(text: string): boolean {
  const normalized = normalizeText(text);
  return VULGAR_WORDS.some(word => {
    const pattern = new RegExp(`\\b${word}\\b`, 'i');
    return pattern.test(normalized);
  });
}

/**
 * Sanitize text by replacing vulgar words with asterisks
 */
export function sanitizeText(text: string): string {
  let sanitized = text;
  VULGAR_WORDS.forEach(word => {
    const pattern = new RegExp(`\\b${word}\\b`, 'gi');
    sanitized = sanitized.replace(pattern, '*'.repeat(word.length));
  });
  return sanitized;
}

/**
 * Filter content based on age group
 * @param content - The content object to filter
 * @param ageGroup - User's age group
 * @returns Filtered content object
 */
export function filterContentForAge(content: any, ageGroup: AgeGroup): any {
  if (!content) return content;
  
  // For UNDER_13 users, apply strict filtering
  if (ageGroup === AgeGroup.UNDER_13) {
    const filtered = { ...content };
    
    // Filter text fields
    if (filtered.title) {
      filtered.title = sanitizeText(filtered.title);
    }
    if (filtered.description) {
      filtered.description = sanitizeText(filtered.description);
    }
    if (filtered.text) {
      filtered.text = sanitizeText(filtered.text);
    }
    if (filtered.body) {
      filtered.body = sanitizeText(filtered.body);
    }
    
    // Hide mature content flags
    if (filtered.mature || filtered.ageRestricted) {
      return null; // Don't show mature content to under-13 users
    }
    
    // Filter comments if present
    if (filtered.comments && Array.isArray(filtered.comments)) {
      filtered.comments = filtered.comments
        .map((comment: any) => filterContentForAge(comment, ageGroup))
        .filter((comment: any) => comment !== null);
    }
    
    return filtered;
  }
  
  // For TEEN users, apply moderate filtering
  if (ageGroup === AgeGroup.TEEN) {
    const filtered = { ...content };
    
    // Filter only explicitly mature content
    if (filtered.mature === true) {
      return null;
    }
    
    return filtered;
  }
  
  // For ADULT users, no filtering
  return content;
}

/**
 * Filter an array of content items based on age group
 */
export function filterContentArrayForAge(contentArray: any[], ageGroup: AgeGroup): any[] {
  if (!Array.isArray(contentArray)) return [];
  
  return contentArray
    .map(item => filterContentForAge(item, ageGroup))
    .filter(item => item !== null);
}
