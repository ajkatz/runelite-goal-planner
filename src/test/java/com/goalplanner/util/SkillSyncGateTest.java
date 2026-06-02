package com.goalplanner.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Exercises the deferral state machine that keeps goal prereq seeding
 * from reading the unsynced (default level-1) skill cache right after
 * login. Uses an injectable clock + logged-in flag so timing is exact.
 */
public class SkillSyncGateTest
{
	private final AtomicBoolean loggedIn = new AtomicBoolean(true);
	private final AtomicLong now = new AtomicLong(0);

	/** settleTicks=1, fallback=5000ms — production config. */
	private SkillSyncGate newGate()
	{
		return new SkillSyncGate(loggedIn::get, now::get, 1, 5_000);
	}

	@Test
	public void queuesActionWhileSkillCacheUnsyncedAfterLogin()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();   // stat block not yet arrived

		AtomicInteger ran = new AtomicInteger();
		gate.runWhenSynced(ran::incrementAndGet);

		assertFalse("not synced before the stat burst", gate.isSynced());
		assertEquals("action deferred, not run", 0, ran.get());
		assertEquals(1, gate.pendingCount());
	}

	@Test
	public void drainsQueuedActionOnceStatBurstSettles()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();

		AtomicInteger ran = new AtomicInteger();
		gate.runWhenSynced(ran::incrementAndGet);

		gate.onStatChanged();          // burst arrives
		assertFalse("still bursting on the same tick", gate.isSynced());
		assertEquals(0, ran.get());

		gate.onTick();                 // one quiet tick = settled
		assertTrue(gate.isSynced());
		assertEquals("deferred action runs on the settling tick", 1, ran.get());
		assertEquals(0, gate.pendingCount());
	}

	@Test
	public void runsImmediatelyWhenAlreadySynced()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();
		gate.onStatChanged();
		gate.onTick();                 // now synced

		AtomicInteger ran = new AtomicInteger();
		gate.runWhenSynced(ran::incrementAndGet);
		assertEquals("steady-state add runs without queuing", 1, ran.get());
		assertEquals(0, gate.pendingCount());
	}

	@Test
	public void anEmptyTickBeforeTheBurstDoesNotSatisfySync()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();
		gate.onTick();                 // tick with no stat yet
		assertFalse("a quiet tick before any StatChanged is not 'settled'", gate.isSynced());
	}

	@Test
	public void aLateStatChangedResetsTheSettleWindow()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();
		gate.onStatChanged();
		gate.onTick();                 // settled
		assertTrue(gate.isSynced());

		// A trailing stat update (e.g. a skill that synced a tick later)
		// re-opens the window until the next quiet tick.
		gate.onStatChanged();
		assertFalse("trailing stat re-opens the window", gate.isSynced());
		gate.onTick();
		assertTrue(gate.isSynced());
	}

	@Test
	public void fallbackDrainsWhenNoStatChangedEverArrives()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();                // fallbackAt = 5000

		AtomicInteger ran = new AtomicInteger();
		gate.runWhenSynced(ran::incrementAndGet);

		now.set(4_999);
		gate.onTick();
		assertEquals("before fallback deadline: still queued", 0, ran.get());

		now.set(5_000);
		gate.onTick();
		assertTrue("fallback considers skills synced", gate.isSynced());
		assertEquals("fallback drains the queue", 1, ran.get());
	}

	@Test
	public void neverSyncedWhileLoggedOut()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();
		gate.onStatChanged();
		gate.onTick();
		assertTrue(gate.isSynced());

		loggedIn.set(false);
		assertFalse("logged out is never synced", gate.isSynced());
	}

	@Test
	public void logoutDropsPendingSoItCannotSeedIntoAnotherProfile()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();

		AtomicInteger ran = new AtomicInteger();
		gate.runWhenSynced(ran::incrementAndGet);
		assertEquals(1, gate.pendingCount());

		gate.onLogout();
		assertEquals("queue cleared on logout", 0, gate.pendingCount());

		// A fresh login + sync must not resurrect the dropped action.
		gate.onLogin();
		gate.onStatChanged();
		gate.onTick();
		assertEquals("dropped action never runs", 0, ran.get());
	}

	@Test
	public void reloginReArmsTheGate()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();
		gate.onStatChanged();
		gate.onTick();
		assertTrue(gate.isSynced());

		gate.onLogout();
		loggedIn.set(true);
		gate.onLogin();                // new login: must re-sync
		assertFalse("re-login requires a fresh stat burst", gate.isSynced());

		gate.onStatChanged();
		gate.onTick();
		assertTrue(gate.isSynced());
	}

	@Test
	public void reEntrantAddDuringDrainRunsImmediatelyWhenSynced()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();

		AtomicInteger inner = new AtomicInteger();
		// A deferred action that itself adds a follow-on action (e.g. a
		// seed that triggers another add). By the time the outer runs we
		// are synced, so the inner add runs immediately rather than being
		// stranded — and iterating a snapshot keeps that safe.
		gate.runWhenSynced(() -> gate.runWhenSynced(inner::incrementAndGet));

		gate.onStatChanged();
		gate.onTick();

		assertEquals("re-entrant add runs in the same drain", 1, inner.get());
		assertEquals(0, gate.pendingCount());
	}

	@Test
	public void nullActionIsANoOp()
	{
		SkillSyncGate gate = newGate();
		gate.onLogin();
		gate.runWhenSynced(null);
		assertEquals(0, gate.pendingCount());
	}
}
