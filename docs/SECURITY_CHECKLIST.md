# Security Checklist

Use this checklist to verify that your project is secure before committing or deploying.

## ✅ Pre-Commit Checklist

- [ ] No hardcoded passwords in any files
- [ ] No hardcoded API keys in source code
- [ ] No hardcoded tokens or secrets
- [ ] `.env` file exists and is NOT tracked by git (check `.gitignore`)
- [ ] All template files use placeholders (not actual values)
- [ ] `google-services.json` is NOT tracked by git (check `.gitignore`)
- [ ] Admin panel files (`public/admin/script.js`, `public/admin/index.html`) are NOT tracked by git

## ✅ Files That Should Be in Git (Templates)

- [x] `env.template` - Environment variables template
- [x] `app/google-services.json.template` - Firebase config template
- [x] `public/admin/script.js.template` - Admin script template
- [x] `public/admin/index.html.template` - Admin HTML template
- [x] `.gitignore` - Should exclude all sensitive files

## ✅ Files That Should NOT Be in Git

- [ ] `.env` - Your actual environment variables
- [ ] `.env.local` - Local environment overrides
- [ ] `app/google-services.json` - Actual Firebase config
- [ ] `public/admin/script.js` - Generated admin script (with real password)
- [ ] `public/admin/index.html` - Generated admin HTML (with real API key)
- [ ] `local.properties` - Local SDK paths

## ✅ Security Best Practices

- [ ] Use strong, unique passwords for admin panel
- [ ] Firebase API keys are restricted to specific HTTP referrers
- [ ] Different credentials for development and production
- [ ] Regular credential rotation schedule in place
- [ ] Team members know not to commit sensitive files
- [ ] CI/CD uses secrets management (not hardcoded values)

## ✅ Code Review Checklist

When reviewing code, check for:

- [ ] No `password = "..."` or similar hardcoded values
- [ ] No `apiKey = "..."` or similar hardcoded API keys
- [ ] No `token = "..."` or similar hardcoded tokens
- [ ] Environment variables are used instead of hardcoded values
- [ ] Template files use placeholders, not actual values
- [ ] Build scripts properly replace placeholders

## Quick Security Scan Commands

```bash
# Search for potential hardcoded secrets (run from project root)
grep -ri "password.*=" app/src/ public/ --exclude-dir=build 2>/dev/null | grep -v "//\|template\|CHANGE_THIS\|YOUR_"
grep -ri "api.*key.*=" app/src/ public/ --exclude-dir=build 2>/dev/null | grep -v "//\|template\|YOUR_FIREBASE"
grep -ri "secret.*=" app/src/ public/ --exclude-dir=build 2>/dev/null | grep -v "//\|template"

# Check if sensitive files are tracked
git ls-files | grep -E "\.env$|google-services\.json$|public/admin/(script\.js|index\.html)$"
```

If any sensitive files appear in the last command, they should be removed from git tracking.

## Reporting Security Issues

If you find a security issue:

1. **DO NOT** commit the fix with the actual credentials
2. Remove credentials from git history if they were committed
3. Rotate all exposed credentials immediately
4. Update this checklist if new patterns are discovered

