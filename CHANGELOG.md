# Changelog

All notable changes to Pixel Fish Tank will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.0] - 2024-12-XX

### Added
- **Firebase Analytics Integration**: Comprehensive event tracking for user interactions
  - Game action events (feed fish, clean tank, place/remove decorations)
  - Mini-game events (start, complete, high scores) with difficulty tracking
  - Progression events (level up, task completion, daily streaks)
  - Screen navigation tracking
  - Settings changes tracking
  - Backup/restore operation tracking
  - App lifecycle events (open, background)
- Analytics helper class for centralized event logging
- Difficulty tracking in mini-game results

### Technical
- Enhanced Firebase Analytics implementation with custom event parameters
- Analytics events logged for all major user interactions
- Privacy-compliant analytics (no personal data collected)

## [1.0.0] - 2024-XX-XX

### Added
- Initial release
- Virtual fish with hunger, cleanliness, happiness, level, and XP stats
- Feeding and tank cleaning mechanics
- Mini-game for earning coins
- Decoration store and placement system
- Daily task system with streak tracking
- Notification support for reminders
- Material Design 3 UI
- Pixel art graphics and animations
- Offline-first gameplay
- Local data persistence with DataStore

### Technical
- Jetpack Compose UI framework
- MVVM architecture
- Kotlin coroutines for async operations
- Firebase integration
- Navigation Compose for screen navigation

---

## Types of Changes

- **Added** for new features
- **Changed** for changes in existing functionality
- **Deprecated** for soon-to-be removed features
- **Removed** for now removed features
- **Fixed** for any bug fixes
- **Security** for vulnerability fixes

