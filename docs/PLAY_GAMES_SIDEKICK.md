# Play Games Sidekick release setup

Pixel Fish Tank integrates Google Play Games Services v2 so Play Games Sidekick can surface achievements and player engagement. Google Play injects Sidekick into the Android App Bundle during upload; the Sidekick SDK must not be bundled into this AAB-based app.

## App integration

The production build supports:

- automatic Play Games platform authentication with a manual retry in Settings;
- native Play Games achievements and achievement UI;
- one all-time public leaderboard for each of the six mini-games;
- milestone reconciliation for existing players when the game starts;
- Sidekick gaming utilities and streak eligibility supplied by Google Play;
- the existing official YouTube promo video for the Play listing and Sidekick video surface.

Game play remains available if Play Games authentication or networking fails.

## Cloud saves, Recall, and Analytics

- The `pixel_fish_tank_autosave` Saved Games slot synchronizes after sign-in,
  after three minutes of settled progress, and when the app enters the background.
- Conflicts keep the save with the highest fish level and XP. Players can save,
  restore, or manage the slot from Settings.
- Every commit includes a progress cover image, description, progress value, and
  accumulated played time.
- Recall links the Play Games profile to a pseudonymous Firebase account. The
  Android client requests a Recall session and the Firebase backend stores only a
  SHA-256 token lookup before issuing a Firebase custom token during recovery.
- Firebase Analytics records cloud-save and Recall outcomes and associates events
  with the pseudonymous Play Games player ID.

## Play Console resources

The production workflow creates or reuses these achievements through the Play Games Services Publishing API. `Mini-game Veteran` is incremental with 25 steps; the others are standard achievements. Draft achievements do not appear in Sidekick.

| Repository variable | Achievement | Trigger |
|---|---|---|
| `PGS_ACH_FIRST_FEED` | First Feeding | Complete a feed task |
| `PGS_ACH_SPARKLING_CLEAN` | Sparkling Clean | Complete a clean task |
| `PGS_ACH_FIRST_MINIGAME` | Game On | Finish any mini-game |
| `PGS_ACH_HIGH_SCORE` | Personal Best | Set a new local high score |
| `PGS_ACH_LEVEL_5` | Growing Up | Reach level 5 |
| `PGS_ACH_LEVEL_10` | Tank Legend | Reach level 10 |
| `PGS_ACH_STREAK_3` | Three Day Splash | Reach a 3-day streak |
| `PGS_ACH_STREAK_7` | Weekly Caretaker | Reach a 7-day streak |
| `PGS_ACH_DECORATOR` | Interior Designer | Place 5 decorations |
| `PGS_ACH_COLLECTOR` | Collector | Own 5 inventory items |
| `PGS_ACH_MINIGAME_VETERAN` | Mini-game Veteran | Finish 25 mini-games |

The workflow also creates numeric leaderboards with larger scores ranked better:

- `PGS_LB_BUBBLE_POP`
- `PGS_LB_TIMING_BAR`
- `PGS_LB_CLEANUP_RUSH`
- `PGS_LB_FOOD_DROP`
- `PGS_LB_MEMORY_SHELLS`
- `PGS_LB_FISH_FOLLOW`

Set the GitHub Actions repository variable `PGS_APP_ID` to the numeric Play Games Services application ID. All achievement and leaderboard IDs are generated during the production build.

## Sidekick console controls

Before production publishing:

1. Link the Android credential for `com.charles.virtualpet.fishtank` to the Play Games Services project and publish the PGS configuration.
2. Add the release signing and Play App Signing SHA-1 certificates to the Android credential.
3. Add tester accounts and verify automatic sign-in, achievement unlock, and leaderboard submission on an internal or closed testing release.
4. In **Testing > Advanced settings > Play Games Sidekick**, enable **Automatically add Sidekick to new app bundles you upload**.
5. Add the official YouTube promo video to the main Play Store listing so it is eligible for Sidekick's video surface.
6. Run the production workflow with `publish` and `sidekick-enabled` confirmations.

Play Points boosters/coupons, Play Pass offers, and Quests require separate program enrollment in Play Console. Gemini Live is restricted to Google's Early Access Partner program. These cannot be enabled by Android code or the Google Play Publishing API.

## Device verification

Sidekick testing requires a Play Store install on a supported Android 13+ device with a gamer profile. Enable Play Store developer options by tapping the Play Store version seven times, then turn on Play Games Sidekick under **General > Developer options**.
