// Simple content filtering service. Replace lists and checks with your moderation engine or ML model.
// Exports filterContent that returns { allowed: boolean, reason?: string, sanitized?: string }
export type FilterResult = { allowed: boolean; reason?: string; sanitized?: string };

const VULGAR_WORDS = [
  // example list — replace with curated, localized lists or external moderation service
  'fuck', 'shit', 'bitch', 'damn', 'pussy', 'cunt'
];

function containsVulgar(text: string): boolean {
  const t = text.toLowerCase();
  return VULGAR_WORDS.some(w => {
    // simple word boundary check
    const re = new RegExp(`\\b${w}\\b`, 'i');
    return re.test(t);
  });
}

export function filterContentForAge(text: string, ageGroup: string): FilterResult {
  if (!text) return { allowed: true, sanitized: text };

  if (ageGroup === 'UNDER_13') {
    // Block any content containing vulgar words
    if (containsVulgar(text)) {
      return { allowed: false, reason: 'Contains explicit language not allowed for users under 13' };
    }
    // Could add further checks (sexual, violent, etc.)
    return { allowed: true, sanitized: text };
  }

  if (ageGroup === 'TEEN_13_17') {
    // Filter or sanitize vulgar words rather than outright blocking
    if (containsVulgar(text)) {
      const sanitized = VULGAR_WORDS.reduce((acc, w) => acc.replace(new RegExp(`\\b${w}\\b`, 'ig'), '****'), text);
      return { allowed: true, sanitized, reason: 'Sanitized vulgar language for teens' };
    }
    return { allowed: true, sanitized: text };
  }

  // Adults — allowed, but keep a log or moderation pipeline as needed
  return { allowed: true, sanitized: text };
}
