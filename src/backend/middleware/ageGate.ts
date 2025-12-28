// Age gating middleware (TypeScript, adapt to your framework)
// Purpose: determine age group from user and attach req.ageGroup for downstream handlers.
//
// Usage: app.use(requireAuth, ageGate); // ensure user is loaded on req.user
import { Request, Response, NextFunction } from 'express';

export type AgeGroup = 'UNDER_13' | 'TEEN_13_17' | 'ADULT_18_PLUS' | 'UNKNOWN';

function computeAgeGroup(birthdate?: string | Date | null): AgeGroup {
  if (!birthdate) return 'UNKNOWN';
  const bd = new Date(birthdate);
  if (isNaN(bd.getTime())) return 'UNKNOWN';
  const diffMs = Date.now() - bd.getTime();
  const age = Math.floor(diffMs / (1000 * 60 * 60 * 24 * 365.25));
  if (age < 13) return 'UNDER_13';
  if (age < 18) return 'TEEN_13_17';
  return 'ADULT_18_PLUS';
}

export function ageGate(req: Request, res: Response, next: NextFunction) {
  try {
    // Expect req.user to be populated by auth middleware
    const user = (req as any).user;
    const birthdate = user?.birthdate ?? null;
    const ageGroup = computeAgeGroup(birthdate);
    (req as any).ageGroup = ageGroup;
    next();
  } catch (err) {
    next(err);
  }
}
