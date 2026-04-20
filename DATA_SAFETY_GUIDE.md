# Google Play Data Safety Questionnaire Guide: NeuroComet v2.0.0

Use this guide when filling out the "Data Safety" section in the Google Play Console for NeuroComet v2.0.0.

## 1. Data Collection and Security

*   **Does your app collect or share any of the required user data types?** Yes.
*   **Is all the user data collected by your app encrypted in transit?** Yes.
*   **Do you provide a way for users to request that their data is deleted?** Yes.

---

## 2. Data Types Collected

### Personal Information (Required)
*   **Name:** Collected for user profile display.
*   **Email address:** Collected for account authentication (Supabase).
*   **User IDs:** Collected for account identification.

### Financial Information (Optional)
*   **Purchase History:** Collected if the user subscribes to Premium (RevenueCat/Google Play Billing).

### App Activity (Required)
*   **App interactions:** Collected for analytics and to provide social feed functionality.
*   **Other user-generated content:** Collected (Posts, Stories, Comments).

### App Info and Performance (Required)
*   **Crash logs:** Collected for stability monitoring.
*   **Diagnostics:** Collected for app performance tuning.

### Device or Other IDs (Required)
*   **Device or other IDs:** Collected for Advertising and Analytics (AdMob and Supabase).

### Location (Optional)
*   **Approximate location:** Only if user chooses to share location in posts or find nearby community.

### Photos and Videos (Optional)
*   **Photos:** Only if user chooses to upload an avatar or post images.
*   **Videos:** Only if user chooses to post videos.

### Audio Files (Optional/Ephemeral)
*   **Voice or sound recordings:** Ephemeral. Used for the AI Practice Call feature. **Not stored or recorded.**

### Contacts (Optional)
*   **Contacts:** Only if user opts-in to "Find Friends" via contact sync.

---

## 3. Data Usage and Sharing

### Why is this data collected?
*   **App Functionality:** Most data (Personal Info, App Activity, Photos/Videos) is required for the core social features of NeuroComet.
*   **Analytics:** Crash logs and Device IDs are used to improve app performance.
*   **Advertising:** Device IDs are used by AdMob to show relevant ads to non-premium users.
*   **Account Management:** Email and User IDs are used for secure login.

### Is this data shared with third parties?
*   **Yes.**
    *   **Supabase:** Authentication and database hosting.
    *   **RevenueCat:** Subscription management.
    *   **Google Play Services:** Billing and app integrity.
    *   **AdMob:** Advertising (for non-premium users).

---

## 4. Account Deletion and Retention

*   **URL for requesting account deletion:** `https://getneurocomet.com/delete-account`
*   **Retention Policy:** Data is retained until the user deletes their account. Upon deletion request, there is a **14-day grace period**. After 14 days, data is permanently purged from production databases.
