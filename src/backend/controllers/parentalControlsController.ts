import { Request, Response } from 'express';

/**
 * Parental controls data structure
 */
interface ParentalControls {
  id?: number;
  user_id: number;
  enabled: boolean;
  allow_chat: boolean;
  allow_profile_public: boolean;
  whitelist: string[];
  created_at?: Date;
  updated_at?: Date;
}

/**
 * User interface for type safety
 */
interface User {
  id: number;
  parent_id?: number;
  [key: string]: any;
}

/**
 * Extended Request with authenticated user
 */
interface AuthenticatedRequest extends Request {
  user?: User;
}

/**
 * Placeholder database operations
 * In production, replace these with actual ORM/database calls
 * (e.g., Prisma, TypeORM, Sequelize, or raw queries)
 */
const db = {
  /**
   * Find user by ID
   */
  async findUserById(userId: number): Promise<User | null> {
    // TODO: Implement with actual database
    // Example: return await prisma.user.findUnique({ where: { id: userId } });
    console.warn('db.findUserById is a placeholder - implement with actual database');
    return null;
  },

  /**
   * Find parental controls by user_id
   */
  async findParentalControlsByUserId(userId: number): Promise<ParentalControls | null> {
    // TODO: Implement with actual database
    // Example: return await prisma.parentalControls.findUnique({ where: { user_id: userId } });
    console.warn('db.findParentalControlsByUserId is a placeholder - implement with actual database');
    return null;
  },

  /**
   * Create or update parental controls
   */
  async upsertParentalControls(data: ParentalControls): Promise<ParentalControls> {
    // TODO: Implement with actual database
    // Example: return await prisma.parentalControls.upsert({
    //   where: { user_id: data.user_id },
    //   update: data,
    //   create: data,
    // });
    console.warn('db.upsertParentalControls is a placeholder - implement with actual database');
    return data;
  },
};

/**
 * GET /api/parental-controls/:childId
 * 
 * Retrieves parental controls for a child user
 * Authorization: Only the parent (child.parent_id === parent.id) may view settings
 */
export async function getParentalControls(
  req: AuthenticatedRequest,
  res: Response
): Promise<void> {
  try {
    const { childId } = req.params;
    const parentUser = req.user;

    if (!parentUser) {
      res.status(401).json({ error: 'Authentication required' });
      return;
    }

    const childIdNum = parseInt(childId, 10);
    if (isNaN(childIdNum)) {
      res.status(400).json({ error: 'Invalid child ID' });
      return;
    }

    // Fetch child user to verify parent relationship
    const childUser = await db.findUserById(childIdNum);
    if (!childUser) {
      res.status(404).json({ error: 'Child user not found' });
      return;
    }

    // Authorization check: parent must be the child's parent
    if (childUser.parent_id !== parentUser.id) {
      res.status(403).json({ error: 'Unauthorized: You are not the parent of this user' });
      return;
    }

    // Fetch parental controls
    const controls = await db.findParentalControlsByUserId(childIdNum);
    
    // Return default values if no controls exist yet
    const result: ParentalControls = controls || {
      user_id: childIdNum,
      enabled: true,
      allow_chat: false,
      allow_profile_public: false,
      whitelist: [],
    };

    res.status(200).json(result);
  } catch (error) {
    console.error('Error fetching parental controls:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/**
 * POST /api/parental-controls/:childId
 * 
 * Updates parental controls for a child user
 * Authorization: Only the parent (child.parent_id === parent.id) may update settings
 * 
 * Request body:
 * {
 *   enabled?: boolean,
 *   allow_chat?: boolean,
 *   allow_profile_public?: boolean,
 *   whitelist?: string[]
 * }
 */
export async function updateParentalControls(
  req: AuthenticatedRequest,
  res: Response
): Promise<void> {
  try {
    const { childId } = req.params;
    const parentUser = req.user;

    if (!parentUser) {
      res.status(401).json({ error: 'Authentication required' });
      return;
    }

    const childIdNum = parseInt(childId, 10);
    if (isNaN(childIdNum)) {
      res.status(400).json({ error: 'Invalid child ID' });
      return;
    }

    // Fetch child user to verify parent relationship
    const childUser = await db.findUserById(childIdNum);
    if (!childUser) {
      res.status(404).json({ error: 'Child user not found' });
      return;
    }

    // Authorization check: parent must be the child's parent
    if (childUser.parent_id !== parentUser.id) {
      res.status(403).json({ error: 'Unauthorized: You are not the parent of this user' });
      return;
    }

    // Extract and validate request body
    const {
      enabled,
      allow_chat,
      allow_profile_public,
      whitelist,
    } = req.body;

    // Validate types
    if (enabled !== undefined && typeof enabled !== 'boolean') {
      res.status(400).json({ error: 'enabled must be a boolean' });
      return;
    }
    if (allow_chat !== undefined && typeof allow_chat !== 'boolean') {
      res.status(400).json({ error: 'allow_chat must be a boolean' });
      return;
    }
    if (allow_profile_public !== undefined && typeof allow_profile_public !== 'boolean') {
      res.status(400).json({ error: 'allow_profile_public must be a boolean' });
      return;
    }
    if (whitelist !== undefined && !Array.isArray(whitelist)) {
      res.status(400).json({ error: 'whitelist must be an array' });
      return;
    }

    // Prepare data for upsert
    const controlsData: ParentalControls = {
      user_id: childIdNum,
      enabled: enabled ?? true,
      allow_chat: allow_chat ?? false,
      allow_profile_public: allow_profile_public ?? false,
      whitelist: whitelist ?? [],
    };

    // Upsert parental controls
    const updated = await db.upsertParentalControls(controlsData);

    res.status(200).json(updated);
  } catch (error) {
    console.error('Error updating parental controls:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
}
