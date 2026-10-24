# Security Specification & Test Matrix

## 1. Data Invariants
- Every document must strictly belong to the authenticated owner (`userId == request.auth.uid`).
- Cross-user reads and writes are forbidden under all circumstances.
- PII (user emails) are isolated under `/users/{userId}/private/` and forbidden from public queries.
- `createdAt` is immutable on update and must be a valid timestamp upper-bounded by `request.time`.
- `updatedAt` is upper-bounded by `request.time`.
- String fields have strict size bounds to prevent resource exhaustion attacks.
- Document IDs must conform to `isValidId` regex `^[a-zA-Z0-9_\-]+$` and length <= 128.

## 2. The Dirty Dozen Payloads & Test Scenarios
1. Unauthenticated user attempts to read `/users/alice_123`. -> REJECT
2. Unauthenticated user attempts to create `/lesson_progress/p1`. -> REJECT
3. Alice attempts to read Bob's user document `/users/bob_456`. -> REJECT
4. Alice attempts to write a user document claiming to be Bob (`userId: 'bob_456'`). -> REJECT
5. Alice attempts to inject unknown ghost fields (`isAdmin: true`, `isVerified: true`) into `/users/alice_123`. -> REJECT
6. Alice attempts to mutate immutable `createdAt` on an existing document. -> REJECT
7. Alice attempts to alter Bob's lesson progress document. -> REJECT
8. Alice attempts to list flashcards without `userId == request.auth.uid` query filter. -> REJECT
9. Alice attempts to inject a 10KB string into `displayName` or `scenarioTitle`. -> REJECT
10. Alice attempts to pass a non-timestamp value (integer epoch) for `createdAt`. -> REJECT
11. Alice queries her own lesson progress with `where('userId', '==', Alice)`. -> SUCCEED
12. Alice queries her own flashcards and updates boxLevel from 1 to 2. -> SUCCEED
