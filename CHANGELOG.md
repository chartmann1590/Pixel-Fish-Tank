# Changelog

All notable changes to Pixel Fish Tank will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.0] - 2024-12-XX

### Added
- **Unlimited Leveling System**: Progressive XP requirements with unlimited levels
  - Each level requires progressively more XP (Level 1→2: 100 XP, Level 2→3: 300 XP total, etc.)
  - No level cap - your fish can level up infinitely!
  - Formula: XP = 100 × level × (level + 1) / 2
- **Six Mini-Games with Difficulty Levels**: Expanded from one to six engaging mini-games
  - 🫧 Bubble Pop: Tap bubbles as they float up
  - ⏱️ Timing Bar: Stop the marker at the perfect moment
  - 🧹 Cleanup Rush: Tap algae spots to clean the tank
  - 🍽️ Food Drop: Drop food and guide your fish to catch it
  - 🐚 Memory Shells: Remember which shell hides the star
  - 🐟 Fish Follow: Repeat the sequence of directions
  - All games feature Easy, Medium, and Hard difficulty levels with reward multipliers
  - High score tracking for each game
- **Background Music**: Gentle ambient music plays while caring for your fish
  - Toggle on/off in settings
  - Automatically pauses when app goes to background
  - Enhances the cozy, relaxing atmosphere
- **Dynamic Store System**: Store can be updated with new items at any time via Firebase
  - New decorations and items can be added remotely without app updates
  - Automatic store sync every 15 minutes
  - Seamless integration with local inventory system
- **Enhanced Decoration System**: Improved decoration management
  - Decoration locking feature to prevent accidental removal
  - Quantity-based inventory system (buy multiple, place multiple)
  - Better placement controls and visual feedback
- **Home Screen Widgets**: Four beautiful widget types for quick fish status
  - Small Fish Status Widget (2×1): Quick overview with mood, hunger, level & coins
  - Medium Fish Status Widget (3×2): Detailed view with all stat progress bars
  - Streak Counter Widget (2×2): Current and best streak records
  - Daily Tasks Widget (4×3): Task checklist with completion status
  - All widgets feature beautiful pastel gradients and real-time updates
- **Backup & Restore**: Export and import your game progress
  - JSON-based save data export/import
  - Validation to ensure data integrity
  - Easy progress transfer between devices
- **Firebase Analytics Integration**: Comprehensive event tracking for user interactions
  - Game action events (feed fish, clean tank, place/remove decorations)
  - Mini-game events (start, complete, high scores) with difficulty tracking
  - Progression events (level up, task completion, daily streaks)
  - Screen navigation tracking
  - Settings changes tracking
  - Backup/restore operation tracking
  - App lifecycle events (open, background)

### Changed
- Mini-game system expanded from single game to six diverse games
- Decoration system now uses quantity-based inventory
- XP system changed from fixed requirements to progressive unlimited system
- Store system now supports remote updates via Firebase

### Technical
- Enhanced Firebase Analytics implementation with custom event parameters
- Analytics events logged for all major user interactions
- Privacy-compliant analytics (no personal data collected)
- Firebase Firestore integration for dynamic store content
- Background music system with lifecycle management
- Widget system using RemoteViews for enhanced design
- Store sync worker for automatic content updates

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

