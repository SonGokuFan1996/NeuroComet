import { 
  filterContentForAge, 
  filterContentArrayForAge,
  containsVulgarWords,
  sanitizeText,
  VULGAR_WORDS 
} from '../src/backend/services/contentFilter';
import { AgeGroup } from '../src/backend/middleware/ageGate';

describe('Content Filter Service', () => {
  describe('containsVulgarWords', () => {
    it('should detect vulgar words in text', () => {
      expect(containsVulgarWords('This is damn bad')).toBe(true);
      expect(containsVulgarWords('What the hell')).toBe(true);
      expect(containsVulgarWords('This is crap')).toBe(true);
    });
    
    it('should be case insensitive', () => {
      expect(containsVulgarWords('This is DAMN bad')).toBe(true);
      expect(containsVulgarWords('What the HELL')).toBe(true);
      expect(containsVulgarWords('THIS IS CRAP')).toBe(true);
    });
    
    it('should not detect clean text', () => {
      expect(containsVulgarWords('This is a nice day')).toBe(false);
      expect(containsVulgarWords('Hello world')).toBe(false);
    });
    
    it('should handle word boundaries correctly', () => {
      expect(containsVulgarWords('condamnation')).toBe(false); // 'damn' is part of word
      expect(containsVulgarWords('hello there')).toBe(false); // 'hell' is part of word
    });
    
    it('should detect vulgar words as whole words', () => {
      expect(containsVulgarWords('damn')).toBe(true);
      expect(containsVulgarWords('hell')).toBe(true);
    });
  });
  
  describe('sanitizeText', () => {
    it('should replace vulgar words with asterisks', () => {
      expect(sanitizeText('This is damn bad')).toBe('This is **** bad');
      expect(sanitizeText('What the hell')).toBe('What the ****');
      expect(sanitizeText('This is crap')).toBe('This is ****');
    });
    
    it('should handle multiple vulgar words', () => {
      expect(sanitizeText('This damn thing is crap')).toBe('This **** thing is ****');
    });
    
    it('should be case insensitive', () => {
      expect(sanitizeText('This is DAMN bad')).toBe('This is **** bad');
      expect(sanitizeText('What the HELL')).toBe('What the ****');
    });
    
    it('should not modify clean text', () => {
      expect(sanitizeText('This is a nice day')).toBe('This is a nice day');
      expect(sanitizeText('Hello world')).toBe('Hello world');
    });
    
    it('should preserve word boundaries', () => {
      expect(sanitizeText('condamnation')).toBe('condamnation');
      expect(sanitizeText('hello there')).toBe('hello there');
    });
  });
  
  describe('filterContentForAge - UNDER_13', () => {
    it('should sanitize text fields for under-13 users', () => {
      const content = {
        title: 'This is damn cool',
        description: 'What the hell is this',
        text: 'This is crap',
        body: 'Some stupid content'
      };
      
      const filtered = filterContentForAge(content, AgeGroup.UNDER_13);
      
      expect(filtered.title).toBe('This is **** cool');
      expect(filtered.description).toBe('What the **** is this');
      expect(filtered.text).toBe('This is ****');
      expect(filtered.body).toBe('Some ****** content');
    });
    
    it('should hide mature content for under-13 users', () => {
      const matureContent = {
        title: 'Some content',
        mature: true
      };
      
      const filtered = filterContentForAge(matureContent, AgeGroup.UNDER_13);
      expect(filtered).toBeNull();
    });
    
    it('should hide age-restricted content for under-13 users', () => {
      const restrictedContent = {
        title: 'Some content',
        ageRestricted: true
      };
      
      const filtered = filterContentForAge(restrictedContent, AgeGroup.UNDER_13);
      expect(filtered).toBeNull();
    });
    
    it('should filter nested comments for under-13 users', () => {
      const content = {
        title: 'Post title',
        comments: [
          { text: 'This is damn good' },
          { text: 'Nice post', mature: true },
          { text: 'Great content' }
        ]
      };
      
      const filtered = filterContentForAge(content, AgeGroup.UNDER_13);
      
      expect(filtered.comments).toHaveLength(2);
      expect(filtered.comments[0].text).toBe('This is **** good');
      expect(filtered.comments[1].text).toBe('Great content');
    });
    
    it('should handle null or undefined content', () => {
      expect(filterContentForAge(null, AgeGroup.UNDER_13)).toBeNull();
      expect(filterContentForAge(undefined, AgeGroup.UNDER_13)).toBeUndefined();
    });
    
    it('should preserve non-text fields', () => {
      const content = {
        title: 'Clean title',
        id: 123,
        timestamp: '2025-12-28',
        metadata: { views: 100 }
      };
      
      const filtered = filterContentForAge(content, AgeGroup.UNDER_13);
      
      expect(filtered.id).toBe(123);
      expect(filtered.timestamp).toBe('2025-12-28');
      expect(filtered.metadata).toEqual({ views: 100 });
    });
  });
  
  describe('filterContentForAge - TEEN', () => {
    it('should not sanitize text for teen users', () => {
      const content = {
        title: 'This is damn cool',
        description: 'What the hell is this'
      };
      
      const filtered = filterContentForAge(content, AgeGroup.TEEN);
      
      expect(filtered.title).toBe('This is damn cool');
      expect(filtered.description).toBe('What the hell is this');
    });
    
    it('should hide explicitly mature content for teen users', () => {
      const matureContent = {
        title: 'Some content',
        mature: true
      };
      
      const filtered = filterContentForAge(matureContent, AgeGroup.TEEN);
      expect(filtered).toBeNull();
    });
    
    it('should show age-restricted (but not mature) content to teens', () => {
      const content = {
        title: 'Teen content',
        ageRestricted: true,
        mature: false
      };
      
      const filtered = filterContentForAge(content, AgeGroup.TEEN);
      expect(filtered).not.toBeNull();
      expect(filtered.title).toBe('Teen content');
    });
    
    it('should preserve all fields for teen users', () => {
      const content = {
        title: 'Content title',
        id: 456,
        comments: [
          { text: 'Comment 1' },
          { text: 'Comment 2' }
        ]
      };
      
      const filtered = filterContentForAge(content, AgeGroup.TEEN);
      
      expect(filtered).toEqual(content);
    });
  });
  
  describe('filterContentForAge - ADULT', () => {
    it('should not filter any content for adult users', () => {
      const content = {
        title: 'This is damn cool',
        description: 'What the hell is this',
        mature: true,
        ageRestricted: true
      };
      
      const filtered = filterContentForAge(content, AgeGroup.ADULT);
      
      expect(filtered).toEqual(content);
    });
    
    it('should return content as-is for adults', () => {
      const content = {
        title: 'Any content',
        text: 'With any text',
        mature: true
      };
      
      const filtered = filterContentForAge(content, AgeGroup.ADULT);
      expect(filtered).toBe(content);
    });
  });
  
  describe('filterContentArrayForAge', () => {
    it('should filter array of content for under-13 users', () => {
      const contentArray = [
        { title: 'Clean content' },
        { title: 'Mature content', mature: true },
        { title: 'Another clean one' },
        { title: 'Age restricted', ageRestricted: true }
      ];
      
      const filtered = filterContentArrayForAge(contentArray, AgeGroup.UNDER_13);
      
      expect(filtered).toHaveLength(2);
      expect(filtered[0].title).toBe('Clean content');
      expect(filtered[1].title).toBe('Another clean one');
    });
    
    it('should handle empty arrays', () => {
      const filtered = filterContentArrayForAge([], AgeGroup.UNDER_13);
      expect(filtered).toEqual([]);
    });
    
    it('should handle non-array input', () => {
      const filtered = filterContentArrayForAge(null as any, AgeGroup.UNDER_13);
      expect(filtered).toEqual([]);
    });
    
    it('should preserve all content for adult users', () => {
      const contentArray = [
        { title: 'Content 1', mature: true },
        { title: 'Content 2', ageRestricted: true },
        { title: 'Content 3' }
      ];
      
      const filtered = filterContentArrayForAge(contentArray, AgeGroup.ADULT);
      
      expect(filtered).toHaveLength(3);
      expect(filtered).toEqual(contentArray);
    });
  });
  
  describe('VULGAR_WORDS configuration', () => {
    it('should export VULGAR_WORDS constant', () => {
      expect(VULGAR_WORDS).toBeDefined();
      expect(Array.isArray(VULGAR_WORDS)).toBe(true);
    });
    
    it('should contain expected words', () => {
      expect(VULGAR_WORDS).toContain('damn');
      expect(VULGAR_WORDS).toContain('hell');
      expect(VULGAR_WORDS).toContain('crap');
      expect(VULGAR_WORDS).toContain('stupid');
    });
  });
});
