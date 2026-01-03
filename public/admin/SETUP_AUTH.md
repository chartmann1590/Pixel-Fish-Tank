# Firebase Authentication Setup for Admin Panel

## Overview

The admin panel now uses Firebase Authentication instead of a simple password. Only users with email addresses in the `admin_users` Firestore collection can access the admin panel.

## Setup Steps

### 1. Enable Email/Password Authentication in Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/project/pixel-fish-tank/authentication)
2. Click on "Authentication" in the left menu
3. Click "Get Started" if you haven't enabled it yet
4. Go to the "Sign-in method" tab
5. Click on "Email/Password"
6. Enable "Email/Password" and click "Save"

### 2. Create Admin User Account

1. In Firebase Console, go to Authentication > Users
2. Click "Add user"
3. Enter the admin email address
4. Enter a temporary password (user will need to change it on first login)
5. Click "Add user"

### 3. Add Admin Email to Firestore

1. Go to [Firestore Database](https://console.firebase.google.com/project/pixel-fish-tank/firestore)
2. Create a collection named `admin_users` (if it doesn't exist)
3. Add a document with:
   - Document ID: Can be auto-generated or use the email
   - Field: `email` (string) - The admin user's email address
   - Optional: `createdAt` (timestamp) - When the admin was added

Example document structure:
```
Collection: admin_users
Document ID: [auto-generated or email]
Fields:
  email: "admin@example.com"
  createdAt: [timestamp]
```

### 4. Test the Login

1. Go to https://pixel-fish-tank.web.app/admin
2. Enter the admin email and password
3. You should be logged in and see the admin panel

## Adding More Admin Users

To add additional admin users:

1. Create the user account in Firebase Authentication (Authentication > Users > Add user)
2. Add their email to the `admin_users` collection in Firestore

## Security Notes

- Only emails in the `admin_users` collection can access the admin panel
- Users must have a Firebase Authentication account
- The system checks both authentication and admin status
- Unauthorized users are automatically signed out

## Troubleshooting

- **"Access denied" error**: Make sure the email is in the `admin_users` Firestore collection
- **"User not found"**: Create the user in Firebase Authentication first
- **"Firebase Auth not initialized"**: Check that the API key is correct in `index.html`

