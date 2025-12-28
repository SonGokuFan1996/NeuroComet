import { Request, Response, NextFunction } from 'express';

/**
 * Age group classifications based on user's birthdate
 */
export type AgeGroup = 'UNDER_13' | 'TEEN_13_17' | 'ADULT_18_PLUS' | 'UNKNOWN';

/**
 * Extends Express Request to include age group information
 */
declare global {
  namespace Express {
    interface Request {
      ageGroup?: AgeGroup;
    }
  }
}

/**
 * Computes the age group based on a birthdate
 * @param birthdate - The user's birthdate as a Date object or ISO string
 * @returns The computed age group
 */
export function computeAgeGroup(birthdate: Date | string | null | undefined): AgeGroup {
  if (!birthdate) {
    return 'UNKNOWN';
  }

  const birth = typeof birthdate === 'string' ? new Date(birthdate) : birthdate;
  
  // Validate date
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

  if (age < 0) {
    return 'UNKNOWN';
  } else if (age < 13) {
    return 'UNDER_13';
  } else if (age < 18) {
    return 'TEEN_13_17';
  } else {
    return 'ADULT_18_PLUS';
  }
}

/**
 * Express middleware that computes and attaches age group to the request
 * Expects user information to be available at req.user (from authentication middleware)
 */
export function ageGate(req: Request, res: Response, next: NextFunction): void {
  // Extract birthdate from user object (assumes auth middleware has populated req.user)
  const user = (req as any).user;
  const birthdate = user?.birthdate;

  // Compute and attach age group to request
  req.ageGroup = computeAgeGroup(birthdate);

  next();
}
