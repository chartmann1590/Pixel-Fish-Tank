# Deployment Guide - Pixel Fish Tank Website

This guide will help you deploy the Pixel Fish Tank website to Firebase Hosting.

## Prerequisites

1. **Node.js and npm** - Install from [nodejs.org](https://nodejs.org/)
2. **Firebase Account** - Sign up at [firebase.google.com](https://firebase.google.com/)

## Step 1: Install Firebase CLI

```bash
npm install -g firebase-tools
```

Verify installation:
```bash
firebase --version
```

## Step 2: Login to Firebase

```bash
firebase login
```

This will open a browser window for you to authenticate with your Google account.

## Step 3: Initialize Firebase Project

If you haven't created a Firebase project yet:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or select an existing project
3. Follow the setup wizard
4. Enable **Firebase Hosting** in the project settings

## Step 4: Link Your Local Project

If you need to link to a different Firebase project, update `.firebaserc`:

```json
{
  "projects": {
    "default": "your-firebase-project-id"
  }
}
```

Or initialize hosting:
```bash
firebase init hosting
```

When prompted:
- **What do you want to use as your public directory?** → `public`
- **Configure as a single-page app?** → `Yes`
- **Set up automatic builds and deploys with GitHub?** → `No` (or Yes if you want CI/CD)
- **File public/index.html already exists. Overwrite?** → `No`

## Step 5: Deploy

Deploy to Firebase Hosting:

```bash
firebase deploy --only hosting
```

Your website will be live at:
- `https://your-project-id.web.app`
- `https://your-project-id.firebaseapp.com`

## Step 6: Custom Domain (Optional)

1. Go to Firebase Console → Hosting
2. Click "Add custom domain"
3. Follow the instructions to verify your domain
4. Update DNS records as instructed

## Updating the Site

To update the site after making changes:

```bash
firebase deploy --only hosting
```

## Preview Before Deploying

To preview your site locally:

```bash
firebase serve
```

Then open `http://localhost:5000` in your browser.

## Troubleshooting

### Firebase CLI not found
- Make sure Node.js and npm are installed
- Try reinstalling: `npm install -g firebase-tools`

### Permission errors
- Make sure you're logged in: `firebase login`
- Check that you have the correct permissions in Firebase Console

### Project not found
- Verify your project ID in `.firebaserc`
- Make sure the project exists in Firebase Console

## File Structure

```
.
├── public/              # Website files (deployed to Firebase)
│   ├── index.html
│   ├── css/
│   ├── js/
│   └── assets/
├── firebase.json        # Firebase configuration
└── .firebaserc          # Firebase project mapping
```

## Next Steps

1. Update screenshots section when screenshots are available
2. Add video embed code when gameplay video is ready
3. Update Google Play link when app is published
4. Consider setting up a custom domain

## GitHub Repository

The website code is part of the main repository:
**https://github.com/chartmann1590/Pixel-Fish-Tank**

