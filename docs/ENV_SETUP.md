# Environment Variables Setup Guide

This project uses environment variables to store sensitive credentials like passwords and API keys. **Never commit actual credentials to git.**

## Quick Start

1. **Copy the template file:**
   ```bash
   # Windows (PowerShell)
   Copy-Item env.template .env
   
   # Linux/Mac
   cp env.template .env
   ```

2. **Edit `.env` and fill in your actual values:**
   ```env
   ADMIN_PASSWORD=your_actual_password_here
   FIREBASE_API_KEY=your_firebase_api_key_here
   IMGUR_CLIENT_ID=546c25a59c58ad7  # Optional, defaults to public anonymous upload ID
   ```

3. **Build the admin panel files:**
   ```bash
   # Windows (PowerShell)
   .\scripts\build-admin-panel.ps1
   
   # Linux/Mac
   chmod +x scripts/build-admin-panel.sh
   ./scripts/build-admin-panel.sh
   ```

## Environment Variables

### Required Variables

- **`ADMIN_PASSWORD`**: Password for accessing the admin panel
  - Use a strong, unique password
  - Never use the default placeholder value

- **`FIREBASE_API_KEY`**: Firebase Web API key
  - Get this from Firebase Console > Project Settings > Your apps > Web app
  - This is used by the admin panel to connect to Firestore

### Optional Variables

- **`IMGUR_CLIENT_ID`**: Imgur API Client ID for image uploads
  - Default: `546c25a59c58ad7` (Imgur's public anonymous upload client ID)
  - You can register your own Imgur app to get a custom client ID if needed

## Android App Configuration

For the Android app, you need to configure `google-services.json`:

1. **Get your Firebase configuration:**
   - Go to Firebase Console > Project Settings
   - Download `google-services.json` for your Android app

2. **Place the file:**
   - Copy `google-services.json` to `app/google-services.json`
   - This file is already in `.gitignore` and will not be committed

3. **Template available:**
   - See `app/google-services.json.template` for the structure
   - Use it as a reference, but always use the actual file from Firebase Console

## Security Best Practices

1. **Never commit `.env` files** - They're already in `.gitignore`
2. **Use strong passwords** - Generate secure passwords for the admin panel
3. **Restrict Firebase API keys** - In Firebase Console, restrict API keys to specific HTTP referrers
4. **Rotate credentials** - If credentials are ever exposed, rotate them immediately
5. **Use different credentials** - Use different passwords/keys for development and production

## Build Scripts

The build scripts (`build-admin-panel.ps1` and `build-admin-panel.sh`) automatically:
- Read values from `.env`
- Replace placeholders in template files
- Generate the actual admin panel files (`public/admin/script.js` and `public/admin/index.html`)
- Validate that required values are set

## Troubleshooting

### "ERROR: .env file not found!"
- Make sure you've copied `env.template` to `.env`
- Check that you're running the script from the project root directory

### "ERROR: ADMIN_PASSWORD is not set or still has placeholder value!"
- Make sure you've replaced `CHANGE_THIS_PASSWORD` with your actual password in `.env`
- Check that there are no quotes around the value in `.env`

### "ERROR: FIREBASE_API_KEY is not set or still has placeholder value!"
- Make sure you've replaced `YOUR_FIREBASE_API_KEY_HERE` with your actual API key
- Get the key from Firebase Console > Project Settings > Your apps > Web app

## Files Overview

- **`env.template`** - Template showing required environment variables (safe to commit)
- **`.env`** - Your actual environment variables (never commit, in .gitignore)
- **`app/google-services.json.template`** - Template for Android Firebase config (safe to commit)
- **`app/google-services.json`** - Your actual Firebase config (never commit, in .gitignore)
- **`public/admin/script.js.template`** - Template for admin script (safe to commit)
- **`public/admin/index.html.template`** - Template for admin HTML (safe to commit)
- **`public/admin/script.js`** - Generated admin script (never commit, in .gitignore)
- **`public/admin/index.html`** - Generated admin HTML (never commit, in .gitignore)

