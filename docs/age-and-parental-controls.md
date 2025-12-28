# Age gating and Parental Controls (proposal)

Overview
- New age groups:
  - UNDER_13: strict blocking of vulgar/explicit material, parental controls applied.
  - TEEN_13_17: sanitized content (vulgar words masked) by default.
  - ADULT_18_PLUS: full access.
- Users must have `birthdate` populated to determine age group. If missing, route as `UNKNOWN` and prompt user to provide birthdate.
- Parental controls: parent accounts can manage child settings (enable/disable chat, whitelist users/content).

DB changes
- `users.birthdate DATE`
- `users.parent_id BIGINT` (optional)
- `parental_controls` table with per-child settings

Backend
- `ageGate` middleware sets `req.ageGroup`.
- `contentFilter` checks content and returns allow/sanitize decisions.
- `parentalControlsController` endpoints for managing per-child settings.

Frontend
- `AgeGate` component: shown on first login or missing birthdate; prompts birthdate input and explains parental controls.
- `ParentalControls` component: parent-only UI to manage child account settings and whitelist.

Notes & next steps
- Replace naive vulgar-word list with an external moderation API (e.g., Perspective API, internal ML model) for production.
- Localize lists and UI text.
- Audit all content-serving endpoints (posts, chat, comments, profile bios) to run through the content filter.
- Add logging/metrics to measure false positives/negatives.
