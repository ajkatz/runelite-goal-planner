# Contributing

Thanks for interest in the Goal Tracker plugin. This doc covers the
day-to-day process for changes against this repo.

## Quick start

```bash
export JAVA_HOME=/path/to/jdk-21
./gradlew run     # Launch RuneLite in dev mode with the plugin loaded
./gradlew test    # Run all tests
./gradlew build   # Full build (compile + test + assemble)
```

JDK 21 is required (Zulu recommended on macOS for FlatLaf
compatibility). The `run` task auto-adds the macOS Cocoa `--add-opens`
flag.

## Test-first rule

Any change that adds or modifies a public/internal API method ships
with tests in the same change. The test covers:

- Happy path (does the method do what it claims)
- At least one error / edge case (null id, invalid input, duplicate
  guard)

See [TESTING.md](TESTING.md) for the fixture playbook.

## Commit style

- **Subject line** — imperative, ~70 char cap. Examples:
  - `Seed BossPrereqs.alternatives as OR-prereqs on boss goals`
  - `Pre-filter recommended combat level in QuestRequirementResolver`
  - `Fix section persistence across restarts`
- **Body** — bullet-pointed summary of what changed + why. Reference
  the user-visible impact if any. Don't dump the diff description.

## Code standards

- **Composition over inheritance** — prefer delegating to helpers
  over subclassing. The one exception is `AbstractTracker` which
  has a legitimate template-method use case.
- **Ask before assuming** — if requirements or approach are ambiguous,
  ask a clarifying question before coding rather than guessing and
  needing to revisit.
- **Document non-obvious decisions** — a comment explaining *why*
  beats a comment restating *what*. Structural decisions go in
  [ARCHITECTURE.md](ARCHITECTURE.md); behavior rationale stays at the
  call site.

## Known pitfalls (learned the hard way)

### EDT vs client thread
RuneLite's `Client`, `Quest.getState()`, `ItemManager.canonicalize()`,
etc. assert they're called on the client thread. If you call them
from inside `SwingUtilities.invokeLater`, they throw
`AssertionError` which the AWT default uncaught handler silently
swallows. The feature appears to do nothing with no error visible.

**Rule:** menu `onClick` handlers that need to open a Swing dialog
should `invokeLater` to the EDT for the dialog, then hop back to
`clientThread.invokeLater` before calling any `@Inject`-ed API that
uses `Client` / `Quest`.

### Pre-filter in resolvers, not post-filter in seeders
Freshly-created ACCOUNT goals have `currentValue=0` from the builder
default and only get their live value on the next tracker tick. The
post-creation `isComplete` check in `seedPrereqsInto` misses
already-met account-metric requirements. Pre-filter at the resolver
(before template emission), not the seeder.

### MockClient hides thread-affinity bugs
`MockClient` is thread-agnostic and doesn't enforce the client-thread
assertion. Code that would throw on the EDT in production passes in
unit tests. Integration tests that go through menu handlers need a
real-client-thread simulation or an explicit EDT-hop check.

### OSRS API codenames diverge from surface names
Jagex's internal codenames sometimes survive into
`net.runelite.api.gameval.VarPlayerID`:

- Brutus / Demonic Brutus → `TOTAL_COWBOSS_KILLS` /
  `TOTAL_COWBOSS_HARDMODE_KILLS`
- Doom of Mokhaiotl → `DOM_*` prefix
- Perilous Moons → `PMOON_*` prefix

When searching for a boss's varp, do a broad `strings | grep _KILLS`
dump rather than a narrow regex on the surface name.

## Pull requests

- One focused change per PR. If you find an unrelated fix along the
  way, open a separate PR for it. Reviewer's context window matters.
- Build green before requesting review: `./gradlew build` must pass.
- Screenshots or a short recording for any user-visible UI change.

## Filing issues

Useful issue reports include:

- The plugin version (see README header or plugin-hub listing).
- OSRS account state relevant to the bug — are you on a leagues world?
  A fresh account? Post-completion of some specific quest?
- Steps to reproduce. "Add a boss goal → I expected X → I got Y."
- RuneLite client log snippet if there was an exception. The EDT
  swallows errors; check the log before assuming the code path is
  the problem.

## License

By contributing, you agree that your contributions will be licensed
under the [BSD 2-Clause License](LICENSE) that covers the project.
