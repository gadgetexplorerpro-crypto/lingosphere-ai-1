# Privacy Policy for LingoSphere

**Effective Date:** October 24, 2026  
**Application Name:** LingoSphere  
**Package ID:** `com.aistudio.lingosphere.kxpqzm`

LingoSphere ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how your personal information and learning data are collected, used, stored, and protected when you use the LingoSphere mobile application.

---

## 1. Information We Collect

### A. Account & Profile Information
When you authenticate using **Google Sign-In / Gmail**, we collect and store:
- **User ID (`uid`):** A unique identifier assigned by Firebase Authentication.
- **Display Name:** Your name from your Google profile to personalize your learning experience.
- **Email Address:** Stored privately to identify your account and process account management or deletion requests.
- **Language Preferences:** Your selected native language, target learning language, and proficiency level.

### B. Voice & Microphone Audio Data
- **Real-Time Voice Calling & Pronunciation Practice:** LingoSphere uses your device's microphone (`android.permission.RECORD_AUDIO`) only while you actively participate in an AI Voice Call or Speaking Practice session.
- **Ephemeral Processing:** Spoken audio is converted into text transcripts in real time to generate AI language tutor responses, grammar corrections, and pronunciation feedback. Raw audio recordings are processed ephemerally and are **never sold, shared with third-party advertisers, or used for ad targeting**.

### C. Learning Progress & Activity Data
We store your educational progress in Google Cloud Firestore so your progress stays synchronized across devices:
- Completed curriculum lessons, earned XP, current level, and daily streaks.
- Spaced repetition flashcards (Leitner box levels and review schedules).
- Conversation session transcripts and post-call learning reports.

### D. Subscription & Purchase Information
If you upgrade to LingoSphere Premium, transactions are processed directly by **Google Play Billing**. We store only the subscription tier status and purchase verification token—we never collect or store your credit card or payment details.

---

## 2. How We Use Your Information

We use the collected information exclusively to:
1. Authenticate your account and synchronize your learning progress across devices.
2. Provide real-time bidirectional AI voice tutoring and customized language exercises.
3. Generate post-call learning reports with vocabulary suggestions and grammar corrections.
4. Verify subscription entitlements via Google Play Billing.

---

## 3. Data Sharing & Security

- **Zero Third-Party Data Sales:** We do not sell, rent, or trade your personal data to any third parties.
- **Service Providers:** We use Google Firebase (Authentication and Cloud Firestore) and Google Gemini AI APIs strictly to operate the core functionality of the app.
- **Encryption:** All data transmitted between the app and cloud servers is encrypted in transit using HTTPS / TLS. Access to Firestore documents is restricted by strict per-user security rules (`request.auth.uid == userId`).

---

## 4. Data Retention & Account Deletion (Right to Erasure)

You have full control over your personal data and may delete your account and all associated data at any time:

### Option 1: Instant In-App Deletion
1. Open the **LingoSphere** app.
2. On the **Login Screen**, tap **Delete Account & Data**, or navigate to the **Profile** tab after signing in.
3. Tap **Delete Account & All Data** and confirm.
4. Your profile, email record, lesson progress, flashcards, speaking sessions, and Firebase Auth record are permanently deleted immediately.

### Option 2: Web Deletion Portal (No App Required)
If you have uninstalled the app, you can submit an account and data deletion request online:
- **Web Deletion URL:** [Delete Account & Data Portal](./DELETE_ACCOUNT.md)
- **Live Cloud Portal:** `https://ais-pre-smnebhnagujwa3llo3lj4b-861509315491.asia-east1.run.app/delete-account.html`

---

## 5. Children's Privacy

LingoSphere is a general-audience educational language learning application. We do not knowingly collect personal information from children without appropriate consent. If you believe a child's data has been unintentionally stored, please use our deletion portal to have it removed immediately.

---

## 6. Contact Us

For questions regarding this Privacy Policy or your personal data:
- **Privacy Support Email:** `privacy@lingosphere.app`
- **Data Deletion Portal:** [DELETE_ACCOUNT.md](./DELETE_ACCOUNT.md)
- **Terms of Service:** [TERMS_OF_SERVICE.md](./TERMS_OF_SERVICE.md)
