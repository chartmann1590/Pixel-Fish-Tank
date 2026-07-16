# Play Games Recall Worker

This Cloudflare Worker is the trusted server required by the Google Play Games
Recall API. It runs on the Cloudflare Workers Free plan and uses Workers KV for
the token-to-Firebase-user lookup. Firebase remains on the Spark plan.

The deployed worker is `pixel-fish-tank-recall`. Its three encrypted secrets are:

- `GOOGLE_SERVICE_ACCOUNT_JSON`: the dedicated Play Games service account;
- `FIREBASE_API_KEY`: used only to validate Firebase ID tokens;
- `TOKEN_SECRET`: a random application-specific key for opaque personas/tokens.

Secrets are configured in Cloudflare and are not stored in this repository.
Run `npm test` before `npm run deploy`. The `/health` endpoint is the only public
GET route; Recall operations use `/v1/recover` and `/v1/link`.
