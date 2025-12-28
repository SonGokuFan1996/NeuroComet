# Age Gating and Parental Controls

## Overview

This document describes the age gating and parental controls system implemented to support users under 13 and provide parental oversight capabilities in compliance with privacy regulations like COPPA (Children's Online Privacy Protection Act).

## Age Groups

The system categorizes users into four age groups based on their birthdate:

- **UNDER_13**: Users younger than 13 years old
- **TEEN_13_17**: Users between 13 and 17 years old (inclusive)
- **ADULT_18_PLUS**: Users 18 years and older
- **UNKNOWN**: Users who haven't provided a birthdate

## Database Schema

### Users Table Additions

Two new columns have been added to the `users` table:

- **`birthdate`** (DATE, nullable): The user's date of birth, used to compute their age group
- **`parent_id`** (BIGINT, nullable): Foreign key reference to the parent's user ID (self-referential)

### Parental Controls Table

A new `parental_controls` table has been created with the following structure:

```sql
CREATE TABLE parental_controls (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  enabled BOOLEAN NOT NULL DEFAULT true,
  allow_chat BOOLEAN NOT NULL DEFAULT false,
  allow_profile_public BOOLEAN NOT NULL DEFAULT false,
  whitelist JSONB NOT NULL DEFAULT '[]'::jsonb,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Fields:**
- `user_id`: The child user's ID (foreign key to users table)
- `enabled`: Whether parental controls are active
- `allow_chat`: Whether the child can use chat features
- `allow_profile_public`: Whether the child's profile is publicly visible
- `whitelist`: JSON array of approved usernames or domains

## Backend Implementation

### Age Gate Middleware

**File:** `src/backend/middleware/ageGate.ts`

This middleware computes the user's age group from their birthdate and attaches it to the request object as `req.ageGroup`.

**Usage:**
```typescript
import { ageGate } from './middleware/ageGate';

app.use(authenticate); // Must run before ageGate to populate req.user
app.use(ageGate);
```

**Exports:**
- `AgeGroup` type: 'UNDER_13' | 'TEEN_13_17' | 'ADULT_18_PLUS' | 'UNKNOWN'
- `computeAgeGroup(birthdate)`: Helper function to compute age group
- `ageGate`: Express middleware function

### Content Filter Service

**File:** `src/backend/services/contentFilter.ts`

Provides content filtering based on age group to ensure age-appropriate content.

**Behavior:**
- **UNDER_13 / UNKNOWN**: Blocks content with vulgar words
- **TEEN_13_17**: Sanitizes vulgar words (replaces with `****`)
- **ADULT_18_PLUS**: Allows all content

**Usage:**
```typescript
import { filterContentForAge } from './services/contentFilter';

const result = filterContentForAge(text, req.ageGroup);
if (!result.allowed) {
  return res.status(403).json({ error: result.reason });
}
// Use result.sanitized if content was sanitized
```

**Exports:**
- `filterContentForAge(text, ageGroup)`: Returns `{ allowed, reason?, sanitized? }`

### Parental Controls Controller

**File:** `src/backend/controllers/parentalControlsController.ts`

Provides API endpoints for managing parental controls.

**Endpoints:**
- `GET /api/parental-controls/:childId`: Retrieve parental control settings
- `POST /api/parental-controls/:childId`: Update parental control settings

**Authorization:**
Both endpoints verify that the requesting user is the parent of the child (via `child.parent_id === parent.id`).

**Note:** Database operations are placeholder implementations and must be adapted to the repository's ORM (Prisma, TypeORM, Sequelize, etc.).

## Frontend Components

### AgeGate Component

**File:** `src/frontend/components/AgeGate.tsx`

A React component that prompts users to enter their birthdate if not already provided.

**Props:**
- `saveBirthdate(birthdate: string)`: Callback function to save the birthdate

**Usage:**
```tsx
<AgeGate saveBirthdate={async (bd) => {
  await api.updateProfile({ birthdate: bd });
}} />
```

### ParentalControls Component

**File:** `src/frontend/components/ParentalControls.tsx`

A React component for parents to manage their child's parental control settings.

**Props:**
- `childId: number`: The ID of the child user

**Features:**
- Toggle parental controls on/off
- Allow/disallow chat
- Allow/disallow public profile
- Manage whitelist of approved usernames/domains

**Usage:**
```tsx
<ParentalControls childId={childUserId} />
```

## Integration Guidelines

### Content-Serving Endpoints

All endpoints that serve user-generated content should:

1. Ensure the `ageGate` middleware has run
2. Filter content through `filterContentForAge`
3. Apply the filtering results:
   - Block content if `allowed === false`
   - Use `sanitized` version if provided

**Example:**
```typescript
app.post('/api/posts', ageGate, async (req, res) => {
  const filterResult = filterContentForAge(req.body.text, req.ageGroup);
  
  if (!filterResult.allowed) {
    return res.status(403).json({ error: filterResult.reason });
  }
  
  // Save post with sanitized content if applicable
  const text = filterResult.sanitized || req.body.text;
  await savePost({ ...req.body, text });
  
  res.json({ success: true });
});
```

### User Registration Flow

When users register:

1. Collect birthdate during registration
2. Compute age group
3. If age < 13:
   - Require parent email/account for verification
   - Create parent-child relationship via `parent_id`
   - Initialize parental controls with default settings
4. Route users without birthdate to AgeGate component

### Server-Side Enforcement

Parental controls must be enforced server-side:

- Check `parental_controls.allow_chat` before allowing chat messages
- Check `parental_controls.allow_profile_public` before displaying profile publicly
- Verify against whitelist for restricted interactions

**Example:**
```typescript
async function canUserChat(userId: number): Promise<boolean> {
  const controls = await db.findParentalControls(userId);
  if (!controls || !controls.enabled) {
    return true; // No restrictions
  }
  return controls.allow_chat;
}
```

## Privacy & Legal Considerations

### COPPA Compliance

The Children's Online Privacy Protection Act (COPPA) requires:

1. **Parental Consent**: Obtain verifiable parental consent before collecting personal information from children under 13
2. **Notice**: Provide clear privacy policy explaining data collection practices
3. **Data Minimization**: Collect only necessary information from children
4. **Parental Access**: Allow parents to review and delete their child's information
5. **Security**: Maintain reasonable security for children's data

### GDPR Compliance

For European users:

1. **Age of Consent**: GDPR sets the age of digital consent at 16 (though member states can lower to 13)
2. **Parental Consent**: Required for users below the age of consent
3. **Right to Erasure**: Parents can request deletion of child's data
4. **Data Portability**: Provide data export functionality

### Recommended Implementation Steps

1. **Parental Verification**: Implement one of these verification methods:
   - Credit card verification (small charge)
   - Government ID verification
   - Email confirmation with follow-up call
   - Knowledge-based authentication

2. **Consent Management**:
   - Create explicit consent forms for parents
   - Log consent timestamp and method
   - Provide easy consent withdrawal

3. **Data Handling**:
   - Encrypt sensitive data at rest
   - Limit data retention periods
   - Implement data deletion workflows
   - Regular security audits

4. **Privacy Policy**:
   - Update to include children's privacy practices
   - Explain parental controls and rights
   - Provide contact information for privacy concerns

## Migration

The database migration file is located at:
```
migrations/20251228_add_age_and_parental_controls.sql
```

**Note:** Running migrations is the responsibility of the operations team. The migration should be reviewed and tested in a staging environment before applying to production.

## Testing Recommendations

### Unit Tests

1. **`computeAgeGroup`**: Test with various birthdates (under 13, teen, adult, edge cases)
2. **`filterContentForAge`**: Test filtering behavior for each age group
3. **Parental Controls**: Test authorization logic

### Integration Tests

1. **Parental Controls API**: Test GET/POST endpoints with various authorization scenarios
2. **Content Filtering**: Test end-to-end content filtering in content-serving endpoints
3. **Age Gate Flow**: Test user flow when birthdate is missing

### Example Test Cases

```typescript
describe('computeAgeGroup', () => {
  it('should return UNDER_13 for 10-year-old', () => {
    const birthdate = new Date();
    birthdate.setFullYear(birthdate.getFullYear() - 10);
    expect(computeAgeGroup(birthdate)).toBe('UNDER_13');
  });
  
  it('should return TEEN_13_17 for 15-year-old', () => {
    const birthdate = new Date();
    birthdate.setFullYear(birthdate.getFullYear() - 15);
    expect(computeAgeGroup(birthdate)).toBe('TEEN_13_17');
  });
});
```

## Next Steps

1. **Implement Database Operations**: Replace placeholder `db` operations in `parentalControlsController.ts` with actual ORM calls
2. **Add API Routes**: Wire up the controller functions to Express routes
3. **Parental Verification**: Implement a parental verification system
4. **Email Notifications**: Notify parents of account activity
5. **Audit Logging**: Log all parental control changes
6. **Content Moderation**: Enhance content filter with ML-based moderation
7. **Reporting System**: Allow parents and children to report inappropriate content
8. **Privacy Dashboard**: Create UI for data export and deletion requests

## Support

For questions or issues related to age gating and parental controls, please contact the development team or file an issue in the repository.
