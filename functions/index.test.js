"use strict";

const assert = require("node:assert/strict");
const test = require("node:test");
const {_test} = require("./index");

test("Recall tokens are stored under a stable one-way document ID", () => {
  const token = "a-private-recall-token";
  const first = _test.tokenDocumentId(token);
  const second = _test.tokenDocumentId(token);
  assert.equal(first, second);
  assert.equal(first.length, 64);
  assert.equal(first.includes(token), false);
});

test("Recall session IDs are validated before calling Google", () => {
  assert.throws(() => _test.requireSessionId({sessionId: "short"}));
  assert.equal(_test.requireSessionId({sessionId: "s".repeat(32)}), "s".repeat(32));
});
