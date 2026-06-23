package com.goalplanner.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

/**
 * Gates skill-level-reading actions (goal prerequisite seeding) until the
 * client's skill cache has actually synced for the current login.
 *
 * <p><b>The problem.</b> Adding a goal that seeds skill prerequisites
 * (e.g. Song of the Elves seeding Herblore/Farming) reads live skill
 * levels via {@code client.getRealSkillLevel} to skip requirements the
 * player already meets. Right after login the skill XP cache has not yet
 * received the server's stat block, so every skill reads as its default
 * (level 1). Already-met requirements then fail the skip check and seed
 * as spurious level-1 cards that only self-correct once {@code StatChanged}
 * fires - exactly the user-reported "Herblore/Farming added at level 1"
 * bug.
 *
 * <p><b>The gate.</b> Seeding actions are routed through
 * {@link #runWhenSynced(Runnable)}. While skills are unsynced the action
 * is queued; it runs once the post-login {@code StatChanged} burst has
 * arrived and settled. "Settled" means at least {@code settleTicks} game
 * ticks have elapsed with no further {@code StatChanged} - the stat block
 * lands as a single burst, so a quiet tick after it means every skill is
 * in. A wall-clock fallback ({@code fallbackMs}) drains the queue anyway
 * if no {@code StatChanged} is ever observed (e.g. a freshly-created
 * account whose skills equal the client default), so an action can never
 * be stranded.
 *
 * <p>Notably this does <em>not</em> wait for every {@link
 * net.runelite.api.Skill} value to report: {@code Skill.OVERALL} never
 * fires {@code StatChanged} and the unreleased {@code SAILING} skill may
 * never sync, so a "all skills reported" predicate could never become
 * true. Burst-settle sidesteps that entirely.
 *
 * <p><b>Pure by design.</b> No RuneLite dependencies - the plugin injects
 * a logged-in predicate and a clock and feeds lifecycle callbacks
 * ({@link #onLogin()}, {@link #onLogout()}, {@link #onStatChanged()},
 * {@link #onTick()}). This keeps the timing state machine unit-testable
 * without a live client, mirroring how {@code QuestRequirementResolver}
 * was split out from the client-bound API layer.
 */
public final class SkillSyncGate
{
	private final BooleanSupplier loggedIn;
	private final LongSupplier nowMs;
	private final int settleTicks;
	private final long fallbackMs;

	private final List<Runnable> pending = new ArrayList<>();
	/** True once at least one StatChanged has arrived since the last login. */
	private boolean sawStatSinceLogin = false;
	/** Game ticks observed with no StatChanged since the last one. */
	private int quietTicks = 0;
	/** Wall-clock deadline (epoch ms) after which we drain regardless; 0 = unset. */
	private long fallbackAt = 0L;

	/**
	 * @param loggedIn    supplies whether the client is currently logged in
	 * @param nowMs       supplies the current epoch-millis (injectable clock)
	 * @param settleTicks quiet game ticks required after the last StatChanged
	 *                    before skills are considered synced (>= 1)
	 * @param fallbackMs  wall-clock window after login after which the queue
	 *                    drains even if no StatChanged was ever seen
	 */
	public SkillSyncGate(BooleanSupplier loggedIn, LongSupplier nowMs, int settleTicks, long fallbackMs)
	{
		this.loggedIn = loggedIn;
		this.nowMs = nowMs;
		this.settleTicks = Math.max(1, settleTicks);
		this.fallbackMs = fallbackMs;
	}

	/** Call when the client transitions to the logged-in state. Re-arms the
	 *  gate: the new login must re-sync before deferred actions run. */
	public void onLogin()
	{
		sawStatSinceLogin = false;
		quietTicks = 0;
		fallbackAt = nowMs.getAsLong() + fallbackMs;
	}

	/** Call when the client leaves the logged-in state (login screen, hop,
	 *  connection lost). Drops any queued action so it can't seed into a
	 *  different account/profile on the next login. */
	public void onLogout()
	{
		sawStatSinceLogin = false;
		quietTicks = 0;
		fallbackAt = 0L;
		pending.clear();
	}

	/** Call on each StatChanged received while logged in. */
	public void onStatChanged()
	{
		sawStatSinceLogin = true;
		quietTicks = 0;
	}

	/** Call on each game tick while logged in. Advances the quiet-tick
	 *  counter and drains the queue if skills are now synced. */
	public void onTick()
	{
		// Cap the increment so a long session can't overflow the counter;
		// once it reaches the settle threshold the exact value is irrelevant.
		if (sawStatSinceLogin && quietTicks <= settleTicks)
		{
			quietTicks++;
		}
		drainIfSynced();
	}

	/** Whether the client's skill cache is considered synced and safe to read. */
	public boolean isSynced()
	{
		if (!loggedIn.getAsBoolean())
		{
			return false;
		}
		if (sawStatSinceLogin && quietTicks >= settleTicks)
		{
			return true;
		}
		return fallbackAt != 0L && nowMs.getAsLong() >= fallbackAt;
	}

	/**
	 * Run {@code action} immediately if skills are synced, otherwise queue
	 * it to run once they are. Queued actions drain on a later {@link
	 * #onTick()} (on the same thread that calls onTick - the client thread
	 * in production).
	 */
	public void runWhenSynced(Runnable action)
	{
		if (action == null)
		{
			return;
		}
		if (isSynced())
		{
			action.run();
			return;
		}
		pending.add(action);
	}

	/** Number of actions currently queued awaiting sync (test/diagnostic). */
	public int pendingCount()
	{
		return pending.size();
	}

	private void drainIfSynced()
	{
		if (pending.isEmpty() || !isSynced())
		{
			return;
		}
		// Snapshot before running so a re-entrant action that mutates the
		// queue (a follow-on add, or onLogout clearing it) can't disturb
		// iteration. While draining we are synced, so a re-entrant
		// runWhenSynced runs immediately rather than re-queuing.
		List<Runnable> drained = new ArrayList<>(pending);
		pending.clear();
		for (Runnable r : drained)
		{
			r.run();
		}
	}
}
