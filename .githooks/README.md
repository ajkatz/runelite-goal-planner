# .githooks

Project-local git hooks. Versioned with the repo so all contributors
get the same checks.

## Hooks

- **`pre-commit`** — blocks Java reflection in staged `.java` diffs.
  Reflection is disallowed in runelite plugins. The hook detects:
  - `import java.lang.reflect.{Field,Method,Constructor,Modifier,InvocationHandler,Proxy}`
  - `getDeclaredField(...)` / `getDeclaredMethod(...)` / `setAccessible(...)`
  Gson's `java.lang.reflect.Type` (TypeToken) is explicitly allowed —
  it's a type marker, not actual reflection.

## Enable

Once per clone:

```bash
git config core.hooksPath .githooks
```

This points git at this directory instead of `.git/hooks/`. Hooks
then update automatically when the repo is pulled.

## Bypass

If you have a genuine reason (e.g., a one-off integration test):

```bash
git commit --no-verify
```

## Test the hooks

```bash
bash .githooks/test-pre-commit.sh
```

Should print 4 passing tests. Run after modifying any hook.
