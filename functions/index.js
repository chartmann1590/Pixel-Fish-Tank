"use strict";

const crypto = require("node:crypto");
const {initializeApp} = require("firebase-admin/app");
const {getAuth} = require("firebase-admin/auth");
const {getFirestore, FieldValue} = require("firebase-admin/firestore");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {google} = require("googleapis");

initializeApp();

const REGION = "us-central1";
const TOKEN_COLLECTION = "playGamesRecallTokens";
const RECALL_SERVICE_ACCOUNT =
  "pixel-fish-recall@pixel-fish-tank.iam.gserviceaccount.com";
const FUNCTION_OPTIONS = {region: REGION, serviceAccount: RECALL_SERVICE_ACCOUNT};

function requireSessionId(data) {
  const sessionId = data && data.sessionId;
  if (typeof sessionId !== "string" || sessionId.length < 20 || sessionId.length > 4096) {
    throw new HttpsError("invalid-argument", "A valid Recall session ID is required.");
  }
  return sessionId;
}

function tokenDocumentId(token) {
  return crypto.createHash("sha256").update(token, "utf8").digest("hex");
}

async function gamesClient() {
  const auth = new google.auth.GoogleAuth({
    scopes: ["https://www.googleapis.com/auth/androidpublisher"],
  });
  return google.games({version: "v1", auth: await auth.getClient()});
}

exports.recallRecover = onCall(FUNCTION_OPTIONS, async (request) => {
  const sessionId = requireSessionId(request.data);
  try {
    const games = await gamesClient();
    const response = await games.recall.retrieveTokens({sessionId});
    const tokens = response.data.tokens || [];
    for (const recallToken of tokens) {
      if (!recallToken.token) continue;
      const tokenRecord = await getFirestore()
          .collection(TOKEN_COLLECTION)
          .doc(tokenDocumentId(recallToken.token))
          .get();
      if (!tokenRecord.exists) continue;
      const uid = tokenRecord.get("uid");
      if (typeof uid !== "string" || !uid) continue;
      return {
        recovered: true,
        customToken: await getAuth().createCustomToken(uid),
      };
    }
    return {recovered: false};
  } catch (error) {
    console.error("Recall recovery failed", error);
    throw new HttpsError("unavailable", "Play Games Recall recovery is temporarily unavailable.");
  }
});

exports.recallLink = onCall(FUNCTION_OPTIONS, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Firebase authentication is required.");
  }
  const sessionId = requireSessionId(request.data);
  const uid = request.auth.uid;
  const token = crypto.randomBytes(32).toString("base64url");

  try {
    const games = await gamesClient();
    const response = await games.recall.linkPersona({
      requestBody: {
        sessionId,
        persona: uid,
        token,
        cardinalityConstraint: "ONE_PERSONA_TO_ONE_PLAYER",
        conflictingLinksResolutionPolicy: "CREATE_NEW_LINK",
      },
    });
    const state = response.data.state || "LINK_CREATED";
    await getFirestore()
        .collection(TOKEN_COLLECTION)
        .doc(tokenDocumentId(token))
        .set({
          uid,
          state,
          createdAt: FieldValue.serverTimestamp(),
          updatedAt: FieldValue.serverTimestamp(),
        });
    return {linked: true, state};
  } catch (error) {
    console.error("Recall linking failed", error);
    throw new HttpsError("unavailable", "Play Games Recall linking is temporarily unavailable.");
  }
});

exports._test = {requireSessionId, tokenDocumentId};
