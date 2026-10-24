# Delete Account & Delete Data — LingoSphere

**Effective Date:** October 24, 2026  
**Application Name:** LingoSphere  
**Package ID:** `com.aistudio.lingosphere.kxpqzm`

In full compliance with **Google Play Store User Data & Account Deletion Policies**, LingoSphere provides multiple instant and web-based ways to permanently delete your account and/or clear your personal learning data.

---

## Method 1: Instant In-App Account & Data Deletion (From Profile Screen)

If you are signed into LingoSphere:
1. Open the **LingoSphere** app on your Android device.
2. Tap the **Profile** tab in the bottom navigation bar.
3. Scroll to the bottom and tap the red **"Delete Account & All Data"** button.
4. Confirm by tapping **"Delete Permanently"**.
5. All of your cloud data and your Firebase Authentication account record are erased immediately.

---

## Method 2: Request Account or Data Deletion from the Login Screen

You can also request account deletion or partial data deletion directly from the **Login Page** without navigating through the app:
1. Open **LingoSphere** to the Login Screen.
2. Tap **"Delete Account & Data"** under the Legal & Data Safety section.
3. Choose either:
   - **Delete Entire Account & All Data** (Deletes your account + all stored data), or
   - **Delete Learning Data Only** (Clears your speaking transcripts, flashcards, and lesson history while keeping your login account).
4. Enter your registered Google/Gmail address and tap **"Submit Deletion Request"** (or sign in to execute immediate self-service deletion).

---

## Method 3: Web Deletion Portal (Without the App Installed)

If you have already uninstalled LingoSphere, you can request full account deletion or data deletion online:
- **Interactive Web Deletion Form:**  
  `https://ais-pre-smnebhnagujwa3llo3lj4b-861509315491.asia-east1.run.app/delete-account.html`
- **GitHub Pages Web Form (when hosted on GitHub Pages):**  
  [docs/delete-account.html](./docs/delete-account.html)
- **Email Deletion Request:**  
  Send an email from your registered Google account to `privacy@lingosphere.app` with the subject **"DELETE MY LINGOSPHERE ACCOUNT"** or **"DELETE MY LINGOSPHERE DATA"**.

---

## What Data Is Deleted vs. Retained

| Data Category | Storage Location | Action Upon Deletion | Retention Period |
| :--- | :--- | :--- | :--- |
| **User Profile (`users/{userId}`)** | Cloud Firestore | **Permanently Deleted** | Deleted immediately (in-app) or within 48 hours (web request) |
| **Private Email (`users_private/{userId}`)** | Cloud Firestore | **Permanently Deleted** | Deleted immediately (in-app) or within 48 hours (web request) |
| **Lesson Progress (`lessonProgress`)** | Cloud Firestore | **Permanently Deleted** | Deleted immediately |
| **Leitner Flashcards (`flashcards`)** | Cloud Firestore | **Permanently Deleted** | Deleted immediately |
| **AI Voice Call Logs (`speakingSessions`)** | Cloud Firestore | **Permanently Deleted** | Deleted immediately |
| **Firebase Auth Account Record** | Firebase Auth | **Permanently Deleted** | Deleted immediately |
| **Google Play Purchase Receipts** | Google Play Billing | Managed by Google Play | Retained by Google per statutory financial & tax regulations |

---

## Related Legal Pages
- [Privacy Policy](./PRIVACY_POLICY.md)
- [Terms of Service](./TERMS_OF_SERVICE.md)
