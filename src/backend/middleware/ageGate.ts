import { Request, Response, NextFunction } from 'express';

/**
 * Age groups based on user's birthdate
 */
export enum AgeGroup {
  UNDER_13 = 'UNDER_13',
  TEEN = 'TEEN',
  ADULT = 'ADULT',
}

/**
 * Compute age group from birthdate using calendar-aware calculation
 * @param birthdate - ISO date string or Date object
 * @returns AgeGroup enum value
 */
export function computeAgeGroup(birthdate: string | Date): AgeGroup {
  const birth = typeof birthdate === 'string' ? new Date(birthdate) : birthdate;
  const today = new Date();
  
  // Calculate age based on calendar years
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  const dayDiff = today.getDate() - birth.getDate();
  
  // Adjust if birthday hasn't occurred this year yet
  if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
    age--;
  }
  
  if (age < 13) {
    return AgeGroup.UNDER_13;
  } else if (age < 18) {
    return AgeGroup.TEEN;
  } else {
    return AgeGroup.ADULT;
  }
}

/**
 * Age gate middleware - ensures user has a valid birthdate and age group
 */
export function ageGate(req: Request, res: Response, next: NextFunction): void {
  try {
    const user = (req as any).user; // Assumes auth middleware sets req.user
    
    if (!user) {
      res.status(401).json({ error: 'Unauthorized' });
      return;
    }
    
    if (!user.birthdate) {
      res.status(403).json({ 
        error: 'Age verification required',
        message: 'Please provide your birthdate to continue'
      });
      return;
    }
    
    // Compute and attach age group to request
    const ageGroup = computeAgeGroup(user.birthdate);
    (req as any).ageGroup = ageGroup;
    (req as any).userAge = ageGroup;
    
    next();
  } catch (error) {
    res.status(500).json({ error: 'Age verification failed' });
  }
}
