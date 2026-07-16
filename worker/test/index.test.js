import assert from "node:assert/strict";
import test from "node:test";
import {webcrypto} from "node:crypto";

globalThis.crypto ??= webcrypto;
globalThis.btoa ??= (value) => Buffer.from(value, "binary").toString("base64");
globalThis.atob ??= (value) => Buffer.from(value, "base64").toString("binary");

const {testable} = await import("../src/index.js");

test("session IDs are validated", () => {
  assert.equal(testable.requireSessionId({sessionId: "a".repeat(20)}), "a".repeat(20));
  assert.throws(() => testable.requireSessionId({sessionId: "short"}));
});

test("tokens are deterministic, scoped, and opaque", async () => {
  const token = await testable.keyedValue("token", "firebase-user", "test-secret");
  const same = await testable.keyedValue("token", "firebase-user", "test-secret");
  const persona = await testable.keyedValue("persona", "firebase-user", "test-secret");
  assert.equal(token, same);
  assert.notEqual(token, persona);
  assert.equal(token.includes("firebase-user"), false);
  assert.equal((await testable.tokenKey(token)).length, 43);
});
