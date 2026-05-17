#!/usr/bin/env bash
# Smoke test for .githooks/pre-commit reflection blocker.
# Run from anywhere: bash .githooks/test-pre-commit.sh

set -e

HOOK_SRC="$(cd "$(dirname "$0")" && pwd)/pre-commit"
if [ ! -f "$HOOK_SRC" ]; then
    echo "ERROR: hook not found at $HOOK_SRC"
    exit 2
fi

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

cd "$tmp"
git init -q
git config user.email test@example.com
git config user.name test
mkdir -p .githooks
cp "$HOOK_SRC" .githooks/pre-commit
chmod +x .githooks/pre-commit
git config core.hooksPath .githooks

passed=0
failed=0

# Test 1: clean Java file should pass the hook
mkdir -p src
cat > src/Clean.java <<'EOF'
package x;
public class Clean {
    public void foo() {}
}
EOF
git add src/Clean.java
if .githooks/pre-commit > /dev/null 2>&1; then
    echo "PASS: clean file accepted"
    passed=$((passed + 1))
else
    echo "FAIL: clean file blocked"
    failed=$((failed + 1))
fi
git commit -q -m "clean" || true

# Test 2: actual reflection should be blocked
cat > src/Bad.java <<'EOF'
package x;
import java.lang.reflect.Field;
public class Bad {
    void hack() throws Exception {
        Field f = String.class.getDeclaredField("hash");
        f.setAccessible(true);
    }
}
EOF
git add src/Bad.java
if .githooks/pre-commit > /dev/null 2>&1; then
    echo "FAIL: reflection was NOT blocked"
    failed=$((failed + 1))
else
    echo "PASS: reflection correctly blocked"
    passed=$((passed + 1))
fi

# Reset stage for next test
git rm --cached -f src/Bad.java > /dev/null 2>&1 || true
rm -f src/Bad.java

# Test 3: Gson TypeToken (java.lang.reflect.Type) should be allowed
cat > src/Gson.java <<'EOF'
package x;
import java.lang.reflect.Type;
public class Gson {
    static class TypeToken<T> { Type getType() { return null; } }
    Type t = new TypeToken<java.util.List<String>>() {}.getType();
}
EOF
git add src/Gson.java
if .githooks/pre-commit > /dev/null 2>&1; then
    echo "PASS: Gson TypeToken allowed (false-positive avoided)"
    passed=$((passed + 1))
else
    echo "FAIL: Gson TypeToken was incorrectly blocked"
    failed=$((failed + 1))
fi

# Test 4: getDeclaredField alone (without import) should still block
git rm --cached -f src/Gson.java > /dev/null 2>&1 || true
rm -f src/Gson.java
cat > src/Sneaky.java <<'EOF'
package x;
public class Sneaky {
    void hack() throws Exception {
        java.lang.reflect.Field f = String.class.getDeclaredField("hash");
        f.setAccessible(true);
    }
}
EOF
git add src/Sneaky.java
if .githooks/pre-commit > /dev/null 2>&1; then
    echo "FAIL: fully-qualified reflection was NOT blocked"
    failed=$((failed + 1))
else
    echo "PASS: fully-qualified reflection correctly blocked"
    passed=$((passed + 1))
fi

echo
echo "Hook smoke tests: $passed passed, $failed failed."
[ "$failed" -eq 0 ]
