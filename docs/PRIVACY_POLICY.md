# Privacy Policy for Pixel Fish Tank

**Last Updated**: July 16, 2026

## Introduction

Pixel Fish Tank ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and safeguard information when you use our mobile application ("App").

## Information We Collect

### Data Stored Locally
Pixel Fish Tank is designed to be **offline-first**. Game data is always stored locally on your device, including:
- Fish statistics (hunger, cleanliness, happiness, level, XP)
- Coins and inventory
- Tank decorations and layout
- Daily streaks and task completion
- Mini-game high scores
- Notification and reminder settings

When you connect Google Play Games, the App also synchronizes a portable copy of this progress to Google Play Games Saved Games so it can be restored on your other devices. Saved-game data does not include purchase-history records.

### Google Play Games Services

If you use Google Play Games, Google processes your Play Games profile identifier, achievements, leaderboard scores, and cloud-saved game progress. The App uses Play Games Recall to associate your Play Games profile with a pseudonymous Firebase account identifier. This allows the same in-game identity to be recovered after reinstalling the App. Recall tokens are cryptographically generated and do not contain your name, email address, or game progress.

### Automatically Collected Information
The App uses Firebase services which may collect certain information automatically:

#### Firebase Analytics
- App usage statistics (sessions, screen views, app opens/closes)
- Device information (model, OS version)
- General location data (country/region level)
- App performance metrics
- Gameplay events (feeding fish, cleaning tank, playing mini-games, placing decorations)
- Progression events (level ups, task completions, daily streaks)
- Mini-game performance (scores, difficulty levels, high scores)
- Settings changes
- Backup/restore operations

**Note**: Analytics uses a pseudonymous Play Games player identifier when you are connected. Events do not include your real name or email address. We use this data to understand how players interact with the game and improve the user experience.

#### Firebase Crashlytics
- Crash reports and stack traces
- Device information at time of crash
- App version information

#### Firebase Cloud Messaging
- Device tokens for push notifications (if enabled)
- Notification delivery status

### Permissions
The App requests the following permissions:
- **Internet**: Required for Firebase services (analytics, crash reporting)
- **Post Notifications**: Required for optional reminder notifications
- **Wake Lock**: Required for reliable notification delivery

## How We Use Information

### Game Progress
Game progress and settings are stored locally. If you connect Google Play Games, progress is also sent to Google Play Games Saved Games for cross-device backup and restoration.

### Firebase Services
We use Firebase services for:
- **Analytics**: To understand how users interact with the App and improve user experience
- **Crashlytics**: To identify and fix bugs and crashes
- **Cloud Messaging**: To send optional reminder notifications (if enabled by user)
- **Authentication and Recall**: To create a pseudonymous in-game account and recover it through your Play Games profile

## Data Sharing

We **do not sell, trade, or rent** your personal information to third parties.

Firebase services are provided by Google and are subject to [Google's Privacy Policy](https://policies.google.com/privacy). Data collected by Firebase is used in accordance with Google's privacy practices.

Google Play Games Services is also provided by Google and is subject to Google's Privacy Policy and the Play Games profile and privacy controls available in your Google settings.

The Recall API gateway is hosted on Cloudflare Workers and stores a one-way token lookup in Cloudflare Workers KV. Cloudflare processes these requests according to [Cloudflare's Privacy Policy](https://www.cloudflare.com/privacypolicy/).

## Data Security

- Local game data remains on your device unless you connect Google Play Games
- Cloud saves, Recall calls, and Firebase services use encrypted network connections
- Recall tokens are stored as one-way hashes by the backend

## Children's Privacy

Pixel Fish Tank is suitable for users aged 8 and above. We do not knowingly collect personal information from children under 13. If you are a parent or guardian and believe your child has provided us with personal information, please contact us.

## Your Rights

### Local Data
You have full control over your local game data:
- You can uninstall the App at any time, which will delete all local data
- You can clear app data through your device settings

### Cloud Data
You can manage or delete Play Games data through your Google Play Games profile settings. You may also contact us to request deletion of a pseudonymous Recall account link.

### Analytics and Crash Reporting
You can opt out of Firebase Analytics by disabling it in your device settings or by not using the App.

### Notifications
You can disable notifications at any time through:
- The App's settings screen
- Your device's notification settings

## Data Retention

- **Local Game Data**: Retained on your device until you uninstall the App or clear app data
- **Play Games Saved Games**: Retained until replaced or deleted through Google Play Games
- **Recall account link**: Retained while the Play Games account link remains active
- **Firebase Analytics Data**: Retained according to Google's data retention policies (typically 14 months)
- **Crash Reports**: Retained by Firebase for analysis and bug fixing

## Third-Party Services

The App uses the following third-party services:
- **Google Firebase**: Analytics, Authentication, Crashlytics, Cloud Messaging
  - Privacy Policy: https://policies.google.com/privacy
  - Terms of Service: https://firebase.google.com/terms
- **Cloudflare**: Recall API hosting and token lookup storage
  - Privacy Policy: https://www.cloudflare.com/privacypolicy/

## Changes to This Privacy Policy

We may update this Privacy Policy from time to time. We will notify you of any changes by:
- Updating the "Last Updated" date at the top of this policy
- Posting the new Privacy Policy in the App (if applicable)

You are advised to review this Privacy Policy periodically for any changes.

## International Users

If you are using the App from outside the United States, please note that Firebase services may transfer data to and process data in the United States and other countries.

## California Privacy Rights

If you are a California resident, you have additional rights under the California Consumer Privacy Act (CCPA):
- Right to know what personal information is collected
- Right to delete personal information
- Right to opt-out of the sale of personal information (we do not sell personal information)

## Contact Us

If you have any questions about this Privacy Policy, please contact us:

- **Email**: tickets@pixel-fish-tank.p.tawk.email
- **Repository**: https://github.com/chartmann1590/Pixel-Fish-Tank

## Consent

By using Pixel Fish Tank, you consent to this Privacy Policy and agree to its terms.

---

