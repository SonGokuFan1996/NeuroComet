# Age Gating and Parental Controls

This document describes the age gating and parental controls system implemented in NeuroNet to support users under 13 years old in compliance with COPPA (Children's Online Privacy Protection Act).

## Overview

The system provides:
- Calendar-aware age calculation from birthdate
- Age-based content filtering
- Parental controls for under-13 users
- Content restriction levels and whitelisting
- Screen time management

## Refinements Applied

### 1. Age Gate Middleware (`src/backend/middleware/ageGate.ts`)
- **Calendar-aware age calculation**: Uses `computeAgeGroup()` function that accurately calculates age based on calendar years, accounting for whether the birthday has occurred this year
- **Age group classification**: Categorizes users into UNDER_13, TEEN, or ADULT groups
- **Exported computeAgeGroup**: The `computeAgeGroup` function is exported for use in other modules
- **Middleware protection**: `ageGate` middleware ensures users have valid birthdates before accessing protected resources

### 2. Content Filter Service (`src/backend/services/contentFilter.ts`)
- **Normalized text processing**: All text comparisons are case-insensitive and trimmed
- **Configurable vulgar words list**: `VULGAR_WORDS` constant can be easily extended or replaced with a comprehensive profanity library
- **Sanitization**: `sanitizeText()` replaces vulgar words with asterisks
- **Age-based filtering**: `filterContentForAge()` applies different filtering levels:
  - UNDER_13: Strict filtering with text sanitization and mature content blocking
  - TEEN: Moderate filtering, blocks explicitly mature content
  - ADULT: No filtering
- **Content validation**: Checks for mature/age-restricted flags and filters accordingly

### 3. Parental Controls Controller (`src/backend/controllers/parentalControlsController.ts`)
- **Input validation**: Validates all parental control settings against whitelists
- **Whitelist validation**: Ensures content types are from approved `VALID_WHITELIST_CATEGORIES`
- **Try/catch error handling**: All endpoints wrapped in try/catch with proper error responses
- **Server-side age verification**: Uses `computeAgeGroup()` to verify child is actually under 13
- **Upsert placeholder**: Includes placeholder code for database upsert operations
- **Endpoints**:
  - `GET /api/parental-controls/:childId`: Retrieve parental controls
  - `POST /api/parental-controls/:childId`: Update parental controls with validation

### 4. Database Migration (`migrations/20251228_add_age_and_parental_controls.sql`)
- **Parental controls table**: Stores settings per child user
- **Indexed lookups**: Index on `user_id` for fast parental control queries
- **Composite indexes**: Indexes on `parent_id` and `(user_id, parent_id)` for efficient queries
- **Auto-updating timestamp**: Trigger function `update_parental_controls_updated_at()` maintains `updated_at` column
- **Birthdate support**: Conditionally adds `birthdate` column to users table with index

### 5. Frontend Components

#### AgeGate Component (`src/frontend/components/AgeGate.tsx`)
- **Improved validation**: Validates birthdate format and ensures user is at least 13 years old (or as configured)
- **Accessibility**: Proper labels, ARIA attributes, and keyboard navigation
- **API integration**: Automatically calls `POST /api/users/me/birthdate` if no `saveBirthdate` prop provided
- **Error handling**: Displays validation errors to users
- **User experience**: Clear instructions and date input with proper formatting

#### Parental Controls Component (`src/frontend/components/ParentalControls.tsx`)
- **Improved UX**: Loading states, success/error messages, and disabled states during saves
- **Whitelist UI**: Checkbox interface for selecting allowed content types
- **Form validation**: Client-side validation before submission
- **State management**: Proper loading and saving states
- **API integration**: Calls `GET/POST /api/parental-controls/:childId` endpoints

### 6. Tests

#### Age Gate Tests (`tests/ageGate.test.ts`)
- Jest tests for `computeAgeGroup()` function
- Tests for edge cases: birthdays today, leap years, boundary conditions
- Tests for all age groups: UNDER_13, TEEN, ADULT

#### Content Filter Tests (`tests/contentFilter.test.ts`)
- Jest tests for `filterContentForAge()` function
- Tests for vulgar word detection and sanitization
- Tests for age-based filtering logic
- Tests for array filtering

## Architecture Notes

### Database Abstraction
The controllers use a placeholder `(global as any).db` for database operations. **You must adapt this to your actual database client**:
- **Sequelize**: Replace with `await ParentalControls.findOne({ where: {...} })`
- **Knex**: Replace with `await db('parental_controls').where({...}).first()`
- **TypeORM**: Replace with `await parentalControlsRepository.findOne({ where: {...} })`
- **Prisma**: Replace with `await prisma.parentalControls.findUnique({ where: {...} })`

### Database Engine
The migration uses PostgreSQL's PL/pgSQL syntax. If you use MySQL, SQLite, or another database:
- Replace `TEXT[]` with appropriate array type or JSON
- Adapt the trigger syntax to your database's stored procedure language
- Adjust `SERIAL` to `AUTO_INCREMENT` (MySQL) or `INTEGER PRIMARY KEY` (SQLite)

### Testing Framework
Tests are written for Jest. If you use Mocha, Vitest, or another framework:
- Adapt the `describe`/`it`/`expect` syntax as needed
- Adjust mock setup and assertion libraries

### API Endpoints
The frontend components assume these endpoints exist:
- `POST /api/users/me/birthdate`: Save user's birthdate
- `GET /api/parental-controls/:childId`: Get parental controls for a child
- `POST /api/parental-controls/:childId`: Update parental controls for a child

Ensure your backend routes are configured to match these paths.

## Security Considerations

1. **Server-side validation**: Always validate age and parental controls on the server
2. **Authentication**: Ensure proper authentication middleware runs before parental controls endpoints
3. **Authorization**: Verify that the requesting parent has permission to manage the child's account
4. **Input sanitization**: All user inputs are validated against whitelists
5. **Age verification**: Server-side age calculation prevents client-side manipulation

## Future Enhancements

- Integrate comprehensive profanity filter library (e.g., `bad-words` npm package)
- Add more granular content categories
- Implement time-based access controls
- Add activity logging for parental oversight
- Support multiple parent accounts per child
- Add email notifications for parental approval requests

## Compliance

This implementation provides a foundation for COPPA compliance but should be reviewed by legal counsel. Additional requirements may include:
- Verifiable parental consent mechanisms
- Data collection limitations for under-13 users
- Privacy policy updates
- Transparent data practices
