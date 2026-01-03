# Admin Panel Setup

## Security Notice

The admin panel files (`script.js` and `index.html`) contain sensitive credentials and should NOT be committed to git.

## Setup Instructions

1. Copy the template files to create the actual files:
   ```bash
   cp script.js.template script.js
   cp index.html.template index.html
   ```

2. Edit `script.js` and replace `'CHANGE_THIS_PASSWORD'` with your actual admin password.

3. Edit `index.html` and replace `"YOUR_FIREBASE_API_KEY_HERE"` with your Firebase API key from Firebase Console > Project Settings.

4. **DO NOT** commit `script.js` or `index.html` to git. They are already in `.gitignore`.

5. Deploy the configured files to Firebase Hosting.

## Security Best Practices

- Use a strong, unique password for the admin panel
- Regularly rotate the admin password
- Restrict Firebase API key usage in Firebase Console (set HTTP referrer restrictions)
- Consider using Firebase Authentication instead of client-side password checking for production

