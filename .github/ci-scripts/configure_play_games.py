"""Create the Play Games resources used by Pixel Fish Tank and export their IDs."""

import json
import os
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build


ACHIEVEMENTS = [
    ("PGS_ACH_FIRST_FEED", "First Feeding", "Feed your fish for the first time.", "STANDARD", None, 5),
    ("PGS_ACH_SPARKLING_CLEAN", "Sparkling Clean", "Clean your fish tank for the first time.", "STANDARD", None, 5),
    ("PGS_ACH_FIRST_MINIGAME", "Game On", "Finish your first mini-game.", "STANDARD", None, 5),
    ("PGS_ACH_HIGH_SCORE", "Personal Best", "Set a new mini-game high score.", "STANDARD", None, 10),
    ("PGS_ACH_LEVEL_5", "Growing Up", "Reach fish level 5.", "STANDARD", None, 10),
    ("PGS_ACH_LEVEL_10", "Tank Legend", "Reach fish level 10.", "STANDARD", None, 25),
    ("PGS_ACH_STREAK_3", "Three Day Splash", "Maintain a three-day care streak.", "STANDARD", None, 10),
    ("PGS_ACH_STREAK_7", "Weekly Caretaker", "Maintain a seven-day care streak.", "STANDARD", None, 25),
    ("PGS_ACH_DECORATOR", "Interior Designer", "Place five decorations in your tank.", "STANDARD", None, 10),
    ("PGS_ACH_COLLECTOR", "Collector", "Collect five items.", "STANDARD", None, 10),
    ("PGS_ACH_MINIGAME_VETERAN", "Mini-game Veteran", "Finish 25 mini-games.", "INCREMENTAL", 25, 25),
]

LEADERBOARDS = [
    ("PGS_LB_BUBBLE_POP", "Bubble Pop High Scores"),
    ("PGS_LB_TIMING_BAR", "Timing Bar High Scores"),
    ("PGS_LB_CLEANUP_RUSH", "Cleanup Rush High Scores"),
    ("PGS_LB_FOOD_DROP", "Food Drop High Scores"),
    ("PGS_LB_MEMORY_SHELLS", "Memory Shells High Scores"),
    ("PGS_LB_FISH_FOLLOW", "Fish Follow High Scores"),
]


def localized(value: str) -> dict:
    return {"translations": [{"locale": "en-US", "value": value}]}


def resource_name(resource: dict) -> str | None:
    detail = resource.get("draft") or resource.get("published") or {}
    translations = detail.get("name", {}).get("translations", [])
    return translations[0].get("value") if translations else None


def all_items(request) -> list[dict]:
    items = []
    while request is not None:
        response = request.execute()
        items.extend(response.get("items", []))
        request = request.list_next(request, response) if hasattr(request, "list_next") else None
    return items


def main() -> None:
    application_id = os.environ["PGS_APP_ID"]
    service_account_info = json.loads(os.environ["GOOGLE_PLAY_SERVICE_ACCOUNT_JSON"])
    credentials = service_account.Credentials.from_service_account_info(
        service_account_info,
        scopes=["https://www.googleapis.com/auth/androidpublisher"],
    )
    service = build("gamesConfiguration", "v1configuration", credentials=credentials, cache_discovery=False)

    achievement_api = service.achievementConfigurations()
    existing_achievements = achievement_api.list(applicationId=application_id).execute().get("items", [])
    achievements_by_name = {resource_name(item): item for item in existing_achievements}

    exported = {"PGS_APP_ID": application_id}
    for variable, name, description, achievement_type, steps, points in ACHIEVEMENTS:
        resource = achievements_by_name.get(name)
        if resource is None:
            body = {
                "achievementType": achievement_type,
                "initialState": "REVEALED",
                "draft": {
                    "name": localized(name),
                    "description": localized(description),
                    "pointValue": points,
                },
            }
            if steps is not None:
                body["stepsToUnlock"] = steps
            resource = achievement_api.insert(applicationId=application_id, body=body).execute()
            print(f"Created achievement: {name}")
        else:
            print(f"Using existing achievement: {name}")
        exported[variable] = resource["id"]

    leaderboard_api = service.leaderboardConfigurations()
    existing_leaderboards = leaderboard_api.list(applicationId=application_id).execute().get("items", [])
    leaderboards_by_name = {resource_name(item): item for item in existing_leaderboards}

    for variable, name in LEADERBOARDS:
        resource = leaderboards_by_name.get(name)
        if resource is None:
            resource = leaderboard_api.insert(
                applicationId=application_id,
                body={
                    "scoreOrder": "LARGER_IS_BETTER",
                    "scoreMin": "0",
                    "scoreMax": "999999999",
                    "draft": {
                        "name": localized(name),
                        "scoreFormat": {
                            "numberFormatType": "NUMERIC",
                            "numDecimalPlaces": 0,
                        },
                    },
                },
            ).execute()
            print(f"Created leaderboard: {name}")
        else:
            print(f"Using existing leaderboard: {name}")
        exported[variable] = resource["id"]

    expected_count = 1 + len(ACHIEVEMENTS) + len(LEADERBOARDS)
    if len(exported) != expected_count or any(not value for value in exported.values()):
        raise RuntimeError("Play Games resource configuration is incomplete")

    output = Path(os.environ.get("PGS_PROPERTIES_FILE", "play-games.properties"))
    output.write_text("".join(f"{key}={value}\n" for key, value in exported.items()), encoding="utf-8")
    print(f"Exported {len(ACHIEVEMENTS)} achievements and {len(LEADERBOARDS)} leaderboards")


if __name__ == "__main__":
    main()
