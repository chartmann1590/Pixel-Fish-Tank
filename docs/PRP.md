# Product Requirements Document (PRP)

## Product Name (Working Title)
**Pixel Fish Tank**

---

## One-Sentence Idea
A cozy Android virtual pet game where players care for a cute pixel-art fish by feeding it, cleaning its tank, playing mini-games, and decorating its environment as the fish grows and levels up.

---

## Target Audience
- Age **8+**
- Casual players and daily check-in players
- Fans of **Tamagotchi-style** virtual pet games
- Players looking for relaxation, routine, and light collecting
- Suitable for short sessions or longer play

---

## Core User Journey

### First Launch
- Short tutorial
- Hatch an egg
- Receive a starter fish

### Daily Gameplay
- App opens directly into the **main tank view**
- Player sees fish, tank, and current stats
- Player can:
  - Feed the fish
  - Clean the tank
  - Play a mini-game
  - Decorate the tank

### Progression Loop
- Complete tasks and mini-games to earn coins
- Spend coins on food, decorations, and upgrades
- Fish gains XP, levels up, and visually grows
- Maintain daily streaks through routine play and reminders

---

## Core Features (v1)

### Virtual Fish System
- Stats: hunger, cleanliness, happiness, level, XP
- Simple offline AI for moods and behavior patterns
- Visual feedback based on care quality

### Tank Interaction
- Feeding system
- Tank cleaning mechanic
- Tank visuals reflect cleanliness and decorations

### Mini-Game
- Simple, repeatable mini-game (e.g., timing, tapping, reaction-based)
- Rewards coins and XP
- Tracks high scores

### Economy
- Coins as the primary currency
- Inventory for food and decorations

### Decoration System
- Place and arrange decorations in the tank
- Decorations affect appearance and/or fish happiness

### Progression & Streaks
- Daily tasks
- Streak tracking to encourage routine play

### Notifications
- Optional reminders for feeding and daily check-ins

### Offline-First
- Fully playable without an internet connection
- All progress stored locally on the device

---

## Data Stored Locally
- Fish stats (hunger, cleanliness, happiness, level, XP)
- Coins and inventory
- Tank decorations and layout
- Daily streaks and task completion
- Notification/reminder settings
- Mini-game high scores

---

## Platform
- **Android only (v1)**
- Native, full-screen, touch-based mobile game
- Offline-first design

---

## Accounts & Backup
- No user accounts required for v1
- All data saved locally
- **Future option:** manual backup and restore via Google Drive
  - Export/import save data (e.g., JSON file)

---

## Suggested Technology (High-Level)
- Native Android game framework
- Local on-device storage (database or file-based saves)
- Rule-based AI for fish behavior
- Android notification system
- No external APIs required for v1

---

## Design & Vibe
- Cartoony **pixel art**
- Cute, chibi-style fish
- Cozy aquarium atmosphere
- Soft animations
- Relaxing sound effects (bubbles, gentle taps)
- Minimal UI inspired by classic Tamagotchi devices

---

## Inspirations
- Tamagotchi

---

## Future Feature Ideas
- Home screen widgets showing fish status
- Screenshot sharing
- Additional fish species and rarity tiers
- More mini-games
- Multiple tanks
- iOS version
