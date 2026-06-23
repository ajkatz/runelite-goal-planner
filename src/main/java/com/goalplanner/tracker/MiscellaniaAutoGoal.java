package com.goalplanner.tracker;

/**
 * Decision logic for auto-adding a "Miscellania 100% approval" goal when the
 * player is actively managing their kingdom (the approval varbit rises).
 *
 * <p>Pure and testable — the plugin supplies the live varbit values, the count
 * of approval increases since login, and the current goal state; this class
 * only decides whether to act.
 *
 * <p>Guardrails: fire only when the toggle is on, no Misc. Approval goal already
 * exists, and approval has actually risen. The key trick: the approval varbit
 * syncs from 0 up to its real value right after login, which registers as the
 * <em>first</em> increase — so we ignore the first increase and only treat the
 * second and later increases as real in-play favour gains. This needs no timing
 * window. (Whenever the goal is missing and approval rises again it re-adds —
 * deleting it is not permanent; the toggle is the off-switch.)
 */
public final class MiscellaniaAutoGoal
{
	/** Kingdom of Miscellania approval varbit value representing 100%. */
	public static final int FULL_APPROVAL = 127;

	/** Approval increases since login to ignore (the login data-sync jump)
	 *  before an increase counts as a real favour gain. */
	public static final int MIN_INCREASES = 2;

	private MiscellaniaAutoGoal()
	{
	}

	/**
	 * @param previous            last approval value seen this session, or -1 if
	 *                            none yet (so the baseline read never fires)
	 * @param current             current approval value
	 * @param increasesSinceLogin count of approval increases observed since login
	 *                            (the 1st is the login data sync, not a gain)
	 * @param enabled             the auto-track config toggle
	 * @param hasExistingGoal     a Misc. Approval goal already exists
	 * @return true iff a 100% goal should be auto-added now
	 */
	public static boolean shouldAdd(int previous, int current, int increasesSinceLogin,
		boolean enabled, boolean hasExistingGoal)
	{
		return enabled
			&& !hasExistingGoal
			&& previous >= 0
			&& current > previous
			&& increasesSinceLogin >= MIN_INCREASES;
	}
}
