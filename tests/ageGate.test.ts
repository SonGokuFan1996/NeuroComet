import { computeAgeGroup, AgeGroup } from '../src/backend/middleware/ageGate';

describe('computeAgeGroup', () => {
  // Mock the current date for consistent testing
  const mockToday = new Date('2025-12-28');
  
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(mockToday);
  });
  
  afterEach(() => {
    jest.useRealTimers();
  });
  
  describe('UNDER_13 age group', () => {
    it('should classify 12-year-old as UNDER_13', () => {
      const birthdate = new Date('2013-06-15'); // 12 years old
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.UNDER_13);
    });
    
    it('should classify newborn as UNDER_13', () => {
      const birthdate = new Date('2025-01-01'); // Less than 1 year
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.UNDER_13);
    });
    
    it('should classify user turning 13 tomorrow as UNDER_13', () => {
      const birthdate = new Date('2012-12-29'); // Turns 13 tomorrow
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.UNDER_13);
    });
    
    it('should handle string birthdate for UNDER_13', () => {
      const birthdate = '2013-06-15'; // 12 years old
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.UNDER_13);
    });
  });
  
  describe('TEEN age group', () => {
    it('should classify 13-year-old as TEEN', () => {
      const birthdate = new Date('2012-06-15'); // 13 years old
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.TEEN);
    });
    
    it('should classify 17-year-old as TEEN', () => {
      const birthdate = new Date('2008-06-15'); // 17 years old
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.TEEN);
    });
    
    it('should classify user who just turned 13 today as TEEN', () => {
      const birthdate = new Date('2012-12-28'); // Turns 13 today
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.TEEN);
    });
    
    it('should classify user turning 18 tomorrow as TEEN', () => {
      const birthdate = new Date('2007-12-29'); // Turns 18 tomorrow
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.TEEN);
    });
    
    it('should handle string birthdate for TEEN', () => {
      const birthdate = '2010-01-01'; // 15 years old
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.TEEN);
    });
  });
  
  describe('ADULT age group', () => {
    it('should classify 18-year-old as ADULT', () => {
      const birthdate = new Date('2007-06-15'); // 18 years old
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.ADULT);
    });
    
    it('should classify 25-year-old as ADULT', () => {
      const birthdate = new Date('2000-06-15'); // 25 years old
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.ADULT);
    });
    
    it('should classify user who just turned 18 today as ADULT', () => {
      const birthdate = new Date('2007-12-28'); // Turns 18 today
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.ADULT);
    });
    
    it('should handle string birthdate for ADULT', () => {
      const birthdate = '1990-01-01'; // 35 years old
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.ADULT);
    });
  });
  
  describe('Edge cases and calendar awareness', () => {
    it('should correctly handle birthday not yet occurred this year', () => {
      // Mock date is 2025-12-28
      // Person born 2012-12-29 has not had birthday yet this year
      const birthdate = new Date('2012-12-29');
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.UNDER_13); // Still 12
    });
    
    it('should correctly handle birthday already occurred this year', () => {
      // Mock date is 2025-12-28
      // Person born 2012-12-27 already had birthday this year
      const birthdate = new Date('2012-12-27');
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.TEEN); // Now 13
    });
    
    it('should correctly handle leap year birthdate', () => {
      // Mock date is 2025-12-28
      const birthdate = new Date('2012-02-29'); // Leap year birthday
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.TEEN); // 13 years old
    });
    
    it('should handle same month but different day', () => {
      // Mock date is 2025-12-28
      const birthdateBefore = new Date('2012-12-15'); // Birthday already passed
      const birthdateAfter = new Date('2012-12-31'); // Birthday hasn't passed
      
      expect(computeAgeGroup(birthdateBefore)).toBe(AgeGroup.TEEN); // 13
      expect(computeAgeGroup(birthdateAfter)).toBe(AgeGroup.UNDER_13); // Still 12
    });
    
    it('should handle exact birthday today', () => {
      const birthdate = new Date('2012-12-28'); // Exact 13th birthday today
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.TEEN);
    });
  });
  
  describe('Boundary testing', () => {
    it('should correctly classify at 12 years 364 days', () => {
      // Just before 13th birthday
      const birthdate = new Date('2012-12-29');
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.UNDER_13);
    });
    
    it('should correctly classify at exactly 13 years', () => {
      const birthdate = new Date('2012-12-28');
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.TEEN);
    });
    
    it('should correctly classify at 17 years 364 days', () => {
      // Just before 18th birthday
      const birthdate = new Date('2007-12-29');
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.TEEN);
    });
    
    it('should correctly classify at exactly 18 years', () => {
      const birthdate = new Date('2007-12-28');
      expect(computeAgeGroup(birthdate)).toBe(AgeGroup.ADULT);
    });
  });
});
