const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

const nowTimestamp = new Date();

function sampleAliceProfile() {
  return {
    userId: ALICE_UID,
    displayName: "Alice Learner",
    nativeLanguageCode: "en",
    targetLanguageCode: "es",
    activeLanguages: ["es", "fr"],
    proficiencyLevel: "Beginner",
    xpPoints: 120,
    level: 1,
    currentStreak: 3,
    longestStreak: 5,
    lastActiveDate: "2026-10-23",
    dailyGoalMinutes: 15,
    dailyAiSpeakingCount: 2,
    dailyAiResetDate: "2026-10-23",
    subscriptionTier: "free",
    completedLessonIds: ["es_vocab_1"],
    achievements: ["first_lesson"],
    createdAt: nowTimestamp,
    updatedAt: nowTimestamp,
  };
}

// 1. Unauthenticated Security Tests
test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot read lesson progress", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("lesson_progress").doc("prog_1").get());
});

// 2. User Profile Permissions & Bounds
test("Authenticated user: can create and read own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(sampleAliceProfile()));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Cross-user isolation: Alice cannot read Bob's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      ...sampleAliceProfile(),
      userId: BOB_UID,
      displayName: "Bob Learner",
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Ghost field injection: rejected when extra keys are provided", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...sampleAliceProfile(),
      isAdmin: true,
    })
  );
});

// 3. Lesson Progress Isolation & Queries
test("Authenticated user: can record and query their own lesson progress", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const progressDoc = {
    userId: ALICE_UID,
    lessonId: "es_vocab_1",
    languageCode: "es",
    category: "Vocabulary",
    scorePercent: 95,
    xpEarned: 50,
    completed: true,
    createdAt: nowTimestamp,
    updatedAt: nowTimestamp,
  };
  await assertSucceeds(aliceDb.collection("lesson_progress").doc("prog_alice_1").set(progressDoc));

  // Client query alignment with where("userId", "==", ALICE_UID)
  await assertSucceeds(
    aliceDb.collection("lesson_progress").where("userId", "==", ALICE_UID).get()
  );
});

test("Query security: Alice cannot list lesson progress without userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("lesson_progress").get());
});

// 4. Flashcards Progress
test("Flashcards: Alice can create and list her flashcards", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const cardDoc = {
    userId: ALICE_UID,
    wordId: "hola_01",
    languageCode: "es",
    frontText: "Hola",
    backText: "Hello",
    pronunciation: "oh-lah",
    boxLevel: 1,
    nextReviewEpochDay: 20750,
    mastered: false,
    createdAt: nowTimestamp,
    updatedAt: nowTimestamp,
  };
  await assertSucceeds(aliceDb.collection("flashcards").doc("card_1").set(cardDoc));
  await assertSucceeds(aliceDb.collection("flashcards").where("userId", "==", ALICE_UID).get());
});

// 5. Speaking Sessions
test("Speaking sessions: Alice can log and read her speaking practice history", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const sessionDoc = {
    userId: ALICE_UID,
    languageCode: "es",
    mode: "Tutor",
    scenarioTitle: "Ordering at a Tapas Restaurant",
    proficiencyLevel: "Beginner",
    turnsCount: 6,
    fluencyScore: 88,
    grammarFeedbackSummary: "Good use of 'quisiera'. Watch gender agreement on adjectives.",
    createdAt: nowTimestamp,
    updatedAt: nowTimestamp,
  };
  await assertSucceeds(aliceDb.collection("speaking_sessions").doc("sess_1").set(sessionDoc));
  await assertSucceeds(aliceDb.collection("speaking_sessions").where("userId", "==", ALICE_UID).get());
});
