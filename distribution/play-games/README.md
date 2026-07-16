# Pixel Fish Tank - Google Play Games listing package

This directory contains every text value and graphic asset needed to complete the separate Google Play Games Services listing.

## Game details

- Display name: `Pixel Fish Tank`
- Default language: `English (United States) - en-US`
- Category: `Casual`
- Saved games: `No`
- Theme color: `#18BCE4`
- Description: use the exact description in `game-details/game-details.txt`
- Icon: `game-details/icon-512.png`
- Banner: `game-details/banner-1024x500.png`

## Android credential

- Package name: `com.charles.virtualpet.fishtank`
- Release SHA-1: `BD:7C:17:03:21:7C:57:CE:7A:51:E5:EA:0E:67:25:2B:F2:0B:BB:72`
- Cloud project: `pixel-fish-tank`
- Play Games project ID: `419224415744`

The Android credential must be created or linked from the Play Games Services Configuration page. Creating an OAuth client only in Google Cloud does not link it to the Play Games project.

## Resource assets

- `achievements.csv` maps all 11 existing achievement IDs to their copy and unique 512 x 512 transparent PNG icons in `achievements/`.
- `leaderboards.csv` maps all 6 existing leaderboard IDs to their score configuration and custom 512 x 512 transparent PNG icons in `leaderboards/`.
- `_sources/` contains the generated source sheets and a combined preview for future editing.

## Final publishing state

After the details, Android credential, achievement icons, and leaderboard icons are saved, the Play Games Services Publishing page must show no outstanding required items. Publishing the Play Games Services changes is separate from publishing the Android App Bundle to the production track.
