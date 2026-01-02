# Pixel Fish Tank Website

This is the website for the Pixel Fish Tank Android game, hosted on Firebase Hosting.

## Setup Instructions

1. **GitHub Links**: Already configured to point to `https://github.com/chartmann1590/Pixel-Fish-Tank`

2. **Firebase Setup**:
   ```bash
   # Install Firebase CLI if not already installed
   npm install -g firebase-tools
   
   # Login to Firebase
   firebase login
   
   # Initialize Firebase (if not already done)
   firebase init hosting
   
   # Deploy
   firebase deploy --only hosting
   ```

3. **Update Project ID**: If your Firebase project has a different ID, update `.firebaserc`:
   ```json
   {
     "projects": {
       "default": "your-project-id"
     }
   }
   ```

## File Structure

```
public/
├── index.html          # Main HTML file
├── css/
│   └── styles.css      # All styling
├── js/
│   └── script.js       # Interactive features
└── assets/            # Game assets (images)
    ├── decorations/
    ├── egg/
    ├── fish/
    ├── food/
    ├── mini-game/
    ├── notifications/
    ├── tank/
    └── ui/
```

## Customization

- Update screenshots section when screenshots are available
- Add video embed code when gameplay video is ready
- Update Google Play link when app is published
- Customize colors in `css/styles.css` (CSS variables at the top)

## Deployment

The site is configured for Firebase Hosting. Simply run:
```bash
firebase deploy --only hosting
```

Your site will be available at: `https://your-project-id.web.app`

