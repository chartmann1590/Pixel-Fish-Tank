#!/usr/bin/env python3
"""Updates the Google Play Store listing (title, descriptions, screenshots)
via the Play Developer API, using the same service account already used to
publish releases.

Nothing goes live until edits().commit() succeeds at the very end - if any
step before that raises, the open edit is simply abandoned and the live
listing is untouched.
"""

import json
import os
import sys
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]
LOCALE = "en-US"
METADATA_DIR = Path("distribution/play-listing") / LOCALE
SCREENSHOTS_DIR = Path("screenshots")

# Order controls how screenshots appear on the store listing.
SCREENSHOT_ORDER = [
    "mainmenu.png",
    "tankview.png",
    "minigames.png",
    "storview.png",
    "decorationsview.png",
    "widgets.png",
    "settings.png",
    "settings2.png",
]


def main() -> None:
    package_name = os.environ["PACKAGE_NAME"]
    service_account_json = os.environ["GOOGLE_PLAY_SERVICE_ACCOUNT_JSON"]

    credentials = service_account.Credentials.from_service_account_info(
        json.loads(service_account_json), scopes=SCOPES
    )
    service = build("androidpublisher", "v3", credentials=credentials)

    edit_id = service.edits().insert(body={}, packageName=package_name).execute()["id"]
    print(f"Opened edit {edit_id}")

    title = (METADATA_DIR / "title.txt").read_text(encoding="utf-8").strip()
    short_description = (METADATA_DIR / "short_description.txt").read_text(encoding="utf-8").strip()
    full_description = (METADATA_DIR / "full_description.txt").read_text(encoding="utf-8").strip()

    if len(short_description) > 80:
        raise SystemExit(f"short_description.txt is {len(short_description)} chars, over Play's 80 limit")
    if len(title) > 30:
        raise SystemExit(f"title.txt is {len(title)} chars, over Play's 30 limit")
    if len(full_description) > 4000:
        raise SystemExit(f"full_description.txt is {len(full_description)} chars, over Play's 4000 limit")

    service.edits().listings().update(
        packageName=package_name,
        editId=edit_id,
        language=LOCALE,
        body={
            "language": LOCALE,
            "title": title,
            "shortDescription": short_description,
            "fullDescription": full_description,
        },
    ).execute()
    print(f"Updated {LOCALE} listing text (title={title!r})")

    service.edits().images().deleteall(
        packageName=package_name,
        editId=edit_id,
        language=LOCALE,
        imageType="phoneScreenshots",
    ).execute()
    print("Cleared existing phoneScreenshots")

    uploaded = 0
    for filename in SCREENSHOT_ORDER:
        path = SCREENSHOTS_DIR / filename
        if not path.exists():
            print(f"  skip (missing): {path}")
            continue
        service.edits().images().upload(
            packageName=package_name,
            editId=edit_id,
            language=LOCALE,
            imageType="phoneScreenshots",
            media_body=str(path),
        ).execute()
        uploaded += 1
        print(f"  uploaded: {path}")

    if uploaded < 2:
        raise SystemExit(f"Only {uploaded} screenshot(s) uploaded; Play requires at least 2. Aborting before commit.")

    service.edits().commit(packageName=package_name, editId=edit_id).execute()
    print(f"Committed edit {edit_id} - Play Store listing is now live with {uploaded} screenshots.")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:  # noqa: BLE001
        print(f"Failed to update Play Store listing: {exc}", file=sys.stderr)
        sys.exit(1)
