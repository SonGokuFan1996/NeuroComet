import { Request, Response, NextFunction } from 'express';

export type AgeGroup = 'UNDER_13' | 'TEEN_13_17' | 'ADULT_18_PLUS' | 'UNKNOWN';

interface User {
  birthdate?: Date | string | null;
}

interface RequestWithAgeGroup extends Request {
  ageGroup?: AgeGroup;
  user?: User;
}

/**
 * Computes the age group based on the user's birthdate.
 * @param birthdate - The user's birthdate (Date, string, or null)
 * @returns The computed AgeGroup
 */
export function computeAgeGroup(birthdate: Date | string | null | undefined): AgeGroup {
  if (!birthdate) {
    return 'UNKNOWN';
  }

  const birthDate = typeof birthdate === 'string' ? new Date(birthdate) : birthdate;
  
  if (isNaN(birthDate.getTime())) {
    return 'UNKNOWN';
  }

  const today = new Date();
  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();
  
  // Adjust age if birthday hasn't occurred yet this year
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
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
 * Express middleware that computes and attaches the age group to the request object.
 * Requires req.user to be populated (e.g., by authentication middleware).
 */
export function ageGate(req: RequestWithAgeGroup, res: Response, next: NextFunction): void {
  const user = req.user;
  
  if (!user) {
    req.ageGroup = 'UNKNOWN';
    return next();
  }

  req.ageGroup = computeAgeGroup(user.birthdate);
  next();
}
