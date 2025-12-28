// Express-style controller endpoints for parental controls.
// Key endpoints:
// - GET /api/parental-controls/:childId
// - POST /api/parental-controls/:childId (update settings)
// - POST /api/parental-controls/:childId/whitelist (add allowed content/user)
// Adapt DB calls to your ORM (pseudo-implementation using await db.*)
import { Request, Response } from 'express';

// Example DB operations are placeholders — replace with your DB/ORM integration
const db = (global as any).db;

export async function getParentalControls(req: Request, res: Response) {
  const parent = (req as any).user;
  const { childId } = req.params;
  // Authorization: ensure parent is parent of child
  const child = await db.users.findById(childId);
  if (!child) return res.status(404).json({ error: 'Child not found' });
  if (child.parent_id !== parent.id) return res.status(403).json({ error: 'Not authorized' });

  const settings = await db.parental_controls.findOne({ user_id: childId });
  return res.json({ settings });
}

export async function updateParentalControls(req: Request, res: Response) {
  const parent = (req as any).user;
  const { childId } = req.params;
  const payload = req.body; // e.g. { enabled: true, allow_chat: false, whitelist: [...] }

  const child = await db.users.findById(childId);
  if (!child) return res.status(404).json({ error: 'Child not found' });
  if (child.parent_id !== parent.id) return res.status(403).json({ error: 'Not authorized' });
  if (child.age && child.age >= 13) {
    return res.status(400).json({ error: 'Parental controls only applicable for children under 13' });
  }

  // Upsert parental controls
  const updated = await db.parental_controls.upsert({ user_id: childId, ...payload });
  return res.json({ settings: updated });
}
