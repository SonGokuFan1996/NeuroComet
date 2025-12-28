# NeuroNet

A social platform with production-ready age gating and parental controls for under-13 support, compliant with COPPA (Children's Online Privacy Protection Act) requirements.

## Features

- **Age Gating**: Calendar-aware age calculation and verification
- **Content Filtering**: Age-appropriate content filtering with configurable profanity detection
- **Parental Controls**: Comprehensive parental oversight for under-13 users
- **COPPA Compliance Foundation**: Building blocks for legal compliance

## Project Structure

```
NeuroNet/
├── src/
│   ├── backend/
│   │   ├── middleware/
│   │   │   └── ageGate.ts              # Age verification middleware
│   │   ├── services/
│   │   │   └── contentFilter.ts        # Content filtering service
│   │   └── controllers/
│   │       └── parentalControlsController.ts  # Parental controls API
│   └── frontend/
│       └── components/
│           ├── AgeGate.tsx             # Age verification UI component
│           └── ParentalControls.tsx    # Parental controls UI component
├── migrations/
│   └── 20251228_add_age_and_parental_controls.sql  # Database schema
├── tests/
│   ├── ageGate.test.ts                 # Age gate unit tests
│   └── contentFilter.test.ts           # Content filter unit tests
└── docs/
    ├── age-and-parental-controls.md    # Detailed documentation
    └── frontend-styling-notes.md       # Frontend integration guide

```

## Quick Start

### Installation

```bash
npm install
```

### Run Tests

```bash
npm test
```

### Build

```bash
npm run build
```

## Key Components

### 1. Age Gate Middleware (`src/backend/middleware/ageGate.ts`)

Calendar-aware age calculation with three age groups:
- **UNDER_13**: Users younger than 13 (COPPA protected)
- **TEEN**: Users 13-17 years old
- **ADULT**: Users 18 and older

```typescript
import { ageGate, computeAgeGroup, AgeGroup } from './middleware/ageGate';

// Use as middleware
app.use('/api/protected', ageGate);

// Or compute age group directly
const ageGroup = computeAgeGroup('2010-05-15');
```

### 2. Content Filter Service (`src/backend/services/contentFilter.ts`)

Age-appropriate content filtering:
- Text sanitization with configurable vulgar word list
- Age-based filtering rules
- Automatic mature content blocking for young users

```typescript
import { filterContentForAge } from './services/contentFilter';

const filteredContent = filterContentForAge(content, ageGroup);
```

### 3. Parental Controls Controller (`src/backend/controllers/parentalControlsController.ts`)

Validated API endpoints for parental oversight:
- Content restriction levels (strict/moderate/permissive)
- Whitelisted content categories
- Screen time limits
- Approval requirements

**Endpoints:**
- `GET /api/parental-controls/:childId` - Get controls
- `POST /api/parental-controls/:childId` - Update controls

### 4. Frontend Components

**AgeGate Component** (`src/frontend/components/AgeGate.tsx`):
- User-friendly age verification UI
- Accessibility features (ARIA labels, keyboard navigation)
- Input validation and error handling
- Automatic API integration

**ParentalControls Component** (`src/frontend/components/ParentalControls.tsx`):
- Intuitive parental control settings UI
- Loading and saving states
- Whitelist checkbox interface
- Form validation

## Database Migration

Run the PostgreSQL migration to set up the database schema:

```bash
psql -U your_user -d your_database -f migrations/20251228_add_age_and_parental_controls.sql
```

The migration creates:
- `parental_controls` table with indexes
- Auto-updating `updated_at` trigger
- Optional `birthdate` column for users table

**Note:** The migration uses PostgreSQL syntax. Adapt for MySQL, SQLite, or other databases as needed.

## Configuration

### Database Setup

The controllers use a placeholder database client. Replace `(global as any).db` with your actual ORM:

**Sequelize:**
```typescript
const controls = await ParentalControls.findOne({ 
  where: { userId: childId, parentId: parentUser.id } 
});
```

**TypeORM:**
```typescript
const controls = await parentalControlsRepository.findOne({ 
  where: { userId: childId, parentId: parentUser.id } 
});
```

**Prisma:**
```typescript
const controls = await prisma.parentalControls.findUnique({ 
  where: { userId_parentId: { userId: childId, parentId: parentUser.id } } 
});
```

### Content Filtering

Customize the vulgar words list in `src/backend/services/contentFilter.ts`:

```typescript
export const VULGAR_WORDS = [
  'damn',
  'hell',
  'crap',
  // Add more words or integrate with a comprehensive profanity library
];
```

For production, consider using a package like `bad-words` or a commercial profanity API.

### Frontend Styling

The frontend components use Next.js-style `<style jsx>` syntax. See [docs/frontend-styling-notes.md](docs/frontend-styling-notes.md) for integration options:
- CSS Modules
- styled-components
- Regular CSS files

## Testing

Run all tests:
```bash
npm test
```

Run with coverage:
```bash
npm test:coverage
```

Watch mode for development:
```bash
npm test:watch
```

**Test Coverage:**
- Age group computation (all age brackets, edge cases, leap years)
- Content filtering (vulgar words, age-based rules, arrays)
- Calendar-aware age calculation

## API Documentation

### POST /api/users/me/birthdate
Save user's birthdate for age verification.

**Request:**
```json
{
  "birthdate": "2010-05-15"
}
```

### GET /api/parental-controls/:childId
Get parental controls for a child.

**Response:**
```json
{
  "userId": "child123",
  "parentId": "parent456",
  "contentRestrictionLevel": "strict",
  "allowedContentTypes": ["educational", "entertainment"],
  "screenTimeLimit": 120,
  "requireApproval": true
}
```

### POST /api/parental-controls/:childId
Update parental controls.

**Request:**
```json
{
  "contentRestrictionLevel": "moderate",
  "allowedContentTypes": ["educational", "social", "gaming"],
  "screenTimeLimit": 180,
  "requireApproval": false
}
```

## Documentation

- [Age and Parental Controls Guide](docs/age-and-parental-controls.md) - Complete documentation with refinements, architecture notes, and compliance considerations
- [Frontend Styling Notes](docs/frontend-styling-notes.md) - Integration guide for frontend components

## Security Considerations

1. **Server-side validation**: All age checks and parental control updates are validated server-side
2. **Input sanitization**: All user inputs validated against whitelists
3. **Authorization checks**: Verify parent has permission to manage child account
4. **Age computation**: Calendar-aware calculation prevents client-side manipulation

## Compliance Notes

This implementation provides a **foundation** for COPPA compliance but should be reviewed by legal counsel. Additional requirements may include:

- Verifiable parental consent mechanisms
- Data collection limitations for under-13 users
- Privacy policy updates
- Transparent data practices
- Age verification methods

## Future Enhancements

- [ ] Integrate comprehensive profanity filter library
- [ ] Add more granular content categories
- [ ] Implement time-based access controls
- [ ] Add activity logging for parental oversight
- [ ] Support multiple parent accounts per child
- [ ] Email notifications for approval requests
- [ ] Real-time content moderation
- [ ] AI-powered content classification

## Contributing

Contributions are welcome! Please ensure:
- All tests pass (`npm test`)
- Code follows TypeScript best practices
- New features include tests and documentation

## License

MIT

## Support

For questions or issues, please open an issue on GitHub.