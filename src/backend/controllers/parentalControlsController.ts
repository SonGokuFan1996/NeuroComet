import { Request, Response } from 'express';
import { computeAgeGroup, AgeGroup } from '../middleware/ageGate';

/**
 * Whitelist categories for parental controls
 */
const VALID_WHITELIST_CATEGORIES = [
  'educational',
  'entertainment',
  'social',
  'gaming',
  'news',
  'sports',
  'music',
  'art',
];

/**
 * Validate parental controls input
 */
function validateParentalControlsInput(data: any): { valid: boolean; errors: string[] } {
  const errors: string[] = [];
  
  if (data.contentRestrictionLevel !== undefined) {
    const validLevels = ['strict', 'moderate', 'permissive'];
    if (!validLevels.includes(data.contentRestrictionLevel)) {
      errors.push(`Invalid contentRestrictionLevel. Must be one of: ${validLevels.join(', ')}`);
    }
  }
  
  if (data.allowedContentTypes !== undefined) {
    if (!Array.isArray(data.allowedContentTypes)) {
      errors.push('allowedContentTypes must be an array');
    } else {
      const invalidTypes = data.allowedContentTypes.filter(
        (type: string) => !VALID_WHITELIST_CATEGORIES.includes(type)
      );
      if (invalidTypes.length > 0) {
        errors.push(`Invalid content types: ${invalidTypes.join(', ')}. Valid types: ${VALID_WHITELIST_CATEGORIES.join(', ')}`);
      }
    }
  }
  
  if (data.screenTimeLimit !== undefined) {
    if (typeof data.screenTimeLimit !== 'number' || data.screenTimeLimit < 0) {
      errors.push('screenTimeLimit must be a positive number');
    }
  }
  
  if (data.requireApproval !== undefined) {
    if (typeof data.requireApproval !== 'boolean') {
      errors.push('requireApproval must be a boolean');
    }
  }
  
  return {
    valid: errors.length === 0,
    errors,
  };
}

/**
 * GET /api/parental-controls/:childId
 * Get parental controls for a child user
 */
export async function getParentalControls(req: Request, res: Response): Promise<void> {
  try {
    const { childId } = req.params;
    const parentUser = (req as any).user;
    
    if (!parentUser) {
      res.status(401).json({ error: 'Unauthorized' });
      return;
    }
    
    if (!childId) {
      res.status(400).json({ error: 'Child ID is required' });
      return;
    }
    
    // Placeholder: Replace with actual database query
    // const db = (global as any).db;
    // const controls = await db.parentalControls.findOne({ where: { userId: childId, parentId: parentUser.id } });
    
    // For now, return a mock response
    const controls = {
      userId: childId,
      parentId: parentUser.id,
      contentRestrictionLevel: 'strict',
      allowedContentTypes: ['educational', 'entertainment'],
      screenTimeLimit: 120, // minutes
      requireApproval: true,
    };
    
    res.json(controls);
  } catch (error) {
    console.error('Error fetching parental controls:', error);
    res.status(500).json({ error: 'Failed to fetch parental controls' });
  }
}

/**
 * POST /api/parental-controls/:childId
 * Update parental controls for a child user
 */
export async function updateParentalControls(req: Request, res: Response): Promise<void> {
  try {
    const { childId } = req.params;
    const parentUser = (req as any).user;
    const updates = req.body;
    
    if (!parentUser) {
      res.status(401).json({ error: 'Unauthorized' });
      return;
    }
    
    if (!childId) {
      res.status(400).json({ error: 'Child ID is required' });
      return;
    }
    
    // Validate input
    const validation = validateParentalControlsInput(updates);
    if (!validation.valid) {
      res.status(400).json({ 
        error: 'Invalid input', 
        details: validation.errors 
      });
      return;
    }
    
    // Server-side age check: verify child is actually under 13
    // Placeholder: Replace with actual database query to get child user
    // const db = (global as any).db;
    // const childUser = await db.users.findById(childId);
    
    // For demonstration, create a mock child user
    const childUser = {
      id: childId,
      birthdate: '2015-01-01', // Mock birthdate
    };
    
    if (!childUser || !childUser.birthdate) {
      res.status(400).json({ error: 'Child user not found or missing birthdate' });
      return;
    }
    
    const childAgeGroup = computeAgeGroup(childUser.birthdate);
    if (childAgeGroup !== AgeGroup.UNDER_13) {
      res.status(400).json({ 
        error: 'Parental controls only apply to users under 13',
        ageGroup: childAgeGroup 
      });
      return;
    }
    
    // Upsert parental controls
    // Placeholder: Replace with actual database upsert
    // const controls = await db.parentalControls.upsert({
    //   userId: childId,
    //   parentId: parentUser.id,
    //   ...updates,
    // });
    
    const controls = {
      userId: childId,
      parentId: parentUser.id,
      contentRestrictionLevel: updates.contentRestrictionLevel || 'strict',
      allowedContentTypes: updates.allowedContentTypes || ['educational'],
      screenTimeLimit: updates.screenTimeLimit || 120,
      requireApproval: updates.requireApproval !== undefined ? updates.requireApproval : true,
      updatedAt: new Date().toISOString(),
    };
    
    res.json({
      success: true,
      controls,
    });
  } catch (error) {
    console.error('Error updating parental controls:', error);
    res.status(500).json({ error: 'Failed to update parental controls' });
  }
}
