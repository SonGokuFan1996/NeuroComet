import { Request, Response, NextFunction } from 'express';

/**
 * Age group types for content filtering and parental controls
 */
export type AgeGroup = 'UNDER_13' | 'TEEN_13_17' | 'ADULT_18_PLUS' | 'UNKNOWN';

/**
 * Extended Request interface to include ageGroup
 */
export interface AgeGatedRequest extends Request {
  ageGroup?: AgeGroup;
  user?: {
    id: string;
    birthdate?: Date | string;
    [key: string]: any;
  };
}

/**
 * Computes the age group based on birthdate
 * @param birthdate - User's birthdate (Date or ISO string)
 * @returns AgeGroup classification
 */
export function computeAgeGroup(birthdate: Date | string | null | undefined): AgeGroup {
  if (!birthdate) {
    return 'UNKNOWN';
  }

  const birth = typeof birthdate === 'string' ? new Date(birthdate) : birthdate;
  
  // Handle invalid dates
  if (isNaN(birth.getTime())) {
    return 'UNKNOWN';
  }

  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  
  // Adjust age if birthday hasn't occurred this year
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--;
  }

  if (age < 13) {
    return 'UNDER_13';
  } else if (age >= 13 && age <= 17) {
    return 'TEEN_13_17';
  } else {
    return 'ADULT_18_PLUS';
  }
}

/**
 * Express middleware that computes and attaches age group to request
 * Requires user object with birthdate to be present (e.g., from auth middleware)
 */
export function ageGate(req: AgeGatedRequest, res: Response, next: NextFunction): void {
  // If user is authenticated and has a birthdate, compute age group
  if (req.user && req.user.birthdate) {
    req.ageGroup = computeAgeGroup(req.user.birthdate);
  } else {
    req.ageGroup = 'UNKNOWN';
  }
  
  next();
}
