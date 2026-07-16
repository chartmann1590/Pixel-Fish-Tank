const GOOGLE_OAUTH_SCOPE = "https://www.googleapis.com/auth/androidpublisher";
const GOOGLE_OAUTH_AUDIENCE = "https://oauth2.googleapis.com/token";
const FIREBASE_CUSTOM_TOKEN_AUDIENCE =
  "https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit";
const SESSION_ID_MIN_LENGTH = 20;
const SESSION_ID_MAX_LENGTH = 4096;

let cachedGoogleAccessToken;
let cachedGoogleAccessTokenExpiry = 0;
let cachedServiceAccount;
let cachedSigningKey;

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === "GET" && url.pathname === "/health") {
      return json({ok: true, service: "pixel-fish-tank-recall"});
    }
    if (request.method !== "POST") return json({error: "not_found"}, 404);

    try {
      const body = await readJson(request);
      if (url.pathname === "/v1/recover") return await recover(body, env);
      if (url.pathname === "/v1/link") return await link(body, env);
      return json({error: "not_found"}, 404);
    } catch (error) {
      console.error("Recall request failed", safeError(error));
      const status = error instanceof RequestError ? error.status : 503;
      return json({error: status === 400 ? "invalid_request" : "temporarily_unavailable"}, status);
    }
  },
};

async function recover(body, env) {
  const sessionId = requireSessionId(body);
  const accessToken = await googleAccessToken(env);
  const response = await googleRequest(
    `https://games.googleapis.com/games/v1/recall/tokens/${encodeURIComponent(sessionId)}`,
    accessToken,
  );

  for (const recallToken of response.tokens || []) {
    if (typeof recallToken.token !== "string") continue;
    const uid = await env.RECALL_TOKENS.get(await tokenKey(recallToken.token));
    if (!uid) continue;
    return json({recovered: true, customToken: await createFirebaseCustomToken(uid, env)});
  }
  return json({recovered: false});
}

async function link(body, env) {
  const sessionId = requireSessionId(body);
  const uid = await verifyFirebaseIdToken(body.idToken, env);
  const persona = await keyedValue("persona", uid, env.TOKEN_SECRET);
  const recallToken = await keyedValue("token", uid, env.TOKEN_SECRET);

  // Persist first so a successful Google link can always be recovered.
  await env.RECALL_TOKENS.put(await tokenKey(recallToken), uid);
  const accessToken = await googleAccessToken(env);
  const response = await googleRequest(
    "https://games.googleapis.com/games/v1/recall:linkPersona",
    accessToken,
    {
      sessionId,
      persona,
      token: recallToken,
      cardinalityConstraint: "ONE_PERSONA_TO_ONE_PLAYER",
      conflictingLinksResolutionPolicy: "CREATE_NEW_LINK",
    },
  );
  return json({linked: true, state: response.state || "LINK_CREATED"});
}

async function verifyFirebaseIdToken(idToken, env) {
  if (typeof idToken !== "string" || idToken.length < 100 || idToken.length > 8192) {
    throw new RequestError(401, "Firebase authentication is required");
  }
  const response = await fetch(
    `https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=${encodeURIComponent(env.FIREBASE_API_KEY)}`,
    {
      method: "POST",
      headers: {"content-type": "application/json"},
      body: JSON.stringify({idToken}),
    },
  );
  if (!response.ok) throw new RequestError(401, "Invalid Firebase authentication");
  const data = await response.json();
  const uid = data.users?.[0]?.localId;
  if (typeof uid !== "string" || !uid) throw new RequestError(401, "Invalid Firebase user");
  return uid;
}

async function googleAccessToken(env) {
  const now = Math.floor(Date.now() / 1000);
  if (cachedGoogleAccessToken && cachedGoogleAccessTokenExpiry > now + 60) {
    return cachedGoogleAccessToken;
  }
  const account = serviceAccount(env);
  const assertion = await signJwt(
    {alg: "RS256", typ: "JWT"},
    {
      iss: account.client_email,
      scope: GOOGLE_OAUTH_SCOPE,
      aud: GOOGLE_OAUTH_AUDIENCE,
      iat: now,
      exp: now + 3600,
    },
    account.private_key,
  );
  const response = await fetch(GOOGLE_OAUTH_AUDIENCE, {
    method: "POST",
    headers: {"content-type": "application/x-www-form-urlencoded"},
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion,
    }),
  });
  if (!response.ok) throw new Error(`Google OAuth failed (${response.status})`);
  const data = await response.json();
  cachedGoogleAccessToken = data.access_token;
  cachedGoogleAccessTokenExpiry = now + Number(data.expires_in || 3600);
  return cachedGoogleAccessToken;
}

async function createFirebaseCustomToken(uid, env) {
  const account = serviceAccount(env);
  const now = Math.floor(Date.now() / 1000);
  return signJwt(
    {alg: "RS256", typ: "JWT"},
    {
      iss: account.client_email,
      sub: account.client_email,
      aud: FIREBASE_CUSTOM_TOKEN_AUDIENCE,
      iat: now,
      exp: now + 3600,
      uid,
    },
    account.private_key,
  );
}

async function googleRequest(url, accessToken, body) {
  const response = await fetch(url, {
    method: body ? "POST" : "GET",
    headers: {
      authorization: `Bearer ${accessToken}`,
      ...(body ? {"content-type": "application/json"} : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!response.ok) throw new Error(`Google Games API failed (${response.status})`);
  return response.json();
}

function serviceAccount(env) {
  if (cachedServiceAccount) return cachedServiceAccount;
  const account = JSON.parse(env.GOOGLE_SERVICE_ACCOUNT_JSON);
  if (!account.client_email || !account.private_key) throw new Error("Invalid service account secret");
  cachedServiceAccount = account;
  return cachedServiceAccount;
}

async function keyedValue(purpose, uid, secret) {
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(secret),
    {name: "HMAC", hash: "SHA-256"},
    false,
    ["sign"],
  );
  return base64Url(new Uint8Array(await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(`${purpose}:${uid}`))));
}

async function tokenKey(token) {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(token));
  return base64Url(new Uint8Array(digest));
}

async function signJwt(header, claims, privateKeyPem) {
  cachedSigningKey ??= crypto.subtle.importKey(
      "pkcs8",
      pemBytes(privateKeyPem),
      {name: "RSASSA-PKCS1-v1_5", hash: "SHA-256"},
      false,
      ["sign"],
    );
  const key = await cachedSigningKey;
  const unsigned = `${base64Url(new TextEncoder().encode(JSON.stringify(header)))}.${base64Url(new TextEncoder().encode(JSON.stringify(claims)))}`;
  const signature = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, new TextEncoder().encode(unsigned));
  return `${unsigned}.${base64Url(new Uint8Array(signature))}`;
}

function pemBytes(pem) {
  const encoded = pem.replace(/-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\s/g, "");
  return Uint8Array.from(atob(encoded), (character) => character.charCodeAt(0));
}

function base64Url(bytes) {
  let binary = "";
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

async function readJson(request) {
  const length = Number(request.headers.get("content-length") || 0);
  if (length > 12288) throw new RequestError(400, "Request is too large");
  try {
    return await request.json();
  } catch {
    throw new RequestError(400, "JSON body is required");
  }
}

function requireSessionId(body) {
  const sessionId = body?.sessionId;
  if (
    typeof sessionId !== "string" ||
    sessionId.length < SESSION_ID_MIN_LENGTH ||
    sessionId.length > SESSION_ID_MAX_LENGTH
  ) {
    throw new RequestError(400, "A valid Recall session ID is required");
  }
  return sessionId;
}

function json(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {"content-type": "application/json; charset=utf-8", "cache-control": "no-store"},
  });
}

function safeError(error) {
  return error instanceof Error ? `${error.name}: ${error.message}` : "Unknown error";
}

class RequestError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

export const testable = {base64Url, keyedValue, requireSessionId, tokenKey};
