# Testing

This doc covers the fixture pattern, mock-vs-fake rules, and the
"why didn't the tests catch that?" gotchas that have bitten us.

## Running tests

```bash
./gradlew test                                         # All tests
./gradlew test --tests "com.goalplanner.data.*"       # Package
./gradlew test --tests "com.goalplanner.data.BossKillDataTest"  # Single class
./gradlew test --tests "*OrSeedingTest"               # Glob
```

As of v0.1.0: 399 tests across 9 packages. Target for 1.0: full
coverage of every tracker's edge cases, property-based tests on
the reordering rules, and UI dialog smoke tests via the Swing
testing framework.

## Test layout

```
src/test/java/com/goalplanner/
├── api/             # Public + internal API surface
├── data/            # BossKillData, QuestRequirements, BossAlternativeSeedingTest
├── integration/     # End-to-end flows: quest chains, boss prereq
│                    # seeding, diary completion, warriors-guild OR-group
├── model/           # Model invariants
├── persistence/     # Load/save/migrate/reconcile + section CRUD
├── service/         # Skill-chain ordering
├── testsupport/     # Shared fixtures (below)
├── tracker/         # All 8 trackers
├── ui/              # Dialog + picker behavior
└── util/            # Number formatting
```

## The shared fixtures

### `InMemoryConfigManager`

Mockito-backed map fake for `ConfigManager`. Real `get` / `setConfiguration`
semantics against an in-memory `Map<String, String>`. Used by any test
that exercises `GoalStore`.

### `MockGameState`

Hand-rolled fake holding skill levels, quest states, varp values,
varbit values, item-container contents, combat level. Used by
`MockClientFactory` to create a stubbed `Client`.

```java
MockGameState state = new MockGameState()
    .bossKills("Zulrah", 150)
    .questFinished(Quest.REGICIDE)
    .skillLevel(Skill.RANGED, 99);
Client client = MockClientFactory.createClient(state);
```

The builder methods return `this` for chaining. `copy()` produces a
deep clone so tests can build a "before / after" pair without shared
mutation.

### `TrackerTestHarness`

Wraps a `GoalStore` + tracker + `MockGameState` for the common tracker
test shape:

```java
TrackerTestHarness<BossKillTracker> h =
    TrackerTestHarness.forBossKills(state);
Goal goal = makeBossGoal("Zulrah", 500);
h.store().addGoal(goal);
h.tracker().checkGoals(h.store().getGoals());
// ... assertions on goal.getCurrentValue() / isComplete()
```

`h.withNewState(newState)` swaps in a modified game state (same store)
so you can simulate "kill count increased" transitions without
reconstructing the harness.

### Test hooks in production code

Two patterns in use:

- **`BossKillData.swapPrereqsForTest(String, BossPrereqs)`** —
  package-private swap-with-return-previous method. Tests inject
  synthetic prereqs for a boss name and restore the original in
  `@AfterEach`. Used for Alternative OR-group seeding tests, since
  no production boss currently uses the field.
- **`QuestRequirementResolver.resolve(Quest, skillFn, questFn,
  combatFn)`** — 4-arg core overload lets tests inject fake
  player-state lookups without mocking `Client`. The 3-arg overload
  defaults combat to 3 for backwards compatibility.

Add more hooks as needed, but keep them package-private and document
them as test-only.

## Rules of thumb

### Mocks for stateless deps

`Client`, `ItemManager`, `ItemContainer`, `Player` — all of these are
stateless collaborators from the test's perspective. Use
`Mockito.mock(...)` and stub the specific methods you exercise.

```java
Client client = mock(Client.class);
when(client.getRealSkillLevel(Skill.HITPOINTS)).thenReturn(70);
```

### Real implementations against fakes for stateful deps

Anything that holds state the test cares about — `GoalStore`,
`GoalReorderingService`, trackers — use the real implementation with
fakes for *its* dependencies. Do not write parallel `TestGoalStore`
classes; they drift.

### Test the API surface, not private helpers

If a private helper has interesting behavior, either expose it via a
public API method and test through that, or test via the public method
that invokes it. Private-method tests via reflection are brittle.

### `@DisplayName` for readable output

Every test gets a `@DisplayName` so the test runner's output reads as
English:

```java
@Test
@DisplayName("reads kill count from varp and records progress")
void readsKillCountFromVarp() { ... }
```

Method names are incidental. CI output and IDE green-lights both use
the display name.

## Gotchas the tests didn't catch (and how to avoid them)

### MockClient is thread-agnostic

`MockClient` does NOT enforce the "must be called on client thread"
assertion that real RuneLite `Client` does. Production code that
throws on the EDT — e.g. `Quest.getState(client)` called inside
`SwingUtilities.invokeLater` — passes in tests and silently fails in
the live plugin.

**Mitigation:** integration tests that exercise a menu-handler path
must pair with a real EDT check. When adding tests for a new flow,
ask: "which thread does the production code path run on?" If it's a
menu `onClick` → `invokeLater` dialog → API call, the test coverage
must include the thread-hop, or the next bug in that class will slip
through.

### Freshly-created ACCOUNT goals have `currentValue=0`

ACCOUNT goals only get their live value populated on the next
AccountTracker tick. Any test that asserts "this newly-created account
goal is/isn't complete at creation time" is testing builder defaults,
not real behavior. Assert after a tracker tick, or assert at the
resolver level (should this template have been emitted at all?).

### Integration-test depth matters

Shallow tests that call `addBossGoal("Vorkath", 100)` and assert the
boss goal was created miss transitive behavior — does DS2 get seeded?
Do DS2's own prereqs get seeded? Do those prereqs' ancestors get
seeded?

For any prereq-chain change, the integration test should verify at
least **three levels deep** (boss → direct quest → quest's ancestor).

### Test data must match production data shape

When testing against real enums (`Quest.DRAGON_SLAYER_II`, `Skill.HITPOINTS`),
using the actual enum values catches drift where production data or
API enums rename constants. Synthetic enum values feel safer but they
miss that class of bug.

## Adding a new tracker test

1. Create a subclass of `MockGameState` setup in `@BeforeEach` with
   the varp/varbit values your tracker reads.
2. Use `TrackerTestHarness` or construct the tracker directly with
   injected deps.
3. Assert at least three cases: no-op when unchanged, progress
   recorded on change, completion stamps `completedAt`.
4. Add `@DisplayName` to every test.

See [BossKillTrackerTest.java](src/test/java/com/goalplanner/tracker/BossKillTrackerTest.java)
for the canonical pattern.

## Adding an API method test

1. Test happy path.
2. Test duplicate guard (second call with same args returns same id).
3. Test validation failures (null / out-of-range / unknown reference
   → null return).
4. If the method triggers prereq seeding, test transitive behavior:
   what gets created, what gets linked, what gets skipped.

See [BossGoalPrereqSeedingTest.java](src/test/java/com/goalplanner/integration/BossGoalPrereqSeedingTest.java)
for a prereq-chain integration-test example.
