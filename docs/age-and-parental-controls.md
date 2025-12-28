# Age Gating and Parental Controls

## Overview

This document describes the age-gating and parental controls implementation in NeuroNet, designed to support users under 13 years of age in compliance with COPPA (Children's Online Privacy Protection Act) and similar regulations.

## Age Groups

Users are classified into three age groups based on their birthdate:

- **UNDER_13**: Users younger than 13 years old
- **TEEN_13_17**: Users aged 13-17 years old
- **ADULT_18_PLUS**: Users 18 years old and above
- **UNKNOWN**: Users who have not provided a birthdate

## Database Schema

### Users Table Updates

Two new columns have been added to the `users` table:

```sql
birthdate DATE NULL          -- User's date of birth for age calculation
parent_id BIGINT NULL        -- Foreign key to parent user (for child accounts)
```

The `parent_id` field establishes a parent-child relationship, allowing parents to manage their children's accounts.

### Parental Controls Table

A new `parental_controls` table stores settings for each child user:

```sql
CREATE TABLE parental_controls (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,              -- Child user ID
  enabled BOOLEAN NOT NULL DEFAULT TRUE,        -- Master control toggle
  allow_chat BOOLEAN NOT NULL DEFAULT FALSE,    -- Chat access permission
  allow_profile_public BOOLEAN NOT NULL DEFAULT FALSE,  -- Profile visibility
  whitelist JSONB NOT NULL DEFAULT '[]',        -- Whitelisted contacts
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Backend Components

### Age Gate Middleware

Located at `src/backend/middleware/ageGate.ts`

The `ageGate` middleware computes a user's age group from their birthdate and attaches it to the Express request object:

```typescript
import { ageGate } from './middleware/ageGate';

// Apply to routes that need age-based restrictions
app.use('/api/content', ageGate);
```

**Functions:**
- `computeAgeGroup(birthdate)`: Calculates age group from a birthdate
- `ageGate(req, res, next)`: Express middleware that attaches `req.ageGroup`

### Content Filtering Service

Located at `src/backend/services/contentFilter.ts`

Filters content based on age group:

```typescript
import { filterContentForAge } from './services/contentFilter';

// In your content-serving endpoint
const result = filterContentForAge(req.body.text, req.ageGroup);

if (!result.allowed) {
  return res.status(403).json({ error: result.reason });
}

// Use result.sanitized for the filtered content
res.json({ content: result.sanitized });
```

**Behavior:**
- **UNDER_13/UNKNOWN**: Blocks content containing vulgar words
- **TEEN_13_17**: Sanitizes vulgar words (masks with `****`)
- **ADULT_18_PLUS**: No filtering applied

### Parental Controls Controller

Located at `src/backend/controllers/parentalControlsController.ts`

Provides REST API endpoints for managing parental controls:

**GET `/api/parental-controls/:childId`**
- Retrieves parental control settings for a child
- Authorization: Only the child's parent can access

**POST `/api/parental-controls/:childId`**
- Updates parental control settings
- Authorization: Only the child's parent can modify
- Request body:
  ```json
  {
    "enabled": true,
    "allow_chat": false,
    "allow_profile_public": false,
    "whitelist": ["user123", "user456"]
  }
  ```

## Frontend Components

### AgeGate Component

Located at `src/frontend/components/AgeGate.tsx`

A React component that prompts users without a birthdate to provide one:

```tsx
import AgeGate from './components/AgeGate';

function App() {
  const handleSaveBirthdate = async (birthdate: string) => {
    await api.updateProfile({ birthdate });
  };

  return user.birthdate ? (
    <MainApp />
  ) : (
    <AgeGate saveBirthdate={handleSaveBirthdate} />
  );
}
```

### ParentalControls Component

Located at `src/frontend/components/ParentalControls.tsx`

A React component for parents to manage their children's settings:

```tsx
import ParentalControls from './components/ParentalControls';

function ParentDashboard() {
  return (
    <div>
      <h1>Manage Child Account</h1>
      <ParentalControls childId={childUserId} />
    </div>
  );
}
```

## Implementation Guide

### 1. Apply Age Gate Middleware

Add the middleware to routes that serve or accept user-generated content:

```typescript
import { ageGate } from './middleware/ageGate';

app.use('/api/posts', ageGate);
app.use('/api/comments', ageGate);
app.use('/api/messages', ageGate);
```

### 2. Filter Content in Endpoints

In endpoints that serve or accept text content:

```typescript
import { filterContentForAge } from './services/contentFilter';

app.post('/api/posts', ageGate, async (req, res) => {
  const filterResult = filterContentForAge(req.body.content, req.ageGroup);
  
  if (!filterResult.allowed) {
    return res.status(403).json({ error: filterResult.reason });
  }
  
  // Save with sanitized content
  await createPost({ content: filterResult.sanitized });
  res.json({ success: true });
});
```

### 3. Enforce Parental Controls

Check parental control settings before allowing actions:

```typescript
app.post('/api/messages', ageGate, async (req, res) => {
  const user = req.user;
  
  // For users under 13, check parental controls
  if (req.ageGroup === 'UNDER_13') {
    const controls = await getParentalControls(user.id);
    
    if (controls.enabled && !controls.allow_chat) {
      return res.status(403).json({ 
        error: 'Chat is disabled by parental controls' 
      });
    }
  }
  
  // Process message...
});
```

### 4. Prompt for Birthdate

Use the AgeGate component to collect birthdates from users who haven't provided them:

```typescript
// In your main app component
{!user.birthdate && <AgeGate saveBirthdate={updateBirthdate} />}
```

## Privacy and Legal Considerations

### COPPA Compliance (United States)

The Children's Online Privacy Protection Act (COPPA) requires:

1. **Parental Consent**: Obtain verifiable parental consent before collecting personal information from children under 13
2. **Privacy Policy**: Clearly disclose data collection practices for children
3. **Parental Access**: Allow parents to review and delete their child's information
4. **Data Minimization**: Collect only necessary information from children
5. **Security**: Implement reasonable security measures

**Recommendations:**
- Implement a parental consent verification flow (email verification, credit card verification, etc.)
- Provide a clear privacy policy specifically for children's accounts
- Allow parents to export and delete their child's data
- Consider age verification for parent accounts

### GDPR Compliance (European Union)

For users in the EU:

1. **Age of Consent**: Varies by member state (13-16 years old)
2. **Parental Consent**: Required for users below the age of consent
3. **Right to Erasure**: Parents can request deletion of child's data
4. **Data Portability**: Provide mechanisms to export data

### Additional Jurisdictions

Different countries have different age restrictions and requirements:
- **UK**: 13 years old (UK GDPR)
- **Australia**: 13 years old (Privacy Act)
- **Canada**: 13 years old (PIPEDA)

**Recommendation**: Consult with legal counsel to ensure compliance with all applicable regulations.

## Security Best Practices

1. **Server-Side Enforcement**: Always enforce parental controls on the server, never trust client-side checks
2. **Authorization**: Verify parent-child relationships before allowing access to parental controls
3. **Input Validation**: Validate all user inputs, especially birthdate and whitelist entries
4. **Audit Logging**: Log parental control changes for accountability
5. **Regular Reviews**: Periodically review and update the vulgar words list and filtering rules

## Testing

### Unit Tests

Test the core functions:

```typescript
// Test computeAgeGroup
expect(computeAgeGroup('2015-01-01')).toBe('UNDER_13');
expect(computeAgeGroup('2007-01-01')).toBe('TEEN_13_17');
expect(computeAgeGroup('2000-01-01')).toBe('ADULT_18_PLUS');
expect(computeAgeGroup(null)).toBe('UNKNOWN');

// Test filterContentForAge
const result = filterContentForAge('This is damn good', 'UNDER_13');
expect(result.allowed).toBe(false);

const teenResult = filterContentForAge('This is damn good', 'TEEN_13_17');
expect(teenResult.allowed).toBe(true);
expect(teenResult.sanitized).toBe('This is **** good');
```

### Integration Tests

1. Test parental controls API endpoints with various authorization scenarios
2. Verify that content filtering is applied in all content-serving endpoints
3. Test the complete flow: user signup → birthdate prompt → age-appropriate content

## Next Steps

1. **Parental Consent Flow**: Implement verifiable parental consent mechanism
2. **Age Verification**: Add age verification for parent accounts
3. **Enhanced Content Filtering**: Use ML-based content moderation for better filtering
4. **Reporting**: Allow parents to receive activity reports for their children
5. **Time Limits**: Add time-based restrictions (e.g., screen time limits)
6. **Content Categories**: Allow parents to restrict access to specific content categories
7. **Notification System**: Notify parents of significant account activities
8. **Compliance Dashboard**: Create admin tools for compliance monitoring

## Support

For questions or issues regarding age-gating and parental controls, please refer to:
- Technical documentation in the codebase
- Privacy policy and terms of service
- Legal compliance team for regulatory questions
