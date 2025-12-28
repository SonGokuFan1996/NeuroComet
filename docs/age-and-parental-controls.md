# Age Gating and Parental Controls

## Overview

This document describes the age-gating and parental controls system implemented in NeuroNet to support users under 13 and provide parents with tools to manage their children's experience on the platform.

## Background and COPPA Compliance

The Children's Online Privacy Protection Act (COPPA) requires special handling for users under 13 years of age. This implementation provides the technical foundation for COPPA compliance, including:

- Age verification through birthdate collection
- Content filtering appropriate to age groups
- Parental controls for managing child accounts
- Restricted access to chat and public profiles for children

**Important:** This implementation provides the technical infrastructure but does not fully satisfy all COPPA requirements. Additional measures such as parental consent verification, privacy policy updates, and legal review are required before accepting under-13 users in production.

## Age Groups

The system defines three age groups based on user birthdate:

1. **UNDER_13**: Users under 13 years old
   - Most restrictive content filtering
   - Requires parental controls
   - Limited feature access by default

2. **TEEN_13_17**: Users aged 13-17
   - Moderate content filtering (sanitization of vulgar words)
   - Optional parental oversight

3. **ADULT_18_PLUS**: Users 18 and older
   - No content filtering restrictions
   - Full feature access

4. **UNKNOWN**: Users without a birthdate on file
   - Treated as UNDER_13 for safety
   - Should be prompted to provide birthdate

## Database Schema

### Users Table Changes

Two new columns added to the `users` table:

- `birthdate` (DATE, nullable): User's birthdate for age calculation
- `parent_id` (BIGINT, nullable): Foreign key to parent user for child accounts

### Parental Controls Table

New table `parental_controls`:

```sql
CREATE TABLE parental_controls (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  allow_chat BOOLEAN NOT NULL DEFAULT FALSE,
  allow_profile_public BOOLEAN NOT NULL DEFAULT FALSE,
  whitelist JSONB DEFAULT '[]'::jsonb,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

Fields:
- `user_id`: Child user ID (unique, foreign key to users)
- `enabled`: Master switch for parental controls
- `allow_chat`: Whether child can use chat features
- `allow_profile_public`: Whether child's profile can be public
- `whitelist`: JSON array for whitelisted content/users (future use)

## Backend Components

### 1. Age Gate Middleware (`src/backend/middleware/ageGate.ts`)

Provides:
- `AgeGroup` type definition
- `computeAgeGroup(birthdate)` helper function
- `ageGate` Express middleware that attaches `req.ageGroup`

**Usage:**
```typescript
import { ageGate } from './middleware/ageGate';

// Apply after authentication middleware
app.use(authenticate); // Your auth middleware
app.use(ageGate);
```

### 2. Content Filter Service (`src/backend/services/contentFilter.ts`)

Provides:
- `filterContentForAge(text, ageGroup)` function
- Returns `{ allowed, reason?, sanitized? }`

**Behavior:**
- **UNDER_13/UNKNOWN**: Block content containing vulgar words
- **TEEN_13_17**: Sanitize vulgar words (replace with ****)
- **ADULT_18_PLUS**: Allow all content

**Integration:**
All content-serving endpoints (posts, comments, chat messages, bios) should call this function:

```typescript
import { filterContentForAge } from './services/contentFilter';

// In your controller
const result = filterContentForAge(req.body.text, req.ageGroup);
if (!result.allowed) {
  return res.status(400).json({ error: result.reason });
}
// Use result.sanitized if available, otherwise use original text
const contentToSave = result.sanitized || req.body.text;
```

### 3. Parental Controls Controller (`src/backend/controllers/parentalControlsController.ts`)

Provides:
- `getParentalControls(req, res)` - GET endpoint
- `updateParentalControls(req, res)` - POST endpoint

**Authorization:**
- Only parent (where `child.parent_id === parent.id`) can view/update settings
- Returns 403 if user is not authorized

**Routes:**
```typescript
// In your routes file
import { getParentalControls, updateParentalControls } from './controllers/parentalControlsController';

router.get('/api/parental-controls/:childId', authenticate, getParentalControls);
router.post('/api/parental-controls/:childId', authenticate, updateParentalControls);
```

**Note:** The controller uses placeholder database operations. Adapt the `db` object to your ORM (Prisma, TypeORM, Sequelize, etc.)

## Frontend Components

### 1. AgeGate Component (`src/frontend/components/AgeGate.tsx`)

Simple React component that prompts users for their birthdate.

**Usage:**
```tsx
import AgeGate from './components/AgeGate';

function App() {
  const handleSaveBirthdate = async (birthdate: string) => {
    await api.updateProfile({ birthdate });
    // AgeGate component will reload page after save
  };

  if (!user.birthdate) {
    return <AgeGate saveBirthdate={handleSaveBirthdate} />;
  }

  return <Dashboard />;
}
```

### 2. ParentalControls Component (`src/frontend/components/ParentalControls.tsx`)

React component for parents to manage child account settings.

**Features:**
- Load current settings via GET /api/parental-controls/:childId
- Update settings via POST
- Toggle switches for `enabled` and `allow_chat`
- Text input for whitelist (future use)

**Usage:**
```tsx
import ParentalControls from './components/ParentalControls';

function ParentDashboard() {
  return (
    <div>
      <h1>Manage Child Account</h1>
      <ParentalControls childId={childUser.id} />
    </div>
  );
}
```

## Integration Guidance

### Required Integrations

1. **Authentication Middleware**: Ensure `req.user` is populated before `ageGate` middleware runs

2. **Content Endpoints**: All endpoints that accept user-generated content must:
   ```typescript
   const filterResult = filterContentForAge(content, req.ageGroup);
   if (!filterResult.allowed) {
     return res.status(400).json({ error: filterResult.reason });
   }
   const safeContent = filterResult.sanitized || content;
   ```

3. **Feature Access Control**: Check parental controls before allowing access:
   ```typescript
   // For chat features
   if (req.ageGroup === 'UNDER_13') {
     const controls = await getParentalControlsForUser(req.user.id);
     if (!controls.allow_chat) {
       return res.status(403).json({ error: 'Chat not allowed by parental controls' });
     }
   }
   ```

4. **Profile Visibility**: Respect `allow_profile_public` setting

5. **Age Gate UI**: Show `AgeGate` component when `user.birthdate` is null

### Database Migration

The SQL migration file must be executed by operations/DBA:

```bash
psql -U postgres -d neuronet -f migrations/20251228_add_age_and_parental_controls.sql
```

**Important:** Review and test migration in staging before production deployment.

## Privacy and Legal Considerations

### COPPA Requirements Not Yet Implemented

This technical implementation does NOT yet include:

1. **Parental Consent Verification**: Parents must provide verifiable consent before child accounts are created
   - Suggested methods: Credit card verification, ID verification, signed consent forms

2. **Privacy Policy Updates**: Privacy policy must be updated to address:
   - Data collection from children
   - Parental rights
   - How to request data deletion

3. **Parental Notification**: Parents should receive email notification when:
   - Child account is created
   - Parental controls are changed
   - Child attempts restricted actions

4. **Data Retention**: Clear policies for child data retention and deletion

5. **Age Verification**: Current implementation relies on self-reported birthdate
   - Consider implementing age verification service for production

### Recommended Next Steps for COPPA Compliance

1. **Legal Review**: Have legal counsel review implementation and policies
2. **Parental Verification Flow**: Implement robust parent verification
3. **Email Notifications**: Add notification system for parental oversight
4. **Privacy Policy**: Update to include child privacy provisions
5. **User Agreement**: Create parent consent agreement
6. **Audit Trail**: Log all parental control changes
7. **Support Process**: Create process for parent inquiries and data requests

## Testing Recommendations

While not included in this PR, the following tests are strongly recommended:

### Unit Tests

1. **Age Calculation Tests**:
   ```typescript
   describe('computeAgeGroup', () => {
     it('should classify under 13 correctly');
     it('should classify teen 13-17 correctly');
     it('should classify adult 18+ correctly');
     it('should handle edge cases (birthdays, leap years)');
     it('should return UNKNOWN for null birthdate');
   });
   ```

2. **Content Filter Tests**:
   ```typescript
   describe('filterContentForAge', () => {
     it('should block vulgar content for UNDER_13');
     it('should sanitize vulgar content for TEEN_13_17');
     it('should allow all content for ADULT_18_PLUS');
     it('should handle empty/null text');
   });
   ```

### Integration Tests

1. **Parental Controls Endpoints**:
   - Test authorization (only parent can access)
   - Test CRUD operations
   - Test validation errors

2. **Content Filtering Integration**:
   - Test post/comment creation with different age groups
   - Test chat message filtering
   - Test bio update filtering

3. **Feature Access Control**:
   - Test chat access based on parental controls
   - Test profile visibility based on settings

## Production Considerations

### Performance

1. **Caching**: Cache parental controls settings to reduce database queries
2. **Content Filter**: Consider using external moderation API for production (e.g., Perspective API, Azure Content Moderator)
3. **Vulgar Words List**: Expand and maintain comprehensive word list, or use ML-based detection

### Security

1. **Server-Side Enforcement**: Never rely solely on client-side checks
2. **Input Validation**: Validate all parental control inputs
3. **Rate Limiting**: Apply rate limits to prevent abuse
4. **Audit Logging**: Log all parental control changes and access attempts

### Monitoring

1. **Metrics**: Track age group distribution, blocked content, parental control usage
2. **Alerts**: Alert on unusual patterns (e.g., spike in blocked content)
3. **Reports**: Generate compliance reports for legal/audit purposes

## Future Enhancements

1. **Advanced Whitelisting**: Allow parents to whitelist specific users, topics, or content
2. **Time Limits**: Screen time limits and usage schedules
3. **Activity Reports**: Provide parents with activity summaries
4. **Multi-Parent Support**: Allow multiple guardians to manage settings
5. **Graduated Permissions**: Automatically adjust restrictions as child ages
6. **AI Content Moderation**: Integrate ML-based content moderation for better accuracy
7. **Reporting System**: Allow users to report inappropriate content
8. **Appeals Process**: Allow users to appeal blocked content decisions

## Support and Maintenance

- **Code Location**: `/src/backend/middleware/ageGate.ts`, `/src/backend/services/contentFilter.ts`, `/src/backend/controllers/parentalControlsController.ts`
- **Database**: See migration file `migrations/20251228_add_age_and_parental_controls.sql`
- **Documentation**: This file and inline code comments
- **Questions**: Contact development team or create GitHub issue

## References

- [COPPA Official Site](https://www.ftc.gov/enforcement/rules/rulemaking-regulatory-reform-proceedings/childrens-online-privacy-protection-rule)
- [COPPA Compliance Guide](https://www.ftc.gov/business-guidance/resources/complying-coppa-frequently-asked-questions)
