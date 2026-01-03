# Script to remove sensitive files from git tracking
# Run this script to clean up git repository

Write-Host "Removing files from git tracking..."

# Remove files that shouldn't be tracked
git rm --cached firestore.rules 2>$null
git rm --cached cors.json 2>$null
git rm -r --cached temp_release 2>$null

Write-Host "Files removed from tracking."
Write-Host "Run: git commit -m 'Remove sensitive files from tracking'"
Write-Host "Then: git push origin master && git push github master"

